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
        provider.addBlock(CDBlocks.ELECTRUM, "琥珀金块");
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
        //coil
        provider.addBlock(CDBlocks.COPPER_COIL, "铜线圈");
        //coin
        coin(provider, CDItems.IRON, "铁币", "铁");
        coin(provider, CDItems.COPPER, "铜币", "铜");
        coin(provider, CDItems.GOLD, "金币", "金");
        coin(provider, CDItems.EMERALD, "绿宝石币", "绿");
        coin(provider, CDItems.NETHERITE, "下界合金币", "下界");
        provider.addBlock(CDBlocks.IRON, "铁币堆");
        provider.addBlock(CDBlocks.COPPER, "铜币堆");
        provider.addBlock(CDBlocks.GOLD, "金币堆");
        provider.addBlock(CDBlocks.EMERALD, "绿宝石币堆");
        provider.addBlock(CDBlocks.NETHERITE, "下界合金币堆");
        //molten fluid
        addFluid(provider, CDFluids.MOLTEN_ANDESITE, "熔融安山合金");
        addFluid(provider, CDFluids.MOLTEN_AZURE_NEODYMIUM, "熔融青钕合金");
        addFluid(provider, CDFluids.MOLTEN_SCARLET_NEODYMIUM, "熔融赤钕合金");
        addFluid(provider, CDFluids.MOLTEN_DESH, "熔融戴斯");
        addFluid(provider, CDFluids.MOLTEN_OSTRUM, "熔融紫金");
        addFluid(provider, CDFluids.MOLTEN_CLAORITE, "熔融耐热金属");
        addFluid(provider, CDFluids.MOLTEN_FIRE_STEEL, "熔融龙炎钢");
        addFluid(provider, CDFluids.MOLTEN_ICE_STEEL, "熔融龙霜钢");
        addFluid(provider, CDFluids.MOLTEN_LIGHTNING_STEEL, "熔融龙霆钢");
        addFluid(provider, CDFluids.MOLTEN_FORGED_STEEL, "熔融锻造钢");
        //ice cream
        addFluid(provider, CDFluids.ADZUKI_ICE_CREAM, "红豆冰淇淋");
        addFluid(provider, CDFluids.BANANA_ICE_CREAM, "香蕉冰淇淋");
        addFluid(provider, CDFluids.CHOCOLATE_ICE_CREAM, "巧克力冰淇淋");
        addFluid(provider, CDFluids.MINT_ICE_CREAM, "薄荷冰淇淋");
        addFluid(provider, CDFluids.STRAWBERRY_ICE_CREAM, "草莓冰淇淋");
        addFluid(provider, CDFluids.VANILLA_ICE_CREAM, "原味冰淇淋");
        addFluid(provider, CDFluids.LIME_ICE_CREAM, "青柠冰淇淋");
        addFluid(provider, CDFluids.POMEGRANATE_ICE_CREAM, "石榴冰淇淋");
        addFluid(provider, CDFluids.SWEETBERRY_ICE_CREAM, "甜浆果冰淇淋");
        //ice cream scoop
        provider.addItem(CDItems.STRAWBERRY_ICE_CREAM_SCOOP, "草莓冰淇淋球");
        provider.addItem(CDItems.BANANA_ICE_CREAM_SCOOP, "香蕉冰淇淋球");
        provider.addItem(CDItems.MINT_ICE_CREAM_SCOOP, "薄荷冰淇淋球");
        provider.addItem(CDItems.ADZUKI_ICE_CREAM_SCOOP, "红豆冰淇淋球");
        provider.addItem(CDItems.POMEGRANATE_ICE_CREAM_SCOOP, "石榴冰淇淋球");
        provider.addItem(CDItems.LIME_ICE_CREAM_SCOOP, "青柠冰淇淋球");
        //ice-cream-item
        provider.addItem(CDItems.SWEETBERRY_ICE_CREAM, "甜浆果冰淇淋");
        //milkshake
        provider.addItem(CDItems.SWEETBERRY_MILKSHAKE, "甜浆果奶昔");
        //slime
        addFluid(provider, CDFluids.SLIME, "黏液");
        addFluid(provider, CDFluids.FERROUSLIME, "富铁黏液");
        //radiation
        addFluid(provider, CDFluids.NUCLEAR_WASTE, "核废液");
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
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid", "烈焰人燃烧流体");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsume", "消耗 %s mb");
        //waystone
        provider.add("gui." + CreateDelightCore.MODID + ".need", "需要：");
        provider.add("gui." + CreateDelightCore.MODID + ".free", "免费");
        //attribute
        provider.add("create.item_attributes.food_quality", "品质为%s");
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
