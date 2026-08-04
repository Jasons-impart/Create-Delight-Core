package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConfigurationModuleManager {
    public static final String TAG_MODE = "Mode";
    public static final String TAG_TARGET = "Target";
    public static final String TAG_CHARGE = "Charge";
    public static final String TAG_CHARGE_COST = "ChargeCost";
    public static final String TAG_MAX_CHARGE = "MaxCharge";
    public static final String TAG_TIER = "Tier";
    public static final String TAG_DATA_VERSION = "DataVersion";
    public static final int DATA_VERSION = 1;

    private static final Gson GSON = new Gson();
    private static final Comparator<ConfigurationMode> MODE_ORDER = Comparator
            .comparingInt(ConfigurationMode::sortIndex)
            .thenComparing(mode -> mode.id().toString());

    private static volatile Map<ResourceLocation, ConfigurationModuleDefinition> definitions = Map.of();
    private static volatile List<ConfigurationMode> modes = List.of();

    public static final SimpleJsonResourceReloadListener MODULE_RELOAD_LISTENER = new ModuleReloadListener();
    public static final SimpleJsonResourceReloadListener MODE_RELOAD_LISTENER = new ModeReloadListener();

    private ConfigurationModuleManager() {
    }

    public static Optional<ConfigurationModuleDefinition> getDefinition(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId == null ? Optional.empty() : Optional.ofNullable(definitions.get(itemId));
    }

    public static List<ConfigurationMode> getAvailableModes(ItemStack stack) {
        ResourceLocation moduleId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (moduleId == null) {
            return List.of();
        }
        int tier = getTier(stack);
        return modes.stream()
                .filter(mode -> mode.module().equals(moduleId) && mode.requiredTier() <= tier)
                .toList();
    }

    public static Optional<ConfigurationMode> getSelectedMode(ItemStack stack) {
        List<ConfigurationMode> available = getAvailableModes(stack);
        if (available.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation selectedId = readResourceLocation(stack, TAG_MODE);
        return available.stream().filter(mode -> mode.id().equals(selectedId)).findFirst();
    }

    public static Optional<ConfigurationMode> ensureSelectedMode(ItemStack stack) {
        Optional<ConfigurationMode> selected = getSelectedMode(stack);
        if (selected.isPresent()) {
            writeSnapshot(stack, selected.get());
            return selected;
        }
        List<ConfigurationMode> available = getAvailableModes(stack);
        if (available.isEmpty()) {
            return Optional.empty();
        }
        ConfigurationMode first = available.get(0);
        writeSnapshot(stack, first);
        return Optional.of(first);
    }

    public static Optional<ConfigurationMode> cycle(ItemStack stack, int direction) {
        List<ConfigurationMode> available = getAvailableModes(stack);
        if (available.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation selectedId = readResourceLocation(stack, TAG_MODE);
        int current = -1;
        for (int i = 0; i < available.size(); i++) {
            if (available.get(i).id().equals(selectedId)) {
                current = i;
                break;
            }
        }
        int next;
        if (current < 0) {
            next = direction < 0 ? available.size() - 1 : 0;
        } else {
            next = Math.floorMod(current + Integer.signum(direction), available.size());
        }
        ConfigurationMode mode = available.get(next);
        writeSnapshot(stack, mode);
        return Optional.of(mode);
    }

    public static Optional<BlockItem> getTargetBlockItem(ConfigurationMode mode) {
        Item item = ForgeRegistries.ITEMS.getValue(mode.target());
        return item instanceof BlockItem blockItem ? Optional.of(blockItem) : Optional.empty();
    }

    public static Optional<BlockItem> getSnapshotTarget(ItemStack stack) {
        ResourceLocation targetId = readResourceLocation(stack, TAG_TARGET);
        if (targetId == null) {
            return Optional.empty();
        }
        Item item = ForgeRegistries.ITEMS.getValue(targetId);
        return item instanceof BlockItem blockItem ? Optional.of(blockItem) : Optional.empty();
    }

    public static int getSnapshotChargeCost(ItemStack stack) {
        return Math.max(0, stack.getOrCreateTag().getInt(TAG_CHARGE_COST));
    }

    public static int getCharge(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_CHARGE)) {
            return Math.max(0, stack.getTag().getInt(TAG_CHARGE));
        }
        return getDefinition(stack).map(ConfigurationModuleDefinition::initialCharge).orElse(0);
    }

    public static void setCharge(ItemStack stack, int charge) {
        int maxCharge = getMaxCharge(stack);
        stack.getOrCreateTag().putInt(TAG_CHARGE, Math.max(0, Math.min(charge, maxCharge)));
        stack.getOrCreateTag().putInt(TAG_DATA_VERSION, DATA_VERSION);
    }

    public static int getMaxCharge(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().getInt(TAG_MAX_CHARGE) > 0) {
            return stack.getTag().getInt(TAG_MAX_CHARGE);
        }
        return getDefinition(stack).map(ConfigurationModuleDefinition::maxCharge).orElse(64);
    }

    public static int getTier(ItemStack stack) {
        return Math.max(0, stack.getOrCreateTag().getInt(TAG_TIER));
    }

    public static void applyDefinitionSnapshot(ItemStack stack, ConfigurationModuleDefinition definition) {
        stack.getOrCreateTag().putInt(TAG_MAX_CHARGE, definition.maxCharge());
        stack.getOrCreateTag().putInt(TAG_DATA_VERSION, DATA_VERSION);
        if (!stack.getTag().contains(TAG_CHARGE)) {
            stack.getTag().putInt(TAG_CHARGE, definition.initialCharge());
        } else if (stack.getTag().getInt(TAG_CHARGE) > definition.maxCharge()) {
            stack.getTag().putInt(TAG_CHARGE, definition.maxCharge());
        }
    }

    public static void writeSnapshot(ItemStack stack, ConfigurationMode mode) {
        stack.getOrCreateTag().putString(TAG_MODE, mode.id().toString());
        stack.getOrCreateTag().putString(TAG_TARGET, mode.target().toString());
        stack.getOrCreateTag().putInt(TAG_CHARGE_COST, mode.chargeCost());
        stack.getOrCreateTag().putInt(TAG_DATA_VERSION, DATA_VERSION);
        getDefinition(stack).ifPresent(definition -> applyDefinitionSnapshot(stack, definition));
    }

    private static ResourceLocation readResourceLocation(ItemStack stack, String key) {
        if (!stack.hasTag() || !stack.getTag().contains(key)) {
            return null;
        }
        try {
            return ResourceLocation.parse(stack.getTag().getString(key));
        } catch (ResourceLocationException ignored) {
            return null;
        }
    }

    private static ConfigurationRequirement parseRequirement(JsonElement element) {
        if (!element.isJsonObject()) {
            return new ConfigurationRequirement(Ingredient.fromJson(element), 1);
        }
        JsonObject object = element.getAsJsonObject();
        int count = GsonHelper.getAsInt(object, "count", 1);
        JsonElement ingredientElement = object.has("ingredient") ? object.get("ingredient") : object;
        return new ConfigurationRequirement(Ingredient.fromJson(ingredientElement), count);
    }

    private static final class ModuleReloadListener extends SimpleJsonResourceReloadListener {
        private ModuleReloadListener() {
            super(GSON, "configuration_modules");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, @NotNull ResourceManager resourceManager,
                             @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, ConfigurationModuleDefinition> loaded = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey().toString());
                    ResourceLocation item = ResourceLocation.parse(GsonHelper.getAsString(object, "item"));
                    int maxCharge = GsonHelper.getAsInt(object, "max_charge", 64);
                    int initialCharge = GsonHelper.getAsInt(object, "initial_charge", 0);
                    loaded.put(item, new ConfigurationModuleDefinition(item, maxCharge, initialCharge));
                } catch (JsonParseException | ResourceLocationException | IllegalArgumentException ex) {
                    CreateDelightCore.LOGGER.error("Failed to load configuration module {}", entry.getKey(), ex);
                }
            }
            definitions = Map.copyOf(loaded);
            CreateDelightCore.LOGGER.info("Loaded {} configuration module definitions", definitions.size());
        }
    }

    private static final class ModeReloadListener extends SimpleJsonResourceReloadListener {
        private ModeReloadListener() {
            super(GSON, "configuration_module_modes");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> entries, @NotNull ResourceManager resourceManager,
                             @NotNull ProfilerFiller profiler) {
            List<ConfigurationMode> loaded = new ArrayList<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
                try {
                    JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey().toString());
                    ResourceLocation module = ResourceLocation.parse(GsonHelper.getAsString(object, "module"));
                    ResourceLocation target = ResourceLocation.parse(GsonHelper.getAsString(object, "target"));
                    int chargeCost = GsonHelper.getAsInt(object, "charge_cost", 0);
                    int requiredTier = GsonHelper.getAsInt(object, "required_tier", 0);
                    int sortIndex = GsonHelper.getAsInt(object, "sort_index", 0);
                    String placement = GsonHelper.getAsString(object, "placement", "createdelightcore:block_item");
                    if (!placement.equals("createdelightcore:block_item")) {
                        throw new JsonParseException("Unsupported placement handler: " + placement);
                    }
                    List<ConfigurationRequirement> requirements = new ArrayList<>();
                    JsonArray ingredients = GsonHelper.getAsJsonArray(object, "extra_ingredients", new JsonArray());
                    for (JsonElement ingredient : ingredients) {
                        requirements.add(parseRequirement(ingredient));
                    }
                    Item targetItem = ForgeRegistries.ITEMS.getValue(target);
                    if (!(targetItem instanceof BlockItem)) {
                        throw new JsonParseException("Target is not a registered BlockItem: " + target);
                    }
                    loaded.add(new ConfigurationMode(entry.getKey(), module, target, chargeCost, requiredTier,
                            sortIndex, requirements));
                } catch (JsonParseException | ResourceLocationException | IllegalArgumentException ex) {
                    CreateDelightCore.LOGGER.error("Failed to load configuration mode {}", entry.getKey(), ex);
                }
            }
            loaded.sort(MODE_ORDER);
            modes = List.copyOf(loaded);
            CreateDelightCore.LOGGER.info("Loaded {} configuration module modes", modes.size());
        }
    }
}
