package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeSerializer;
import com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability;
import io.github.jasonsimpart.CreateDelightCore;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Opt-in validation for a disposable full-pack server, including network serialization. */
final class MbdRecipeValidation {
    private MbdRecipeValidation() {}

    static void started(ServerStartedEvent event) {
        int count = 0;
        for (var holder : event.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof MBDRecipe recipe)
                    || !recipe.recipeType.getRegistryName().getNamespace().equals(CreateDelightCore.MODID)) continue;
            if (!holder.id().equals(recipe.id)) throw new IllegalStateException("Missing/mismatched MBD recipe id: " + holder.id());
            validateSource(event, recipe);
            for (var contents : java.util.List.of(recipe.inputs, recipe.outputs)) {
                for (var content : contents.getOrDefault(ItemRecipeCapability.CAP, java.util.List.of())) {
                    var ingredient = (net.neoforged.neoforge.common.crafting.SizedIngredient) content.content;
                    if (ingredient.getItems().length == 0) throw new IllegalStateException("Empty item ingredient in " + holder.id());
                }
                for (var content : contents.getOrDefault(com.lowdragmc.mbd2.common.capability.recipe.FluidRecipeCapability.CAP, java.util.List.of())) {
                    var ingredient = (net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient) content.content;
                    if (ingredient.getFluids().length == 0 || java.util.Arrays.stream(ingredient.getFluids()).allMatch(net.neoforged.neoforge.fluids.FluidStack::isEmpty)) {
                        throw new IllegalStateException("Empty fluid ingredient in " + holder.id());
                    }
                }
            }
            if (recipe.recipeType.getRegistryName().equals(MbdCompat.id("butchery"))) {
                var stress = recipe.getInputContents(com.lowdragmc.mbd2.integration.create.CreateRotationRecipeCapability.CAP);
                if (stress.size() != 1 || !stress.getFirst().perTick
                        || !(stress.getFirst().content instanceof com.lowdragmc.mbd2.integration.create.CreateRotation rotation)
                        || rotation.value != 1024 || rotation.mode != com.lowdragmc.mbd2.integration.create.CreateRotation.Mode.STRESS) {
                    throw new IllegalStateException("Butchery must require 1024 stress every tick: " + holder.id());
                }
            }
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), event.getServer().registryAccess());
            try {
                MBDRecipeSerializer.STREAM_CODEC.encode(buffer, recipe);
                var decoded = MBDRecipeSerializer.STREAM_CODEC.decode(buffer);
                var ops = event.getServer().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
                var original = MBDRecipeSerializer.CODEC.codec().encodeStart(ops, normalized(recipe)).getOrThrow();
                var restored = MBDRecipeSerializer.CODEC.codec().encodeStart(ops, normalized(decoded)).getOrThrow();
                if (!recipe.id.equals(decoded.id) || !original.equals(restored)) {
                    throw new IllegalStateException("MBD network round-trip mismatch: " + holder.id());
                }
            } finally {
                buffer.release();
            }
            count++;
        }
        if (count == 0) throw new IllegalStateException("No Core MBD recipes were loaded for validation");
        CreateDelightCore.LOGGER.info("MBD migration validation: {} recipes have nonempty ingredients and pass network round-trip", count);
    }

    private static MBDRecipe normalized(MBDRecipe recipe) {
        var copy = recipe.copy();
        // Simple ingredient tags expand to concrete alternatives over the network.
        // Compare their resolved contents, while retaining all amounts and metadata.
        for (var side : java.util.List.of(copy.inputs, copy.outputs)) for (var contents : side.values()) for (var content : contents) {
            if (content.content instanceof net.neoforged.neoforge.common.crafting.SizedIngredient item && item.ingredient().isSimple()) {
                var stacks = java.util.Arrays.stream(item.getItems()).sorted(java.util.Comparator.comparing(stack ->
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()))).toArray(net.minecraft.world.item.ItemStack[]::new);
                content.content = new net.neoforged.neoforge.common.crafting.SizedIngredient(
                        net.minecraft.world.item.crafting.Ingredient.of(stacks), item.count());
            } else if (content.content instanceof net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient fluid && fluid.ingredient().isSimple()) {
                var stacks = java.util.Arrays.stream(fluid.getFluids()).sorted(java.util.Comparator.comparing(stack ->
                        net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(stack.getFluid()))).toArray(net.neoforged.neoforge.fluids.FluidStack[]::new);
                content.content = new net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient(
                        net.neoforged.neoforge.fluids.crafting.FluidIngredient.of(stacks), fluid.amount());
            }
        }
        return copy;
    }

    private static void validateSource(ServerStartedEvent event, MBDRecipe recipe) {
        var resource = event.getServer().getResourceManager().getResource(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                recipe.id.getNamespace(), "recipe/" + recipe.id.getPath() + ".json"));
        if (resource.isEmpty()) return; // Generated Create proxies have no Core JSON source.
        try (var reader = resource.get().openAsReader()) {
            var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
            var ops = event.getServer().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            for (var side : java.util.List.of("inputs", "outputs")) {
                if (!json.has(side)) continue;
                var source = json.getAsJsonObject(side);
                // MBD's lenientOptionalFieldOf silently drops malformed whole input/output
                // maps. Decode the inner codec strictly before trusting runtime contents.
                var expected = MBDRecipeSerializer.CONTENTS_CODEC.parse(ops, source).getOrThrow();
                var actual = side.equals("inputs") ? recipe.inputs : recipe.outputs;
                var expectedJson = MBDRecipeSerializer.CONTENTS_CODEC.encodeStart(ops, expected).getOrThrow();
                var actualJson = MBDRecipeSerializer.CONTENTS_CODEC.encodeStart(ops, actual).getOrThrow();
                if (!expectedJson.equals(actualJson)) throw new IllegalStateException("Dropped/changed " + side + " in " + recipe.id);
                var allowed = java.util.Set.of("content", "perTick", "chance", "tierChanceBoost", "slotName", "uiName");
                for (var entries : source.asMap().values()) for (var entry : entries.getAsJsonArray()) {
                    for (var key : entry.getAsJsonObject().keySet()) if (!allowed.contains(key)) {
                        throw new IllegalStateException("Unknown content field " + key + " in " + recipe.id);
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid MBD source recipe " + recipe.id, exception);
        }
    }
}
