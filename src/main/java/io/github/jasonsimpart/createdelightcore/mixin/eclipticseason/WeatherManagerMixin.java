package io.github.jasonsimpart.createdelightcore.mixin.eclipticseason;

import com.llamalad7.mixinextras.sugar.Local;
import com.teamabnormals.neapolitan.core.other.tags.NeapolitanItemTags;
import com.teamtea.eclipticseasons.common.core.biome.WeatherManager;
import com.teamtea.eclipticseasons.common.core.solar.SolarDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherManager.class)
public class WeatherManagerMixin {
    @Inject(method = "lambda$tickPlayerSeasonEffecct$4", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ServerPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"), remap = false, cancellable = true)
    private static void tickPlayerSeasonEffecctMixin(Level level, ServerPlayer player, SolarDataManager solarDataManager, CallbackInfo ci,@Local boolean isColdHe) {
        if (!isColdHe) {
            for (ItemStack itemstack : player.getInventory().items) {
                if (itemstack.is(NeapolitanItemTags.ICE_CUBES)) {
                    ci.cancel();
                }
            }
        }

    }
}
