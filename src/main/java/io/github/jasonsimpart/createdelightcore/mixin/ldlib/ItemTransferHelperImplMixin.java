package io.github.jasonsimpart.createdelightcore.mixin.ldlib;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * LDLib 1.0.50 的 importToTarget / exportToTarget 按模拟结果抽取后，
 * 会丢掉真实 insertItem 的余量。过滤器使模拟能进 64、实际只能进 8 时
 * 会吞掉剩余物品（官网反馈 9SSZBK，典型为合金电炉锻造钢输入总线 AutoIO）。
 *
 * 本 mixin 不改模拟阶段；真实插入后先把余量退回源槽，再尝试源容器其余槽，
 * maxAmount 只减去目标实际接受的数量。源仍拒收则记日志，并在服务端把物品
 * 丢在源方块处，避免悄悄消失。
 */
@Mixin(ItemTransferHelperImpl.class)
public abstract class ItemTransferHelperImplMixin {
    @Inject(
            method = "exportToTarget",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void createdelightcore$exportToTargetKeepRemainder(
            IItemTransfer source,
            int maxAmount,
            Predicate<ItemStack> predicate,
            Level level,
            BlockPos pos,
            @Nullable Direction direction,
            CallbackInfo ci
    ) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                var cap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve();
                if (cap.isPresent()) {
                    var target = cap.get();
                    for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
                        ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true, false);
                        if (sourceStack.isEmpty() || !predicate.test(sourceStack)) {
                            continue;
                        }
                        ItemStack remainder = ItemTransferHelperImpl.insertItem(target, sourceStack, true);
                        int amountToInsert = sourceStack.getCount() - remainder.getCount();
                        if (amountToInsert > 0) {
                            sourceStack = source.extractItem(srcIndex, Math.min(maxAmount, amountToInsert), false, true);
                            ItemStack leftover = ItemTransferHelperImpl.insertItem(target, sourceStack, false);
                            int accepted = sourceStack.getCount() - leftover.getCount();
                            if (accepted < 0) {
                                accepted = 0;
                            }
                            if (!leftover.isEmpty()) {
                                leftover = returnLeftoverToTransfer(source, srcIndex, leftover);
                                if (!leftover.isEmpty()) {
                                    logAndDropLeftover(level, pos, leftover);
                                }
                            }
                            maxAmount -= accepted;
                            if (maxAmount <= 0) {
                                ci.cancel();
                                return;
                            }
                        }
                    }
                }
            }
        }
        ci.cancel();
    }

    @Inject(
            method = "importToTarget",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void createdelightcore$importToTargetKeepRemainder(
            IItemTransfer target,
            int maxAmount,
            Predicate<ItemStack> predicate,
            Level level,
            BlockPos pos,
            @Nullable Direction direction,
            CallbackInfo ci
    ) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                var cap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve();
                if (cap.isPresent()) {
                    var source = cap.get();
                    for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
                        ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true);
                        if (sourceStack.isEmpty() || !predicate.test(sourceStack)) {
                            continue;
                        }
                        ItemStack remainder = ItemTransferHelperImpl.insertItem(target, sourceStack, true);
                        int amountToInsert = sourceStack.getCount() - remainder.getCount();
                        if (amountToInsert > 0) {
                            sourceStack = source.extractItem(srcIndex, Math.min(maxAmount, amountToInsert), false);
                            ItemStack leftover = ItemTransferHelperImpl.insertItem(target, sourceStack, false);
                            int accepted = sourceStack.getCount() - leftover.getCount();
                            if (accepted < 0) {
                                accepted = 0;
                            }
                            if (!leftover.isEmpty()) {
                                leftover = returnLeftoverToHandler(source, srcIndex, leftover);
                                if (!leftover.isEmpty()) {
                                    logAndDropLeftover(level, pos, leftover);
                                }
                            }
                            maxAmount -= accepted;
                        }
                        if (maxAmount <= 0) {
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
        ci.cancel();
    }

    private static ItemStack returnLeftoverToTransfer(IItemTransfer source, int srcIndex, ItemStack leftover) {
        leftover = source.insertItem(srcIndex, leftover, false, true);
        if (leftover.isEmpty()) {
            return leftover;
        }
        int slots = source.getSlots();
        for (int i = 0; i < slots; i++) {
            if (i == srcIndex) {
                continue;
            }
            leftover = source.insertItem(i, leftover, false, true);
            if (leftover.isEmpty()) {
                return leftover;
            }
        }
        return leftover;
    }

    private static ItemStack returnLeftoverToHandler(IItemHandler source, int srcIndex, ItemStack leftover) {
        leftover = source.insertItem(srcIndex, leftover, false);
        if (leftover.isEmpty()) {
            return leftover;
        }
        int slots = source.getSlots();
        for (int i = 0; i < slots; i++) {
            if (i == srcIndex) {
                continue;
            }
            leftover = source.insertItem(i, leftover, false);
            if (leftover.isEmpty()) {
                return leftover;
            }
        }
        return leftover;
    }

    private static void logAndDropLeftover(Level level, BlockPos pos, ItemStack leftover) {
        CreateDelightCore.LOGGER.warn(
                "[Create Delight] LDLib ItemTransferHelperImpl leftover could not be returned to source at {}; dropping remaining items: {}",
                pos,
                leftover
        );
        if (!level.isClientSide) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, leftover);
        }
    }
}
