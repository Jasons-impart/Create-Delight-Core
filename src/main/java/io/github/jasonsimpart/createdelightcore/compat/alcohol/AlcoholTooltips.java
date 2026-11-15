package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import de.cadentem.quality_food.config.ClientConfig;
import de.cadentem.quality_food.core.EffectComponent;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, value = Dist.CLIENT)
public final class AlcoholTooltips {
    private AlcoholTooltips() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void gather(RenderTooltipEvent.GatherComponents event) {
        if (!ModList.get().isLoaded("quality_food")) return;
        if (ClientConfig.SPEC.isLoaded() && !ClientConfig.EFFECT_TOOLTIPS.get()) return;
        for (var effect : AlcoholDrinks.tooltipEffects(event.getItemStack())) {
            event.getTooltipElements().add(Either.right(new EffectComponent(Pair.of(effect, 1.0F))));
        }
    }
}
