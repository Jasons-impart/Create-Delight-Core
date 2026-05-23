package io.github.jasonsimpart.registry;

import com.github.alexmodguy.alexscaves.server.entity.item.ThrownIceCreamScoopEntity;
import com.github.alexmodguy.alexscaves.server.item.ThrownProjectileItem;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.content.item.FoiledItem;
import io.github.jasonsimpart.content.item.OptionalEffectFoodItem;
import io.github.jasonsimpart.content.item.OxygenTankItem;
import io.github.jasonsimpart.content.item.QualityAbsorberItem;
import io.github.jasonsimpart.content.item.ReturningFoodItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateDelightCore.MODID);

    // 空气套装材料：0 防御、不可破坏，实际功能后续交给兼容逻辑处理。
    private static final Holder<ArmorMaterial> AIR_ARMOR_MATERIAL = Holder.direct(new ArmorMaterial(Map.of(ArmorItem.Type.HELMET, 0, ArmorItem.Type.CHESTPLATE, 0, ArmorItem.Type.LEGGINGS, 0, ArmorItem.Type.BOOTS, 0, ArmorItem.Type.BODY, 0), 9, Holder.direct(SoundEvents.MOSS_STEP), () -> Ingredient.of(Items.GLASS_BOTTLE), List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "air"))), 0.0F, 0.0F));

    // AE 普通中间件名称表。
    private static final String[] AE_PROCESSOR_TYPES = {"engineering", "calculation", "logic", "accumulation", "omni_link", "complex_link", "multidimensional_expansion"};
    private static final String[] AE_CELL_HOUSING_TYPES = {"item", "fluid", "mega_item", "mega_fluid", "omni", "complex_omni", "quantum_omni"};

    // Create 动力/机器类序列装配半成品，必须用 SequencedAssemblyItem 保留进度条。
    private static final String[] CREATE_MACHINE_TRANSITIONAL_ITEM_NAMES = {"incomplete_layered_magnet", "incomplete_alternator", "incomplete_diesel_engine", "incomplete_huge_diesel_engine", "incomplete_large_diesel_engine", "incomplete_basic_motor", "incomplete_advanced_motor", "incomplete_reinforced_motor", "incomplete_carbon_brushes", "incomplete_electric_motor", "incompleted_modular_accumulator", "incomplete_electron_tube", "incomplete_fs_upgrade", "incomplete_graviton_tube", "incomplete_planet_gear", "incomplete_magnetic_mechanism"};

    // 弹药类序列装配半成品。
    private static final String[] AMMO_TRANSITIONAL_ITEM_NAMES = {"incomplete_12g", "incomplete_slap", "incomplete_rbapb", "incomplete_gas_pistol_ammo"};

    // 汉堡/三明治序列装配半成品。
    private static final String[] BURGER_SANDWICH_TRANSITIONAL_ITEM_NAMES = {"incomplete_bison_burger", "incomplete_eggplant_burger", "incomplete_hamburger", "incomplete_heartburger", "incomplete_cheese_burger", "incomplete_deluxe_burger", "incomplete_portobello_burger", "incomplete_land_and_sea_burger", "incomplete_kangaroo_burger", "incomplete_bunfungus_sandwich", "incomplete_mutton_sandwich", "incomplete_egg_sandwich", "incomplete_chicken_sandwich", "incomplete_bacon_sandwich", "incomplete_prawn_po_boy", "incomplete_lux_and_ham_sandwich", "incomplete_pine_and_sap_sandwich", "incomplete_vegan_hamburger", "incomplete_squid_sandwich", "incomplete_insect_sandwich"};

    // 寿司/冰淇淋序列装配半成品。
    private static final String[] SUSHI_TRANSITIONAL_ITEM_NAMES = {"incomplete_gunkan", "incomplete_nigiri", "incomplete_hosomaki", "incomplete_futomaki", "incomplete_california", "incomplete_neapolitan_ice_cream"};

    // 大批量生成的未完成物品：AE2/AE 扩展/龙钢插板等配方中间件。
    private static final String[] GENERATED_UNFINISHED_ITEM_NAMES = {"incomplete_cell_component_1k", "incomplete_cell_component_4k", "incomplete_cell_component_16k", "incomplete_cell_component_64k", "incomplete_cell_component_256k", "incomplete_basic_card", "incomplete_advanced_card", "incomplete_formation_core", "incomplete_annihilation_core", "incomplete_me_p2p_tunnel", "incomplete_charged_staff", "incomplete_entropy_manipulator", "incomplete_blank_pattern", "incomplete_wireless_terminal", "incomplete_wireless_crafting_terminal", "incomplete_quantum_bridge_card", "incomplete_wireless_pattern_access_terminal", "incomplete_wireless_pattern_encoding_terminal", "incomplete_cell_component_1m", "incomplete_cell_component_4m", "incomplete_cell_component_16m", "incomplete_cell_component_64m", "incomplete_cell_component_256m", "incomplete_wireless_ex_pat", "incomplete_pattern_modifier", "incomplete_infinity_cell", "incomplete_mega_energy_cell", "incomplete_mega_crafting_unit", "incomplete_decompression_module", "incomplete_drive", "incomplete_interface", "incomplete_energy_cell", "incomplete_dense_energy_cell", "incomplete_crafting_unit", "incomplete_pattern_provider", "incomplete_molecular_assembler", "incomplete_controller", "incomplete_crystal_fixer", "incomplete_fire_dragonsteel_armorplate", "incomplete_ice_dragonsteel_armorplate", "incomplete_lightning_dragonsteel_armorplate"};

    // 食品中间件批量名称表。
    private static final String[] COOKIE_DOUGH_ITEM_NAMES = {"persimmon_cookie_dough", "lemon_cookie_dough", "oatmeal_cookie_dough", "green_tea_cookie_dough", "cranberry_cookie_dough", "bayberry_cookie_dough", "chocolate_cookie_dough", "honey_cookie_dough", "sweet_berry_cookie_dough", "lime_cookie_dough", "chorus_cookie_dough", "bat_cookie_dough", "paw_cookie_dough"};
    private static final String[] UNBAKED_MUFFIN_ITEM_NAMES = {"unbaked_red_velvet_cupcake", "unbaked_mixed_berry_muffin", "unbaked_chocolate_pumpkin_muffin", "unbaked_blueberry_muffin", "unbaked_cranberry_muffin", "unbaked_monster_muffin"};
    private static final String[] POPSICLE_MOLD_FILLED_ITEM_NAMES = {"empty_popsicle_mold_filled", "chorus_fruit_popsicle_mold_filled", "tear_popsicle_mold_filled", "milk_popsicle_mold_filled", "hamimelon_popsicle_mold_filled", "lime_popsicle_mold_filled", "kiwi_popsicle_mold_filled", "berry_popsicle_mold_filled", "big_popsicle_mold_filled", "green_tongue_mold_filled"};
    private static final String[] POPSICLE_MOLD_SOLID_ITEM_NAMES = {"empty_popsicle_mold_solid", "chorus_fruit_popsicle_mold_solid", "tear_popsicle_mold_solid", "milk_popsicle_mold_solid", "hamimelon_popsicle_mold_solid", "lime_popsicle_mold_solid", "kiwi_popsicle_mold_solid", "berry_popsicle_mold_solid", "big_popsicle_mold_solid", "green_tongue_mold_solid"};

    // 旧 Core 生炸食物。
    public static final DeferredItem<Item> UNFRIED_SHRIMP = rawFood("unfried_shrimp", 4, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CHICKEN_CHIP = rawFood("unfried_chicken_chip", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CHICKEN_LEG = rawFood("unfried_chicken_leg", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_TONKATSU = rawFood("unfried_tonkatsu", 4, 0.3F);
    public static final DeferredItem<Item> UNFRIED_FISH = rawFood("unfried_fish", 3, 0.3F);
    public static final DeferredItem<Item> UNFRIED_POTATO = rawFood("unfried_potato", 2, 0.3F);
    public static final DeferredItem<Item> UNFRIED_CALAMARI = rawFood("unfried_calamari", 3, 0.3F);

    // Alex's Caves 投掷冰淇淋球。
    public static final DeferredItem<ThrownProjectileItem> STRAWBERRY_ICE_CREAM_SCOOP = iceCreamScoop("strawberry");
    public static final DeferredItem<ThrownProjectileItem> BANANA_ICE_CREAM_SCOOP = iceCreamScoop("banana");
    public static final DeferredItem<ThrownProjectileItem> MINT_ICE_CREAM_SCOOP = iceCreamScoop("mint");
    public static final DeferredItem<ThrownProjectileItem> ADZUKI_ICE_CREAM_SCOOP = iceCreamScoop("adzuki");
    public static final DeferredItem<ThrownProjectileItem> POMEGRANATE_ICE_CREAM_SCOOP = iceCreamScoop("pomegranate");
    public static final DeferredItem<ThrownProjectileItem> LIME_ICE_CREAM_SCOOP = iceCreamScoop("lime");
    public static final DeferredItem<ThrownProjectileItem> APPLE_ICE_CREAM_SCOOP = iceCreamScoop("apple");
    public static final DeferredItem<ThrownProjectileItem> BEETROOT_ICE_CREAM_SCOOP = iceCreamScoop("beetroot");
    public static final DeferredItem<ThrownProjectileItem> CARROT_ICE_CREAM_SCOOP = iceCreamScoop("carrot");
    public static final DeferredItem<ThrownProjectileItem> ENCHANTED_FRUIT_ICE_CREAM_SCOOP = iceCreamScoop("enchanted_fruit");
    public static final DeferredItem<ThrownProjectileItem> GLOW_BERRY_ICE_CREAM_SCOOP = iceCreamScoop("glow_berry");
    public static final DeferredItem<ThrownProjectileItem> PUMPKIN_ICE_CREAM_SCOOP = iceCreamScoop("pumpkin");

    // 巧克力模具。
    public static final DeferredItem<Item> BLACK_CHOCOLATE_MOLD_SOLID = simpleItem("black_chocolate_mold_solid");
    public static final DeferredItem<Item> BLACK_CHOCOLATE_MOLD_FILLED = simpleItem("black_chocolate_mold_filled");
    public static final DeferredItem<Item> WHITE_CHOCOLATE_MOLD_SOLID = simpleItem("white_chocolate_mold_solid");
    public static final DeferredItem<Item> WHITE_CHOCOLATE_MOLD_FILLED = simpleItem("white_chocolate_mold_filled");
    public static final DeferredItem<Item> RUBY_CHOCOLATE_MOLD_SOLID = simpleItem("ruby_chocolate_mold_solid");
    public static final DeferredItem<Item> RUBY_CHOCOLATE_MOLD_FILLED = simpleItem("ruby_chocolate_mold_filled");

    // 钱币。
    public static final DeferredItem<Item> IRON_COIN = coin("iron_coin", Rarity.COMMON);
    public static final DeferredItem<Item> COPPER_COIN = coin("copper_coin", Rarity.UNCOMMON);
    public static final DeferredItem<Item> GOLD_COIN = coin("gold_coin", Rarity.RARE);
    public static final DeferredItem<Item> EMERALD_COIN = coin("emerald_coin", Rarity.RARE);
    public static final DeferredItem<Item> NETHERITE_COIN = coin("netherite_coin", Rarity.EPIC);

    // 金属锭、粒和粗矿。
    public static final DeferredItem<Item> TIN_INGOT = simpleItem("tin_ingot");
    public static final DeferredItem<Item> TIN_NUGGET = simpleItem("tin_nugget");
    public static final DeferredItem<Item> RAW_TIN = simpleItem("raw_tin");
    public static final DeferredItem<Item> BRONZE_INGOT = simpleItem("bronze_ingot");
    public static final DeferredItem<Item> BRONZE_NUGGET = simpleItem("bronze_nugget");

    // 材料、粉末、板材和普通中间件。
    public static final DeferredItem<Item> CARBON_DUST = simpleItem("carbon_dust");
    public static final DeferredItem<Item> TIN_DUST = simpleItem("tin_dust");
    public static final DeferredItem<Item> DIRTY_TIN_DUST = simpleItem("dirty_tin_dust");
    public static final DeferredItem<Item> SILVER_DUST = simpleItem("silver_dust");
    public static final DeferredItem<Item> DIRTY_SILVER_DUST = simpleItem("dirty_silver_dust");
    public static final DeferredItem<Item> TITANIUM_DUST = simpleItem("titanium_dust");
    public static final DeferredItem<Item> DIRTY_TITANIUM_DUST = simpleItem("dirty_titanium_dust");
    public static final DeferredItem<Item> URANIUM_DUST = simpleItem("uranium_dust");
    public static final DeferredItem<Item> ENRICHED_URANIUMDUST = simpleItem("enriched_uraniumdust");
    public static final DeferredItem<Item> DEPLETED_URANIUM_DUST = simpleItem("depleted_uranium_dust");
    public static final DeferredItem<Item> CRUSHED_RAW_TITANIUM = simpleItem("crushed_raw_titanium");
    public static final DeferredItem<Item> CARBON_PLATE = simpleItem("carbon_plate");
    public static final DeferredItem<Item> ANDESITE_ALLOY_NUGGET = simpleItem("andesite_alloy_nugget");
    public static final DeferredItem<Item> VERMICELLI = simpleItem("vermicelli");
    public static final DeferredItem<Item> BOARD_NOODLES = simpleItem("board_noodles");
    public static final DeferredItem<Item> CORN_FLOUR = simpleItem("corn_flour");
    public static final DeferredItem<Item> WAFER_DOUGH = simpleItem("wafer_dough");
    public static final DeferredItem<Item> GUNCOTTON = simpleItem("guncotton");
    public static final DeferredItem<Item> OTHERWORLD_NOTE = rarityItem("otherworld_note", Rarity.RARE);
    public static final DeferredItem<Item> UNIVERSAL_PRESS = simpleItem("universal_press");
    public static final DeferredItem<Item> ULTIMATE_UNIVERSAL_PRESS = simpleItem("ultimate_universal_press");
    public static final DeferredItem<Item> REDSTONE_PASTE = durableItem("redstone_paste", 64);
    public static final DeferredItem<Item> GLOWSTONE_PASTE = durableItem("glowstone_paste", 64);
    public static final DeferredItem<Item> SKY_STONE_PASTE = durableItem("sky_stone_paste", 64);
    public static final DeferredItem<Item> QUARTZ_GLASS_PARTS = simpleItem("quartz_glass_parts");
    public static final DeferredItem<Item> QUARTZ_VIBRANT_GLASS_PARTS = simpleItem("quartz_vibrant_glass_parts");
    public static final DeferredItem<Item> SKY_COPPER_INGOT = fireResistantItem("sky_copper_ingot");
    public static final DeferredItem<Item> CELL_HOUSING_CURVING_HEAD = simpleItem("cell_housing_curving_head");
    public static final DeferredItem<Item> PLANET_GEAR = rarityItem("planet_gear", Rarity.UNCOMMON);
    public static final DeferredItem<Item> MAGNETIC_MECHANISM = rarityItem("magnetic_mechanism", Rarity.UNCOMMON);
    public static final DeferredItem<Item> PROSPECTOR = unstackableItem("prospector");
    public static final DeferredItem<Item> PROSPECTOR_CORE = simpleItem("prospector_core");
    public static final DeferredItem<Item> PHASE_TRANSITION_IRON = simpleItem("phase_transition_iron");
    public static final DeferredItem<Item> MMD_DIAMOND = simpleItem("mmd_diamond");
    public static final DeferredItem<Item> INCOMPLETE_PAPER = simpleItem("incomplete_paper");
    public static final DeferredItem<Item> WASTE_PAPER = simpleItem("waste_paper");
    public static final DeferredItem<Item> UNFINISHED_LEATHER = simpleItem("unfinished_leather");
    public static final DeferredItem<Item> DRY_YEAST = simpleItem("dry_yeast");
    public static final DeferredItem<Item> STEEL_SHEET = simpleItem("steel_sheet");
    public static final DeferredItem<Item> FORGED_STEEL_SHEET = simpleItem("forged_steel_sheet");
    public static final DeferredItem<Item> BLOOD_COLLECTION_DEVICE = simpleItem("blood_collection_device");
    public static final DeferredItem<Item> NEEDLE = simpleItem("needle");
    public static final DeferredItem<Item> ORDER = simpleItem("order");
    public static final DeferredItem<Item> UNOPENED_ORDER = simpleItem("unopened_order");

    // 工具、调试物品和装备。
    public static final DeferredItem<Item> ORDER_DELIVERER_ITEM = simpleItem("order_deliverer_item");
    public static final DeferredItem<QualityAbsorberItem> QUALITY_ABSORBER = ITEMS.registerItem("quality_absorber", QualityAbsorberItem::new);
    public static final DeferredItem<Item> UNACTIVATED_CRYSTALLINE_FLOWER = simpleItem("unactivated_crystalline_flower");
    public static final DeferredItem<Item> DEBUG_RELOAD_TOOL = ITEMS.registerSimpleItem("debug_reload_tool", new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> DEBUG_INFO_TOOL = ITEMS.registerSimpleItem("debug_info_tool", new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<ArmorItem> AIR_HELMET = airArmor("air_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> AIR_CHESTPLATE = airArmor("air_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> AIR_LEGGINGS = airArmor("air_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> AIR_BOOTS = airArmor("air_boots", ArmorItem.Type.BOOTS);
    public static final DeferredItem<OxygenTankItem> OXYGEN_TANK = oxygenTank("oxygen_tank");
    public static final DeferredItem<OxygenTankItem> STURDY_OXYGEN_TANK = oxygenTank("sturdy_oxygen_tank");

    // 高稀有度材料和特殊模板。
    public static final DeferredItem<Item> DREAD_HEART = rarityItem("dread_heart", Rarity.EPIC);
    public static final DeferredItem<SmithingTemplateItem> DREAD_UPGRADE_SMITHING_TEMPLATE = ITEMS.registerItem("dread_upgrade_smithing_template", properties -> new DreadUpgradeSmithingTemplateItem());
    public static final DeferredItem<Item> DEVIL_EYE = rarityItem("devil_eye", Rarity.EPIC);
    public static final DeferredItem<Item> DEMONIC_CODEX = rarityItem("demonic_codex", Rarity.EPIC);
    public static final DeferredItem<Item> BLEAK_ELECTRON_TUBE = simpleItem("bleak_electron_tube");
    public static final DeferredItem<Item> EMPTY_RICEBALL = simpleFood("empty_riceball", 4, 0.6F);

    // 矿簇和基因种子。
    public static final DeferredItem<Item> OVERWORLD_METAL_ORE_CLUSTER = simpleItem("overworld_metal_ore_cluster");
    public static final DeferredItem<Item> OVERWORLD_NOBLE_METAL_ORE_CLUSTER = rarityItem("overworld_noble_metal_ore_cluster", Rarity.RARE);
    public static final DeferredItem<Item> NETHER_ORE_CLUSTER = rarityItem("nether_ore_cluster", Rarity.RARE);
    public static final DeferredItem<Item> MOON_ORE_CLUSTER = rarityItem("moon_ore_cluster", Rarity.EPIC);
    public static final DeferredItem<Item> MARS_ORE_CLUSTER = rarityItem("mars_ore_cluster", Rarity.EPIC);
    public static final DeferredItem<Item> MERCURY_ORE_CLUSTER = simpleItem("mercury_ore_cluster");
    public static final DeferredItem<Item> VENUS_ORE_CLUSTER = rarityItem("venus_ore_cluster", Rarity.EPIC);
    public static final DeferredItem<Item> GLACIO_ORE_CLUSTER = rarityItem("glacio_ore_cluster", Rarity.EPIC);
    public static final DeferredItem<Item> MARS_GEMSTONE_CLUSTER = rarityItem("mars_gemstone_cluster", Rarity.UNCOMMON);
    public static final DeferredItem<Item> INFERIOR_GENETIC_SEED = simpleItem("inferior_genetic_seed");
    public static final DeferredItem<Item> NORMAL_GENETIC_SEED = simpleItem("normal_genetic_seed");
    public static final DeferredItem<Item> REFINED_GENETIC_SEED = simpleItem("refined_genetic_seed");
    public static final DeferredItem<Item> PURE_GENETIC_SEED = simpleItem("pure_genetic_seed");
    public static final DeferredItem<Item> FLAWLESS_GENETIC_SEED = simpleItem("flawless_genetic_seed");

    // 普通食物和带效果食物。
    public static final DeferredItem<Item> FUGU_ROLL = ITEMS.registerSimpleItem("fugu_roll", new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationModifier(1.0F).effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 1200), 1.0F).build()));
    public static final DeferredItem<Item> RADGILL_SUSHI = ITEMS.registerSimpleItem("radgill_sushi", new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).effect(() -> new MobEffectInstance(ACEffectRegistry.IRRADIATED, 1200, 2), 1.0F).effect(() -> new MobEffectInstance(MobEffects.SATURATION, 1200), 1.0F).build()));
    public static final DeferredItem<Item> DEEP_SEA_SUSHI_ROLL_SLICE = ITEMS.registerSimpleItem("deep_sea_sushi_roll_slice", new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(1.0F).effect(() -> new MobEffectInstance(ACEffectRegistry.DEEPSIGHT, 1200), 1.0F).build()));
    public static final DeferredItem<Item> BUTTER = badFastFood("butter");
    public static final DeferredItem<Item> OIL_DOUGH = badFastFood("oil_dough");
    public static final DeferredItem<Item> PUFF_PASTRY = badFastFood("puff_pastry");
    public static final DeferredItem<Item> RAW_CALAMARI = simpleFood("raw_calamari", 1, 1.0F);
    public static final DeferredItem<Item> RAW_GHAST_CALAMARI = simpleFood("raw_ghast_calamari", 1, 1.0F);
    public static final DeferredItem<Item> RAW_EMPANADA = simpleFood("raw_empanada", 4, 0.25F);
    public static final DeferredItem<Item> RAW_CHEESE_PIZZA = simpleItem("raw_cheese_pizza");
    public static final DeferredItem<Item> OAT_BREAD = ITEMS.registerSimpleItem("oat_bread", new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.5F).effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 60, 1), 1.0F).build()));
    public static final DeferredItem<Item> SALAMI = ITEMS.registerSimpleItem("salami", new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 600, 1), 1.0F).build()));
    public static final DeferredItem<Item> RAW_POTATO_PANCAKE = simpleFood("raw_potato_pancake", 2, 0.5F);
    public static final DeferredItem<Item> YORKSHIRE_PUDDING_AND_BEEF = simpleFood("yorkshire_pudding_and_beef", 12, 1.0F);
    public static final DeferredItem<Item> EMPTY_POPSICLE = ITEMS.registerSimpleItem("empty_popsicle", new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.25F).fast().effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200), 1.0F).build()));
    public static final DeferredItem<Item> BRAISED_INTESTINES_IN_BROWN_SAUCE = ITEMS.registerSimpleItem("braised_intestines_in_brown_sauce", new Item.Properties().rarity(Rarity.EPIC).stacksTo(16).food(new FoodProperties.Builder().nutrition(20).saturationModifier(1.0F).effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 6000), 1.0F).usingConvertsTo(Items.BOWL).build()));
    public static final DeferredItem<Item> BOILING_WATER_CABBAGE = ITEMS.registerSimpleItem("boiling_water_cabbage", new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1), 0.5F).effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 3000), 1.0F).usingConvertsTo(Items.BOWL).build()));
    public static final DeferredItem<Item> MAYO_CORN_DOG = ITEMS.registerSimpleItem("mayo_corn_dog", new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200), 1.0F).build()));
    public static final DeferredItem<Item> KETCHUP_CORN_DOG = ITEMS.registerSimpleItem("ketchup_corn_dog", new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200), 1.0F).build()));
    public static final DeferredItem<ReturningFoodItem> WRAPPED_FRIES_GHASTA = ITEMS.registerItem("wrapped_fries_ghasta", properties -> new ReturningFoodItem(properties.food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build()), ResourceLocation.fromNamespaceAndPath("create_bic_bit", "dirty_paper")));
    public static final DeferredItem<FoiledItem> ENCHANTED_GOLDEN_LANTERN_FRUIT = ITEMS.registerItem("enchanted_golden_lantern_fruit", properties -> new FoiledItem(properties.rarity(Rarity.EPIC).food(new FoodProperties.Builder().nutrition(4).saturationModifier(1.5F).effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000), 1.0F).effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 3600, 3), 1.0F).effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 2), 1.0F).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600), 1.0F).build())));
    public static final DeferredItem<FoiledItem> ENCHANTED_GOLDEN_CARROT = ITEMS.registerItem("enchanted_golden_carrot", properties -> new FoiledItem(properties.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.2F).effect(() -> new MobEffectInstance(MobEffects.SATURATION, 20), 1.0F).effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT, 3600), 1.0F).build())));
    public static final DeferredItem<Item> FUEL_HOTCREAM = simpleItem("fuel_hotcream");
    public static final DeferredItem<OptionalEffectFoodItem> LUSH_CONFITURE_JELLO_ITEM = ITEMS.registerItem("lush_confiture_jello_item", properties -> new OptionalEffectFoodItem(properties.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.5F).build()), List.of(OptionalEffectFoodItem.OptionalEffect.of("cosmopolitan", "tracer", 600, 0), OptionalEffectFoodItem.OptionalEffect.of("cosmopolitan", "phototaxis", 600, 0)), Items.BOWL));
    public static final DeferredItem<OptionalEffectFoodItem> ENCHANTED_GOLDEN_ARBUTUS_BERRIES = ITEMS.registerItem("enchanted_golden_arbutus_berries", properties -> new OptionalEffectFoodItem(properties.rarity(Rarity.EPIC).food(new FoodProperties.Builder().nutrition(4).saturationModifier(1.0F).build()), List.of(OptionalEffectFoodItem.OptionalEffect.of("cosmopolitan", "phototaxis", 1200, 1), OptionalEffectFoodItem.OptionalEffect.of("minecraft", "strength", 1200, 2)), true));

    // 批量注册的食品中间件。
    public static final List<DeferredItem<Item>> COOKIE_DOUGH_ITEMS = registerCookieDoughItems(List.of(COOKIE_DOUGH_ITEM_NAMES));
    public static final List<DeferredItem<Item>> UNBAKED_MUFFIN_ITEMS = registerSimpleItems(List.of(UNBAKED_MUFFIN_ITEM_NAMES));
    public static final List<DeferredItem<Item>> POPSICLE_MOLD_FILLED_ITEMS = registerSimpleItems(List.of(POPSICLE_MOLD_FILLED_ITEM_NAMES));
    public static final List<DeferredItem<Item>> POPSICLE_MOLD_SOLID_ITEMS = registerSimpleItems(List.of(POPSICLE_MOLD_SOLID_ITEM_NAMES));

    // 序列装配半成品。
    public static final DeferredItem<SequencedAssemblyItem> POTATO_STEW_BEEF = ITEMS.registerItem("potato_stew_beef", properties -> new SequencedAssemblyItem(properties.food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.6F).build())));
    public static final DeferredItem<SequencedAssemblyItem> OIL_DOUGH_WITH_BUTTER = ITEMS.registerItem("oil_dough_with_butter", properties -> new SequencedAssemblyItem(properties.food(new FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).fast().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 1), 0.5F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 1), 1.0F).build())));
    public static final List<DeferredItem<Item>> AE_INTERMEDIATE_ITEMS = registerSimpleItems(aeIntermediateItemNames());
    public static final List<DeferredItem<SequencedAssemblyItem>> CREATE_MACHINE_TRANSITIONAL_ITEMS = registerSequencedAssemblyItems(List.of(CREATE_MACHINE_TRANSITIONAL_ITEM_NAMES));
    public static final List<DeferredItem<SequencedAssemblyItem>> AMMO_TRANSITIONAL_ITEMS = registerSequencedAssemblyItems(List.of(AMMO_TRANSITIONAL_ITEM_NAMES));
    public static final List<DeferredItem<SequencedAssemblyItem>> BURGER_SANDWICH_TRANSITIONAL_ITEMS = registerSequencedAssemblyItems(List.of(BURGER_SANDWICH_TRANSITIONAL_ITEM_NAMES));
    public static final List<DeferredItem<SequencedAssemblyItem>> SUSHI_TRANSITIONAL_ITEMS = registerSequencedAssemblyItems(List.of(SUSHI_TRANSITIONAL_ITEM_NAMES));
    public static final List<DeferredItem<SequencedAssemblyItem>> GENERATED_UNFINISHED_ITEMS = registerSequencedAssemblyItems(List.of(GENERATED_UNFINISHED_ITEM_NAMES));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    // ---- 注册辅助方法 ----

    private static DeferredItem<Item> simpleItem(String name) {
        return ITEMS.registerSimpleItem(name);
    }

    private static DeferredItem<ArmorItem> airArmor(String name, ArmorItem.Type type) {
        return ITEMS.registerItem(name, properties -> new ArmorItem(AIR_ARMOR_MATERIAL, type, properties.rarity(Rarity.UNCOMMON).durability(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true))));
    }

    private static DeferredItem<OxygenTankItem> oxygenTank(String name) {
        return ITEMS.registerItem(name, properties -> new OxygenTankItem(properties, "tooltip." + CreateDelightCore.MODID + "." + name + ".oxygen", "tooltip." + CreateDelightCore.MODID + "." + name + ".environment"));
    }

    private static DeferredItem<ThrownProjectileItem> iceCreamScoop(String flavor) {
        return ITEMS.registerItem(flavor + "_ice_cream_scoop", properties -> new ThrownProjectileItem(properties, player -> new ThrownIceCreamScoopEntity(player.level(), player), -10.0F, 1.0F, 0.2F));
    }

    private static DeferredItem<Item> simpleFood(String name, int nutrition, float saturation) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build()));
    }

    private static DeferredItem<Item> badFastFood(String name) {
        FoodProperties food = new FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).fast().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 1), 1.0F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 1), 1.0F).build();
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(food));
    }

    private static List<DeferredItem<Item>> registerSimpleItems(List<String> names) {
        return names.stream().map(ModItems::simpleItem).toList();
    }

    private static List<DeferredItem<Item>> registerCookieDoughItems(List<String> names) {
        return names.stream().map(ModItems::cookieDoughItem).toList();
    }

    private static DeferredItem<Item> cookieDoughItem(String name) {
        FoodProperties food = new FoodProperties.Builder().nutrition(1).saturationModifier(1.0F).fast().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 1), 0.8F).build();
        if ("green_tea_cookie_dough".equals(name)) {
            return ITEMS.registerItem(name, properties -> new OptionalEffectFoodItem(properties.food(food), List.of(OptionalEffectFoodItem.OptionalEffect.of("youkaishomecoming", "tea_polyphenols", 200, 0))));
        }
        if ("chorus_cookie_dough".equals(name)) {
            return ITEMS.registerItem(name, properties -> new OptionalEffectFoodItem(properties.food(food), List.of(OptionalEffectFoodItem.OptionalEffect.of("fruitsdelight", "chorus", 1, 0))));
        }
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(food));
    }

    private static List<DeferredItem<SequencedAssemblyItem>> registerSequencedAssemblyItems(List<String> names) {
        return names.stream().map(name -> ITEMS.registerItem(name, SequencedAssemblyItem::new)).toList();
    }

    private static List<String> aeIntermediateItemNames() {
        List<String> names = new ArrayList<>();
        for (String type : AE_PROCESSOR_TYPES) {
            names.add("initial_processing_of_printed_" + type + "_processor");
            names.add(type + "_processor_inscribed");
        }
        for (String type : AE_CELL_HOUSING_TYPES) {
            names.add("initial_processing_of_" + type + "_cell_housing");
            names.add(type + "_cell_housing_blank");
            names.add("unformed_" + type + "_cell_housing");
        }
        return List.copyOf(names);
    }

    private static DeferredItem<Item> durableItem(String name, int durability) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().durability(durability));
    }

    private static DeferredItem<Item> fireResistantItem(String name) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().fireResistant());
    }

    private static DeferredItem<Item> rarityItem(String name, Rarity rarity) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().rarity(rarity));
    }

    private static DeferredItem<Item> unstackableItem(String name) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().stacksTo(1));
    }

    private static DeferredItem<Item> rawFood(String name, int nutrition, float saturation) {
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 50), 0.5F).build();
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(food));
    }

    private static DeferredItem<Item> coin(String name, Rarity rarity) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().rarity(rarity).fireResistant());
    }

    private static Component dreadUpgradeText(String key) {
        return Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "smithing_template.dread_upgrade." + key))).withStyle(ChatFormatting.BLUE);
    }

    private static List<ResourceLocation> dreadUpgradeBaseSlotIcons() {
        return List.of(ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet"), ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate"), ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings"), ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots"));
    }

    private static List<ResourceLocation> dreadUpgradeAdditionSlotIcons() {
        return List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"));
    }

    private static final class DreadUpgradeSmithingTemplateItem extends SmithingTemplateItem {
        private DreadUpgradeSmithingTemplateItem() {
            super(dreadUpgradeText("applies_to"), dreadUpgradeText("ingredients"), Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "dread_upgrade_smithing_template"))).withStyle(ChatFormatting.BLUE), dreadUpgradeText("base_slot_description"), dreadUpgradeText("additions_slot_description"), dreadUpgradeBaseSlotIcons(), dreadUpgradeAdditionSlotIcons());
        }

        @Override
        public Component getName(ItemStack stack) {
            return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.BLUE);
        }
    }
}
