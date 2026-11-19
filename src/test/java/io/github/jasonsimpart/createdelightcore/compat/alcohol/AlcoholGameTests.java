package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(CreateDelightCore.MODID)
@PrefixGameTestTemplate(false)
public class AlcoholGameTests {
    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void fireAleReturnsOneTankardForEachConsumedServing(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        var tankard = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("brewinandchewin:tankard"));
        helper.assertTrue(tankard != null && tankard != Items.AIR, "B&C is present in the regression runtime");
        player.getInventory().clearContent();
        player.getAbilities().instabuild = false;
        var drink = new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("irons_spellbooks:fire_ale")), 2);
        try {
            var remaining = drink.finishUsingItem(helper.getLevel(), player);
            helper.assertTrue(remaining.getCount() == 1 && player.getInventory().countItem(tankard) == 1
                            && player.getInventory().countItem(Items.GLASS_BOTTLE) == 0,
                    "The first serving returns exactly one tankard to inventory and no glass bottle");
            var last = remaining.finishUsingItem(helper.getLevel(), player);
            helper.assertTrue(last.is(tankard) && last.getCount() == 1 && player.getInventory().countItem(tankard) == 1,
                    "The last serving returns its tankard directly, without duplicating the previous one");
        } finally {
            player.getInventory().clearContent();
            player.removeAllEffects();
        }
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void fireAleTooltipsMatchItsNativeEffectsWithoutDoubleApplying(GameTestHelper helper) {
        String source = "irons_spellbooks:fire_ale";
        AlcoholDrinks.registerDrink(source, 2000, 0);
        AlcoholDrinks.registerTooltipEffect(source, "minecraft:nausea", 100, 3);
        AlcoholDrinks.registerTooltipEffect(source, "minecraft:fire_resistance", 900, 0);
        AlcoholDrinks.registerTooltipEffect(source, "minecraft:haste", 900, 2);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.removeAllEffects();
        player.getAbilities().instabuild = false;
        var drink = new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(source)));
        var shown = AlcoholDrinks.tooltipEffects(drink);
        helper.assertTrue(shown.size() == 4 && AlcoholDrinks.effects(drink).size() == 1,
                "Show the three native effects and drunk, but only apply one managed effect");
        var result = drink.finishUsingItem(helper.getLevel(), player);
        var tankard = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("brewinandchewin:tankard"));
        helper.assertTrue(result.is(tankard != null && tankard != Items.AIR ? tankard : Items.GLASS_BOTTLE),
                "Fire Ale returns the B&C tankard when available, otherwise its native glass container");
        for (var effect : shown) {
            var actual = player.getEffect(effect.getEffect());
            helper.assertTrue(actual != null && actual.getDuration() == effect.getDuration()
                            && actual.getAmplifier() == effect.getAmplifier(),
                    "Tooltip must describe the real native or managed effect: " + effect);
        }
        player.removeAllEffects();
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void genericCompletionKeepsInitialStrengthAndStacksByDose(GameTestHelper helper) {
        AlcoholDrinks.registerDrink("minecraft:honey_bottle", 2400, 2);
        var consumer = EntityType.COW.create(helper.getLevel());
        var dose = new ItemStack(Items.HONEY_BOTTLE);
        var result = dose.finishUsingItem(helper.getLevel(), consumer);
        helper.assertTrue(result.is(Items.GLASS_BOTTLE), "Keep the native empty container");
        assertDrunk(helper, consumer.getEffect(effect("drunk")), 2400, 2);
        consumer.removeEffect(effect("drunk"));
        consumer.addEffect(new MobEffectInstance(effect("drunk"), 800));
        new ItemStack(Items.HONEY_BOTTLE).finishUsingItem(helper.getLevel(), consumer);
        assertDrunk(helper, consumer.getEffect(effect("drunk")), 800, 3);
        new ItemStack(Items.HONEY_BOTTLE).finishUsingItem(helper.getLevel(), consumer);
        assertDrunk(helper, consumer.getEffect(effect("drunk")), 800, 4);
        var capped = consumer.getEffect(effect("drunk"));
        new ItemStack(Items.HONEY_BOTTLE).finishUsingItem(helper.getLevel(), consumer);
        helper.assertTrue(capped == consumer.getEffect(effect("drunk")), "Do not replace or refresh at V");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void soberBlocksOnlyTheAdditionalDrunkenness(GameTestHelper helper) {
        AlcoholDrinks.registerDrink("minecraft:golden_apple", 2000, 0);
        var consumer = EntityType.COW.create(helper.getLevel());
        consumer.addEffect(new MobEffectInstance(effect("sober"), 600));
        new ItemStack(Items.GOLDEN_APPLE).finishUsingItem(helper.getLevel(), consumer);
        helper.assertTrue(!consumer.hasEffect(effect("drunk")), "Sober prevents the additional drunk dose");
        helper.assertTrue(consumer.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION), "Keep native positive effects");
        helper.succeed();
    }

    @GameTest(template = "humidity_regulator_empty", timeoutTicks = 40)
    public static void nativeYoukaiDoseIsCapturedBeforeFoodBuilderRemoval(GameTestHelper helper) throws Exception {
        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("youkaishomecoming:scarlet_mist"));
        helper.assertTrue(item != null, "Load real Youkai drink fixture");
        AlcoholDrinks.registerDrink("youkaishomecoming:scarlet_mist", 2000, 0);
        // Simulate startup's removal without making the Core test runtime depend on KubeJS.
        var original = item.getFoodProperties();
        var builder = new FoodProperties.Builder().nutrition(original.getNutrition()).saturationMod(original.getSaturationModifier());
        if (original.canAlwaysEat()) builder.alwaysEat();
        if (original.isFastFood()) builder.fast();
        if (original.isMeat()) builder.meat();
        for (var pair : original.getEffects()) {
            if (pair.getFirst().getEffect() != effect("drunk")) builder.effect(() -> new MobEffectInstance(pair.getFirst()), pair.getSecond());
        }
        var field = java.util.Arrays.stream(Item.class.getDeclaredFields())
                .filter(candidate -> candidate.getType() == FoodProperties.class).findFirst().orElseThrow();
        field.setAccessible(true);
        field.set(item, builder.build());
        var consumer = EntityType.COW.create(helper.getLevel());
        new ItemStack(item).finishUsingItem(helper.getLevel(), consumer);
        assertDrunk(helper, consumer.getEffect(effect("drunk")), 1200, 1);
        helper.assertTrue(consumer.hasEffect(effect("youkaified")), "Keep the drink's other native effect");
        consumer.removeEffect(effect("drunk"));
        consumer.addEffect(new MobEffectInstance(effect("drunk"), 200));
        new ItemStack(item).finishUsingItem(helper.getLevel(), consumer);
        var upgraded = consumer.getEffect(effect("drunk"));
        assertDrunk(helper, upgraded, 200, 2);
        for (int i = 0; i < 200; i++) upgraded.tick(consumer, () -> {});
        helper.assertTrue(upgraded.getDuration() == 0, "No native or hidden lower-level drunkenness may resume");
        helper.succeed();
    }

    private static MobEffect effect(String name) {
        return ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse("youkaishomecoming:" + name));
    }

    private static void assertDrunk(GameTestHelper helper, MobEffectInstance actual, int duration, int amplifier) {
        helper.assertTrue(actual != null && actual.getDuration() == duration && actual.getAmplifier() == amplifier,
                "Expected drunk duration=" + duration + " amp=" + amplifier + ", got " + actual);
    }
}
