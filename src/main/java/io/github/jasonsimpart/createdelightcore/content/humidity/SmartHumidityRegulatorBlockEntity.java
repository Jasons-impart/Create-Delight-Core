package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class SmartHumidityRegulatorBlockEntity extends KineticBlockEntity {
    public static final int WATER_PER_TICK = 5;
    public static final int LEVEL_COUNT = 5;
    public static final int GUI_LEVEL_SPACING = 30;
    private final FluidTank tank = new FluidTank(1000, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override protected void onContentsChanged() { notifyUpdate(); }
    };
    private final IFluidHandler waterInlet = new IFluidHandler() {
        @Override public int getTanks() { return tank.getTanks(); }
        @Override public net.minecraftforge.fluids.FluidStack getFluidInTank(int index) { return tank.getFluidInTank(index); }
        @Override public int getTankCapacity(int index) { return tank.getTankCapacity(index); }
        @Override public boolean isFluidValid(int index, net.minecraftforge.fluids.FluidStack fluid) { return tank.isFluidValid(index, fluid); }
        @Override public int fill(net.minecraftforge.fluids.FluidStack fluid, FluidAction action) { return tank.fill(fluid, action); }
        @Override public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.fluids.FluidStack fluid, FluidAction action) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        @Override public net.minecraftforge.fluids.FluidStack drain(int amount, FluidAction action) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
    };
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> waterInlet);
    public ScrollValueBehaviour targetHumidity;
    // 0: stopped; 2: simultaneous water spray and dehumidification effects.
    private int operatingMode;
    public SmartHumidityRegulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static Component humidityName(int value) {
        String[] names = {"arid", "dry", "average", "moist", "humid"};
        return Component.translatable("createdelightcore.humidity.level." + names[Mth.clamp(value, 0, LEVEL_COUNT - 1)]);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        targetHumidity = new ScrollValueBehaviour(Component.translatable("createdelightcore.humidity.regulator.target"),
                this, new HumidityValueBox()) {
            @Override public ValueSettingsBoard createBoard(Player player, BlockHitResult hit) {
                return new ValueSettingsBoard(label, (LEVEL_COUNT - 1) * GUI_LEVEL_SPACING, GUI_LEVEL_SPACING,
                        List.of(Component.translatable("createdelightcore.humidity.regulator.humidity")),
                        new ValueSettingsFormatter(settings -> humidityName(guiHumidity(settings.value())).copy()));
            }

            @Override public ValueSettings getValueSettings() {
                return new ValueSettings(0, getTargetHumidity() * GUI_LEVEL_SPACING);
            }

            @Override public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
                int selected = guiHumidity(settings.value());
                if (getValue() == selected) return;
                setValue(selected);
                playFeedbackSound(this);
            }

            @Override public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
                if (!new HumidityValueBox().fromSide(side).testHit(getWorld(), getPos(), getBlockState(),
                    hit.getLocation().subtract(Vec3.atLowerCornerOf(getPos())))) return;
                if (getWorld().isClientSide)
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SmartHumidityRegulatorClient.open(this, hit, player));
            }
        };
        targetHumidity.between(0, LEVEL_COUNT - 1).withFormatter(value -> humidityName(value).getString());
        targetHumidity.value = 2;
        // Geometry does not change when a player selects a new target.
        targetHumidity.withCallback(value -> operatingMode = 0);
        behaviours.add(targetHumidity);
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = 16;
        return lastStressApplied;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel server) || !ModList.get().isLoaded("eclipticseasons")) return;
        operatingMode = getSpeed() != 0 && !isOverStressed() && hasWater() ? 2 : 0;
        if (operatingMode != 0) tank.drain(WATER_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
        SmartHumidityRegulatorSeasonCompat.update(server, this, operatingMode != 0);
        if (operatingMode != 0) {
            // Match the existing sprinkler and dryer, including their particle cadence.
            if (server.getGameTime() % 40 == 0)
                server.sendParticles(ParticleTypes.FALLING_WATER, worldPosition.getX() + .5,
                    worldPosition.getY() - .1, worldPosition.getZ() + .5, 20, .4, .2, .4, .02);
            if (server.getGameTime() % 60 == 0)
                server.sendParticles(ParticleTypes.POOF, worldPosition.getX() + .5,
                    worldPosition.getY() - .1, worldPosition.getZ() + .5, 3, .4, .2, .4, .02);
        }
    }

    public int getTargetHumidity() { return Mth.clamp(targetHumidity.getValue(), 0, LEVEL_COUNT - 1); }
    public boolean hasWater() { return tank.getFluidAmount() >= WATER_PER_TICK; }
    public int getOperatingMode() { return operatingMode; }

    private void stopRegulating() {
        if (level instanceof ServerLevel server && ModList.get().isLoaded("eclipticseasons"))
            SmartHumidityRegulatorSeasonCompat.remove(server, this);
        operatingMode = 0;
    }

    @Override public void remove() { stopRegulating(); super.remove(); }
    @Override public void onChunkUnloaded() { stopRegulating(); super.onChunkUnloaded(); }

    @Override protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("Water", tank.writeToNBT(new CompoundTag()));
    }

    @Override protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tank.readFromNBT(tag.getCompound("Water"));
        if (targetHumidity != null) targetHumidity.value = Mth.clamp(targetHumidity.value, 0, LEVEL_COUNT - 1);
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && (side == Direction.UP || side == null))
            return fluidCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); fluidCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); fluidCapability = LazyOptional.of(() -> waterInlet); }

    public static int guiHumidity(int value) {
        return Mth.clamp(Math.round(value / (float) GUI_LEVEL_SPACING), 0, LEVEL_COUNT - 1);
    }

    private static class HumidityValueBox extends ValueBoxTransform.Sided {
        @Override protected Vec3 getSouthLocation() { return new Vec3(.5, 3 / 16D, 15.5 / 16D); }
        @Override protected boolean isSideActive(BlockState state, Direction side) { return side.getAxis().isHorizontal(); }

    }
}
