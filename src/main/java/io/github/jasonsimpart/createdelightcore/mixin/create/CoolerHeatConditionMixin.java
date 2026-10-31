// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import io.github.jasonsimpart.createdelightcore.content.cooler.CoolerHeat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Arrays;

@Mixin(value = HeatCondition.class, remap = false)
public abstract class CoolerHeatConditionMixin {
    @Shadow @Final @Mutable private static HeatCondition[] $VALUES;

    @Invoker("<init>")
    private static HeatCondition createdelightcore$newCondition(String name, int ordinal, int color) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void createdelightcore$addCooling(CallbackInfo ci) {
        createdelightcore$append("COOLED", 0x55FFFF);
        createdelightcore$append("FROZEN", 0x5555FF);
    }

    @Unique
    private static void createdelightcore$append(String name, int color) {
        for (HeatCondition value : $VALUES) if (value.name().equals(name)) return;
        HeatCondition value = createdelightcore$newCondition(name, $VALUES.length, color);
        $VALUES = Arrays.copyOf($VALUES, $VALUES.length + 1);
        $VALUES[$VALUES.length - 1] = value;
    }

    @Inject(method = "testBlazeBurner", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$testCooling(BlazeBurnerBlock.HeatLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (CoolerHeat.isCooling((HeatCondition) (Object) this)) cir.setReturnValue(level == BlazeBurnerBlock.HeatLevel.NONE);
    }
}
