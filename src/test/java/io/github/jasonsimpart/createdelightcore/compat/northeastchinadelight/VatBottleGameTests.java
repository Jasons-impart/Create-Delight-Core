package io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight;

import com.gunmu.northeast_china_delight.block.ModBlocks;
import com.gunmu.northeast_china_delight.block.Vat;
import com.gunmu.northeast_china_delight.block.VatBlockEntity;
import com.gunmu.northeast_china_delight.crafting.VatRecipes;
import com.gunmu.northeast_china_delight.fluid.ModFluids;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        helper.assertTrue(player.getItemInHand(mainHand).onItemUseFirst(new UseOnContext(player, mainHand, hit)).consumesAction(),
                "The bottle should take finished fluid before the block handles the click");
        assertContents(helper, player.getItemInHand(mainHand), soySauce, 250);
        helper.assertTrue(vat.productMb(soySauce) == 180, "The empty bottle should take exactly 250 mB");

        helper.assertTrue(player.getItemInHand(mainHand).onItemUseFirst(new UseOnContext(player, mainHand, hit)) == InteractionResult.PASS,
                "A full bottle should leave the click to the block");
        helper.assertTrue(vat.productMb(soySauce) == 180, "A full bottle should leave the vat unchanged");

        var vinegar = ModFluids.whiteVinegarSource();
        player.setItemInHand(mainHand, bottleWith(vinegar, 50));
        helper.assertTrue(player.getItemInHand(mainHand).onItemUseFirst(new UseOnContext(player, mainHand, hit)) == InteractionResult.PASS,
                "An incompatible bottle should leave the click to the block");
        assertContents(helper, player.getItemInHand(mainHand), vinegar, 50);
        helper.assertTrue(vat.productMb(soySauce) == 180, "An incompatible fluid should leave the vat unchanged");

        player.setItemInHand(offHand, bottleWith(soySauce, 100));
        helper.assertTrue(player.getItemInHand(offHand).onItemUseFirst(new UseOnContext(player, offHand, hit)).consumesAction(),
                "The bottle in the off hand should also take finished fluid");
        assertContents(helper, player.getItemInHand(offHand), soySauce, 250);
        helper.assertTrue(vat.productMb(soySauce) == 30, "Refilling should take only the 150 mB of free space");

        player.setItemInHand(mainHand, new ItemStack(CDItems.SEASONING_BOTTLE.get()));
        helper.assertTrue(player.getItemInHand(mainHand).onItemUseFirst(new UseOnContext(player, mainHand, hit)).consumesAction(),
                "An empty bottle should take the final partial amount");
        assertContents(helper, player.getItemInHand(mainHand), soySauce, 30);
        helper.assertTrue(!level.getBlockState(pos).getValue(Vat.FERMENTED), "An emptied vat should reset");
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
