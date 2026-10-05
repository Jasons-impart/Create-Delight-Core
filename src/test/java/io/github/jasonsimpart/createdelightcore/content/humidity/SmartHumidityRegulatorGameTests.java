package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import com.teamtea.eclipticseasons.api.constant.biome.Humidity;
import com.teamtea.eclipticseasons.common.block.blockentity.HumidityControlBlockEntity;
import com.teamtea.eclipticseasons.common.registry.BlockRegistry;
import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import com.teamtea.eclipticseasons.common.core.SolarHolders;
import com.teamtea.eclipticseasons.common.core.crop.HumidityControlProvider;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class SmartHumidityRegulatorGameTests {
    private static final BlockPos MACHINE = new BlockPos(3, 3, 3);
    private static final BlockPos CROP = new BlockPos(3, 1, 3);










    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void uniformTargetRequiresWaterEvenAtNaturalEquality(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            smart.targetHumidity.setValue(Humidity.getHumid(EclipticUtil.getHumidityLevelAt(helper.getLevel(), helper.absolutePos(CROP))).ordinal());
            smart.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP)) == null,
                    "No water must mean no smart effect even at natural equality");
            inlet(smart).fill(new FluidStack(Fluids.WATER, 10), IFluidHandler.FluidAction.EXECUTE);
            smart.tick();
            helper.assertTrue(smart.getOperatingMode() == 2 && inlet(smart).getFluidInTank(0).getAmount() == 5,
                    "Natural equality must still consume water and enable both effects");
            Float target = SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP));
            helper.assertTrue(target != null && target.equals(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(new BlockPos(1, 3, 1)))),
                    "All greenhouse samples must have the same absolute target");
            var future = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                for (int i = 0; i < 100; i++) {
                    if (!target.equals(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP)))) return false;
                }
                return true;
            });
            helper.assertTrue(future.join(), "Network-thread reads must retain the same immutable target without deleting owners");
        });
        helper.runAfterDelay(8, () -> {
            helper.assertTrue(machine(helper).getOperatingMode() == 0 && SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP)) == null,
                    "After the final paid tick, an empty tank must stop both effects and the override");
            helper.succeed();
        });
    }





    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void otherHumidityFacilitiesKeepWorking(GameTestHelper helper) {
        greenhouse(helper);
        BlockPos sprinklerPos = MACHINE.west(), dryerPos = MACHINE.east(), nativePos = new BlockPos(2, 1, 2);
        helper.setBlock(sprinklerPos, CDBlocks.SPRINKLER.get());
        helper.setBlock(dryerPos, CDBlocks.DRYER.get());
        helper.setBlock(nativePos, BlockRegistry.humidity_tank.get());
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            smart.targetHumidity.setValue(4);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            inlet(helper.getBlockEntity(sprinklerPos)).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            ((HumidityMachineBlockEntity) helper.getBlockEntity(dryerPos)).setSpeed(16);
        });
        helper.runAfterDelay(8, () -> {
            var manager = SolarHolders.getSaveData(helper.getLevel());
            helper.assertTrue(inlet(helper.getBlockEntity(sprinklerPos)).getFluidInTank(0).getAmount() < 1000,
                    "Ordinary sprinklers must keep consuming water");
            helper.assertTrue(manager.queryHumidityControlProvider(helper.absolutePos(dryerPos)) != null
                            && manager.queryHumidityControlProvider(helper.absolutePos(nativePos)) != null,
                    "CDC and native humidity facilities must keep publishing their providers");
            helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), helper.absolutePos(CROP)) == Humidity.HUMID,
                    "Other working facilities must not alter the final smart target");
            machine(helper).setSpeed(0);
            machine(helper).tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP)) == null,
                    "Without smart power the native modifier path must resume");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void nativeCogPreviewAndTransmissionMatchCompressor(GameTestHelper helper) {
        greenhouse(helper);
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        var state = helper.getBlockState(MACHINE);
        helper.assertTrue(!state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING),
                "The machine must not expose a facing property that Create mistakes for the gear axis");
        var small = new com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem.IntegratedSmallCogHelper();
        var large = new com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem.IntegratedLargeCogHelper();
        for (var placement : new net.createmod.catnip.placement.IPlacementHelper[]{small, large}) {
            helper.assertTrue(placement.getStatePredicate().test(state), "The original integrated cog helper must recognize the machine");
            for (Direction face : Direction.values()) {
                var pos = helper.absolutePos(MACHINE);
                var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.5)), face, pos, false);
                var offset = placement.getOffset(player, helper.getLevel(), state, pos, hit);
                helper.assertTrue(offset.isSuccessful() == face.getAxis().isHorizontal(),
                        "Native small/large cog previews must exclude top and bottom and preserve all four sides: " + face);
            }
        }
        for (Direction side : Direction.values()) helper.setBlock(MACHINE.relative(side), com.simibubi.create.AllBlocks.COGWHEEL.getDefaultState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,
                        side.getAxis().isVertical() ? Direction.Axis.X : Direction.Axis.Y));
        helper.runAfterDelay(2, () -> {
            try {
                var method = com.simibubi.create.content.kinetics.RotationPropagator.class.getDeclaredMethod("getRotationSpeedModifier",
                        com.simibubi.create.content.kinetics.base.KineticBlockEntity.class, com.simibubi.create.content.kinetics.base.KineticBlockEntity.class);
                method.setAccessible(true);
                var smart = machine(helper);
                for (Direction side : Direction.values()) {
                    var cog = (com.simibubi.create.content.kinetics.base.KineticBlockEntity) helper.getBlockEntity(MACHINE.relative(side));
                    float forward = (float) method.invoke(null, smart, cog), reverse = (float) method.invoke(null, cog, smart);
                    helper.assertTrue(side.getAxis().isVertical() ? forward == 0 && reverse == 0 : forward != 0 && reverse != 0,
                            "Only horizontal cog connections may transfer rotation, in both directions: " + side);
                }
            } catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void detectorWarningReadsControlledGroundCrop(GameTestHelper helper) {
        greenhouse(helper);
        helper.setBlock(CROP.below(), vectorwing.farmersdelight.common.registry.ModBlocks.RICH_SOIL_FARMLAND.get());
        helper.setBlock(CROP, Blocks.DEAD_BUSH);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            var grow = new com.teamtea.eclipticseasons.api.data.crop.GrowParameter(1, 0, 1, java.util.Optional.empty());
            var noGrow = new com.teamtea.eclipticseasons.api.data.crop.GrowParameter(0, 0, 0, java.util.Optional.empty());
            var humidity = new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<Humidity, com.teamtea.eclipticseasons.api.data.crop.GrowParameter>(Humidity.class);
            for (var value : Humidity.values()) humidity.put(value, value.ordinal() >= 3 ? grow : noGrow);
            // Keep seasonal chance low so the resolver actually enters its warning branch.
            var base = new com.teamtea.eclipticseasons.api.data.crop.CropGrow(java.util.Optional.of(noGrow), java.util.Optional.of(noGrow),
                    new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<>(com.teamtea.eclipticseasons.api.constant.solar.SolarTerm.class),
                    new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<>(com.teamtea.eclipticseasons.api.constant.solar.Season.class), humidity);
            var control = new com.teamtea.eclipticseasons.api.data.crop.CropGrowControl(base, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty());
            java.util.Map registry;
            try {
                var field = com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.class.getDeclaredField("CROP_GROW_MAP");
                field.setAccessible(true);
                registry = (java.util.Map) field.get(null);
            } catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
            var controls = new java.util.HashMap<>();
            controls.put(com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.getDefaultAgroClimaticZoneHolder(helper.getLevel()), control);
            Object previous = registry.put(Blocks.DEAD_BUSH, controls);
            try {
                var pos = helper.absolutePos(CROP);
                for (int target : new int[]{3, 4}) {
                    smart.targetHumidity.setValue(target);
                    smart.tick();
                    var info = com.teamtea.eclipticseasons.common.item.info.GrowthInfoResolver.resolve(helper.getLevel(), pos, helper.getBlockState(CROP));
                    helper.assertTrue(info != null && !info.humidityMismatch(),
                            "Native detector warning must accept target " + target + ": " + info);
                    helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), pos).ordinal() == target,
                            "Detector API must read target at the ground crop position");
                }
                helper.succeed();
            } finally {
                if (previous == null) registry.remove(Blocks.DEAD_BUSH); else registry.put(Blocks.DEAD_BUSH, previous);
            }
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void cropAndDetectorUseExactTargetDespiteClampAndWrongBase(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.targetHumidity.setValue(4);
            smart.setSpeed(16);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            smart.tick();
            var grow = new com.teamtea.eclipticseasons.api.data.crop.GrowParameter(1, 0, 1, java.util.Optional.empty());
            var noGrow = new com.teamtea.eclipticseasons.api.data.crop.GrowParameter(0, 0, 0, java.util.Optional.empty());
            var humidity = new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<Humidity, com.teamtea.eclipticseasons.api.data.crop.GrowParameter>(Humidity.class);
            for (var value : Humidity.values()) humidity.put(value, value == Humidity.HUMID ? grow : noGrow);
            var base = new com.teamtea.eclipticseasons.api.data.crop.CropGrow(java.util.Optional.of(grow), java.util.Optional.of(noGrow),
                    new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<>(com.teamtea.eclipticseasons.api.constant.solar.SolarTerm.class),
                    new com.teamtea.eclipticseasons.api.util.fast.Enum2ObjectMap<>(com.teamtea.eclipticseasons.api.constant.solar.Season.class), humidity);
            var control = new com.teamtea.eclipticseasons.api.data.crop.CropGrowControl(base, java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty());
            var pos = helper.absolutePos(CROP);
            var state = helper.getLevel().getBlockState(pos);
            var event = new net.minecraftforge.event.level.BlockEvent.CropGrowEvent.Pre(helper.getLevel(), pos, state);
            // Deliberately use the wrong base: an absolute target must override it.
            float natural = 0;
            float expectedChance = 1;
            com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.checkHumidity(event, helper.getLevel(), control, natural,
                    com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.RoomStatus.UNKNOWN, pos, state,
                    com.teamtea.eclipticseasons.api.constant.solar.Season.SPRING, false, 0, 0);
            helper.assertTrue(event.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY,
                    "Absolute HUMID must select the pure HUMID growth parameter, despite native clamping");
            try {
                var detector = com.teamtea.eclipticseasons.common.item.GrowthDetectorItem.class.getDeclaredMethod(
                        "getHumidityGrowChance", net.minecraft.world.level.Level.class,
                        com.teamtea.eclipticseasons.api.data.crop.CropGrowControl.class, float.class,
                        com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.RoomStatus.class,
                        BlockPos.class, net.minecraft.world.level.block.state.BlockState.class,
                        com.teamtea.eclipticseasons.api.constant.solar.Season.class, boolean.class);
                detector.setAccessible(true);
                float chance = (float) detector.invoke(null, helper.getLevel(), control, natural,
                        com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.RoomStatus.GREEN_HOUSE,
                        pos, state, com.teamtea.eclipticseasons.api.constant.solar.Season.SPRING, false);
                helper.assertTrue(chance == expectedChance, "Native growth detector chance=" + chance + " parameter="
                        + control.getGrowParameter(4F, state) + " target="
                        + SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), pos));
            } catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
            smart.setSpeed(0);
            smart.tick();
            var denied = new net.minecraftforge.event.level.BlockEvent.CropGrowEvent.Pre(helper.getLevel(), pos, state);
            com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.checkHumidity(denied, helper.getLevel(), control, 0,
                    com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.RoomStatus.NORMAL, pos, state,
                    com.teamtea.eclipticseasons.api.constant.solar.Season.SPRING, true, 0, 1);
            helper.assertTrue(denied.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY,
                    "Without control the same humidity requirement must reject the mismatched base");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 240)
    public static void wallMeterRetainsNativeRefreshTiming(GameTestHelper helper) {
        greenhouse(helper);
        BlockPos cog = MACHINE.east();
        helper.setBlock(cog, com.simibubi.create.AllBlocks.COGWHEEL.getDefaultState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.Y));
        helper.setBlock(cog.above(), com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.DOWN));
        BlockPos meter = new BlockPos(1, 1, 1);
        helper.setBlock(meter, net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("eclipticseasons", "hygrometer")).defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(meter),
                            helper.getBlockState(meter).getBlock()),
                    "Ordinary placement must not start a refresh loop absent in the native mod");
            var smart = machine(helper);
            smart.targetHumidity.setValue(4);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        });
        helper.runAfterDelay(3, () -> {
            var state = helper.getBlockState(meter);
            state.getBlock().tick(state, helper.getLevel(), helper.absolutePos(meter), helper.getLevel().random);
        });
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(helper.getBlockState(meter).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) == 15,
                    "A native scheduled-tick update must read the controlled target");
            machine(helper).targetHumidity.setValue(3);
        });
        helper.runAfterDelay(55, () -> {
            int power = helper.getBlockState(meter).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER);
            helper.assertTrue(power == 15, "Do not replace the native 200-tick interval with a one-second refresh");
        });
        helper.runAfterDelay(150, () -> inlet(machine(helper)).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE));
        helper.runAfterDelay(215, () -> {
            int power = helper.getBlockState(meter).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER);
            helper.assertTrue(com.teamtea.eclipticseasons.common.block.HygrometerBlock.getHumidityLevelFromPower(power) == 3,
                    "Native interval: power=" + power + " target=" + SmartHumidityRegulatorSeasonCompat.target(
                            helper.getLevel(), helper.absolutePos(meter.south())) + " speed=" + machine(helper).getSpeed()
                            + " water=" + inlet(machine(helper)).getFluidInTank(0).getAmount()
                            + " pending=" + helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(meter), helper.getBlockState(meter).getBlock()));
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 80)
    public static void suspendedRegulatorControlsLimeOnRichSoil(GameTestHelper helper) {
        for (int x = 0; x <= 6; x++) for (int y = 0; y <= 8; y++) for (int z = 0; z <= 6; z++)
            helper.setBlock(new BlockPos(x, y, z), x == 0 || x == 6 || y == 0 || y == 8 || z == 0 || z == 6 ? Blocks.GLASS : Blocks.AIR);
        var registry = net.minecraftforge.registries.ForgeRegistries.BLOCKS;
        var soil = registry.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("farmersdelight", "rich_soil_farmland"));
        var lime = registry.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("collectorsreap", "lime_bush"));
        helper.assertTrue(soil != null && lime != null && lime != Blocks.AIR, "Real rich soil and lime dependencies must be installed");
        helper.setBlock(CROP.below(), soil);
        helper.setBlock(CROP, lime);
        BlockPos suspended = new BlockPos(3, 4, 3);
        helper.setBlock(suspended, CDBlocks.SMART_HUMIDITY_REGULATOR.get());
        helper.runAfterDelay(2, () -> {
            var smart = (SmartHumidityRegulatorBlockEntity) helper.getBlockEntity(suspended);
            smart.targetHumidity.setValue(4);
            smart.setSpeed(16);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        });
        helper.runAfterDelay(25, () -> {
            var smart = (SmartHumidityRegulatorBlockEntity) helper.getBlockEntity(suspended);
            for (BlockPos sample : new BlockPos[]{CROP.below(), CROP, CROP.above(), new BlockPos(1, 1, 1), suspended.below()}) {
                BlockPos pos = helper.absolutePos(sample);
                Float target = SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), pos);
                helper.assertTrue(target != null && Humidity.getHumid(target).ordinal() == 4,
                        "Suspended regulator must reach ground lime: sample=" + sample + " target=" + target + " mode=" + smart.getOperatingMode());
                helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), pos) == Humidity.HUMID,
                        "The native adjusted-humidity API must report HUMID at ground and canopy");
                float raw = EclipticUtil.getHumidityLevelAt(helper.getLevel(), pos);
                float modifier = SolarHolders.getSaveData(helper.getLevel()).calculateHumidityModification(pos);
                helper.assertTrue(Humidity.getHumid(raw + modifier) == Humidity.HUMID,
                        "Native player/crop modifier queries must include the fractional-height floor");
            }
            BlockPos soilPos = helper.absolutePos(CROP.below());
            var wetSoil = helper.getLevel().getBlockState(soilPos).setValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE, 3);
            helper.setBlock(CROP.below(), wetSoil);
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), soilPos) != null,
                    "Farmland moisture updates must preserve controlled floor readings");
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), soilPos.below()) == null,
                    "Floor sampling must not spread through the soil into the ground below");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void hygrometersReadAllFiveTargetsAcrossRoom(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            // Both wall meters sample air more than two blocks from the regulator.
            BlockPos[] meters = {new BlockPos(1, 1, 1), new BlockPos(5, 1, 5)};
            Direction[] faces = {Direction.SOUTH, Direction.NORTH};
            for (int i = 0; i < meters.length; i++)
                helper.setBlock(meters[i], net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("eclipticseasons", "hygrometer")).defaultBlockState()
                        .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, faces[i]));
            for (int target = 0; target < 5; target++) {
                smart.targetHumidity.setValue(target);
                smart.tick();
                for (int i = 0; i < meters.length; i++) {
                    BlockPos pos = helper.absolutePos(meters[i]);
                    var state = helper.getLevel().getBlockState(pos);
                    state.getBlock().randomTick(state, helper.getLevel(), pos, helper.getLevel().random);
                    var reading = helper.getLevel().getBlockState(pos);
                    int power = reading.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER);
                    helper.assertTrue(com.teamtea.eclipticseasons.common.block.HygrometerBlock.getHumidityLevelFromPower(power) == target,
                            "A distant wall meter must read target " + target + ", including HUMID");
                    BlockPos sample = pos.relative(faces[i]);
                    float actual = EclipticUtil.getHumidityAfterCheck(helper.getLevel(), sample,
                            EclipticUtil.getHumidityLevelAt(helper.getLevel(), sample));
                    helper.assertTrue(Humidity.getHumid(actual).ordinal() == target,
                            "Server humidity target=" + target + " actual=" + actual + " override="
                                    + SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), sample)
                                    + " water=" + inlet(smart).getFluidInTank(0).getAmount());
                }
            }
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void roomReadingPacketPreservesDistantAndHumidSamples(GameTestHelper helper) {
        var values = java.util.Map.of(new BlockPos(1, 2, 1).asLong(), 4.5F,
                new BlockPos(30, 2, 30).asLong(), 3.5F);
        var packet = new io.github.jasonsimpart.createdelightcore.network.SyncHumidityRoomsPacket(
                helper.getLevel().dimension().location(), values);
        var buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            packet.encode(buf);
            var decoded = io.github.jasonsimpart.createdelightcore.network.SyncHumidityRoomsPacket.decode(buf);
            helper.assertTrue(decoded.dimension().equals(packet.dimension()) && decoded.values().equals(values),
                    "Room readings must retain exact positions and highest humidity over the network");
            helper.succeed();
        } finally { buf.release(); }
    }

    private static void greenhouse(GameTestHelper helper) {
        for (int x = 0; x <= 6; x++) for (int y = 0; y <= 4; y++) for (int z = 0; z <= 6; z++)
            helper.setBlock(new BlockPos(x, y, z), x == 0 || x == 6 || y == 0 || y == 4 || z == 0 || z == 6
                    ? Blocks.GLASS : Blocks.AIR);
        helper.setBlock(CROP.below(), Blocks.FARMLAND);
        helper.setBlock(CROP, Blocks.WHEAT);
        helper.setBlock(MACHINE, CDBlocks.SMART_HUMIDITY_REGULATOR.get());
    }

    private static SmartHumidityRegulatorBlockEntity machine(GameTestHelper helper) {
        return (SmartHumidityRegulatorBlockEntity) helper.getBlockEntity(MACHINE);
    }

    private static IFluidHandler inlet(BlockEntity machine) {
        return machine.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElseThrow(
                () -> new IllegalStateException("Missing top fluid capability"));
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void waterPortsAndSingleRow(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var machine = machine(helper);
            var tank = inlet(machine);
            helper.assertTrue(tank.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE) == 0,
                    "The inlet must reject lava");
            helper.assertTrue(tank.fill(new FluidStack(Fluids.WATER, 1500), IFluidHandler.FluidAction.EXECUTE) == 1000,
                    "Water capacity must be exactly 1000 mB");
            helper.assertTrue(tank.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Pipes must not extract water");
            for (Direction face : Direction.values()) if (face != Direction.UP)
                helper.assertTrue(!machine.getCapability(ForgeCapabilities.FLUID_HANDLER, face).isPresent(),
                        "Only the top may expose a fluid port");
            var player = helper.makeMockPlayer();
            var hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(MACHINE)), Direction.NORTH,
                    helper.absolutePos(MACHINE), false);
            var board = machine.targetHumidity.createBoard(player, hit);
            helper.assertTrue(board.rows().size() == 1 && board.maxValue() == 120 && board.milestoneInterval() == 30,
                    "The GUI must contain one row with all five humidity levels");
            machine.targetHumidity.setValue(4);
            var saved = machine.saveWithFullMetadata();
            var restored = new SmartHumidityRegulatorBlockEntity(machine.getType(), machine.getBlockPos(), machine.getBlockState());
            restored.setLevel(helper.getLevel());
            restored.load(saved);
            helper.assertTrue(inlet(restored).getFluidInTank(0).getAmount() == 1000 && restored.getTargetHumidity() == 4,
                    "Tank and target must survive saving and loading");
            helper.assertTrue(machine.calculateStressApplied() == 16, "Stress impact must be 16");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 100)
    public static void humidifiesAndStopsWithPower(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var machine = machine(helper);
            SolarHolders.getSaveData(helper.getLevel()).addHumidityControlProvider(helper.absolutePos(CROP),
                    new HumidityControlProvider(-8, 10, 100, false));
            inlet(machine).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            machine.targetHumidity.setValue(4);
            machine.setSpeed(16);
        });
        helper.runAfterDelay(8, () -> {
            var machine = machine(helper);
            helper.assertTrue(machine.getOperatingMode() == 2,
                    "A working regulator always runs both effects");
            helper.assertTrue(inlet(machine).getFluidInTank(0).getAmount() < 1000, "Humidification must consume water");
            helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), helper.absolutePos(CROP)).ordinal() == 4,
                    "The real adjusted humidity API must report HUMID");
            helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), helper.absolutePos(MACHINE)).ordinal() == 4,
                    "Reading humidity at the controller must also report the regulated level");
            float cropHumidity = EclipticUtil.getHumidityLevelAt(helper.getLevel(), helper.absolutePos(CROP))
                    + SolarHolders.getSaveData(helper.getLevel()).calculateHumidityModification(helper.absolutePos(CROP));
            helper.assertTrue(cropHumidity >= 4, "Crop humidity must also reach HUMID");
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(new BlockPos(8, 1, 3))) == null,
                    "Humidity must not pass through the greenhouse wall");
            machine.targetHumidity.setValue(3);
        });
        for (int target = 3; target >= 0; target--) {
            final int expected = target;
            helper.runAfterDelay(12 + (3 - target) * 4, () -> {
                helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), helper.absolutePos(CROP)).ordinal() == expected,
                        "Every selectable humidity level must reach the real Ecliptic Seasons API");
                if (expected > 0) machine(helper).targetHumidity.setValue(expected - 1);
                else machine(helper).setSpeed(0);
            });
        }
        helper.runAfterDelay(26, () -> {
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), helper.absolutePos(CROP)) == null,
                    "Removing power must clear regulation");
            helper.succeed();
        });
    }






    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void fourLowerHotspotsAndCompressorCog(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var player = helper.makeMockPlayer();
            BlockPos pos = helper.absolutePos(MACHINE);
            {
                var state = CDBlocks.SMART_HUMIDITY_REGULATOR.get().defaultBlockState();
                helper.setBlock(MACHINE, state);
                var smart = machine(helper);
                var block = (SmartHumidityRegulatorBlock) state.getBlock();
                var transform = (com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided)
                    smart.targetHumidity.getSlotPositioning();
                helper.assertTrue(block.getRotationAxis(state) == Direction.Axis.Y,
                    "The compressor-style side cog must rotate about Y for every block orientation");
                for (Direction face : Direction.values()) {
                    Vec3 center = new Vec3(.5, face.getAxis().isHorizontal() ? 3 / 16D : .5, .5)
                        .add(Vec3.atLowerCornerOf(face.getNormal()).scale(.5));
                    transform.fromSide(face);
                    helper.assertTrue(transform.testHit(helper.getLevel(), pos, state, center) == face.getAxis().isHorizontal(),
                        "All four vertical faces must expose a lower setting hotspot, but top/bottom must not");
                    Vec3 faceCenter = new Vec3(.5, .5, .5).add(Vec3.atLowerCornerOf(face.getNormal()).scale(.5));
                    helper.assertTrue(!transform.testHit(helper.getLevel(), pos, state, faceCenter),
                        "Ordinary face-center clicks must not open the lower setting hotspot");
                    Vec3 corner = switch (face.getAxis()) {
                        case X -> new Vec3(center.x, .9, .9);
                        case Y -> new Vec3(.9, center.y, .9);
                        case Z -> new Vec3(.9, .9, center.z);
                    };
                    helper.assertTrue(!transform.testHit(helper.getLevel(), pos, state, corner),
                        "Face corners must not trigger settings");
                    helper.assertTrue(!block.hasShaftTowards(helper.getLevel(), pos, state, face),
                        "Like the compressor, gear meshing must not expose any shaft port");
                }
                for (int humidity = 0; humidity < 5; humidity++) {
                    smart.targetHumidity.setValueSettings(player,
                        new com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings(0, humidity * 30), false);
                    helper.assertTrue(smart.getTargetHumidity() == humidity
                        && smart.targetHumidity.getValueSettings().value() == humidity * 30,
                        "The widened GUI must map its five milestones to exactly the five humidity levels");
                }
                smart.targetHumidity.setValueSettings(player,
                    new com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings(0, 44), false);
                helper.assertTrue(smart.getTargetHumidity() == 1, "Intermediate bar positions must select the nearest level");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 80)
    public static void compressorCogReceivesPowerFromSide(GameTestHelper helper) {
        greenhouse(helper);
        BlockPos cogPos = MACHINE.east();
        helper.setBlock(cogPos, com.simibubi.create.AllBlocks.COGWHEEL.getDefaultState()
            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.Y));
        helper.setBlock(cogPos.above(), com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.DOWN));
        helper.runAfterDelay(25, () -> {
            var smart = machine(helper);
            var cog = (com.simibubi.create.content.kinetics.base.KineticBlockEntity) helper.getBlockEntity(cogPos);
            helper.assertTrue(cog.getSpeed() != 0 && smart.getSpeed() == -cog.getSpeed(),
                "A neighboring vertical-axis cog must drive the regulator from the side with reversed rotation");
            helper.succeed();
        });
    }


    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void fixedCubeIncludesCornersAndIgnoresRoomBoundaries(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            smart.targetHumidity.setValue(3);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            smart.tick();
            BlockPos center = smart.getBlockPos();
            for (int x = -4; x <= 4; x++) for (int y = -4; y <= 4; y++) for (int z = -4; z <= 4; z++) {
                Float target = SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), center.offset(x, y, z));
                helper.assertTrue(target != null && target == 3.5F, "Every one of the 729 cube coordinates must be covered");
            }
            for (Direction side : Direction.values())
                helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), center.relative(side, 5)) == null,
                        "The fifth block outside each face must not be covered");
            for (int x = 0; x <= 6; x++) for (int z = 0; z <= 6; z++) helper.setBlock(new BlockPos(x, 4, z), Blocks.AIR);
            smart.tick();
            helper.assertTrue(smart.getOperatingMode() == 2 && SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), center.below(4)) == 3.5F,
                    "Removing the roof must not change fixed-range operation");
            smart.onChunkUnloaded();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), center) == null,
                    "Unloaded devices must remove their provider");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void overlapUsesHighestWhileEveryDeviceConsumesWater(GameTestHelper helper) {
        greenhouse(helper);
        BlockPos second = MACHINE.east(2);
        helper.setBlock(second, CDBlocks.SMART_HUMIDITY_REGULATOR.get());
        helper.runAfterDelay(2, () -> {
            var low = machine(helper);
            var high = (SmartHumidityRegulatorBlockEntity) helper.getBlockEntity(second);
            low.targetHumidity.setValue(1); high.targetHumidity.setValue(4);
            for (var device : new SmartHumidityRegulatorBlockEntity[]{low, high}) {
                device.setSpeed(16);
                inlet(device).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                int before = inlet(device).getFluidInTank(0).getAmount();
                device.tick();
                helper.assertTrue(device.getOperatingMode() == 2 && inlet(device).getFluidInTank(0).getAmount() == before - 5,
                        "Both overlapping devices must independently consume water and run both effects");
            }
            BlockPos overlap = low.getBlockPos();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap) == 4.5F,
                    "Overlap must choose the highest target");
            helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), overlap) == Humidity.HUMID,
                    "The real humidity API must also select the highest overlapping target");
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap.west(4)) == 1.5F,
                    "A position covered only by the lower target must retain that target");
            low.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap) == 4.5F,
                    "Tick order must not replace the maximum with the lower target");
            high.setSpeed(0); high.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap) == 1.5F,
                    "An unpowered higher target must fall back to the working lower target");
            helper.assertTrue(EclipticSeasonsApi.getInstance().getAdjustedHumidity(helper.getLevel(), overlap) == Humidity.DRY,
                    "The real humidity API must immediately fall back when the higher device stops");
            high.setSpeed(16); high.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap) == 4.5F,
                    "Restoring power must restore the higher target");
            helper.setBlock(second, Blocks.AIR);
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), overlap) == 1.5F,
                    "Removing the higher device must immediately restore the lower target");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void providerReadingsAreSafeOnDetectorThread(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16); smart.targetHumidity.setValue(4);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 5), IFluidHandler.FluidAction.EXECUTE);
            smart.tick();
            BlockPos pos = helper.absolutePos(CROP);
            Float target = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                    SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), pos)).join();
            helper.assertTrue(target != null && target == 4.5F, "Detector threads must read the paid final water tick safely");
            smart.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), pos) == null,
                    "Exhausted water must stop the provider on the next tick");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 100)
    public static void absoluteTargetWorksOutsideGreenhouseAndMaintainsWithPower(GameTestHelper helper) {
        greenhouse(helper);
        BlockPos cog = MACHINE.east();
        helper.setBlock(cog, com.simibubi.create.AllBlocks.COGWHEEL.getDefaultState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.Y));
        helper.setBlock(cog.above(), com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.DOWN));
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16); smart.targetHumidity.setValue(4);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            smart.tick();
            BlockPos exposed = smart.getBlockPos().east(4).below(2);
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), exposed) == 4.5F,
                    "The active provider's fixed range must include the exposed boundary coordinate");
            boolean nativeRoom = com.teamtea.eclipticseasons.common.core.crop.CropGrowthHandler.isInRoom(
                    helper.getLevel(), exposed, helper.getLevel().getBlockState(exposed), java.util.Optional.empty());
            helper.assertTrue(!nativeRoom, "The exterior fixture must fail the original greenhouse predicate");
            float natural = EclipticUtil.getHumidityLevelAt(helper.getLevel(), exposed);
            float expected = 4.5F;
            helper.assertTrue(EclipticUtil.getHumidityAfterCheck(helper.getLevel(), exposed, natural) == expected,
                    "The absolute target must apply even where native greenhouse detection rejects the position");
        });
        helper.runAfterDelay(65, () -> {
            var smart = machine(helper);
            helper.assertTrue(smart.getOperatingMode() == 2
                            && SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), smart.getBlockPos()) == 4.5F,
                    "Continuous powered operation must maintain the absolute target: speed=" + smart.getSpeed()
                            + " water=" + inlet(smart).getFluidInTank(0).getAmount());
            smart.setSpeed(0); smart.tick();
            helper.assertTrue(SmartHumidityRegulatorSeasonCompat.target(helper.getLevel(), smart.getBlockPos()) == null,
                    "Stopping power must immediately remove the absolute target");
            helper.succeed();
        });
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 60)
    public static void absoluteReadingsIgnoreNaturalBaseAndOtherModifiers(GameTestHelper helper) {
        greenhouse(helper);
        helper.runAfterDelay(2, () -> {
            var smart = machine(helper);
            smart.setSpeed(16);
            inlet(smart).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            var level = helper.getLevel();
            var manager = SolarHolders.getSaveData(level);
            BlockPos pos = helper.absolutePos(CROP);
            float natural = EclipticUtil.getHumidityLevelAt(level, pos);
            for (int adjustment : new int[]{100, -100}) {
                manager.addHumidityControlProvider(pos, new HumidityControlProvider(adjustment, 10, 100, false));
                for (int selected = 0; selected < 5; selected++) {
                    smart.targetHumidity.setValue(selected); smart.tick();
                    float expected = selected + .5F;
                    for (float input : new float[]{natural, natural + 1, -100, 100})
                        helper.assertTrue(EclipticUtil.getHumidityAfterCheck(level, pos, input) == expected,
                                "Absolute readings must ignore rain-adjusted, clamped or mismatched bases and other facilities");
                    helper.assertTrue(Math.abs(natural + manager.calculateHumidityModification(pos) - expected) < .0001F,
                            "Legacy modifier queries must replace, rather than stack with, other facility effects");
                }
            }
            smart.setSpeed(0); smart.tick();
            helper.assertTrue(manager.calculateHumidityModification(pos) == -100,
                    "Stopping the smart device must restore the other facility's unchanged modifier");
            helper.succeed();
        });
    }

    private static class NativeControlProbe extends HumidityControlBlockEntity {
        int originalTickCalls;
        NativeControlProbe(BlockPos pos) { super(pos, Blocks.AIR.defaultBlockState()); }
        @Override public int getHumidityModifiedLevel() { originalTickCalls++; return 1; }
    }

}
