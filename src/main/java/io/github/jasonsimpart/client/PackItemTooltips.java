package io.github.jasonsimpart.client;

import com.google.gson.JsonParser;
import io.github.jasonsimpart.compat.eclipticseasons.QualityCropGrowth;
import net.neoforged.fml.ModList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import io.github.jasonsimpart.util.ModIds;

/** Pack descriptions use current components and preserve the item's original name and diagnostics. */
public final class PackItemTooltips {
    private static final Map<String, String> HINTS = loadHints();
    private static final Set<String> AIR_ITEMS = Set.of("create:copper_backtank", "create:netherite_backtank",
            "create_jetpack:jetpack", "create_jetpack:netherite_jetpack");

    private PackItemTooltips() {}

    private static Map<String, String> loadHints() {
        var result = new LinkedHashMap<String, String>();
        try (var input = PackItemTooltips.class.getResourceAsStream("/assets/createdelightcore/pack_tooltips.json")) {
            if (input == null) throw new IllegalStateException("Missing pack descriptions");
            JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject()
                    .entrySet().forEach(e -> result.put(e.getKey(), e.getValue().getAsString()));
            return result;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot load pack descriptions", exception);
        }
    }

    public static void append(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        var lines = event.getToolTip();
        String hint = HINTS.get(id);
        if (hint != null) {
            for (String mode : hint.split(",")) {
                boolean held = mode.equals("shift") ? Screen.hasShiftDown() : Screen.hasControlDown();
                lines.add(Component.translatable("tooltip.createdelightcore.pack.hold_" + mode));
                if (held) lines.add(Component.translatable("tooltip.createdelightcore.pack." + mode + "_" + id.split(":", 2)[1]));
            }
        }
        if (AIR_ITEMS.contains(id)) {
            var type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse("create:banktank_air"));
            Object air = type == null ? null : stack.get(type);
            lines.add(Component.translatable("tooltip.createdelightcore.pack.air", air instanceof Number n ? n.intValue() : 0));
        }
        var quality = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse("quality_food:quality"));
        if (quality != null && stack.has(quality)) {
            if (stack.has(DataComponents.FOOD)) lines.add(Component.translatable("tooltip.createdelightcore.pack.quality_food_eaten"));
            if (ModList.get().isLoaded(ModIds.ECLIPTIC_SEASONS) && ModList.get().isLoaded(ModIds.QUALITY_FOOD)
                    && QualityCropGrowth.isQualityCrop(stack)) {
                lines.add(Component.translatable("tooltip.createdelightcore.pack.quality_crop_resistance"));
            }
            lines.add(Component.translatable("tooltip.createdelightcore.pack.quality_sell"));
        }
    }
}
