package io.github.jasonsimpart.createdelightcore.compat.cmr;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.util.Triplet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.MODID;

public class LiquidCoolerFuelJsonLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "drainable_fuel_loader");

    private static final Gson GSON = new Gson();

    public static final LiquidCoolerFuelJsonLoader INSTANCE = new LiquidCoolerFuelJsonLoader();

    public LiquidCoolerFuelJsonLoader() {
        super(GSON, "snowman_cooler_fuel");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_10793_, @NotNull ResourceManager p_10794_, @NotNull ProfilerFiller p_10795_) {
        CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : p_10793_.entrySet()) {
            parseAndAddCoolerFuel(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Client-side loading: read snowman cooler fuel data from local ResourceManager.
     */
    public static void loadFromResourceManager(ResourceManager rm) {
        if (!CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.isEmpty()) {
            return;
        }

        Map<ResourceLocation, Resource> resources = rm.listResources("snowman_cooler_fuel", 
                rl -> rl.getPath().endsWith(".json"));
        
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element != null && element.isJsonObject()) {
                    parseAndAddCoolerFuel(id, element);
                }
            } catch (IOException e) {
                CreateDelightCore.LOGGER.warn("Failed to load snowman cooler fuel data from {}: {}", id, e.getMessage());
            } catch (ResourceLocationException e) {
                CreateDelightCore.LOGGER.warn("Invalid fluid in snowman cooler fuel {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Client-side loading: read blaze burner fuel data from local ResourceManager.
     * Handles data for create-liquid-fuel mod.
     */
    public static void loadBlazeBurnerFuelFromResourceManager(ResourceManager rm) {
        Map<ResourceLocation, Resource> resources = rm.listResources("blaze_burner_fuel", 
                rl -> rl.getPath().endsWith(".json"));
        
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element != null && element.isJsonObject()) {
                    parseAndAddBlazeBurnerFuel(id, element.getAsJsonObject());
                }
            } catch (IOException e) {
                CreateDelightCore.LOGGER.warn("Failed to load blaze burner fuel data from {}: {}", id, e.getMessage());
            } catch (ResourceLocationException e) {
                CreateDelightCore.LOGGER.warn("Invalid fluid in blaze burner fuel {}: {}", id, e.getMessage());
            }
        }
    }

    private static void parseAndAddCoolerFuel(ResourceLocation id, JsonElement element) {
        if (!element.isJsonObject()) return;
        JsonObject object = element.getAsJsonObject();
        JsonElement fluidElement = object.get("fluid");
        if (fluidElement == null) {
            throw new RuntimeException("No fluid specified for liquid cooler fuel: " + id);
        }
        
        try {
            Fluid value = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidElement.getAsString()));
            if (value != null) {
                CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.put(value,
                        Pair.of(
                                IDENTIFIER,
                                io.github.jasonsimpart.createdelightcore.util.Triplet.of(
                                    object.has("burnTime") ?
                                            object.get("burnTime").getAsInt() :
                                            object.has("freezing") && object.get("freezing").getAsBoolean() ?
                                                    32 : 20,
                                    object.has("freezing") && object.get("freezing").getAsBoolean(),
                                    object.has("amountConsumedPerTick") ?
                                            object.get("amountConsumedPerTick").getAsInt() :
                                            object.has("freezing") && object.get("freezing").getAsBoolean() ?
                                                    10 : 1
                                )
                        )
                );
            }
        } catch (ResourceLocationException e) {
            throw new RuntimeException("Fluid liquid cooler fuel " + id + " has invalid fluid: " + fluidElement.getAsString());
        }
    }

    private static void parseAndAddBlazeBurnerFuel(ResourceLocation id, JsonObject object) {
        JsonElement fluidElement = object.get("fluid");
        if (fluidElement == null) return;

        Fluid value = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidElement.getAsString()));
        if (value == null) return;

        int burnTime = object.has("burnTime") ? object.get("burnTime").getAsInt() : 20;
        boolean superHeat = object.has("superHeat") && object.get("superHeat").getAsBoolean();
        int amountConsumed = object.has("amountConsumedPerTick") ? object.get("amountConsumedPerTick").getAsInt() : 1;

        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.put(value,
                Pair.of(
                        id,
                        Triplet.of(burnTime, superHeat, amountConsumed)
                )
        );
    }
}
