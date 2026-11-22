package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.Vat;
import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatBrewing;
import com.gunmu.northeast_china_delight.crafting.VatRecipe;
import com.gunmu.northeast_china_delight.crafting.VatRecipes;
import com.gunmu.northeast_china_delight.fluid.ModFluids;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.SeasoningBottleCompat;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Vat.class, remap = false)
public abstract class VatMixin {
    @Shadow
    private static boolean canPourSourWater(VatBlockEntity vat, BlockState state) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract boolean tryStart(Level level, BlockPos pos, VatBlockEntity vat);

    @Inject(method = "vatUseItem", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$useBottle(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit,
                                             CallbackInfoReturnable<InteractionResult> cir) {
        if (!SeasoningBottleCompat.isBottle(stack)
                || !(level.getBlockEntity(pos) instanceof VatBlockEntity vat)
                || createdelightcore$isRemovingSeal(player, vat)) {
            return;
        }
        if (SeasoningBottleItem.isVatInput(stack)) {
            if (createdelightcore$pourSourWater(stack, state, level, pos, player, vat)) {
                cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
            }
            return;
        }
        if (!level.isClientSide) {
            createdelightcore$collectProduct(stack, level, pos, player, hand, vat);
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }

    @Inject(method = "vatUseItem", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$keepInputInteraction(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                        Player player, InteractionHand hand, BlockHitResult hit,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() == InteractionResult.PASS && SeasoningBottleCompat.isBottle(stack)
                && level.getBlockEntity(pos) instanceof VatBlockEntity vat
                && !createdelightcore$isRemovingSeal(player, vat)) {
            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    @Unique
    private static boolean createdelightcore$isRemovingSeal(Player player, VatBlockEntity vat) {
        return player.isSecondaryUseActive() && (vat.isPressed() || vat.isCovered());
    }

    @Unique
    private boolean createdelightcore$pourSourWater(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                    Player player, VatBlockEntity vat) {
        FluidStack fluid = SeasoningBottleItem.contents(stack);
        if (!fluid.getFluid().isSame(ModFluids.sourWaterSource()) || vat.isPressed() || !canPourSourWater(vat, state)) {
            return false;
        }
        if (!level.isClientSide) {
            int amount = SeasoningBottleItem.CAPACITY;
            if (fluid.getAmount() < amount) {
                player.displayClientMessage(Component.translatable(
                        "tooltip.createdelightcore.seasoning_bottle.vat_input_amount", amount), true);
                return true;
            }
            if (!player.getAbilities().instabuild) {
                SeasoningBottleCompat.consume(stack, amount);
            }
            if (vat.kind() == VatRecipes.Kind.NONE) {
                vat.setKind(VatRecipes.Kind.PICKLE);
            }
            vat.addSourWater(amount);
            level.playSound(null, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1f, 1f);
            tryStart(level, pos, vat);
        }
        return true;
    }

    @Unique
    private static void createdelightcore$collectProduct(ItemStack stack, Level level, BlockPos pos,
                                                          Player player, InteractionHand hand, VatBlockEntity vat) {
        FluidStack product = vat.getFluidInTank(0);
        if (product.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.northeast_china_delight.vat.bottle_empty"), true);
            return;
        }
        IFluidHandlerItem bottle = FluidUtil.getFluidHandler(stack).orElse(null);
        if (bottle == null) {
            return;
        }
        int amount = bottle.fill(product, FluidAction.SIMULATE);
        if (amount > 0) {
            // The vat's fluid capability awards its pump advancement on drain.
            product.setAmount(vat.drainProduct(amount));
            bottle.fill(product, FluidAction.EXECUTE);
            player.setItemInHand(hand, bottle.getContainer());
            vat.resetIfDrained();
            level.playSound(null, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1f, 1f);
        } else {
            String message = SeasoningBottleCompat.hasAmount(stack, SeasoningBottleItem.CAPACITY)
                    ? "message.northeast_china_delight.vat.bottle_full"
                    : "message.northeast_china_delight.vat.bottle_mismatch";
            player.displayClientMessage(Component.translatable(message), true);
        }
    }

    @WrapOperation(method = "vatUseItem", at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/crafting/VatBrewing;forInsert(Lcom/gunmu/northeast_china_delight/block/VatBlockEntity;Lnet/minecraft/world/item/ItemStack;)Lcom/gunmu/northeast_china_delight/crafting/VatRecipe;"))
    private VatRecipe createdelightcore$matchSeasoning(VatBlockEntity vat, ItemStack stack, Operation<VatRecipe> original) {
        return original.call(vat, SeasoningBottleCompat.asIngredient(stack));
    }

    @WrapOperation(method = "vatUseItem", at = @At(value = "INVOKE", target =
            "Lcom/gunmu/northeast_china_delight/block/Vat;insert(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lcom/gunmu/northeast_china_delight/block/VatBlockEntity;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lcom/gunmu/northeast_china_delight/crafting/VatRecipe;Lnet/minecraft/world/InteractionHand;)Z"))
    private boolean createdelightcore$insertDose(Vat block, Level level, BlockPos pos, VatBlockEntity vat,
                                                 Player player, ItemStack stack, VatRecipe recipe, InteractionHand hand,
                                                 Operation<Boolean> original) {
        if (!SeasoningBottleCompat.isBottle(stack)) {
            return original.call(block, level, pos, vat, player, stack, recipe, hand);
        }
        ItemStack ingredient = SeasoningBottleCompat.asIngredient(stack);
        int slot = recipe.findSlotFor(ingredient, VatBrewing.counts(vat, null));
        if (slot < 0) {
            return false;
        }
        int amount = recipe.slots().get(slot).perDose() ? SeasoningBottleCompat.COOKING_DOSE : SeasoningBottleItem.CAPACITY;
        if (!SeasoningBottleCompat.hasAmount(stack, amount)) {
            return false;
        }
        ItemStack dose = SeasoningBottleCompat.asIngredient(stack, amount);
        if (!original.call(block, level, pos, vat, player, dose, recipe, hand)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            SeasoningBottleCompat.consume(stack, amount);
        }
        return true;
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE", remap = true, target =
            "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void createdelightcore$keepBottle(Player player, InteractionHand hand, ItemStack remainder,
                                               Operation<Void> original) {
        if (!SeasoningBottleCompat.isBottle(player.getItemInHand(hand))) {
            original.call(player, hand, remainder);
        }
    }
}
