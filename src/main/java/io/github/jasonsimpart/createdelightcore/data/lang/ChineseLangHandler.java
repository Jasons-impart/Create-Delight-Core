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
        provider.add(CDCreativeTabs.MISC.get(), "机械动力：悠然乐事 | 杂项");
        provider.add(CDCreativeTabs.COIN.get(), "机械动力：悠然乐事 | 货币");
        provider.add(CDCreativeTabs.Fluid.get(), "机械动力：悠然乐事 | 流体");
        // chocolate
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_FILLED, "盛满的黑巧克力模具");
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_SOLID, "凝固的黑巧克力");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_FILLED, "盛满的白巧克力模具");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_SOLID, "凝固的白巧克力");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_FILLED, "盛满的红宝石巧克力模具");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_SOLID, "凝固的红宝石巧克力");
        //block
        provider.addBlock(CDBlocks.FRAGMENT_OF_BORDER, "边境碎片");
        //coin
        coin(provider, CDItems.IRON_COIN, "铁币", "铁");
        coin(provider, CDItems.COPPER_COIN, "铜币", "铜");
        coin(provider, CDItems.GOLD_COIN, "金币", "金");
        coin(provider, CDItems.EMERALD_COIN, "绿宝石币", "绿");
        coin(provider, CDItems.NETHERITE_COIN, "下界合金币", "下界");
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
        //ice cream
        addFluid(provider, CDFluids.ADZUKI_ICE_CREAM, "红豆冰淇淋");
        addFluid(provider, CDFluids.BANANA_ICE_CREAM, "香蕉冰淇淋");
        addFluid(provider, CDFluids.CHOCOLATE_ICE_CREAM, "巧克力冰淇淋");
        addFluid(provider, CDFluids.MINT_ICE_CREAM, "薄荷冰淇淋");
        addFluid(provider, CDFluids.STRAWBERRY_ICE_CREAM, "草莓冰淇淋");
        addFluid(provider, CDFluids.VANILLA_ICE_CREAM, "原味冰淇淋");
        //slime
        addFluid(provider, CDFluids.SLIME, "粘液");
        //recipes
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing.fan", "在细雪后放置鼓风机");
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing", "批量冷冻");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal", "%1$s变成了熔融金属的一部分");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal.player", "%2$s以为%1$s是一块没有融化的金属");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream.player", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
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
