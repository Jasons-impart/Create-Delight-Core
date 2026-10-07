package io.github.jasonsimpart.createdelightcore.compat.farmersrespite;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class TeaHarvestGameTests {
    private static final BlockPos PLANT = new BlockPos(3, 2, 3);
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void allStagesBothHalvesAndBothHandsHarvestRawLeaves(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        var pos = helper.absolutePos(PLANT);
        for (int age = 0; age <= 3; age++) {
            for (InteractionHand hand : InteractionHand.values()) {
                for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
                    clearDrops(helper);
                    place(helper, age);
                    player.setItemInHand(hand, new ItemStack(Items.SHEARS));
                    var hitPos = half == DoubleBlockHalf.LOWER ? pos : pos.above();
                    var hit = new BlockHitResult(Vec3.atCenterOf(hitPos), Direction.UP, hitPos, false);
                    level.getBlockState(hitPos).use(level, player, hand, hit);
                    int leaves = 0;
                    int sticks = 0;
                    for (var item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))) {
                        var id = ForgeRegistries.ITEMS.getKey(item.getItem().getItem());
                        if (ResourceLocation.tryParse("youkaishomecoming:tea_leaves").equals(id)) {
                            leaves += item.getItem().getCount();
                        } else if (item.getItem().is(Items.STICK)) {
                            sticks += item.getItem().getCount();
                        } else {
                            helper.fail("Unexpected harvest item: " + id);
                        }
                    }
                    helper.assertTrue(leaves >= 2 && leaves <= (age == 2 ? 4 : 3),
                            "Keep the original leaf counts; age=" + age + ", hand=" + hand
                                    + ", half=" + half + ", leaves=" + leaves);
                    helper.assertTrue(sticks >= 2 && sticks <= 3, "The original stick output must remain intact");
                    helper.assertTrue(level.getBlockState(pos).is(block("farmersrespite:small_tea_bush")),
                            "The original harvest must still reset to its own seedling");
                    helper.assertTrue(level.getBlockState(pos.above()).isAir(),
                            "The original harvest must remove its upper half");
                    clearDrops(helper);
                    level.getBlockState(hitPos).use(level, player, hand, hit);
                    helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).isEmpty(),
                            "Repeating the interaction must not harvest a second time");
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void plantedLegacyTreesAreNotMigrated(GameTestHelper helper) {
        place(helper, 1);
        var state = helper.getBlockState(PLANT);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getBlockState(PLANT).getBlock() == state.getBlock(),
                    "An existing tree must retain its original block type");
            helper.assertTrue(helper.getBlockState(PLANT.above()).is(state.getBlock()),
                    "An existing tree must retain its upper half");
            helper.succeed();
        });
    }

    private static void place(GameTestHelper helper, int age) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(PLANT);
        BlockState state = block("farmersrespite:tea_bush").defaultBlockState();
        var ageProperty = (IntegerProperty) state.getProperties().stream()
                .filter(property -> property.getName().equals("age")).findFirst().orElseThrow();
        var stunted = (net.minecraft.world.level.block.state.properties.BooleanProperty) state.getProperties().stream()
                .filter(property -> property.getName().equals("stunted")).findFirst().orElseThrow();
        state = state.setValue(ageProperty, age).setValue(stunted, false);
        level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), FLAGS);
        level.setBlock(pos, state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), FLAGS);
        level.setBlock(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), FLAGS);
    }

    private static void clearDrops(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(PLANT)).inflate(2))
                .forEach(ItemEntity::discard);
    }

    private static Block block(String name) {
        var id = ResourceLocation.tryParse(name);
        if (!ForgeRegistries.BLOCKS.containsKey(id)) {
            throw new IllegalStateException("Tea GameTest runtime is missing " + id);
        }
        return ForgeRegistries.BLOCKS.getValue(id);
    }
}
