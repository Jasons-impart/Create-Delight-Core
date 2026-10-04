package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.teamtea.eclipticseasons.api.constant.solar.Season;
import com.teamtea.eclipticseasons.api.data.crop.CropGrowControl;
import com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler;
import io.github.jasonsimpart.createdelightcore.content.humidity.SmartHumidityRegulatorSeasonCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CropGrowthHandler.class, remap = false)
public abstract class SmartHumidityCropMixin {
    @org.spongepowered.asm.mixin.injection.Redirect(method = "checkHumidity", at = @At(value = "INVOKE",
            target = "Lcom/teamtea/eclipticseasons/api/data/crop/CropGrowControl;getGrowParameter(FLnet/minecraft/world/level/block/state/BlockState;)Lcom/teamtea/eclipticseasons/api/data/crop/GrowParameter;"))
    private static com.teamtea.eclipticseasons.api.data.crop.GrowParameter createdelightcore$selectedHumidityLevel(
            CropGrowControl control, float humidity, BlockState state,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Level level,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BlockPos pos) {
        Float target = level instanceof ServerLevel server ? SmartHumidityRegulatorSeasonCompat.target(server, pos) : null;
        return target == null ? control.getGrowParameter(humidity, state)
                : control.getGrowParameter(com.teamtea.eclipticseasons.api.constant.biome.Humidity.getHumid(target), state);
    }

    @Inject(method = "checkHumidity", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$absoluteCropHumidity(Event event, Level level, CropGrowControl control,
            float natural, CropGrowthHandler.RoomStatus room, BlockPos pos, BlockState state, Season season,
            boolean updated, int light, float random, CallbackInfo ci) {
        if (updated || !(level instanceof ServerLevel server)) return;
        Float target = SmartHumidityRegulatorSeasonCompat.target(server, pos);
        if (target == null) return;
        // Native crop checks clamp their base and can use a different biome sample than the API.
        // Apply the absolute target once; preserve the caller's room status and other growth rules.
        CropGrowthHandler.checkHumidity(event, level, control, target, room,
                pos, state, season, true, light, random);
        ci.cancel();
    }
}
