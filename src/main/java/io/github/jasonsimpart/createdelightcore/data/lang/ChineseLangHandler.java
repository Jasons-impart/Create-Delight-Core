package io.github.jasonsimpart.createdelightcore.data.lang;

import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTab;
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

    public static void init(RegistrateCNLangProvider provider) {
        provider.add(CDCreativeTab.CREATE_DELIGHT_TAB.get(), "机械动力：悠然乐事");

        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_FILLED, "盛满的黑巧克力模具");
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_SOLID, "凝固的黑巧克力");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_FILLED, "盛满的白巧克力模具");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_SOLID, "凝固的白巧克力");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_FILLED, "盛满的红宝石巧克力模具");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_SOLID, "凝固的红宝石巧克力");

        coin(provider, CDItems.IRON_COIN, "铁币", "铁");
        coin(provider, CDItems.COPPER_COIN, "铜币", "铜");
        coin(provider, CDItems.GOLD_COIN, "金币", "金");
        coin(provider, CDItems.EMERALD_COIN, "绿宝石币", "绿");
        coin(provider, CDItems.NETHERITE_COIN, "下界合金币", "下界");
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
