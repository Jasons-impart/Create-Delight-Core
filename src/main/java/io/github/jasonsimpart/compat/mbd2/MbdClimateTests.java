package io.github.jasonsimpart.compat.mbd2;

import com.teamtea.eclipticseasons.common.core.SolarHolders;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class MbdClimateTests {
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void sprinklerAndDryerHumidityLifecycle(GameTestHelper helper) {
        var sprinklerPos = new BlockPos(4, 4, 4);
        var dryerPos = new BlockPos(10, 2, 10);
        helper.setBlock(sprinklerPos.below(2), Blocks.STONE);
        var sprinkler = MbdSingleMachineTests.place(helper, "sprinkler", sprinklerPos);
        var dryer = MbdSingleMachineTests.place(helper, "dryer", dryerPos);
        helper.runAfterDelay(2, () -> {
            var data = SolarHolders.getSaveData(helper.getLevel());
            helper.assertTrue(data != null, "Ecliptic Seasons solar data must be available");
            MbdClimate.update(sprinkler, true);
            var anchor = helper.absolutePos(sprinklerPos.below(2));
            var wet = data.queryHumidityControlProvider(anchor);
            helper.assertTrue(wet != null && wet.getRemainTime() == 500, "Sprinkler anchors humidity to the first solid floor");
            wet.setRemainTime(100);
            MbdClimate.update(sprinkler, true);
            helper.assertTrue(wet.getRemainTime() == 200, "Refresh by 100 ticks when below threshold");
            MbdClimate.update(sprinkler, false);
            helper.assertTrue(data.queryHumidityControlProvider(anchor) == null, "Idle sprinkler clears its field");
            MbdClimate.update(sprinkler, true);
            helper.setBlock(sprinklerPos.below(), Blocks.STONE);
            MbdClimate.update(sprinkler, true);
            helper.assertTrue(data.queryHumidityControlProvider(anchor) == null
                    && data.queryHumidityControlProvider(helper.absolutePos(sprinklerPos.below())) != null,
                    "Changing the floor moves the humidity field and retires the old anchor");
            MbdClimate.update(dryer, true);
            var dry = data.queryHumidityControlProvider(dryer.getPos());
            helper.assertTrue(dry != null, "Dryer creates a field at its own position");
            var conflicting = MbdSingleMachineTests.place(helper, "sprinkler", dryerPos.above(2));
            MbdClimate.update(conflicting, true);
            MbdClimate.update(conflicting, false);
            helper.setBlock(dryerPos.above(2), Blocks.AIR);
            helper.assertTrue(data.queryHumidityControlProvider(dryer.getPos()) == dry,
                    "A conflicting sprinkler must neither adopt nor remove the dryer's provider");
            MbdClimate.update(dryer, false);
            helper.assertTrue(data.queryHumidityControlProvider(dryer.getPos()) != null, "Stopped dryer lets its field expire naturally");
            helper.setBlock(dryerPos, Blocks.AIR);
            helper.assertTrue(data.queryHumidityControlProvider(dryer.getPos()) == null, "Removing dryer clears its field immediately");
            helper.succeed();
        });
    }
}
