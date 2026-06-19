package io.github.jasonsimpart.mixin.vintageimprovements;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumizingRecipe", remap = false)
public abstract class VacuumizingRecipeMixin {
    private static final String VACUUMIZING_RECIPE =
            "com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumizingRecipe";

    @Inject(method = "getSequenceId", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$fixCreate6SequenceId(
            RecipeHolder<? extends Recipe<?>> holder,
            CallbackInfoReturnable<String> cir
    ) {
        if (!holder.value().getClass().getName().equals(VACUUMIZING_RECIPE)) {
            cir.setReturnValue("");
            return;
        }

        String id = holder.id().toString();
        int index = id.lastIndexOf("_step_");
        cir.setReturnValue(index > -1 ? id.substring(0, index) : id);
    }
}
