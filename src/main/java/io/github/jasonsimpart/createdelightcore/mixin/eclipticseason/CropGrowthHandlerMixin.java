package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.llamalad7.mixinextras.sugar.Local;
import com.teamtea.eclipticseasons.api.data.crop.GrowParameter;
import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import de.cadentem.quality_food.capability.LevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CropGrowthHandler.class, remap = false)
public abstract class CropGrowthHandlerMixin {

    @Shadow
    public static float getGrowChance(Event event, GrowParameter growParameter) {
        return 0;
    }

    @Redirect(method = "beforeCropGrowUp(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;getGrowChance(Lnet/minecraftforge/eventbus/api/Event;Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;)F"))
    private static float beforeCropGrowUpClimateMixin(Event event, GrowParameter growParameter, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        return create_Delight_Core$calculateChance(event, growParameter, level, pos);
    }
    @Redirect(method = "checkHumidity", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;getGrowChance(Lnet/minecraftforge/eventbus/api/Event;Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;)F"))
    private static float checkHumidity$modifyGrowChance(Event event, GrowParameter growParameter, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        return create_Delight_Core$calculateChance(event, growParameter, level, pos);
    }

    @Unique
    private static float create_Delight_Core$calculateChance(Event event, GrowParameter growParameter, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        int rank = LevelData.get(level, pos).level();
        float num = (float) (Math.pow(2, rank - 1) / 4);
        float growChance = getGrowChance(event, growParameter);
        if (rank > 0)
            return num + (1 - num) * growChance;
        return growChance;
    }
}
