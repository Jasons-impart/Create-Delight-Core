package io.github.jasonsimpart.createdelightcore.mixin.ironsspellbooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;

@Mixin(value = AlchemistCauldronTile.class, remap = false)
public abstract class AlchemistCauldronQualityMixin {
    @ModifyConstant(method = "serverTick", constant = @Constant(intValue = 100))
    private static int createdelightcore$wortPreparationTime(int original, Level level, BlockPos pos,
                                                            BlockState state, AlchemistCauldronTile tile,
                                                            @Local(index = 5) ItemStack item) {
        if (item.isEmpty()) return original;
        for (String drink : List.of("amethyst_tequila", "catacombs_wine", "ice_venom_wine")) {
            var candidate = level.getRecipeManager().byKey(new ResourceLocation("farmers_spell", "alchemist_cauldron/" + drink));
            if (candidate.isEmpty() || !(candidate.get() instanceof BrewAlchemistCauldronRecipe recipe)) continue;
            // Retaining the upstream ID must not speed up its original, finished-drink recipe.
            if (recipe.results().stream().noneMatch(output -> new ResourceLocation("createdelight", drink + "_wort")
                    .equals(net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(output.getFluid())))) continue;
            for (FluidStack fluid : tile.fluidInventory.fluids()) {
                if (recipe.matches(new BrewAlchemistCauldronRecipe.Input(fluid, item), level)) return 19;
            }
        }
        return original;
    }

    @WrapOperation(method = "tryMeltInput", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/BrewAlchemistCauldronRecipe;fluidIn()Lnet/minecraftforge/fluids/FluidStack;"))
    private FluidStack createdelightcore$consumeActualBatch(BrewAlchemistCauldronRecipe recipe,
                                                            Operation<FluidStack> original,
                                                            @Local FluidStack fluid,
                                                            @Share("brewInput") LocalRef<FluidStack> input) {
        FluidStack required = original.call(recipe);
        FluidStack actual = fluid.copy();
        actual.setAmount(required.getAmount());
        input.set(actual.copy());
        // contains/drain must use the actual batch's NBT, not the unqualified recipe template.
        return actual;
    }

    @WrapOperation(method = "tryMeltInput", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/BrewAlchemistCauldronRecipe;results()Ljava/util/List;"))
    private List<FluidStack> createdelightcore$brewQuality(BrewAlchemistCauldronRecipe recipe,
                                                          Operation<List<FluidStack>> original,
                                                          @Local(argsOnly = true) ItemStack item,
                                                          @Share("brewInput") LocalRef<FluidStack> input) {
        List<FluidStack> outputs = original.call(recipe);
        if (input.get() == null) return outputs;
        var quality = QualityFoodFluidsCreateRules.rollBasinQuality(List.of(item.copy()), List.of(input.get()));
        return outputs.stream().map(output -> QualityFoodFluidsCreateRules.applyFluidOutputQuality(
                quality, output.copy())).toList();
    }

    @WrapOperation(method = "tryExecuteRecipeInteractions", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/FillAlchemistCauldronRecipe;result()Lnet/minecraftforge/fluids/FluidStack;"))
    private FluidStack createdelightcore$emptyBottle(FillAlchemistCauldronRecipe recipe,
                                                      Operation<FluidStack> original,
                                                      @Local(argsOnly = true) ItemStack item) {
        return QualityFoodFluidsCreateRules.applyEmptyingQuality(item, original.call(recipe).copy());
    }

    @WrapOperation(method = "tryExecuteRecipeInteractions", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/EmptyAlchemistCauldronRecipe;fluid()Lnet/minecraftforge/fluids/FluidStack;"))
    private FluidStack createdelightcore$drainActualBatch(EmptyAlchemistCauldronRecipe recipe,
                                                         Operation<FluidStack> original,
                                                         @Local FluidStack topFluid) {
        FluidStack actual = topFluid.copy();
        actual.setAmount(original.call(recipe).getAmount());
        return actual;
    }

    @WrapOperation(method = "tryExecuteRecipeInteractions", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/EmptyAlchemistCauldronRecipe;assemble(Lio/redspace/ironsspellbooks/recipe_types/alchemist_cauldron/EmptyAlchemistCauldronRecipe$Input;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$fillBottle(EmptyAlchemistCauldronRecipe recipe,
                                                   EmptyAlchemistCauldronRecipe.Input input,
                                                   RegistryAccess access, Operation<ItemStack> original) {
        return QualityFoodFluidsCreateRules.applyFillingQuality(input.fluid(), original.call(recipe, input, access).copy());
    }
}
