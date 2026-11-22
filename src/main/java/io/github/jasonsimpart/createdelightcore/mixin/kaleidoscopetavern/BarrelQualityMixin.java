package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.BarrelRecipeContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelAccess;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelBatch;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelFluidOutput;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernBarrelServing;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.function.Predicate;

@Pseudo
@Mixin(value = BarrelBlockEntity.class, remap = false)
public abstract class BarrelQualityMixin implements TavernBarrelAccess {
    @Shadow private int brewLevel;
    @Shadow private int brewTime;
    @Shadow public abstract ItemStackHandler getIngredient();
    @Shadow public abstract ItemStackHandler getOutput();
    @Shadow public abstract FluidTank getFluid();
    @Shadow public abstract boolean isOpen();
    @Shadow public abstract ResourceLocation getRecipeId();
    @Shadow private Ingredient getCurrentCarrier(Level level) { throw new AssertionError(); }
    @Shadow private void transform(Level level, BlockPos pos, Ingredient carrier, ItemStack result) { throw new AssertionError(); }
    @Shadow private void resetIfOutputEmpty() { throw new AssertionError(); }
    @Unique private TavernBarrelBatch createdelightcore$qualityBatch;
    @Unique private boolean createdelightcore$migrateLegacyOutput;

    @Unique
    private void createdelightcore$refresh() { ((BarrelBlockEntity) (Object) this).refresh(); }

    @Override
    public TavernBarrelBatch createdelightcore$getQualityBatch() { return createdelightcore$qualityBatch; }

    @Override
    public void createdelightcore$fluidDrained() {
        var output = createdelightcore$getFluidOutput();
        if (output == null) return;
        if (output.amount() == 0) resetIfOutputEmpty();
        else createdelightcore$refresh();
    }

