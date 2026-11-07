// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.content.cooler;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.jasonsimpart.createdelightcore.util.GoggleTooltipStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock.HeatLevel;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class SnowmanCoolerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	private final SmartFluidTank tank = new SmartFluidTank(SnowmanCoolerFuelManager.TANK_CAPACITY, this::fluidChanged) {
		@Override
		public boolean isFluidValid(FluidStack stack) {
			return level != null && level.isClientSide
                    ? io.github.jasonsimpart.createdelightcore.network.ClientFuelCache.COOLER_MAP.containsKey(stack.getFluid())
                    : SnowmanCoolerFuelManager.find(stack) != null;
		}
	};
	private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
	private long clientTotalTicks;
	private long clientSyncTime;

	public static final int MAX_HEAT_CAPACITY = 10000;
	public static final int INSERTION_THRESHOLD = 500;

	public LerpedFloat headAnimation;
	public boolean stockKeeper;
	public boolean isCreative;
	public boolean goggles;
	public boolean hat;

	protected FuelType activeFuel;
	protected int remainingBurnTime;
	protected LerpedFloat headAngle;

	public SnowmanCoolerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		headAnimation = LerpedFloat.linear();
		headAngle = LerpedFloat.angular();
		isCreative = false;
		goggles = false;
		setLazyTickRate(20);

		headAngle.startWithValue((AngleHelper.horizontalAngle(state.getOptionalValue(SnowmanCoolerBlock.FACING)
			.orElse(Direction.SOUTH)) + 180) % 360);
	}

	public FuelType getActiveFuel() {
		return activeFuel;
	}

	public int getRemainingBurnTime() {
		return remainingBurnTime;
	}

	public boolean isCreative() {
		return isCreative;
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide) {
			if (shouldTickAnimation())
				tickAnimation();
			if (!isVirtual())
				spawnParticles(getHeatLevelFromBlock(), 1);
			return;
		}

		if (isCreative)
			return;

		if (remainingBurnTime > 0)
			remainingBurnTime--;

		if (remainingBurnTime <= 0) {
			activeFuel = FuelType.NONE;
			SnowmanCoolerFuelManager.Fuel fuel = SnowmanCoolerFuelManager.find(tank.getFluid());
			if (fuel != null && tank.getFluidAmount() >= fuel.amountConsumedPerTick()) {
				tank.drain(fuel.amountConsumedPerTick(), IFluidHandler.FluidAction.EXECUTE);
				activeFuel = fuel.freezing() ? FuelType.SPECIAL : FuelType.NORMAL;
				remainingBurnTime = fuel.burnTime();
			}
		}

		updateBlockState();
		if (remainingBurnTime > 0) setChanged();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		stockKeeper = getStockTicker(level, worldPosition) != null;
		if (!level.isClientSide) sendData();
	}

	@Nullable
	public static StockTickerBlockEntity getStockTicker(LevelAccessor level, BlockPos pos) {
		for (Direction direction : Iterate.horizontalDirections) {
			if (level instanceof Level l && !l.isLoaded(pos))
				return null;
			BlockState blockState = level.getBlockState(pos.relative(direction));
			if (!AllBlocks.STOCK_TICKER.has(blockState))
				continue;
			if (level.getBlockEntity(pos.relative(direction)) instanceof StockTickerBlockEntity stbe)
				return stbe;
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	private boolean shouldTickAnimation() {
		// Offload the animation tick to the visual when flywheel in enabled
		return !VisualizationManager.supportsVisualization(level);
	}

	@OnlyIn(Dist.CLIENT)
    protected void tickAnimation() {
		boolean active = getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && isValidBlockAbove();

		if (!active) {
			float target = 0;
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && !player.isInvisible()) {
				double x;
				double z;
				if (isVirtual()) {
					x = -4;
					z = -10;
				} else {
					x = player.getX();
					z = player.getZ();
				}
				double dx = x - (getBlockPos().getX() + 0.5);
				double dz = z - (getBlockPos().getZ() + 0.5);
				target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
			}
			target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
			headAngle.chase(target, .25f, LerpedFloat.Chaser.exp(5));
			headAngle.tickChaser();
		} else {
			headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(SnowmanCoolerBlock.FACING)
				.orElse(Direction.SOUTH)) + 180) % 360, .125f, LerpedFloat.Chaser.EXP);
			headAngle.tickChaser();
		}

		headAnimation.chase(active ? 1 : 0, .25f, LerpedFloat.Chaser.exp(.25f));
		headAnimation.tickChaser();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Coolant", tank.writeToNBT(new CompoundTag()));
		if (clientPacket) compound.putLong("TotalCoolTicks", totalCoolTicks());
		if (!isCreative) {
			compound.putInt("fuelLevel", activeFuel.ordinal());
			compound.putInt("burnTimeRemaining", remainingBurnTime);
		} else
			compound.putBoolean("isCreative", true);
		if (goggles)
			compound.putBoolean("Goggles", true);
		if (hat)
			compound.putBoolean("TrainHat", true);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		activeFuel = FuelType.values()[Mth.clamp(compound.getInt("fuelLevel"), 0, FuelType.values().length - 1)];
		remainingBurnTime = Math.max(0, compound.getInt("burnTimeRemaining"));
		tank.readFromNBT(compound.getCompound("Coolant"));
		if (clientPacket) {
			clientTotalTicks = compound.getLong("TotalCoolTicks");
			clientSyncTime = level == null ? 0 : level.getGameTime();
		}
		isCreative = compound.getBoolean("isCreative");
		goggles = compound.contains("Goggles");
		hat = compound.contains("TrainHat");
		super.read(compound, clientPacket);
	}

	public SnowmanCoolerBlock.HeatLevel getHeatLevelFromBlock() {
		return SnowmanCoolerBlock.getHeatLevelOf(getBlockState());
	}

	public HeatLevel getHeatLevelForRender() {
		HeatLevel heatLevel = getHeatLevelFromBlock();
		if (!heatLevel.isAtLeast(HeatLevel.FADING) && stockKeeper)
			return HeatLevel.FADING;
		return heatLevel;
	}

	public void updateBlockState() {
		setBlockHeat(getHeatLevel());
	}

	protected void setBlockHeat(HeatLevel heat) {
		HeatLevel inBlockState = getHeatLevelFromBlock();
		if (inBlockState == heat)
			return;
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SnowmanCoolerBlock.HEAT_LEVEL, heat));
		if (level.getBlockEntity(worldPosition.above()) instanceof com.simibubi.create.content.processing.basin.BasinBlockEntity basin)
			basin.notifyChangeOfContents();
		notifyUpdate();
	}

	/**
	 * @return true if the heater updated its burn time and an item should be
	 *         consumed
	 */
	protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
		if (isCreative)
			return false;

		FuelType newFuel = FuelType.NONE;
		int newBurnTime = 0;

		if (itemStack.is(SnowmanCoolerFuelManager.SPECIAL_ITEMS)) {
			newBurnTime = 3200;
			newFuel = FuelType.SPECIAL;
		} else if (itemStack.is(SnowmanCoolerFuelManager.REGULAR_ITEMS)) {
			newBurnTime = 1600;
			newFuel = FuelType.NORMAL;
		}

		if (newFuel == FuelType.NONE)
			return false;
		if (newFuel.ordinal() < activeFuel.ordinal())
			return false;

		if (newFuel == activeFuel) {
			if (remainingBurnTime <= INSERTION_THRESHOLD) {
				newBurnTime += remainingBurnTime;
			} else if (forceOverflow && newFuel == FuelType.NORMAL) {
				if (remainingBurnTime < MAX_HEAT_CAPACITY) {
					newBurnTime = Math.min(remainingBurnTime + newBurnTime, MAX_HEAT_CAPACITY);
				} else return false;
			} else {
				return false;
			}
		}

		if (simulate)
			return true;

		activeFuel = newFuel;
		remainingBurnTime = newBurnTime;

		if (level.isClientSide) {
			spawnParticleBurst(activeFuel == FuelType.SPECIAL);
			return true;
		}

		HeatLevel prev = getHeatLevelFromBlock();
		playSound();
		updateBlockState();
		notifyUpdate();

		if (prev != getHeatLevelFromBlock())
			level.playSound(null, worldPosition, SoundEvents.SNOW_FALL, SoundSource.BLOCKS,
				.125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);

		return true;
	}

	protected void applyCreativeFuel() {
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		isCreative = true;

		HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();

		if (level.isClientSide) {
			spawnParticleBurst(next.isAtLeast(HeatLevel.FREEZING));
			return;
		}

		playSound();
		if (next == HeatLevel.FADING)
			next = next.nextActiveLevel();
		setBlockHeat(next);
	}

	public boolean isCreativeFuel(ItemStack stack) {
		return AllItems.CREATIVE_BLAZE_CAKE.isIn(stack);
	}

	public boolean isValidBlockAbove() {
		if (isVirtual())
			return false;
		BlockState blockState = level.getBlockState(worldPosition.above());
		return BasinBlock.isBasin(level, worldPosition.above()) || blockState.getBlock() instanceof FluidTankBlock;
	}

	protected void playSound() {
		level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS,
			.125f + level.random.nextFloat() * .125f, .75f - level.random.nextFloat() * .25f);
	}

	protected HeatLevel getHeatLevel() {
		HeatLevel level = HeatLevel.IDLE;
		switch (activeFuel) {
		case SPECIAL:
			level = HeatLevel.FREEZING;
			break;
		case NORMAL:
			level = remainingBurnTime > 0 ? HeatLevel.COOLING : HeatLevel.IDLE;
			break;
		default:
		case NONE:
			break;
		}
		return level;
	}

	protected void spawnParticles(HeatLevel heatLevel, double burstMult) {
		if (level == null)
			return;

		RandomSource r = level.getRandom();

		Vec3 c = VecHelper.getCenterOf(worldPosition);
		Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
			.multiply(1, 0, 1));
		
		if (r.nextInt(4) != 0)
			return;

		boolean empty = level.getBlockState(worldPosition.above())
			.getCollisionShape(level, worldPosition.above())
			.isEmpty();

		double yMotion = empty ? .0625f : r.nextDouble() * .0125f;

		if (empty || r.nextInt(8) == 0)
			level.addParticle(new DustParticleOptions(new Vector3f(0.94f,0.99f,0.99f),2.5f), v.x, v.y, v.z, 0, yMotion*8, 0);

		Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
			.multiply(1, .25f, 1)
			.normalize()
			.scale((empty ? .25f : .5) + r.nextDouble() * .125f))
			.add(0, .5, 0);

		if (heatLevel.isAtLeast(HeatLevel.FREEZING)) {
			level.addParticle(ParticleTypes.ITEM_SNOWBALL, v2.x, v2.y, v2.z, 0, yMotion, 0);
		} else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
			level.addParticle(ParticleTypes.SNOWFLAKE, v2.x, v2.y, v2.z, 0, yMotion, 0);
		}
		return;
	}

	public void spawnParticleBurst(boolean frozing) {
		Vec3 c = VecHelper.getCenterOf(worldPosition);
		RandomSource r = level.random;
		for (int i = 0; i < (frozing ? 50 : 20); i++) {
			Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
				.multiply(1, .25f, 1)
				.normalize();
			Vec3 v = c.add(offset.scale(.5 + r.nextDouble() * .125f))
				.add(0, .125, 0);
			Vec3 m = offset.scale(1 / 32f);

			level.addParticle(frozing ? ParticleTypes.ITEM_SNOWBALL : ParticleTypes.SNOWFLAKE, v.x, v.y, v.z, m.x, m.y,
				m.z);
		}
	}

	public enum FuelType {
		NONE, NORMAL, SPECIAL
	}

	public SmartFluidTank getFluidTank() {
		return tank;
	}

	private void fluidChanged(FluidStack fluid) {
		setChanged();
	}

	private long totalCoolTicks() {
		SnowmanCoolerFuelManager.Fuel fuel = SnowmanCoolerFuelManager.find(tank.getFluid());
		return Math.max(remainingBurnTime, 0) + (fuel == null ? 0L
				: (long) (tank.getFluidAmount() / fuel.amountConsumedPerTick()) * fuel.burnTime());
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
		HeatLevel heatLevel = getHeatLevelFromBlock();
		ChatFormatting stateColor = switch (heatLevel) {
			case IDLE -> ChatFormatting.GRAY;
			case FADING -> ChatFormatting.DARK_AQUA;
			case COOLING -> ChatFormatting.AQUA;
			case FREEZING -> ChatFormatting.BLUE;
		};
		GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.coolerState")
				.withStyle(ChatFormatting.AQUA)
				.append(Component.translatable("createdelightcore.cooler." + heatLevel.getSerializedName())
						.withStyle(stateColor)));
		if (!tank.isEmpty()) {
			GoggleTooltipStyle.addFluidLine(tooltip, tank.getFluid(), tank.getCapacity());
		}
		if (isCreative) {
			GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.cooler.creative"));
		} else {
			long ticks = level != null && level.isClientSide
					? Math.max(0, clientTotalTicks - Math.max(0, level.getGameTime() - clientSyncTime)) : totalCoolTicks();
			GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.cooler.remaining", (ticks + 19) / 20));
		}
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
		return capability == ForgeCapabilities.FLUID_HANDLER ? fluidCapability.cast() : super.getCapability(capability, side);
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
