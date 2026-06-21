package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlock;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlockEntity;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinInventory;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezingRecipe;
import org.forsteri.ratatouille.entry.CRItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SqueezingRecipe.class, remap = false)
public abstract class SqueezingRecipeMixin {

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$matchInventoryWithFluidAmount(SqueezeBasinInventory inventory, Level level, CallbackInfoReturnable<Boolean> cir) {
        SqueezingRecipe recipe = (SqueezingRecipe) (Object) this;

        if (recipe.getFluidIngredients().isEmpty()) {
            return;
        }

        if (inventory.blockEntity == null) {
            cir.setReturnValue(false);
            return;
        }

        IFluidHandler fluidHandler = inventory.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (!createdelightcore$hasEnoughFluid(recipe, fluidHandler)) {
            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(createdelightcore$matchesItems(recipe, inventory, inventory.blockEntity, recipe.useCasing()));
    }

    @Inject(method = "match", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$matchBasinWithFluidAmount(SqueezeBasinBlockEntity blockEntity, boolean hasCasing, CallbackInfoReturnable<Boolean> cir) {
        SqueezingRecipe recipe = (SqueezingRecipe) (Object) this;

        if (recipe.getFluidIngredients().isEmpty()) {
            return;
        }

        IFluidHandler fluidHandler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (blockEntity.inputInventory == null || !createdelightcore$hasEnoughFluid(recipe, fluidHandler)) {
            cir.setReturnValue(false);
            return;
        }

        if (recipe.useCasing() != hasCasing) {
            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(createdelightcore$matchesItems(recipe, blockEntity.inputInventory, blockEntity, hasCasing));
    }

    @Unique
    private static boolean createdelightcore$hasEnoughFluid(SqueezingRecipe recipe, IFluidHandler fluidHandler) {
        if (fluidHandler == null) {
            return false;
        }

        FluidStack fluidStack = fluidHandler.getFluidInTank(0);
        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            if (fluidIngredient.test(fluidStack) && fluidStack.getAmount() >= fluidIngredient.getRequiredAmount()) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static boolean createdelightcore$matchesItems(SqueezingRecipe recipe, SqueezeBasinInventory inventory, SqueezeBasinBlockEntity blockEntity, boolean hasCasing) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        ItemStack inputStack = inventory.getItem(0);

        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(CRItems.SAUSAGE_CASING.asStack())) {
                if (!Boolean.TRUE.equals(blockEntity.getBlockState().getValue(SqueezeBasinBlock.CASING))) {
                    return false;
                }
                continue;
            }

            if (ingredient.test(inputStack)) {
                return true;
            }
        }

        return hasCasing && ingredients.size() == 1;
    }
}
