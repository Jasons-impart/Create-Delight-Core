package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.llamalad7.mixinextras.sugar.Local;
import com.teamtea.eclipticseasons.api.data.crop.GrowParameter;
import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import de.cadentem.quality_food.capability.LevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CropGrowthHandler.class, remap = false)
public abstract class CropGrowthHandlerMixin {
    @Shadow
    public static void setResult(Event event, int flag){};
    @Shadow @Final public static int GROW;

    @Shadow
    public static float getGrowChance(Event event, GrowParameter growParameter) {
        return 0;
    }

    @Redirect(method = "beforeCropGrowUp(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;getGrowChance(Lnet/minecraftforge/eventbus/api/Event;Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;)F"))
    private static float beforeCropGrowUpClimateMixin(Event event, GrowParameter growParameter, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (LevelData.get(level, pos).level() > 0)
            return 1;
        return getGrowChance(event, growParameter);
    }
    @Inject(method = "beforeCropGrowUp(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;checkHumidity(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/Level;Lcom/teamtea/eclipticseasons/api/data/crop/CropGrowControl;Lcom/teamtea/eclipticseasons/api/constant/biome/Humidity;Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler$RoomStatus;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/teamtea/eclipticseasons/api/constant/solar/Season;ZIF)V"), cancellable = true)
    private static void beforeCropGrowUpHumidityMixin(Event event, Level level, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (LevelData.get(level, pos).level() > 1) {
            setResult(event, GROW);
            ci.cancel();
        }
    }

}
