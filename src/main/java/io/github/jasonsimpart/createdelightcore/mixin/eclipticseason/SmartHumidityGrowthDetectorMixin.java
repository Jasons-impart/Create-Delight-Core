package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.api.constant.solar.Season;
import com.teamtea.eclipticseasons.api.data.crop.CropGrowControl;
import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import com.teamtea.eclipticseasons.common.item.GrowthDetectorItem;
import io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GrowthDetectorItem.class, remap = false)
public abstract class SmartHumidityGrowthDetectorMixin {
    @org.spongepowered.asm.mixin.Unique
    private static Float createdelightcore$target(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) return SmartHumidityRegulatorSeasonCompat.target(server, pos);
        return level.isClientSide ? io.github.jasonsimpart.createdelightcore.network.ClientHumidityRoomCache.target(level, pos) : null;
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "getHumidityGrowChance", at = @At(value = "INVOKE",
            target = "Lcom/teamtea/eclipticseasons/api/data/crop/CropGrowControl;getGrowParameter(FLnet/minecraft/world/level/block/state/BlockState;)Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;"))
    private static com.teamtea.eclipticseasons.api.data.crop.GrowParameter createdelightcore$selectedHumidityLevel(
            CropGrowControl control, float humidity, BlockState state,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Level level,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BlockPos pos) {
        Float target = createdelightcore$target(level, pos);
        return target == null ? control.getGrowParameter(humidity, state)
                : control.getGrowParameter(com.teamtea.eclipticseasons.api.constant.biome.Humidity.getHumid(target), state);
    }

    @Shadow private static float getHumidityGrowChance(Level level, CropGrowControl control, float humidity,
            CropGrowthHandler.RoomStatus room, BlockPos pos, BlockState state, Season season, boolean updated) {
        throw new AssertionError();
    }
    @Inject(method = "getHumidityGrowChance", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$absoluteGrowthReading(Level level, CropGrowControl control, float humidity,
            CropGrowthHandler.RoomStatus room, BlockPos pos, BlockState state, Season season, boolean updated,
            CallbackInfoReturnable<Float> cir) {
        if (updated) return;
        Float target = createdelightcore$target(level, pos);
        if (target != null) cir.setReturnValue(getHumidityGrowChance(level, control, target,
                room, pos, state, season, true));
    }
}
