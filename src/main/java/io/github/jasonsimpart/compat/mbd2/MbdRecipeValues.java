package io.github.jasonsimpart.compat.mbd2;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import java.util.*;

/** Pack-specific OEV recipe valuation rules, preserving integer rounding and minimum alternatives. */
final class MbdRecipeValues {
    static final com.google.gson.JsonObject DATA = MbdOrders.readData("values");
    private static final Map<ResourceLocation, Integer> BASE = new HashMap<>();
    private static Map<ResourceLocation, Integer> values = Map.of();
    private MbdRecipeValues() {}
    static int value(ItemStack stack) { return values.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), -1); }
    static io.github.jasonsimpart.network.SyncFoodValuesPayload snapshot() {
        return new io.github.jasonsimpart.network.SyncFoodValuesPayload(values);
    }

    static void rebuild(MinecraftServer server) {
        BASE.clear();
        DATA.getAsJsonObject("base").entrySet().forEach(entry -> {
            if (entry.getKey().startsWith("#")) {
                var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(entry.getKey().substring(1)));
                for (var item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) BASE.put(BuiltInRegistries.ITEM.getKey(item.value()), entry.getValue().getAsInt());
            } else BASE.put(ResourceLocation.parse(entry.getKey()), entry.getValue().getAsInt());
        });
        var result = new HashMap<>(BASE);
        var nodes = new ArrayList<Node>();
        var dependents = new HashMap<ResourceLocation, Set<Node>>();
        for (var holder : server.getRecipeManager().getRecipes()) {
            var recipe = holder.value();
            var output = recipe.getResultItem(server.registryAccess());
            if (output.isEmpty()) continue;
            var node = new Node(recipe, output);
            nodes.add(node);
            for (var ingredient : node.inputs) for (var stack : ingredient.getItems())
                dependents.computeIfAbsent(BuiltInRegistries.ITEM.getKey(stack.getItem()), ignored -> new HashSet<>()).add(node);
        }
        var queue = new ArrayDeque<>(nodes);
        var queued = new HashSet<>(nodes);
        while (!queue.isEmpty()) {
            var node = queue.removeFirst();
            queued.remove(node);
            double cost = 0;
            boolean known = true;
            for (var ingredient : node.inputs) {
                int minimum = Integer.MAX_VALUE;
                for (var stack : ingredient.getItems()) minimum = Math.min(minimum,
                        result.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), Integer.MAX_VALUE));
                if (minimum == Integer.MAX_VALUE) { known = false; break; }
                cost += minimum;
            }
            if (!known) continue;
            // Only strictly lower positive integer values are queued, so even cycles converge.
            var type = BuiltInRegistries.RECIPE_TYPE.getKey(node.recipe.getType());
            var multiplier = DATA.getAsJsonObject("recipe_multipliers").get(type.toString());
            if (multiplier == null) multiplier = DATA.getAsJsonObject("recipe_multipliers").get(type.getPath());
            cost *= multiplier == null ? DATA.get("multiplier").getAsDouble() : multiplier.getAsDouble();
            double unpriced = 0, consumed = 0;
            for (var output : node.outputs) {
                var id = BuiltInRegistries.ITEM.getKey(output.stack.getItem());
                if (blacklisted(id)) continue;
                if (BASE.containsKey(id)) consumed += BASE.get(id) * output.count;
                else unpriced += output.count;
            }
            double each = cost > 0 && unpriced > 0 ? Math.max(1, (cost - consumed) / unpriced) : 1;
            for (var output : node.outputs) {
                var id = BuiltInRegistries.ITEM.getKey(output.stack.getItem());
                if (BASE.containsKey(id) || blacklisted(id)) continue;
                int price = (int) Math.min(Integer.MAX_VALUE, each);
                if (price >= result.getOrDefault(id, Integer.MAX_VALUE)) continue;
                result.put(id, price);
                for (var dependent : dependents.getOrDefault(id, Set.of())) if (queued.add(dependent)) queue.addLast(dependent);
            }
        }
        values = Map.copyOf(result);
    }
    private static boolean blacklisted(ResourceLocation id) {
        return DATA.getAsJsonArray("blacklist").asList().stream().anyMatch(e -> e.getAsString().equals(id.toString()));
    }
    private record Output(ItemStack stack, double count) {}
    private static final class Node {
        final Recipe<?> recipe;
        final List<Ingredient> inputs;
        final List<Output> outputs = new ArrayList<>();
        Node(Recipe<?> recipe, ItemStack output) {
            this.recipe = recipe;
            inputs = new ArrayList<>(recipe.getIngredients());
            if (recipe instanceof SequencedAssemblyRecipe assembly) {
                inputs.add(assembly.getIngredient());
                for (var step : assembly.getSequence()) {
                    var processing = step.getRecipe();
                    if (processing instanceof DeployerApplicationRecipe deploying && deploying.shouldKeepHeldItem()) continue;
                    if (processing.getIngredients().size() == 2)
                        for (int loop = 0; loop < assembly.getLoops(); loop++) inputs.add(processing.getIngredients().get(1));
                }
            }
            inputs.removeIf(ingredient -> ingredient == Ingredient.EMPTY);
            outputs.add(new Output(output, output.getCount()));
            // Legacy script counted the primary stack and rollable outputs separately.
            if (recipe instanceof ProcessingRecipe<?, ?> processing) processing.getRollableResults().forEach(
                    roll -> outputs.add(new Output(roll.getStack(), roll.getStack().getCount() * roll.getChance())));
        }
    }
}
