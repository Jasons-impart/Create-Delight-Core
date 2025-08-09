package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.llamalad7.mixinextras.sugar.Local;
import com.teamtea.eclipticseasons.api.constant.biome.Humidity;
import com.teamtea.eclipticseasons.api.constant.solar.Season;
import com.teamtea.eclipticseasons.api.data.crop.CropGrowControl;
import com.teamtea.eclipticseasons.api.data.crop.GrowParameter;
import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import com.teamtea.eclipticseasons.common.item.GrowthDetectorItem;
import de.cadentem.quality_food.capability.LevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GrowthDetectorItem.class, remap = false)
public class GrowthDetectorItemMixin {
    @Redirect(method = "getGrowChance", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;grow_chance()F"))
    private static float getGrowChanceMixin(GrowParameter instance, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        int rank = LevelData.get(level, pos).level();
        float num = (float) (Math.pow(2, rank - 1) / 4);
        float growChance = instance.grow_chance();
        if (rank > 0)
            return num + (1 - num) * growChance;
        return instance.grow_chance();
    }

    @Inject(method = "getHumidityGrowChance", at = @At(value = "HEAD"), cancellable = true)
    private static void getHumidityGrowChanceMixin(Level world, CropGrowControl growControl, Humidity env, CropGrowthHandler.RoomStatus roomStatus, BlockPos pos, BlockState blockState, Season season, boolean hasUpdate, CallbackInfoReturnable<Float> cir) {
        int rank = LevelData.get(world, pos).level();
        float num = (float) (Math.pow(2, rank - 1) / 4);
        float growChance = cir.getReturnValue();
        if (rank > 0)
            cir.setReturnValue(num + (1 - num) * growChance);
        cir.setReturnValue(1.f);
    }
}
