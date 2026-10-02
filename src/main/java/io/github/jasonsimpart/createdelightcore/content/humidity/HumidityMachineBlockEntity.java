package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class HumidityMachineBlockEntity extends KineticBlockEntity {
    private final FluidTank tank = new FluidTank(1000, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            notifyUpdate();
        }
    };
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
    private int remainingTicks;
    private BlockPos humidityCenter;
    private Object humidityProvider;

    public HumidityMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private boolean isDryer() {
        return ((HumidityMachineBlock) getBlockState().getBlock()).isDryer();
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = isDryer() ? 1 : 0;
        return lastStressApplied;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel serverLevel) || !ModList.get().isLoaded("eclipticseasons")) {
            return;
        }
        BlockPos center = isDryer() ? worldPosition : findHumidityCenter();
        if (center == null) {
            stopHumidity();
            return;
        }
        boolean active;
        if (isDryer()) {
            active = Math.abs(getSpeed()) > 8;
        } else {
            if (remainingTicks == 0 && tank.getFluidAmount() >= 500) {
                tank.drain(500, IFluidHandler.FluidAction.EXECUTE);
                remainingTicks = 100;
            }
            active = remainingTicks > 0;
            if (active) {
                remainingTicks--;
                setChanged();
            }
        }
        if (!active) {
            stopHumidity();
            return;
        }
        if (!center.equals(humidityCenter)) {
            stopHumidity();
            humidityCenter = center;
        }
        if (humidityProvider == null || serverLevel.getGameTime() % 20 == 0) {
            humidityProvider = HumiditySeasonCompat.refresh(serverLevel, humidityCenter,
                    isDryer() ? -1 : 1, humidityProvider);
        }
        if (serverLevel.getGameTime() % (isDryer() ? 60 : 40) == 0) {
            serverLevel.sendParticles(isDryer() ? ParticleTypes.POOF : ParticleTypes.FALLING_WATER,
                    worldPosition.getX() + 0.5, worldPosition.getY() - 0.1,
                    worldPosition.getZ() + 0.5, isDryer() ? 3 : 20, 0.4, 0.2, 0.4, 0.02);
        }
    }

    @Nullable
    private BlockPos findHumidityCenter() {
        for (int distance = 1; distance <= 10; distance++) {
            BlockPos candidate = worldPosition.below(distance);
            if (candidate.getY() < level.getMinBuildHeight()) {
                break;
            }
            if (!level.getBlockState(candidate).isAir()) {
                return candidate;
            }
        }
        return null;
    }

    private void stopHumidity() {
        if (humidityCenter != null && level instanceof ServerLevel serverLevel
                && ModList.get().isLoaded("eclipticseasons")) {
            HumiditySeasonCompat.remove(serverLevel, humidityCenter, humidityProvider);
        }
        humidityCenter = null;
        humidityProvider = null;
    }

    @Override
    public void remove() {
        stopHumidity();
        super.remove();
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("Water", tank.writeToNBT(new CompoundTag()));
        tag.putInt("RemainingTicks", remainingTicks);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tank.readFromNBT(tag.getCompound("Water"));
        remainingTicks = Math.max(0, Math.min(100, tag.getInt("RemainingTicks")));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isDryer() && capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidCapability = LazyOptional.of(() -> tank);
    }
}
