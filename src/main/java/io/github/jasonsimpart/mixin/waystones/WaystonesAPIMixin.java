package io.github.jasonsimpart.mixin.waystones;

import io.github.jasonsimpart.compat.waystones.WaystonesCurrencyCompat;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.blay09.mods.waystones.api.WaystonesAPI", remap = false)
public abstract class WaystonesAPIMixin {
    @Inject(method = "resolveRequirements", at = @At("RETURN"), cancellable = true)
    private static void createdelightcore$replaceXpRequirements(WaystoneTeleportContext context, CallbackInfoReturnable<WarpRequirement> cir) {
        Entity entity = context.getEntity();
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            cir.setReturnValue(WaystonesCurrencyCompat.replaceXpRequirements(cir.getReturnValue()));
        }
    }
}
