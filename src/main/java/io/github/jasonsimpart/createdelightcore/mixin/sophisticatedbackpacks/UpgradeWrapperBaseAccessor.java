package io.github.jasonsimpart.createdelightcore.mixin.sophisticatedbackpacks;

import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = UpgradeWrapperBase.class, remap = false)
public interface UpgradeWrapperBaseAccessor {
    @Accessor("storageWrapper")
    IStorageWrapper createdelightcore$getStorageWrapper();
}
