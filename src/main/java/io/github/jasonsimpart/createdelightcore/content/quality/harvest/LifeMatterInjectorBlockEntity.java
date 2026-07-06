package io.github.jasonsimpart.createdelightcore.content.quality.harvest;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.jasonsimpart.createdelightcore.registry.CDBlockEntities;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LifeMatterInjectorBlockEntity extends SmartBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int INJECT_AMOUNT = 16;
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
        }
    };
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inventory);

    private int lastTransferred;

    public LifeMatterInjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide || isPowered()) {
            return;
        }
        lastTransferred = tryInject();
        if (lastTransferred > 0) {
            setChanged();
            sendData();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemCapability.invalidate();
    }

    public ItemStack getInputStack() {
        return inventory.getStackInSlot(INPUT_SLOT);
    }

    public int getLastTransferred() {
        return lastTransferred;
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

    private int tryInject() {
        ItemStack input = getInputStack();
        if (!QualityHarvestControllerBlockEntity.isLifeMatter(input)) {
            return 0;
        }

        DockedController target = findDockedController();
        if (target == null) {
            return 0;
        }

        int stored = QualityHarvestControllerBlockEntity.getLifeMatterStored(target.tag());
        int capacity = QualityHarvestControllerBlockEntity.CAPACITY;
        int transferred = Math.min(Math.min(INJECT_AMOUNT, input.getCount()), capacity - stored);
        if (transferred <= 0) {
            return 0;
        }

        ItemStack prototype = input.copyWithCount(1);
        inventory.extractItem(INPUT_SLOT, transferred, false);
        CompoundTag updatedTag = QualityHarvestControllerBlockEntity.setLifeMatterStored(
                target.tag(), stored + transferred, prototype);
        StructureTemplate.StructureBlockInfo updatedInfo =
                new StructureTemplate.StructureBlockInfo(target.info().pos(), target.info().state(), updatedTag);
        target.entity().getContraption().getBlocks().put(target.key(), updatedInfo);
        return transferred;
    }

    private @Nullable DockedController findDockedController() {
        Direction facing = getBlockState().getValue(LifeMatterInjectorBlock.FACING);
        Vec3 injectorCenter = Vec3.atCenterOf(worldPosition);
        AABB searchBox = new AABB(worldPosition).expandTowards(
                facing.getStepX() * 3.0D,
                facing.getStepY() * 3.0D,
                facing.getStepZ() * 3.0D).inflate(2.0D);

        List<AbstractContraptionEntity> entities = level.getEntitiesOfClass(
                AbstractContraptionEntity.class,
                searchBox,
                entity -> entity.isAlive() && entity.getContraption() != null);

        DockedController best = null;
        double bestDistance = Double.MAX_VALUE;
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
                if (stored >= QualityHarvestControllerBlockEntity.CAPACITY) {
                    continue;
                }

                best = new DockedController(entity, entry.getKey(), info, tag);
                bestDistance = distance;
            }
        }
        return best;
    }

    private boolean isController(BlockState state) {
        return CDBlocks.QUALITY_HARVEST_CONTROLLER.has(state)
                || CDTags.AllBlockTags.QUALITY_HARVEST_CONTROLLERS.matches(state);
    }

    private double dockingDistance(Vec3 injectorCenter, Vec3 targetCenter, Direction facing) {
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 offset = targetCenter.subtract(injectorCenter);
        double along = offset.dot(normal);
        if (along < 0.5D || along > 2.75D) {
            return -1.0D;
        }

        Vec3 perpendicular = offset.subtract(normal.scale(along));
        if (perpendicular.lengthSqr() > 0.85D * 0.85D) {
            return -1.0D;
        }
        return along;
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        lastTransferred = tag.getInt("LastTransferred");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put(INVENTORY_TAG, inventory.serializeNBT());
        tag.putInt("LastTransferred", lastTransferred);
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        super.writeSafe(tag);
        tag.put(INVENTORY_TAG, inventory.serializeNBT());
    }

    private record DockedController(AbstractContraptionEntity entity, BlockPos key,
                                    StructureTemplate.StructureBlockInfo info, CompoundTag tag) {
    }
}
