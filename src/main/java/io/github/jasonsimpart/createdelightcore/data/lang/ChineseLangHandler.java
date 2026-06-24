package io.github.jasonsimpart.createdelightcore.data.lang;

import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTabs;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class ChineseLangHandler {
    private static void coin(RegistrateCNLangProvider provider, Supplier<Item> item, String name, String initial) {
        var id = item.get().getDescriptionId();
        provider.add(id, name);
        provider.add(id + ".initial", initial);
        provider.add(id + ".plural", name);
    }
    private static void addFluid(RegistrateCNLangProvider provider, FluidEntry<?> fluid, String name) {
        var namespace = fluid.getId().getNamespace();
        var id = fluid.getId().getPath();
        if (id.startsWith("flowing_"))
            id = id.substring("flowing_".length());
        provider.add("fluid." + namespace + "." + id, name);
        provider.add("block." + namespace + "." + id, name);
        provider.add(fluid.getBucket().get(), name + "桶");
    }
    private static void addVirtualFluid(RegistrateCNLangProvider provider, FluidEntry<?> fluid, String name) {
        var namespace = fluid.getId().getNamespace();
        var id = fluid.getId().getPath();
        if (id.startsWith("flowing_"))
            id = id.substring("flowing_".length());
        provider.add("fluid." + namespace + "." + id, name);
    }

    public static void init(RegistrateCNLangProvider provider) {
        //creativetabs
        provider.add(CDCreativeTabs.MISC.get(), "齿轮盛宴 | 杂项");
        provider.add(CDCreativeTabs.COIN.get(), "齿轮盛宴 | 货币");
        provider.add(CDCreativeTabs.FLUID.get(), "齿轮盛宴 | 流体");
        provider.add(CDCreativeTabs.FOOD.get(), "齿轮盛宴 | 食物");
        // food
        provider.addItem(CDItems.UNFRIED_SHRIMP, "生炸虾仁");
        provider.addItem(CDItems.UNFRIED_CHICKEN_CHIP, "生炸鸡块");
        provider.addItem(CDItems.UNFRIED_CHICKEN_LEG, "生炸鸡腿");
        provider.addItem(CDItems.UNFRIED_TONKATSU, "生炸猪排");
        provider.addItem(CDItems.UNFRIED_FISH, "生炸鱼");
        provider.addItem(CDItems.UNFRIED_POTATO, "生炸土豆");
        provider.addItem(CDItems.UNFRIED_CALAMARI, "生炸鱿鱼圈");
        replace(provider, CDItems.PIZZA_SLICE.get().getDescriptionId(), "披萨切片");
        replace(provider, CDItems.VEGETABLE_PIZZA_SLICE.get().getDescriptionId(), "蔬菜披萨切片");
        replace(provider, CDItems.MEATLOVERS_PIZZA_SLICE.get().getDescriptionId(), "肉披萨切片");
        replace(provider, CDItems.NETHER_PIZZA_SLICE.get().getDescriptionId(), "下界披萨切片");
        // chocolate
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_FILLED, "盛满的黑巧克力模具");
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_SOLID, "凝固的黑巧克力");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_FILLED, "盛满的白巧克力模具");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_SOLID, "凝固的白巧克力");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_FILLED, "盛满的红宝石巧克力模具");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_SOLID, "凝固的红宝石巧克力");
        //tin
        provider.addItem(CDItems.RAW_TIN, "粗锡");
        provider.addItem(CDItems.TIN_INGOT, "锡锭");
        provider.addItem(CDItems.TIN_NUGGET, "锡粒");
        //bronze
        provider.addItem(CDItems.BRONZE_INGOT, "青铜锭");
        provider.addItem(CDItems.BRONZE_NUGGET, "青铜粒");

        //block
        provider.addBlock(CDBlocks.FRAGMENT_OF_BORDER, "边境碎片");
        provider.addBlock(CDBlocks.TIN_ORE, "锡矿石");
        provider.addBlock(CDBlocks.DEEPSLATE_TIN_ORE, "深层锡矿石");
        provider.addBlock(CDBlocks.RAW_TIN, "粗锡块");
        provider.addBlock(CDBlocks.TIN, "锡块");
        provider.addBlock(CDBlocks.BRONZE, "青铜块");
        provider.addBlock(CDBlocks.FORGED_STEEL, "锻造钢块");
        //casing
        provider.addBlock(CDBlocks.STEEL_CASING, "钢制机壳");
        provider.addBlock(CDBlocks.FORGE_STEEL_CASING, "锻造钢机壳");
        provider.addBlock(CDBlocks.STEEL_GLASS_CASING, "钢制玻璃机壳");
        provider.addBlock(CDBlocks.STEEL_CLEAR_GLASS_CASING, "钢制通透玻璃机壳");
        //syrup
        provider.addBlock(CDBlocks.BASE, "基础糖浆块");
        provider.addBlock(CDBlocks.STRAWBERRY, "草莓糖浆块");
        provider.addBlock(CDBlocks.VANILLA, "香草糖浆块");
        provider.addBlock(CDBlocks.MINT, "薄荷糖浆块");
        provider.addBlock(CDBlocks.BANANA, "香蕉糖浆块");
        //ice-cream bricks
        provider.addBlock(CDBlocks.LUCUMA_ICE_CREAM_BRICKS, "蛋黄果味冰淇淋砖");
        provider.addBlock(CDBlocks.PINK_DRAGON_FRUIT_ICE_CREAM_BRICKS, "火龙果味冰淇淋砖");
        //fruits leaves
        provider.addBlock(CDBlocks.JUJUBE_LEAVES, "大枣树叶");
        provider.addBlock(CDBlocks.JUJUBE_SAPLING, "大枣树苗");
        provider.addBlock(CDBlocks.WALNUT_LEAVES, "核桃树叶");
        provider.addBlock(CDBlocks.WALNUT_SAPLING, "核桃树苗");
        //jelly bottle
        provider.addBlock(CDBlocks.LUSH_CONFITURE, "繁茂果酱瓶");
        //jelly_block
        provider.addBlock(CDBlocks.LUSH_CONFITURE_JELLY, "繁茂果酱块");
        //jello_block
        provider.addBlock(CDBlocks.LUSH_CONFITURE_JELLO, "繁茂果冻块");
        //coin
        coin(provider, CDItems.IRON, "铁币", "铁");
        coin(provider, CDItems.COPPER, "铜币", "铜");
        coin(provider, CDItems.GOLD, "金币", "金");
        coin(provider, CDItems.EMERALD, "绿宝石币", "绿");
        coin(provider, CDItems.NETHERITE, "下界合金币", "下界");
        provider.addBlock(CDBlocks.IRON_COIN_PILE, "铁币堆");
        provider.addBlock(CDBlocks.COPPER_COIN_PILE, "铜币堆");
        provider.addBlock(CDBlocks.GOLD_COIN_PILE, "金币堆");
        provider.addBlock(CDBlocks.EMERALD_COIN_PILE, "绿宝石币堆");
        provider.addBlock(CDBlocks.NETHERITE_COIN_PILE, "下界合金币堆");
        provider.addBlock(CDBlocks.PHANTOM_COMPOST, "幻灵肥料");
        provider.addBlock(CDBlocks.LUNA_SOIL, "月壤沃土");
        provider.addBlock(CDBlocks.LUNA_SOIL_FARMLAND, "月壤沃土耕地");
        provider.addBlock(CDBlocks.FIRE_LILY_CLUSTER, "烈焰百合簇");
        provider.addBlock(CDBlocks.FROST_LILY_CLUSTER, "寒冰百合簇");
        provider.addBlock(CDBlocks.LIGHTNING_LILY_CLUSTER, "闪电百合簇");
        replace(provider, CDBlocks.RAW_VEGETABLE_PIZZA.get().getDescriptionId(), "生蔬菜披萨");
        replace(provider, CDBlocks.VEGETABLE_PIZZA.get().getDescriptionId(), "蔬菜披萨");
        replace(provider, CDBlocks.RAW_MEATLOVERS_PIZZA.get().getDescriptionId(), "生肉披萨");
        replace(provider, CDBlocks.MEATLOVERS_PIZZA.get().getDescriptionId(), "肉披萨");
        replace(provider, CDBlocks.RAW_NETHER_PIZZA.get().getDescriptionId(), "生下界披萨");
        replace(provider, CDBlocks.NETHER_PIZZA.get().getDescriptionId(), "下界披萨");
        //molten fluid
        addFluid(provider, CDFluids.MOLTEN_ANDESITE, "熔融安山合金");
        addFluid(provider, CDFluids.MOLTEN_AZURE_NEODYMIUM, "熔融青钕合金");
        addFluid(provider, CDFluids.MOLTEN_SCARLET_NEODYMIUM, "熔融赤钕合金");
        addFluid(provider, CDFluids.MOLTEN_TITANIUM, "熔融钛");
        addFluid(provider, CDFluids.MOLTEN_MARTIAN_STEEL, "熔融火星钢");
        addFluid(provider, CDFluids.MOLTEN_FIRE_STEEL, "熔融龙炎钢");
        addFluid(provider, CDFluids.MOLTEN_ICE_STEEL, "熔融龙霜钢");
        addFluid(provider, CDFluids.MOLTEN_LIGHTNING_STEEL, "熔融龙霆钢");
        addFluid(provider, CDFluids.MOLTEN_FORGED_STEEL, "熔融锻造钢");
        addFluid(provider, CDFluids.MOLTEN_GLASS, "熔融玻璃");
        addFluid(provider, CDFluids.MOLTEN_QUARTZ_GLASS, "熔融石英玻璃");
        addFluid(provider, CDFluids.MOLTEN_QUARTZ_VIBRANT_GLASS, "熔融聚能石英玻璃");
        //ice cream scoop
        provider.addItem(CDItems.STRAWBERRY_ICE_CREAM_SCOOP, "草莓味冰淇淋球");
        provider.addItem(CDItems.BANANA_ICE_CREAM_SCOOP, "香蕉味冰淇淋球");
        provider.addItem(CDItems.MINT_ICE_CREAM_SCOOP, "薄荷味冰淇淋球");
        provider.addItem(CDItems.ADZUKI_ICE_CREAM_SCOOP, "红豆味冰淇淋球");
        provider.addItem(CDItems.POMEGRANATE_ICE_CREAM_SCOOP, "石榴味冰淇淋球");
        provider.addItem(CDItems.LIME_ICE_CREAM_SCOOP, "青柠味冰淇淋球");
        provider.addItem(CDItems.APPLE_ICE_CREAM_SCOOP, "苹果味冰淇淋球");
        provider.addItem(CDItems.BEETROOT_ICE_CREAM_SCOOP, "甜菜根味冰淇淋球");
        provider.addItem(CDItems.CARROT_ICE_CREAM_SCOOP, "胡萝卜味冰淇淋球");
        provider.addItem(CDItems.ENCHANTED_FRUIT_ICE_CREAM_SCOOP, "附魔之果味冰淇淋球");
        provider.addItem(CDItems.GLOW_BERRY_ICE_CREAM_SCOOP, "发光浆果味冰淇淋球");
        provider.addItem(CDItems.PUMPKIN_ICE_CREAM_SCOOP, "南瓜味冰淇淋球");
        provider.addItem(CDItems.LUCUMA_ICE_CREAM_SCOOP, "蛋黄果味冰淇淋球");
        provider.addItem(CDItems.PINK_DRAGON_FRUIT_ICE_CREAM_SCOOP, "火龙果味冰淇淋球");
        provider.addItem(CDItems.SUNNY_ICE_CREAM_SANDWICH, "阳光风味冰淇淋三明治");
        provider.addItem(CDItems.LUCUMA_ICE_CREAM_CONE, "蛋黄果味甜筒");
        provider.addItem(CDItems.PINK_DRAGON_FRUIT_ICE_CREAM_CONE, "火龙果味甜筒");
        //slime
        addFluid(provider, CDFluids.SLIME, "黏液");
        addFluid(provider, CDFluids.FERROUSLIME, "富铁黏液");
        addFluid(provider, CDFluids.CHORUSSLIME, "紫颂黏液");
        //radiation
        addFluid(provider, CDFluids.NUCLEAR_WASTE, "核废液");
        //milkshake
        addVirtualFluid(provider, CDFluids.APPLE, "苹果味奶昔");
        addVirtualFluid(provider, CDFluids.GLOW_BERRY, "发光浆果味奶昔");
        addVirtualFluid(provider, CDFluids.CARROT, "胡萝卜味奶昔");
        addVirtualFluid(provider, CDFluids.BEETROOT, "甜菜味奶昔");
        addVirtualFluid(provider, CDFluids.ENCHANTED_FRUIT, "附魔之果味奶昔");
        addVirtualFluid(provider, CDFluids.LUCUMA, "蛋黄果味奶昔");
        addVirtualFluid(provider, CDFluids.PINK_DRAGON_FRUIT, "火龙果味奶昔");
        //grape juice
        addVirtualFluid(provider, CDFluids.RED_GRAPE, "红葡萄汁");
        addVirtualFluid(provider, CDFluids.JUNGLE_RED_GRAPE, "丛林红葡萄汁");
        addVirtualFluid(provider, CDFluids.SAVANNA_RED_GRAPE, "热带草原红葡萄汁");
        addVirtualFluid(provider, CDFluids.TAIGA_RED_GRAPE, "针叶林红葡萄汁");
        addVirtualFluid(provider, CDFluids.WHITE_GRAPE, "白葡萄汁");
        addVirtualFluid(provider, CDFluids.JUNGLE_WHITE_GRAPE, "丛林白葡萄汁");
        addVirtualFluid(provider, CDFluids.SAVANNA_WHITE_GRAPE, "热带草原白葡萄汁");
        addVirtualFluid(provider, CDFluids.TAIGA_WHITE_GRAPE, "针叶林白葡萄汁");
        addVirtualFluid(provider, CDFluids.WARPED_GRAPE, "诡异葡萄汁");
        addVirtualFluid(provider, CDFluids.CRIMSON_GRAPE, "绯红葡萄汁");
        //recipes
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing.fan", "在细雪后放置鼓风机");
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing", "批量冷冻");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal", "%1$s变成了熔融金属的一部分");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal.player", "%2$s以为%1$s是一块没有融化的金属");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream.player", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation", "%1$s因辐射而浑身溃烂");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation.player", "%1$s因辐射而浑身溃烂");
        //jei
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid", "烈焰人液体燃料配方");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsume", "消耗%smB");
        provider.add("jei." + CreateDelightCore.MODID + ".SnowmanCoolerFluid", "雪傀儡液体冷却剂配方");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsumeCool", "消耗%smB");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting", "幻化");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.dimension", "在外星球中幻化");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.light", "有日光时会加快");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.fluid", "周围有灵质时会加快");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.accelerators", "周围有以下幻化催化剂时会加快");
        //tooltip
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftToSeeHeat", "§r§8按住 [§r§7Shift§r§8] 查看烈焰人燃烧信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftHeat", "§r§8按住 [§r§rShift§r§8] 查看烈焰人燃烧信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".amountConsume", "§8消耗燃料: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".burnTime", "§8燃烧时间: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".heatType", "§8燃烧类型: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".superHeat", "超级燃烧");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Heat", "加热");

        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlToSeeCool", "§r§8按住 [§r§7Ctrl§r§8] 查看雪傀儡冷却信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlCool", "§r§8按住 [§r§rCtrl§r§8] 查看雪傀儡冷却信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolTime", "§8冷却时间: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolType", "§8冷却类型: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Frozen", "速冻");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Cooled", "冷却");

        provider.add("tooltip." + CreateDelightCore.MODID + ".jelly_block", "带有粘性，但是不和其他粘性方块粘黏");
        provider.add("tooltip." + CreateDelightCore.MODID + ".jello_block", "光滑，但是能和相同果酱/果冻方块粘黏");
        //jade
        provider.add("config.jade.plugin_balm.jade", "Jade");
        provider.add("config.jade.plugin_createdelightcore.cmr.snowman_cooler", "雪傀儡冷却器");
        //waystone
        provider.add("gui." + CreateDelightCore.MODID + ".need", "需要：");
        provider.add("gui." + CreateDelightCore.MODID + ".free", "免费");
    }

    public static void replace(@NotNull RegistrateCNLangProvider provider, @NotNull String key,
                               @NotNull String value) {
        try {
            // the regular lang mappings
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
