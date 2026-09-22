package io.github.jasonsimpart.content.quality.harvest;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LifeMatterInjectorBlockEntity extends SmartBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int INJECT_AMOUNT = 16;
    public static final int INJECT_INTERVAL = 5;
    private static final int SCAN_INTERVAL = 10;
    private static final String INVENTORY_TAG = "Inventory";

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && QualityHarvestControllerBlockEntity.isLifeMatter(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendData();
            if (level != null) {
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    };

    private int lastTransferred;
    private int transferCooldown;
    private int scanCooldown;
    private DockingScan cachedScan = DockingScan.none();
    private boolean controllerInRange;
    private boolean acceptingControllerInRange;
    private int lastTargetStored = -1;

    public LifeMatterInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(io.github.jasonsimpart.registry.ModBlockEntities.LIFE_MATTER_INJECTOR.get(), pos, state);
        setLazyTickRate(1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) {
            return;
        }

        if (transferCooldown > 0) {
            transferCooldown--;
        }

        if (isPowered()) {
            cachedScan = DockingScan.none();
            scanCooldown = 0;
            if (applyScanStatus(DockingScan.none())) {
                setChanged();
                sendData();
            }
            return;
        }

        if (scanCooldown > 0) {
            scanCooldown--;
        }

        DockingScan scan;
        DockedController cached = cachedScan.controller();
        if (scanCooldown <= 0 || (cached != null && !cached.entity().isAlive())) {
            scanCooldown = SCAN_INTERVAL;
            scan = findDockingTarget();
            cachedScan = scan;
        } else {
            scan = cachedScan;
        }

        boolean changed = applyScanStatus(scan);
        ItemStack input = getInputStack();
        if (QualityHarvestControllerBlockEntity.isLifeMatter(input)
                && scan.controller() != null
                && transferCooldown <= 0) {
            lastTransferred = tryInject(scan.controller());
            transferCooldown = INJECT_INTERVAL;
            changed = true;
        } else if (lastTransferred != 0) {
            lastTransferred = 0;
            changed = true;
        }

        if (changed) {
            setChanged();
            sendData();
        }
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public ItemStack getInputStack() {
        return inventory.getStackInSlot(INPUT_SLOT);
    }

    public int getLastTransferred() {
        return lastTransferred;
    }

    public boolean hasControllerInRange() {
        return controllerInRange;
    }

    public boolean hasAcceptingControllerInRange() {
        return acceptingControllerInRange;
    }

    public int getLastTargetStored() {
        return lastTargetStored;
    }

    public boolean isPowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public int insertLifeMatter(ItemStack stack) {
        if (!QualityHarvestControllerBlockEntity.isLifeMatter(stack)) {
            return 0;
        }
        ItemStack remainder = inventory.insertItem(INPUT_SLOT, stack, false);
        return stack.getCount() - remainder.getCount();
    }

    public ItemStack removeInputStack() {
        ItemStack stack = inventory.extractItem(INPUT_SLOT, inventory.getSlotLimit(INPUT_SLOT), false);
        setChanged();
        sendData();
        return stack;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        ItemStack stack = removeInputStack();
        if (!stack.isEmpty()) {
            Block.popResource(level, worldPosition, stack);
        }
    }

    private int tryInject(DockedController target) {
        ItemStack input = getInputStack();
        if (!QualityHarvestControllerBlockEntity.isLifeMatter(input)) {
            return 0;
        }

        // Re-read live NBT: the cached scan can be up to SCAN_INTERVAL ticks old and the
        // controller's stored count may have changed via harvest consumption or another injector.
        StructureTemplate.StructureBlockInfo current = target.entity().getContraption().getBlocks().get(target.key());
        if (current == null) {
            cachedScan = DockingScan.none();
            return 0;
        }
        CompoundTag currentTag = current.nbt() == null ? new CompoundTag() : current.nbt();
        int stored = QualityHarvestControllerBlockEntity.getLifeMatterStored(currentTag);
        int capacity = QualityHarvestControllerBlockEntity.CAPACITY;
        int transferred = Math.min(Math.min(INJECT_AMOUNT, input.getCount()), capacity - stored);
        if (transferred <= 0) {
            return 0;
        }

        ItemStack prototype = input.copyWithCount(1);
        inventory.extractItem(INPUT_SLOT, transferred, false);
        CompoundTag updatedTag = QualityHarvestControllerBlockEntity.setLifeMatterStored(
                currentTag, stored + transferred, prototype, level.registryAccess());
        StructureTemplate.StructureBlockInfo updatedInfo =
                new StructureTemplate.StructureBlockInfo(target.info().pos(), target.info().state(), updatedTag);
        target.entity().getContraption().getBlocks().put(target.key(), updatedInfo);
        lastTargetStored = stored + transferred;
        return transferred;
    }

    private DockingScan findDockingTarget() {
        Direction facing = getBlockState().getValue(LifeMatterInjectorBlock.FACING);
        Vec3 injectorCenter = Vec3.atCenterOf(worldPosition);
        AABB searchBox = new AABB(worldPosition).expandTowards(
                facing.getStepX() * 4.0D,
                facing.getStepY() * 4.0D,
                facing.getStepZ() * 4.0D).inflate(2.5D);

        List<AbstractContraptionEntity> entities = level.getEntitiesOfClass(
                AbstractContraptionEntity.class,
                searchBox,
                entity -> entity.isAlive() && entity.getContraption() != null);

        DockedController best = null;
        double bestDistance = Double.MAX_VALUE;
        int bestStored = -1;
        for (AbstractContraptionEntity entity : entities) {
            for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry
                    : entity.getContraption().getBlocks().entrySet()) {
                StructureTemplate.StructureBlockInfo info = entry.getValue();
                if (!isController(info.state())) {
                    continue;
                }

                Vec3 controllerCenter = entity.toGlobalVector(Vec3.atCenterOf(entry.getKey()), 1.0F);
                double distance = dockingDistance(injectorCenter, controllerCenter, facing);
                if (distance < 0 || distance >= bestDistance) {
                    continue;
                }

                CompoundTag tag = info.nbt() == null ? new CompoundTag() : info.nbt();
                int stored = QualityHarvestControllerBlockEntity.getLifeMatterStored(tag);
                best = stored < QualityHarvestControllerBlockEntity.CAPACITY
                        ? new DockedController(entity, entry.getKey(), info)
                        : null;
                bestDistance = distance;
                bestStored = stored;
            }
        }
        if (bestStored < 0) {
            return DockingScan.none();
        }
        return new DockingScan(best, true, best != null, bestStored);
    }

    private boolean isController(BlockState state) {
        return state.is(ModBlocks.QUALITY_HARVEST_CONTROLLER.get())
                || state.is(ModTags.Blocks.QUALITY_HARVEST_CONTROLLERS);
    }

    private double dockingDistance(Vec3 injectorCenter, Vec3 targetCenter, Direction facing) {
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 offset = targetCenter.subtract(injectorCenter);
        double along = offset.dot(normal);
        if (along < 0.25D || along > 3.25D) {
            return -1.0D;
        }

        Vec3 perpendicular = offset.subtract(normal.scale(along));
        if (perpendicular.lengthSqr() > 1.25D * 1.25D) {
            return -1.0D;
        }
        return along;
    }

    private boolean applyScanStatus(DockingScan scan) {
        boolean changed = controllerInRange != scan.controllerInRange()
                || acceptingControllerInRange != scan.acceptingControllerInRange()
                || lastTargetStored != scan.stored();
        controllerInRange = scan.controllerInRange();
        acceptingControllerInRange = scan.acceptingControllerInRange();
        lastTargetStored = scan.stored();
        return changed;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inventory.deserializeNBT(registries, tag.getCompound(INVENTORY_TAG));
        lastTransferred = tag.getInt("LastTransferred");
        transferCooldown = tag.getInt("TransferCooldown");
        controllerInRange = tag.getBoolean("ControllerInRange");
        acceptingControllerInRange = tag.getBoolean("AcceptingControllerInRange");
        lastTargetStored = tag.contains("LastTargetStored") ? tag.getInt("LastTargetStored") : -1;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
        tag.putInt("LastTransferred", lastTransferred);
        tag.putInt("TransferCooldown", transferCooldown);
        tag.putBoolean("ControllerInRange", controllerInRange);
        tag.putBoolean("AcceptingControllerInRange", acceptingControllerInRange);
        tag.putInt("LastTargetStored", lastTargetStored);
    }

    @Override
    public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
    }

    private record DockedController(AbstractContraptionEntity entity, BlockPos key,
                                    StructureTemplate.StructureBlockInfo info) {
    }

    private record DockingScan(@Nullable DockedController controller, boolean controllerInRange,
                               boolean acceptingControllerInRange, int stored) {
        private static DockingScan none() {
            return new DockingScan(null, false, false, -1);
        }
    }
}
