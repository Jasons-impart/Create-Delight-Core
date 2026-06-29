package io.github.jasonsimpart.createdelightcore.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators.BulkFermenterFilteringAccess;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BulkFermenterBlockEntity.class, remap = false)
public abstract class BulkFermenterBlockEntityMixin implements BulkFermenterFilteringAccess {
    @Shadow
    int width;

    @Shadow
    int height;

    @Shadow
    public int processingTime;

    @Shadow
    BulkFermentingRecipe currentRecipe;

    @Unique
    private FilteringBehaviour createdelightcore$filtering;

    @Shadow
    public abstract boolean isController();

    @Shadow
    public abstract void updateHeat();

    @Inject(method = "addBehaviours", at = @At("HEAD"))
    private void createdelightcore$addFiltering(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        BulkFermenterBlockEntity self = (BulkFermenterBlockEntity) (Object) this;
        createdelightcore$filtering = new BulkFermenterFilteringBehaviour((SmartBlockEntity) (Object) this, new BulkFermenterValueBox(self), self)
                .withCallback($ -> createdelightcore$onFilterChanged())
                .forRecipes()
                .forFluids();
        behaviours.add(createdelightcore$filtering);
    }

    @Inject(method = "getMatchingRecipes", at = @At("RETURN"), cancellable = true)
    private void createdelightcore$filterMatchingRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        FilteringBehaviour filtering = createdelightcore$getFilter();
        if (filtering == null || filtering.getFilter().isEmpty()) {
            return;
        }

