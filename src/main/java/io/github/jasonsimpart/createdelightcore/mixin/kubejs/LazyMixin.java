package io.github.jasonsimpart.createdelightcore.mixin.kubejs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/** Marker for CombatMixinPlugin's atomic cache bridge; no compile-time KubeJS dependency. */
@Pseudo
@Mixin(targets = "dev.latvian.mods.kubejs.util.Lazy", remap = false)
public abstract class LazyMixin {
}
