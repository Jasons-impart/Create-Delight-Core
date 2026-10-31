package io.github.jasonsimpart.createdelightcore.content.cooler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/** Native cooler fuels; no Create Liquid Fuel or Fluid Logistics classes are loaded here. */
public final class SnowmanCoolerFuelManager extends SimpleJsonResourceReloadListener {
    public static final int TANK_CAPACITY = 1000;
    public static final SnowmanCoolerFuelManager INSTANCE = new SnowmanCoolerFuelManager();
    public static final TagKey<Item> REGULAR_ITEMS = ItemTags.create(CreateDelightCore.id("snowman_cooler_fuel/regular"));
    public static final TagKey<Item> SPECIAL_ITEMS = ItemTags.create(CreateDelightCore.id("snowman_cooler_fuel/special"));
    private static Map<Fluid, Fuel> fuels = Map.of();

    public record Fuel(int burnTime, boolean freezing, int amountConsumedPerTick) {
        public Fuel {
            if (burnTime <= 0 || amountConsumedPerTick <= 0 || amountConsumedPerTick > TANK_CAPACITY)
                throw new JsonParseException("burnTime must be positive; amountConsumedPerTick must be 1.." + TANK_CAPACITY);
        }
    }

    private SnowmanCoolerFuelManager() {
        super(new Gson(), "snowman_cooler_fuel");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        Map<Fluid, Fuel> next = new HashMap<>();
        entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(json, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
                if (fluid == null || fluid == Fluids.EMPTY) {
                    CreateDelightCore.LOGGER.warn("Skipping cooler fuel {}: unknown fluid {}", entry.getKey(), id);
                    return;
                }
                next.put(fluid, parseFuel(json));
            } catch (RuntimeException ex) {
                CreateDelightCore.LOGGER.error("Invalid snowman cooler fuel {}", entry.getKey(), ex);
            }
        });
        fuels = Map.copyOf(next);
    }

    public static Fuel parseFuel(JsonObject json) {
        // Retain the old CDC spelling while accepting the Liquid Fuel mod's original field.
        boolean freezing = GsonHelper.getAsBoolean(json, "freezing", GsonHelper.getAsBoolean(json, "superHeat", false));
        return new Fuel(GsonHelper.getAsInt(json, "burnTime", freezing ? 32 : 20), freezing,
                GsonHelper.getAsInt(json, "amountConsumedPerTick", freezing ? 10 : 1));
    }

    public static Fuel find(FluidStack stack) {
        return stack.isEmpty() ? null : fuels.get(stack.getFluid());
    }

    public static Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> snapshot() {
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> result = new HashMap<>();
        fuels.forEach((fluid, fuel) -> result.put(ForgeRegistries.FLUIDS.getKey(fluid),
                Triplet.of(fuel.burnTime(), fuel.freezing(), fuel.amountConsumedPerTick())));
        return result;
    }
}
