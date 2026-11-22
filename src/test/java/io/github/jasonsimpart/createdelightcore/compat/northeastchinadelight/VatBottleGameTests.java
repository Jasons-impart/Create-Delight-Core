package io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.ModBlocks;
import com.gunmu.northeast_china_delight.block.Vat;
import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatRecipes;
import com.gunmu.northeast_china_delight.fluid.ModFluids;
import com.simibubi.create.AllBlocks;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class VatBottleGameTests {
    @GameTest(batch = "seasoning_bottle", template = "humidity_regulator_empty")
    public static void seasoningBottleTakesOnlyTheAmountItCanHold(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(pos, ModBlocks.VAT.get().defaultBlockState().setValue(Vat.FERMENTED, true));
        var vat = (VatBlockEntity) level.getBlockEntity(pos);
        var soySauce = ModFluids.soySauceSource();
        vat.setKind(VatRecipes.Kind.SOY_SAUCE);
        vat.setProductMb(soySauce, 430);

        var player = helper.makeMockPlayer();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var mainHand = InteractionHand.MAIN_HAND;
        var offHand = InteractionHand.OFF_HAND;
        var emptyBottle = new ItemStack(CDItems.SEASONING_BOTTLE.get());
        player.setItemInHand(mainHand, emptyBottle);
        level.getBlockState(pos).use(level, player, mainHand, hit);
        assertContents(helper, player.getItemInHand(mainHand), soySauce, 250);
        helper.assertTrue(vat.productMb(soySauce) == 180, "The empty bottle should take exactly 250 mB");

        level.getBlockState(pos).use(level, player, mainHand, hit);
        helper.assertTrue(vat.productMb(soySauce) == 180, "A full bottle should leave the vat unchanged");

        var vinegar = ModFluids.whiteVinegarSource();
        player.setItemInHand(mainHand, bottleWith(vinegar, 50));
        level.getBlockState(pos).use(level, player, mainHand, hit);
        assertContents(helper, player.getItemInHand(mainHand), vinegar, 50);
        helper.assertTrue(vat.productMb(soySauce) == 180, "An incompatible fluid should leave the vat unchanged");

        player.setItemInHand(offHand, bottleWith(soySauce, 100));
        level.getBlockState(pos).use(level, player, offHand, hit);
        assertContents(helper, player.getItemInHand(offHand), soySauce, 250);
        helper.assertTrue(vat.productMb(soySauce) == 30, "Refilling should take only the 150 mB of free space");

        player.setItemInHand(mainHand, new ItemStack(CDItems.SEASONING_BOTTLE.get()));
        level.getBlockState(pos).use(level, player, mainHand, hit);
        assertContents(helper, player.getItemInHand(mainHand), soySauce, 30);
        helper.assertTrue(!level.getBlockState(pos).getValue(Vat.FERMENTED), "An emptied vat should reset");
        helper.succeed();
    }

    @GameTest(batch = "seasoning_bottle", template = "humidity_regulator_empty")
    public static void directionAppliesOnlyToVatAndSupportsRepeatedDeposits(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(pos, ModBlocks.VAT.get().defaultBlockState());
        var vat = (VatBlockEntity) level.getBlockEntity(pos);
        var sourWater = ModFluids.sourWaterSource();
        var player = helper.makeMockPlayer();
        var hand = InteractionHand.MAIN_HAND;
        var bottle = bottleWith(sourWater, 250);
        var liquidHandler = FluidUtil.getFluidHandler(bottle).orElseThrow(IllegalStateException::new);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        player.setItemInHand(hand, bottle);

        level.getBlockState(pos).use(level, player, hand, hit);
        assertContents(helper, bottle, sourWater, 250);
        helper.assertTrue(vat.sourWaterMb() == 0, "Draw mode must not fall through to a valid deposit");

        player.setShiftKeyDown(true);
        player.setPos(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
        player.setXRot(90);
        bottle.use(level, player, hand);
        helper.assertTrue(!SeasoningBottleItem.isVatInput(bottle), "Using while looking at a block must not toggle the mode");

        player.setPos(pos.getX() + 0.5, pos.getY() + 8, pos.getZ() + 0.5);
        player.setXRot(-90);
        bottle.use(level, player, hand);
        player.setShiftKeyDown(false);
        helper.assertTrue(SeasoningBottleItem.isVatInput(bottle), "Sneak-use in air must switch to deposit mode");

        level.getBlockState(pos).use(level, player, hand, hit);
        helper.assertTrue(vat.sourWaterMb() == 250 && SeasoningBottleItem.contents(bottle).isEmpty(),
                "A full bottle must deposit exactly 250 mB");
        helper.assertTrue(SeasoningBottleItem.isVatInput(bottle), "The mode must survive emptying the bottle");

        liquidHandler.fill(new FluidStack(sourWater, 75), FluidAction.EXECUTE);
        level.getBlockState(pos).use(level, player, hand, hit);
        assertContents(helper, bottle, sourWater, 75);
        helper.assertTrue(vat.sourWaterMb() == 250, "A partial bottle must not supply the native 250 mB sour-water dose");
        liquidHandler.fill(new FluidStack(sourWater, 175), FluidAction.EXECUTE);
        level.getBlockState(pos).use(level, player, hand, hit);
        helper.assertTrue(vat.sourWaterMb() == 500 && SeasoningBottleItem.contents(bottle).isEmpty(),
                "A second bottle must add to the vat's existing sour water");

        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(Vat.FERMENTED, true));
        level.getBlockState(pos).use(level, player, hand, hit);
        helper.assertTrue(vat.sourWaterMb() == 500 && SeasoningBottleItem.contents(bottle).isEmpty(),
                "Deposit mode must not fall through to drawing the finished liquid");

        var tankPos = pos.east(2);
        level.setBlockAndUpdate(tankPos, AllBlocks.FLUID_TANK.get().defaultBlockState());
        var tank = FluidUtil.getFluidHandler(level, tankPos, Direction.UP).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(sourWater, 400), FluidAction.EXECUTE);
        var tankHit = new BlockHitResult(Vec3.atCenterOf(tankPos), Direction.UP, tankPos, false);
        bottle.useOn(new UseOnContext(player, hand, tankHit));
        assertContents(helper, player.getItemInHand(hand), sourWater, 250);
        helper.assertTrue(SeasoningBottleItem.isVatInput(player.getItemInHand(hand)),
                "A Forge tank must be able to fill a bottle in vat deposit mode");
        helper.succeed();
    }

    private static ItemStack bottleWith(Fluid fluid, int amount) {
        var bottle = new ItemStack(CDItems.SEASONING_BOTTLE.get());
        FluidUtil.getFluidHandler(bottle).orElseThrow(IllegalStateException::new)
                .fill(new FluidStack(fluid, amount), FluidAction.EXECUTE);
        return bottle;
    }

    private static void assertContents(GameTestHelper helper, ItemStack bottle, Fluid fluid, int amount) {
        var contents = SeasoningBottleItem.contents(bottle);
        helper.assertTrue(contents.getFluid() == fluid && contents.getAmount() == amount,
                "Expected " + amount + " mB, got " + contents.getAmount() + " mB of " + contents.getFluid());
    }
}
