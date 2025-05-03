package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import io.github.jasonsimpart.createdelightcore.registry.CDCItemAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemAttribute.StandardTraits.class, remap = false)
public class StandardTraitsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false, require = 1)
    private static void registerAdditional(CallbackInfo ci) {
        CDCItemAttributes.register();
    }
}
