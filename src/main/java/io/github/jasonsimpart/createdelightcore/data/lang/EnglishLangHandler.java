package io.github.jasonsimpart.createdelightcore.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTabs;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

import java.lang.reflect.Field;
import java.util.Map;
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

    private static void addManualOverrides(RegistrateLangProvider provider) {
        replace(provider, "itemGroup.createdelightcore.coin", "Coins & Items");
        replace(provider, "item.createdelightcore.iron_coin", "§7Iron Coin");
        replace(provider, "item.createdelightcore.iron_coin.plural", "§7Iron Coins");
        replace(provider, "item.createdelightcore.copper_coin", "§eCopper Coin");
        replace(provider, "item.createdelightcore.copper_coin.plural", "§eCopper Coins");
        replace(provider, "item.createdelightcore.gold_coin", "§6Gold Coin");
        replace(provider, "item.createdelightcore.gold_coin.plural", "§6Gold Coins");
        replace(provider, "item.createdelightcore.emerald_coin", "§2Emerald Coin");
        replace(provider, "item.createdelightcore.emerald_coin.plural", "§2Emerald Coins");
        replace(provider, "item.createdelightcore.netherite_coin", "§5Netherite Coin");
        replace(provider, "item.createdelightcore.netherite_coin.plural", "§5Netherite Coins");
        replace(provider, "block.createdelightcore.order_parser", "Order Parser");
        replace(provider, "block.createdelightcore.order_requester", "Order Requester");
        replace(provider, "block.createdelightcore.quality_harvest_controller", "Quality Harvest Controller");
        replace(provider, "createdelightcore.quality_harvest_controller.no_calibrator", "No calibrator");
        replace(provider, "createdelightcore.quality_harvest_controller.status", "Life Matter: %s/%s | %s");
        replace(provider, "createdelightcore.gui.address", "Address");
        replace(provider, "createdelightcore.gui.candidates", "Candidates");
        replace(provider, "createdelightcore.gui.estimate", "Expected: %s");
        replace(provider, "createdelightcore.gui.expected_reward", "Expected Reward: %s");
        replace(provider, "createdelightcore.gui.reward_score", "Reward Score: %s");
        replace(provider, "createdelightcore.gui.estimate.incomplete", "Incomplete");
        replace(provider, "createdelightcore.gui.estimate.normal", "Normal");
        replace(provider, "createdelightcore.gui.estimate.good", "Good");
        replace(provider, "createdelightcore.gui.estimate.excellent", "Excellent");
        replace(provider, "createdelightcore.gui.estimate.great", "Great");
        replace(provider, "createdelightcore.gui.estimate.great_overflow", "%s %s");
        replace(provider, "createdelightcore.gui.full", "Full");
        replace(provider, "createdelightcore.gui.missing", "Missing");
        replace(provider, "createdelightcore.gui.mode_fixed", "Count");
        replace(provider, "createdelightcore.gui.mode_ratio", "Mix");
        replace(provider, "createdelightcore.gui.order", "Order");
        replace(provider, "createdelightcore.gui.partial", "Partial");
        replace(provider, "createdelightcore.gui.planned_count", "Use %s");
        replace(provider, "createdelightcore.gui.shortage_count", "Need %s");
        replace(provider, "createdelightcore.gui.summary_total", "Total");
        replace(provider, "createdelightcore.gui.ratio_parts_short", "%s pt");
        replace(provider, "createdelightcore.gui.quantity_hint", "Click to adjust quantity. Hold Shift for 16.");
        replace(provider, "createdelightcore.gui.weight_hint", "Click to adjust parts. Hold Shift for 16.");
        replace(provider, "createdelightcore.gui.refresh", "Refresh");
        replace(provider, "createdelightcore.gui.save", "Save");
        replace(provider, "createdelightcore.gui.select_candidates", "Select candidates");
        replace(provider, "createdelightcore.gui.send", "Send");
        replace(provider, "createdelightcore.gui.parser.blank_seal", "Blank seal");
        replace(provider, "createdelightcore.gui.parser.count_multiplier", "Item amount");
        replace(provider, "createdelightcore.gui.parser.draft", "Order draft");
        replace(provider, "createdelightcore.gui.parser.entry_count_multiplier", "Entry count");
        replace(provider, "createdelightcore.gui.parser.formal_order", "Formal order");
        replace(provider, "createdelightcore.gui.parser.insert", "Insert an order, draft, or seal");
        replace(provider, "createdelightcore.gui.parser.min_quality_bonus", "Quality bonus");
        replace(provider, "createdelightcore.gui.parser.money_multiplier", "Coin reward");
        replace(provider, "createdelightcore.gui.parser.no_data", "No matching data");
        replace(provider, "createdelightcore.gui.parser.no_seal", "No direction seal has been applied");
        replace(provider, "createdelightcore.gui.parser.order_entries", "Required categories");
        replace(provider, "createdelightcore.gui.parser.owner", "Owner");
        replace(provider, "createdelightcore.gui.parser.possible_categories", "Possible categories");
        replace(provider, "createdelightcore.gui.parser.possible_customers", "Possible customers");
        replace(provider, "createdelightcore.gui.parser.reputation", "Generated at reputation");
        replace(provider, "createdelightcore.gui.parser.rule_view", "Order rules");
        replace(provider, "createdelightcore.gui.parser.seal", "Seal");
        replace(provider, "createdelightcore.gui.parser.unknown_seal", "Unknown seal");
        replace(provider, "createdelightcore.gui.parser.unsupported", "Unsupported item");
        replace(provider, "createdelightcore.gui.parser.weight", "Weight %s");
        replace(provider, "createdelightcore.gui.help.select", "Left-click a candidate to select up to one stack.");
        replace(provider, "createdelightcore.gui.help.multi", "Select more candidates from the same entry to mix items.");
        replace(provider, "createdelightcore.gui.help.quantity", "Use - / + to adjust selected quantities.");
        replace(provider, "createdelightcore.gui.help.cancel", "Right-click a candidate to cancel it.");
        replace(provider, "createdelightcore.gui.help.ratio_select", "Left-click to add a candidate; right-click to remove it.");
        replace(provider, "createdelightcore.gui.help.ratio_weight", "Use - / + to adjust parts; more parts means more of this order goes to that item.");
        replace(provider, "createdelightcore.gui.help.ratio_planned", "Each Use X label shows how many items this order will consume.");
        replace(provider, "createdelightcore.gui.help.ratio_shortage", "Red rows mean stock is short; Need X is how many more items are required.");
        replace(provider, "createdelightcore.gui.help.ratio_missing", "Missing candidates stay saved and work again after restocking.");
        replace(provider, "createdelightcore.gui.help.score", "Higher reward score means more reward bundles and coins when the order is completed.");
        replace(provider, "createdelightcore.gui.help.redstone", "Give the machine a redstone pulse to send the saved request.");
        replace(provider, "createdelightcore.order_request.no_selection", "Select at least one candidate item");
        replace(provider, "createdelightcore.order_request.saved", "Order request saved");
        replace(provider, "createdelightcore.order_request.sent", "Order request sent");
        replace(provider, "createdelightcore.order_request.not_enough_items", "Not enough matching items in the linked network");
        replace(provider, "createdelightcore.order_request.failed", "Order request failed: check the link, address, and package network");
        replace(provider, "block.createdelightcore.lush_confiture_jelly_bottle", "Lush Confiture Jam");
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
        addVirtualFluid(provider, CDFluids.LUCUMA, "Lucuma Milkshake");
        addVirtualFluid(provider, CDFluids.PINK_DRAGON_FRUIT, "Pink Dragon fruit Milkshake");
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
        addManualOverrides(provider);
    }

    private static void replace(RegistrateLangProvider provider, String key, String value) {
        try {
            Field field = LanguageProvider.class.getDeclaredField("data");
            field.setAccessible(true);
            // noinspection unchecked
            Map<String, String> map = (Map<String, String>) field.get(provider);
            map.put(key, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error replacing entry in datagen.", e);
        }
    }
}
