package io.github.jasonsimpart.createdelightcore.mixin.sophisticatedbackpacks;

import io.github.jasonsimpart.createdelightcore.compat.sophisticatedbackpacks.SophisticatedBackpacksCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.refill.RefillUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RefillUpgradeWrapper.class, remap = false)
public abstract class RefillUpgradeWrapperMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void createdelightcore$refillConfigurationModules(Entity entity, Level level, BlockPos pos,
                                                               CallbackInfo ci) {
        RefillUpgradeWrapper self = (RefillUpgradeWrapper) (Object) this;
        if (level.isClientSide || self.isInCooldown(level) || !(entity instanceof Player player)) {
            return;
        }
        SophisticatedBackpacksCompat.onRefillUpgradeTick(player, self,
                ((UpgradeWrapperBaseAccessor) self).createdelightcore$getStorageWrapper());
    }
}