        List<Recipe<?>> recipes = cir.getReturnValue();
        List<Recipe<?>> filtered = new ArrayList<>(recipes.size());
        for (Recipe<?> recipe : recipes) {
            if (createdelightcore$matchesFilter(recipe, filtering)) {
                filtered.add(recipe);
            }
        }
        cir.setReturnValue(filtered);
    }

    @Override
    public FilteringBehaviour createdelightcore$getFilter() {
        return createdelightcore$filtering;
    }

    @Override
    public BulkFermentingRecipe createdelightcore$getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public int createdelightcore$getWidth() {
        return width;
    }

    @Override
    public int createdelightcore$getHeight() {
        return height;
    }

    @Unique
    private void createdelightcore$onFilterChanged() {
        if (!isController()) {
            return;
        }
        currentRecipe = null;
        processingTime = -1;
        updateHeat();
    }

    @Unique
    private boolean createdelightcore$matchesFilter(Recipe<?> recipe, FilteringBehaviour filtering) {
        BulkFermenterBlockEntity self = (BulkFermenterBlockEntity) (Object) this;
        RegistryAccess registryAccess = self.getLevel().registryAccess();
        if (filtering.test(recipe.getResultItem(registryAccess))) {
            return true;
        }
        if (!(recipe instanceof ProcessingRecipe<?> processingRecipe)) {
            return false;
        }
        for (ProcessingOutput output : processingRecipe.getRollableResults()) {
            ItemStack stack = output.getStack();
            if (!stack.isEmpty() && filtering.test(stack)) {
                return true;
            }
        }
        for (FluidStack fluidStack : processingRecipe.getFluidResults()) {
            if (!fluidStack.isEmpty() && filtering.test(fluidStack)) {
                return true;
            }
        }
        return false;
    }

    private static class BulkFermenterFilteringBehaviour extends FilteringBehaviour {
        private final BulkFermenterBlockEntity blockEntity;

        private BulkFermenterFilteringBehaviour(SmartBlockEntity be, Sided slot, BulkFermenterBlockEntity blockEntity) {
            super(be, slot);
            this.blockEntity = blockEntity;
        }

        @Override
        public void read(CompoundTag nbt, boolean clientPacket) {
            if (blockEntity.isController()) {
                super.read(nbt, clientPacket);
            }
        }

        @Override
        public void write(CompoundTag nbt, boolean clientPacket) {
            if (blockEntity.isController()) {
                super.write(nbt, clientPacket);
            }
        }

        @Override
        public void destroy() {
            if (blockEntity.isController()) {
                super.destroy();
            }
        }

        @Override
        public ItemStack getFilter(Direction side) {
            if (!createdelightcore$isFilterSideDisplayed(blockEntity, side)) {
                return ItemStack.EMPTY;
            }
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.getFilter(side) : controller.getFilter();
        }

        @Override
        public ItemStack getFilter() {
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.getFilter() : controller.getFilter();
        }

        @Override
        public boolean setFilter(Direction side, ItemStack stack) {
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.setFilter(side, stack) : controller.setFilter(side, stack);
        }

        @Override
        public boolean setFilter(ItemStack stack) {
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.setFilter(stack) : controller.setFilter(stack);
        }

        @Override
        public boolean test(ItemStack stack) {
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.test(stack) : controller.test(stack);
        }

        @Override
        public boolean test(FluidStack stack) {
            FilteringBehaviour controller = createdelightcore$getControllerFiltering();
            return controller == this ? super.test(stack) : controller.test(stack);
        }

        private FilteringBehaviour createdelightcore$getControllerFiltering() {
            BlockEntity controllerBE = blockEntity.getControllerBE();
            if (controllerBE instanceof BulkFermenterFilteringAccess access) {
                FilteringBehaviour controllerFiltering = access.createdelightcore$getFilter();
                if (controllerFiltering != null) {
                    return controllerFiltering;
                }
            }
            return this;
        }
    }

    private class BulkFermenterValueBox extends Sided {
        private final BulkFermenterBlockEntity blockEntity;

        private BulkFermenterValueBox(BulkFermenterBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        protected Vec3 getSouthLocation() {
            BulkFermenterBlockEntity controllerBE = blockEntity.getControllerBE();
            int width = controllerBE == null ? blockEntity.getWidth() : controllerBE.getWidth();
            double sideCenter = 8.0D;
            if (width % 2 == 0) {
                boolean proxySide = createdelightcore$isFilterSideProxy(blockEntity, getSide());
                sideCenter = switch (getSide()) {
                    case NORTH, EAST -> proxySide ? 16.0D : 0.0D;
                    case SOUTH, WEST -> proxySide ? 0.0D : 16.0D;
                    default -> 8.0D;
                };
            }
            return VecHelper.voxelSpace(sideCenter, 15.5D, 16.05D);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && createdelightcore$isFilterSideInteractable(blockEntity, direction);
        }

        @Override
        public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            Direction previousSide = getSide();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!createdelightcore$isFilterSideInteractable(blockEntity, direction)) {
                    continue;
                }
                fromSide(direction);
                if (super.testHit(level, pos, state, localHit)) {
                    return true;
                }
            }
            fromSide(previousSide);
            return false;
        }

    }

    @Unique
    private static boolean createdelightcore$hasDisplayedFilterSide(BulkFermenterBlockEntity blockEntity) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (createdelightcore$isFilterSideDisplayed(blockEntity, direction)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean createdelightcore$isFilterSideInteractable(BulkFermenterBlockEntity blockEntity, Direction direction) {
        return createdelightcore$isFilterSideDisplayed(blockEntity, direction)
                || createdelightcore$isFilterSideProxy(blockEntity, direction);
    }

    @Unique
    private static boolean createdelightcore$isFilterSideProxy(BulkFermenterBlockEntity blockEntity, Direction direction) {
        if (!direction.getAxis().isHorizontal()) {
            return false;
        }
        BlockPos controller = blockEntity.getController();
        if (controller == null) {
            return false;
        }

        BulkFermenterBlockEntity controllerBE = blockEntity.getControllerBE();
        int width = controllerBE == null ? blockEntity.getWidth() : controllerBE.getWidth();
        if (width % 2 != 0) {
            return false;
        }

        BlockPos local = blockEntity.getBlockPos().subtract(controller);
        if (local.getY() != 0) {
            return false;
        }
        int otherCenter = width / 2;
        return switch (direction) {
            case NORTH -> local.getX() == otherCenter && local.getZ() == 0;
            case SOUTH -> local.getX() == otherCenter && local.getZ() == width - 1;
            case WEST -> local.getX() == 0 && local.getZ() == otherCenter;
            case EAST -> local.getX() == width - 1 && local.getZ() == otherCenter;
            default -> false;
        };
    }

    @Unique
    private static boolean createdelightcore$isFilterSideDisplayed(BulkFermenterBlockEntity blockEntity, Direction direction) {
        if (!direction.getAxis().isHorizontal()) {
            return false;
        }
        BlockPos controller = blockEntity.getController();
        if (controller == null) {
            return false;
        }

        BulkFermenterBlockEntity controllerBE = blockEntity.getControllerBE();
        int width = controllerBE == null ? blockEntity.getWidth() : controllerBE.getWidth();
        BlockPos local = blockEntity.getBlockPos().subtract(controller);
        if (local.getY() != 0) {
            return false;
        }
        int center = (width - 1) / 2;
        return switch (direction) {
            case NORTH -> local.getX() == center && local.getZ() == 0;
            case SOUTH -> local.getX() == center && local.getZ() == width - 1;
            case WEST -> local.getX() == 0 && local.getZ() == center;
            case EAST -> local.getX() == width - 1 && local.getZ() == center;
            default -> false;
        };
    }
}
