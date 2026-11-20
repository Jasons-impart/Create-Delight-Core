package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.BarrelRecipeContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import de.cadentem.quality_food.core.Quality;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelBatch;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(value = BarrelBlockEntity.class, remap = false)
public abstract class BarrelQualityMixin implements TavernBarrelAccess {
    @Shadow private int brewLevel;
    @Shadow private int brewTime;
    @Shadow public abstract ItemStackHandler getIngredient();
    @Shadow public abstract ItemStackHandler getOutput();
    @Shadow public abstract FluidTank getFluid();
    @Shadow public abstract boolean isOpen();
    @Shadow private void resetIfOutputEmpty() { throw new AssertionError(); }
    @Unique private TavernBarrelBatch createdelightcore$qualityBatch;

    @Unique
    private void createdelightcore$refresh() { ((BarrelBlockEntity) (Object) this).refresh(); }

    @Override
    public TavernBarrelBatch createdelightcore$getQualityBatch() { return createdelightcore$qualityBatch; }

    @WrapOperation(method = "lambda$tick$0", at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/recipe/BarrelRecipe;assemble(Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/container/BarrelRecipeContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$startBatch(BarrelRecipe recipe, BarrelRecipeContainer input,
                                                  RegistryAccess registries, Operation<ItemStack> original) {
        ItemStack output = original.call(recipe, input, registries);
        createdelightcore$qualityBatch = null;
        if (!TavernQuality.isBatchProduct(output)) return output;
        createdelightcore$qualityBatch = new TavernBarrelBatch(createdelightcore$rollInputs(),
                Math.max(1, recipe.unitTime()), TavernQuality.canAge(output));
        return output;
    }

    @Unique
    private Quality createdelightcore$rollInputs() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < getIngredient().getSlots(); slot++) {
            ItemStack stack = getIngredient().getStackInSlot(slot);
            if (!stack.isEmpty()) items.add(stack.copy());
        }
        var fluid = getFluid().getFluid().copy();
        return QualityFoodFluidsCreateRules.rollBasinQuality(items, fluid.isEmpty() ? List.of() : List.of(fluid));
    }

    @Inject(method = "lambda$tick$1", at = @At("HEAD"))
    private void createdelightcore$startVinegar(CallbackInfo ci) {
        createdelightcore$qualityBatch = new TavernBarrelBatch(createdelightcore$rollInputs(), 2400, false);
    }

    @WrapOperation(method = {"lambda$tick$0", "lambda$tick$1"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"))
    private void createdelightcore$deferBrewedOutput(ItemStackHandler inventory, int slot, ItemStack output,
                                                    Operation<Void> original) {
        if (inventory == getOutput() && createdelightcore$qualityBatch != null) {
            createdelightcore$qualityBatch.deferOutput(output);
            original.call(inventory, slot, ItemStack.EMPTY);
        } else {
            original.call(inventory, slot, output);
        }
    }

    @Inject(method = {"lambda$tick$0", "lambda$tick$1"}, at = @At("TAIL"))
    private void createdelightcore$replaceNativeAging(CallbackInfo ci) {
        if (createdelightcore$qualityBatch == null) return;
        brewLevel = createdelightcore$qualityBatch.brewing() ? 1 : TavernQuality.NORMAL_BREW_LEVEL;
        brewTime = createdelightcore$qualityBatch.processingTicks();
        createdelightcore$refresh();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$tickQuality(Level level, CallbackInfo ci) {
        var batch = createdelightcore$qualityBatch;
        if (batch == null) return;
        ci.cancel(); // New batches never execute the native six-stage aging loop.
        if (level.isClientSide) return;
        if (getOutput().getStackInSlot(0).isEmpty() && !batch.hasPendingOutput()) {
            resetIfOutputEmpty();
            return;
        }
        if (isOpen() || batch.finished()) return;
        boolean changedPhase = batch.tick();
        if (changedPhase) {
            ItemStack completed = batch.takeCompletedOutput();
            if (completed.isEmpty()) completed = getOutput().getStackInSlot(0);
            getOutput().setStackInSlot(0, TavernQuality.qualify(completed, batch.quality()));
        }
        brewLevel = batch.brewing() ? 1 : TavernQuality.NORMAL_BREW_LEVEL;
        brewTime = batch.processingTicks();
        ((BlockEntity) (Object) this).setChanged();
        if (changedPhase || brewTime % 20 == 0) createdelightcore$refresh();
    }

    @Inject(method = "resetIfOutputEmpty", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$resetBatch(CallbackInfo ci) {
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.hasPendingOutput()) {
            ci.cancel();
            return;
        }
        if (getOutput().getStackInSlot(0).isEmpty()) createdelightcore$qualityBatch = null;
    }

    @Inject(method = "isMaxBrewLevel", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$finishedAging(CallbackInfoReturnable<Boolean> cir) {
        if (createdelightcore$qualityBatch != null) cir.setReturnValue(createdelightcore$qualityBatch.finished());
    }

    @Inject(method = "canTapExtract", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$waitBeforeTapping(Level level, net.minecraft.core.BlockPos pos,
                                                    LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()) cir.setReturnValue(false);
    }

    @Inject(method = "extractOneOutput", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$waitBeforeBottling(ItemStack carrier, CallbackInfoReturnable<ItemStack> cir) {
        // Tap, placed bottles and dropped carriers converge here before output or carrier consumption.
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()) cir.setReturnValue(ItemStack.EMPTY);
    }

    @ModifyReturnValue(method = "extractOneOutput", at = @At("RETURN"))
    private ItemStack createdelightcore$qualifyBottle(ItemStack result) {
        return createdelightcore$qualifyOutput(result);
    }

    @WrapOperation(method = "placeBottleResult", at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/item/BottleBlockItem;getFilledStack(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$qualifyPlacedBottle(BottleBlockItem bottle, int level, Operation<ItemStack> original) {
        ItemStack result = original.call(bottle, level);
        return createdelightcore$qualityBatch == null ? result : TavernQuality.qualify(result, createdelightcore$qualityBatch.quality());
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void createdelightcore$saveBatch(CompoundTag tag, CallbackInfo ci) {
        if (createdelightcore$qualityBatch != null) tag.put(TavernBarrelBatch.NBT_KEY, createdelightcore$qualityBatch.save());
    }

    @Inject(method = "load", at = @At("TAIL"), remap = true)
    private void createdelightcore$loadBatch(CompoundTag tag, CallbackInfo ci) {
        createdelightcore$qualityBatch = tag.contains(TavernBarrelBatch.NBT_KEY, Tag.TAG_COMPOUND)
                ? TavernBarrelBatch.load(tag.getCompound(TavernBarrelBatch.NBT_KEY)) : null;
        // Earlier test builds stored brewing output in the native slot; quarantine it on load as well.
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()
                && !createdelightcore$qualityBatch.hasPendingOutput() && !getOutput().getStackInSlot(0).isEmpty()) {
            createdelightcore$qualityBatch.deferOutput(getOutput().getStackInSlot(0));
            getOutput().setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Inject(method = "tipBrewInfo", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$qualityInfo(LivingEntity entity, CallbackInfo ci) {
        if (createdelightcore$qualityBatch == null) return;
        ci.cancel();
        if (entity instanceof ServerPlayer player) {
            var lines = TavernQuality.barrelTooltip((BarrelBlockEntity) (Object) this);
            var message = net.minecraft.network.chat.Component.empty();
            for (var line : lines) {
                if (!message.getSiblings().isEmpty()) message.append(" · ");
                message.append(line);
            }
            player.displayClientMessage(message, true);
        }
    }
}
