package io.github.jasonsimpart.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.jasonsimpart.compat.create.DrainOutputs;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = ItemDrainBlockEntity.class, remap = false)
public abstract class ItemDrainOutputsMixin extends SmartBlockEntity implements DrainOutputs {
    @Shadow TransportedItemStack heldItem;
    @Shadow protected int processingTicks;
    @Unique private final Deque<TransportedItemStack> createdelightcore$pending = new ArrayDeque<>();
    @Unique private static final String CREATEDELIGHT_OUTPUTS = "CreateDelightDrainOutputs";

    protected ItemDrainOutputsMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(method = "continueProcessing", at = @At(value = "INVOKE", target =
            "Lcom/simibubi/create/content/fluids/transfer/GenericItemEmptying;emptyItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Z)Lnet/createmod/catnip/data/Pair;"))
    private Pair<FluidStack, ItemStack> createdelightcore$emptyWithOutputs(Level level, ItemStack stack, boolean simulate) {
        var holder = AllRecipeTypes.EMPTYING.find(new SingleRecipeInput(stack), level);
        if (holder.isEmpty() || !(holder.get().value() instanceof EmptyingRecipe recipe)
                || recipe.getRollableResults().size() <= 1) {
            return GenericItemEmptying.emptyItem(level, stack, simulate);
        }
        // Simulation must not roll chance outputs, queue items, or consume the input.
        if (simulate) return Pair.of(recipe.getResultingFluid().copy(), ItemStack.EMPTY);
        var results = recipe.rollResults(level.random);
        stack.shrink(1);
        for (int i = 1; i < results.size(); i++) {
            var output = heldItem.copy();
            output.stack = results.get(i);
            output.beltPosition = output.prevBeltPosition = .5f;
            createdelightcore$pending.addLast(output);
        }
        setChanged();
        return Pair.of(recipe.getResultingFluid().copy(), results.isEmpty() ? ItemStack.EMPTY : results.getFirst());
    }

    @Unique
    private void createdelightcore$advanceOutput() {
        if (heldItem == null && !createdelightcore$pending.isEmpty() && level != null && !level.isClientSide) {
            heldItem = createdelightcore$pending.removeFirst();
            // Start beyond the recipe trigger: a product must not be processed a second time.
            processingTicks = 0;
            notifyUpdate();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void createdelightcore$advanceBeforeTick(CallbackInfo ci) {
        createdelightcore$advanceOutput();
    }

    @Inject(method = "getHeldItemStack", at = @At("HEAD"))
    private void createdelightcore$advanceBeforeAccess(CallbackInfoReturnable<ItemStack> cir) {
        // Belt insertion, item handlers and manual pickup all use this getter. Move an already
        // produced item onto the normal slot before any of them can observe a vacant drain.
        createdelightcore$advanceOutput();
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void createdelightcore$saveOutputs(CompoundTag tag, HolderLookup.Provider registries,
                                              boolean clientPacket, CallbackInfo ci) {
        ListTag outputs = new ListTag();
        for (var output : createdelightcore$pending) outputs.add(output.serializeNBT(registries));
        tag.put(CREATEDELIGHT_OUTPUTS, outputs);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void createdelightcore$loadOutputs(CompoundTag tag, HolderLookup.Provider registries,
                                              boolean clientPacket, CallbackInfo ci) {
        createdelightcore$pending.clear();
        for (Tag entry : tag.getList(CREATEDELIGHT_OUTPUTS, Tag.TAG_COMPOUND)) {
            var output = TransportedItemStack.read((CompoundTag) entry, registries);
            if (!output.stack.isEmpty()) createdelightcore$pending.addLast(output);
        }
    }

    @Override
    public void createdelightcore$dropPendingOutputs() {
        if (level == null || level.isClientSide) return;
        while (!createdelightcore$pending.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    createdelightcore$pending.removeFirst().stack);
        }
        setChanged();
    }
}
