package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import de.cadentem.quality_food.core.Quality;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TavernCreativeTabs {
    private TavernCreativeTabs() {}

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (!ModList.get().isLoaded("kaleidoscope_tavern") || !ModList.get().isLoaded("quality_food")
                || !ModList.get().isLoaded("quality_food_fluids")
                || !event.getTabKey().location().getNamespace().equals("kaleidoscope_tavern")) return;
        var entries = event.getEntries();
        var snapshot = new ArrayList<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>>();
        for (var entry : entries) snapshot.add(Map.entry(entry.getKey(), entry.getValue()));
        for (var entry : snapshot) {
            var original = entry.getKey();
            if (!TavernQuality.supportsQuality(original)) continue;
            var qualified = TavernQuality.qualify(original, Quality.DIAMOND);
            if (ItemStack.matches(original, qualified)) continue;
            entries.putBefore(original, qualified, entry.getValue());
            entries.remove(original);
        }
    }
}