    @WrapOperation(method = "lambda$tick$0", at = @At(value = "INVOKE",
            target = "Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/recipe/BarrelRecipe;assemble(Lcom/github/ysbbbbbb/kaleidoscopetavern/crafting/container/BarrelRecipeContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$startBatch(BarrelRecipe recipe, BarrelRecipeContainer input,
                                                  RegistryAccess registries, Operation<ItemStack> original) {
        ItemStack output = original.call(recipe, input, registries);
        createdelightcore$qualityBatch = null;
        var fluidOutput = TavernBarrelFluidOutput.select(recipe.getId(), output, recipe.carrier(), TavernQuality.NORMAL_BREW_LEVEL);
        if (fluidOutput != null || TavernQuality.isBatchProduct(output)) {
            createdelightcore$qualityBatch = new TavernBarrelBatch(createdelightcore$rollInputs(),
                    Math.max(1, recipe.unitTime()), TavernQuality.canAge(output));
            if (fluidOutput != null) createdelightcore$qualityBatch.useFluidOutput(fluidOutput);
        }
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

    @Inject(method = "lambda$tick$1", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$keepUnmatchedIngredients(CallbackInfo ci) {
        ci.cancel();
    }

    @WrapOperation(method = "lambda$tick$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"))
    private void createdelightcore$deferBrewedOutput(ItemStackHandler inventory, int slot, ItemStack output,
                                                    Operation<Void> original) {
        if (inventory == getOutput() && createdelightcore$qualityBatch != null) {
            if (createdelightcore$getFluidOutput() == null) createdelightcore$qualityBatch.deferOutput(output);
            original.call(inventory, slot, ItemStack.EMPTY);
        } else {
            original.call(inventory, slot, output);
        }
    }

    @Inject(method = "lambda$tick$0", at = @At("TAIL"))
    private void createdelightcore$replaceNativeAging(CallbackInfo ci) {
        if (createdelightcore$qualityBatch == null) return;
        createdelightcore$updateBrewFields(createdelightcore$qualityBatch);
        createdelightcore$refresh();
    }

    @Unique
    private void createdelightcore$updateBrewFields(TavernBarrelBatch batch) {
        brewLevel = batch.brewing() ? 1 : TavernQuality.NORMAL_BREW_LEVEL;
        brewTime = batch.processingTicks();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$tickQuality(Level level, CallbackInfo ci) {
        if (!level.isClientSide && createdelightcore$migrateLegacyOutput) {
            createdelightcore$migrateLegacyOutput = false;
            createdelightcore$migrateOutput(level);
        }
        var batch = createdelightcore$qualityBatch;
        if (batch == null) return;
        ci.cancel(); // New batches never execute the native six-stage aging loop.
        if (level.isClientSide) return;
        var output = batch.fluidOutput();
        if (output == null && getOutput().getStackInSlot(0).isEmpty() && !batch.hasPendingOutput()
                || output != null && !batch.brewing() && output.amount() == 0) {
            resetIfOutputEmpty();
            return;
        }
        if (output == null && !batch.brewing() && batch.hasPendingOutput()) {
            createdelightcore$writeItemOutput(batch);
            createdelightcore$refresh();
        }
        if (isOpen() || batch.finished()) return;
        boolean brewing = batch.brewing();
        boolean changedPhase = batch.tick();
        if (changedPhase) {
            if (output == null) {
                createdelightcore$writeItemOutput(batch);
            } else if (!brewing) {
                output.updateQuality(batch.quality());
            } else if (!output.completeBrewing(batch.quality())) {
                batch.fallBackToItems();
                createdelightcore$writeItemOutput(batch);
            }
        }
        createdelightcore$updateBrewFields(batch);
        ((BlockEntity) (Object) this).setChanged();
        if (changedPhase || brewTime % 20 == 0) createdelightcore$refresh();
    }

    @Unique
    private void createdelightcore$writeItemOutput(TavernBarrelBatch batch) {
        ItemStack completed = batch.takeCompletedOutput();
        if (completed.isEmpty()) completed = getOutput().getStackInSlot(0);
        getOutput().setStackInSlot(0, TavernQuality.qualify(completed, batch.quality()));
    }

    @Inject(method = "resetIfOutputEmpty", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$resetBatch(CallbackInfo ci) {
        var output = createdelightcore$getFluidOutput();
        if (createdelightcore$qualityBatch != null && (createdelightcore$qualityBatch.hasPendingOutput()
                || output != null && output.amount() > 0)) {
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
    private void createdelightcore$waitBeforeTapping(Level level, BlockPos pos,
                                                    LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()) cir.setReturnValue(false);
    }

    @WrapOperation(method = "canTapExtract", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack createdelightcore$checkServing(ItemStackHandler inventory, int slot, Operation<ItemStack> original) {
        var output = createdelightcore$getFluidOutput();
        return output == null ? original.call(inventory, slot) : output.previewBottle();
    }

    @Inject(method = "getCurrentCarrier", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$useSavedCarrier(Level level, CallbackInfoReturnable<Ingredient> cir) {
        var output = createdelightcore$getFluidOutput();
        if (output != null) cir.setReturnValue(output.carrier());
    }

    @Inject(method = "doTapExtract", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$tapFluidOutput(Level level, BlockPos tapPos, CallbackInfo ci) {
        var output = createdelightcore$getFluidOutput();
        if (output == null) return;
        ci.cancel();
        if (level.isClientSide) return;
        ItemStack preview = output.previewBottle();
        if (!preview.isEmpty()) transform(level, tapPos, output.carrier(), preview);
    }

    @Inject(method = "transformPlacedCarrier", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$fillPlacedCarrier(Level level, BlockPos below, BlockState state,
                                                     ItemStack template, CallbackInfo ci) {
        var output = createdelightcore$getFluidOutput();
        if (output == null) return;
        ci.cancel();
        if (!level.isClientSide && output.carrier().test(state.getBlock().asItem().getDefaultInstance())) {
            createdelightcore$transferServing(output, filled -> TavernBarrelServing.intoPlacedCarrier(level, below, state, filled));
        }
    }

    @Inject(method = "transformItemCarrier", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$fillGroundCarrier(Level level, BlockPos below, ItemEntity entity,
                                                     ItemStack template, CallbackInfo ci) {
        var output = createdelightcore$getFluidOutput();
        if (output == null) return;
        ci.cancel();
        if (!level.isClientSide && output.carrier().test(entity.getItem())) {
            createdelightcore$transferServing(output, filled -> TavernBarrelServing.intoDroppedCarrier(level, below, entity, filled));
        }
    }

    @Inject(method = "extractOneOutput", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$waitBeforeBottling(ItemStack carrier, CallbackInfoReturnable<ItemStack> cir) {
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        var output = createdelightcore$getFluidOutput();
        if (output != null) cir.setReturnValue(createdelightcore$transferServing(output, filled -> true));
    }

    @Unique
    private ItemStack createdelightcore$transferServing(TavernBarrelFluidOutput output, Predicate<ItemStack> deliver) {
        var filled = output.transferServing(deliver);
        if (!filled.isEmpty()) createdelightcore$fluidDrained();
        return filled;
    }

    @ModifyReturnValue(method = "extractOneOutput", at = @At("RETURN"))
    private ItemStack createdelightcore$qualifyBottle(ItemStack result) {
        return createdelightcore$getFluidOutput() == null ? createdelightcore$qualifyOutput(result) : result;
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
        var saved = tag.getCompound(TavernBarrelBatch.NBT_KEY);
        createdelightcore$qualityBatch = TavernBarrelBatch.load(saved);
        createdelightcore$migrateLegacyOutput = brewLevel > 0 && saved.getInt("version") < TavernBarrelBatch.NBT_VERSION;
        // Earlier test builds stored brewing output in the native slot; quarantine it on load as well.
        if (createdelightcore$qualityBatch != null && createdelightcore$qualityBatch.brewing()
                && !createdelightcore$qualityBatch.hasPendingOutput() && !getOutput().getStackInSlot(0).isEmpty()) {
            createdelightcore$qualityBatch.deferOutput(getOutput().getStackInSlot(0));
            getOutput().setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Unique
    private void createdelightcore$migrateOutput(Level level) {
        var batch = createdelightcore$qualityBatch;
        ItemStack stock = batch != null && batch.hasPendingOutput() ? batch.pendingOutput() : getOutput().getStackInSlot(0);
        if (stock.isEmpty()) return;
        var recipeId = getRecipeId() == null ? BarrelRecipeSerializer.EMPTY_RECIPE_ID : getRecipeId();
        int variant = batch == null ? brewLevel : TavernQuality.NORMAL_BREW_LEVEL;
        var output = TavernBarrelFluidOutput.select(recipeId, stock, getCurrentCarrier(level), variant);
        if (output == null) return;
        if (batch == null) {
            // Native saves without a CDC batch keep their already-established variant and do not restart aging.
            var quality = QualityUtils.getQuality(stock);
            batch = new TavernBarrelBatch(quality, brewLevel == 1 ? Math.max(0, brewTime) : 0, false);
        }
        if (!batch.brewing() && !output.completeBrewing(batch.quality())) return;
        batch.useFluidOutput(output);
        createdelightcore$qualityBatch = batch;
        getOutput().setStackInSlot(0, ItemStack.EMPTY);
        createdelightcore$updateBrewFields(batch);
        createdelightcore$refresh();
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
