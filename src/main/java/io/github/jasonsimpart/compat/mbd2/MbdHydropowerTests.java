package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.lowdragmc.mbd2.integration.create.machine.CreateMachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdHydropowerTests {
    @GameTest(template = "mbd_hydro", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void woodenHydropower(GameTestHelper helper) { check(helper, "wooden_fan", 512); }

    @GameTest(template = "mbd_hydro", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void steelHydropower(GameTestHelper helper) { check(helper, "steel_fan", 2048); }

    @GameTest(template = "mbd_hydro", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void forgedHydropower(GameTestHelper helper) { check(helper, "forge_steel_fan", 4096); }

    @GameTest(template = "mbd_hydro", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void dragonHydropower(GameTestHelper helper) { check(helper, "dragon_steel_fan", 8192); }

    @GameTest(template = "mbd_hydro_long", templateNamespace = "createdelightcore", timeoutTicks = 400)
    public static void maximumLengthHydropower(GameTestHelper helper) { check(helper, "wooden_fan", 512); }

    private static void check(GameTestHelper helper, String fanName, int torque) {
        var controllerPos = new BlockPos(6, 2, 0);
        var fanPos = new BlockPos(2, 3, 1);
        var definition = MBDRegistries.MACHINE_DEFINITIONS.get(MbdCompat.id(fanName));
        helper.assertTrue(definition.stateMachine().getRootState() instanceof CreateMachineState,
                "Fan must retain its rotating model state");
        helper.setBlock(fanPos, definition.block());
        // The minimum fixture is only 3 blocks deep, but the 13-repeat search
        // reaches 15 blocks. Load that whole envelope as a nearby player would.
        for (int x = -1; x <= 13; x += 7) {
            for (int z = -1; z <= 16; z += 7) {
                var pos = helper.absolutePos(new BlockPos(x, 2, z));
                helper.getLevel().getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            }
        }
        helper.runAfterDelay(250, () -> {
            var controller = (MBDMultiblockMachine) ((IMachineBlockEntity) helper.getBlockEntity(controllerPos)).getMetaMachine();
            if (!controller.isFormed()) {
                var error = controller.getMultiblockState().error;
                var snapshot = com.lowdragmc.mbd2.api.pattern.MultiblockWorldSavedData.getOrCreate(helper.getLevel()).getSnapshot(controller);
                String capture = snapshot == null ? "no snapshot" : snapshot.capturedSize() + "/" + snapshot.trackedSize() + "; pending=" + snapshot.pendingSize();
                boolean live = controller.checkPatternWithLock();
                helper.assertTrue(false, "Hydro auto-form error=" + (error == null ? "none" : error.getErrorInfo().getString())
                        + "; snapshot=" + capture + "; live=" + live + "; liveError=" + (controller.getMultiblockState().error == null ? "none" : controller.getMultiblockState().error.getErrorInfo().getString()));
            }
            var fan = (MBDKineticMachineBlockEntity) helper.getBlockEntity(fanPos);
            helper.assertTrue(fan.getGeneratedSpeed() == -32, "Hydropower must generate -32 RPM");
            helper.assertTrue(fan.calculateAddedStressCapacity() == torque, "Preserve fan tier capacity");
            helper.assertTrue(Math.abs(fan.getSpeed()) == 32, "Generated rotation must reach the kinetic network");
            helper.setBlock(controllerPos, Blocks.AIR);
            helper.runAfterDelay(5, () -> {
                helper.assertTrue(fan.getGeneratedSpeed() == 0, "Removing hydro controller must stop the fan");
                helper.succeed();
            });
        });
    }
}
