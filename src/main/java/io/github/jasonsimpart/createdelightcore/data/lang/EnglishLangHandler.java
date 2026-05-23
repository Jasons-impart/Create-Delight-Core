package io.github.jasonsimpart.createdelightcore.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTabs;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class EnglishLangHandler {
    private static void coin(RegistrateLangProvider provider, Supplier<Item> item, String name, String initial) {
        var id = item.get().getDescriptionId();
        // provider.add(id, name);
        provider.add(id + ".initial", initial);
        provider.add(id + ".plural", name);
    }
    private static void addVirtualFluid(RegistrateLangProvider provider, FluidEntry<?> fluid, String name) {
        var namespace = fluid.getId().getNamespace();
        var id = fluid.getId().getPath();
        if (id.startsWith("flowing_"))
            id = id.substring("flowing_".length());
        provider.add("fluid." + namespace + "." + id, name);
    }


    public static void init(RegistrateLangProvider provider) {
        provider.add(CDCreativeTabs.MISC.get(), "Create Delight | Misc");
        provider.add(CDCreativeTabs.COIN.get(), "Create Delight | Coin");
        provider.add(CDCreativeTabs.FLUID.get(), "Create Delight | Fluid");
        provider.add(CDCreativeTabs.FOOD.get(), "Create Delight | Food");

        coin(provider, CDItems.IRON, "Iron Coin", "Iron");
        coin(provider, CDItems.COPPER, "Copper Coin", "Copper");
        coin(provider, CDItems.GOLD, "Gold Coin", "Gold");
        coin(provider, CDItems.EMERALD, "Emerald Coin", "Emerald");
        coin(provider, CDItems.NETHERITE, "Netherite Coin", "Netherite");

        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal", "%1$s became a part of the molten metal");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal.player", "%2$s thought that %1$s was a piece of unmelted metal");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream", "%1$s was frozen into a popsicle because of eating too much ice cream");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream.player", "%1$s was frozen into a popsicle because of eating too much ice cream");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation", "%1$s rotted away from radiation exposure");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation.player", "%1$s rotted away from radiation exposure");


        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing.fan", "Fan behind Powdered Snow");
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing", "Bulk Freezing");
        //milkshake
        addVirtualFluid(provider, CDFluids.APPLE, "Apple Milkshake");
        addVirtualFluid(provider, CDFluids.GLOW_BERRY, "Glow Berry Milkshake");
        addVirtualFluid(provider, CDFluids.CARROT, "Carrot Milkshake");
        addVirtualFluid(provider, CDFluids.BEETROOT, "Beetroot Milkshake");
        addVirtualFluid(provider, CDFluids.ENCHANTED_FRUIT, "Enchanted fruit Milkshake");
        //grape juice
        addVirtualFluid(provider, CDFluids.RED_GRAPE, "Red Grape Juice");
        addVirtualFluid(provider, CDFluids.JUNGLE_RED_GRAPE, "Jungle Red Grape Juice");
        addVirtualFluid(provider, CDFluids.SAVANNA_RED_GRAPE, "Savanna Red Grape Juice");
        addVirtualFluid(provider, CDFluids.TAIGA_RED_GRAPE, "Taiga Red Grape Juice");
        addVirtualFluid(provider, CDFluids.WHITE_GRAPE, "White Grape Juice");
        addVirtualFluid(provider, CDFluids.JUNGLE_WHITE_GRAPE, "Jungle White Grape Juice");
        addVirtualFluid(provider, CDFluids.SAVANNA_WHITE_GRAPE, "Savanna White Grape Juice");
        addVirtualFluid(provider, CDFluids.TAIGA_WHITE_GRAPE, "Taiga White Grape Juice");
        addVirtualFluid(provider, CDFluids.WARPED_GRAPE, "Warped Grape Juice");
        addVirtualFluid(provider, CDFluids.CRIMSON_GRAPE, "Crimson Grape Juice");
        //jei
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid", "Blaze Burner Fluid Recipe");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsume", "Consumes %smB");
        provider.add("jei." + CreateDelightCore.MODID + ".SnowmanCoolerFluid", "Snowman Cooler Fluid Recipe");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsumeCool", "Consumes %smB");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting", "Phantom Composting");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.dimension", "Transforms in outer planet dimensions");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.light", "Sky light increases the transformation chance");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.fluid", "Nearby Ectoplasm source blocks increase the transformation chance");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.accelerators", "Nearby activator blocks increase the transformation chance");
        //tooltip
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftToSeeHeat", "§r§8Hold [§r§7Shift§r§8] to view Blaze heating information§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftHeat", "§r§8Hold [§r§rShift§r§8] to view Blaze heating information§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".amountConsume", "§8Fuel Consume: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".burnTime", "§8Burn Time: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".heatType", "§8Heating Type: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".superHeat", "Super Heating");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Heat", "Heating");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlToSeeCool", "§r§8Hold [§r§7Ctrl§r§8] to view Snow Golem cooling information§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlCool", "§r§8Hold [§r§rCtrl§r§8] to view Snow Golem cooling information§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolTime", "§8Cooling Time: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolType", "§8Cooling Type: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Frozen", "Frozen");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Cooled", "Cooled");
        //jade
        provider.add("config.jade.plugin_balm.jade", "Jade");
        provider.add("config.jade.plugin_createdelightcore.cmr.snowman_cooler", "Snowman Cooler");
        //waystone
        provider.add("gui." + CreateDelightCore.MODID + ".need", "Need ");
        provider.add("gui." + CreateDelightCore.MODID + ".free", "Free");
        //tooltip
        provider.add("tooltip." + CreateDelightCore.MODID + ".jelly_block", "Sticky, but not connecting to other sticky blocks.");
        provider.add("tooltip." + CreateDelightCore.MODID + ".jello_block", "Slippery. Sticks to sticky blocks and same jello/jelly blocks.");
    }
}
