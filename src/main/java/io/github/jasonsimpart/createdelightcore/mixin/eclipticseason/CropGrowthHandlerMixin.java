package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CropGrowthHandler.class, remap = false)
public class CropGrowthHandlerMixin {
    @Shadow
    public static void setResult(Event event, int flag){};
    @Shadow @Final public static int GROW;

    @Inject(method = "beforeCropGrowUp(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;setResult(Lnet/minecraftforge/eventbus/api/Event;I)V"), cancellable = true)
    private static void beforeCropGrowUpClimateMixin(Event event, LevelAccessor level, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (LevelData.get(level, pos).level() > 0) {
            setResult(event, GROW);
            ci.cancel();
        }
    }
    @Inject(method = "beforeCropGrowUp(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler;checkHumidity(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lcom/teamtea/eclipticseasons/api/data/crop/CropGrowControl;Lcom/teamtea/eclipticseasons/api/constant/biome/Humidity;Lcom/teamtea/eclipticseasons/common/core/crop/CropGrowthHandler$RoomStatus;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/teamtea/eclipticseasons/api/constant/solar/Season;ZI)V"), cancellable = true)
    private static void beforeCropGrowUpHumidityMixin(Event event, LevelAccessor level, BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (LevelData.get(level, pos).level() > 1) {
            setResult(event, GROW);
            ci.cancel();
        }
    }

}
