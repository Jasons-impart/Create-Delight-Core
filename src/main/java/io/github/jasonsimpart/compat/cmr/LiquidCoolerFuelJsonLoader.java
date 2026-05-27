package io.github.jasonsimpart.compat.cmr;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.github.jasonsimpart.CreateDelightCore.MODID;

public class LiquidCoolerFuelJsonLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "liquid_cooler_fuel_json_loader");
    public static final LiquidCoolerFuelJsonLoader INSTANCE = new LiquidCoolerFuelJsonLoader();

    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "compat_cooler";

    public LiquidCoolerFuelJsonLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            loadFuel(entry.getKey(), entry.getValue());
        }

        DrainableFuelLoader.load();
    }

    private static void loadFuel(ResourceLocation id, JsonElement element) {
        if (!element.isJsonObject()) {
            CreateDelightCore.LOGGER.warn("Skipping liquid cooler fuel {} because it is not a JSON object", id);
            return;
        }

        JsonObject object = element.getAsJsonObject();
        JsonElement fluidElement = object.get("fluid");
        if (fluidElement == null) {
            CreateDelightCore.LOGGER.warn("Skipping liquid cooler fuel {} because no fluid was specified", id);
            return;
        }

        try {
            ResourceLocation fluidId = ResourceLocation.parse(fluidElement.getAsString());
            BuiltInRegistries.FLUID.getOptional(fluidId).ifPresentOrElse(
                    fluid -> registerFuel(id, fluid, object),
                    () -> CreateDelightCore.LOGGER.warn("Skipping liquid cooler fuel {} because fluid {} is not registered", id, fluidId)
            );
        } catch (ResourceLocationException exception) {
            throw new IllegalArgumentException("Fluid liquid cooler fuel " + id + " has invalid fluid: " + fluidElement.getAsString(), exception);
        }
    }

    private static void registerFuel(ResourceLocation id, Fluid fluid, JsonObject object) {
        boolean freezing = object.has("freezing") && object.get("freezing").getAsBoolean();
        int burnTime = object.has("burnTime") ? object.get("burnTime").getAsInt() : freezing ? 32 : 20;
        int amountConsumedPerTick = object.has("amountConsumedPerTick") ? object.get("amountConsumedPerTick").getAsInt() : freezing ? 10 : 1;

        if (burnTime <= 0 || amountConsumedPerTick <= 0) {
            throw new IllegalArgumentException("Liquid cooler fuel " + id + " must have positive burnTime and amountConsumedPerTick");
        }

        CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.put(
                fluid,
                Pair.of(IDENTIFIER, new CoolerStomachHandler.LiquidCoolerFuel(burnTime, freezing, amountConsumedPerTick))
        );
    }
}
