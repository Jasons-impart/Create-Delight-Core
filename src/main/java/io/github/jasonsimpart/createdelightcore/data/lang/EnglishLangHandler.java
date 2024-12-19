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

    public static void init(RegistrateLangProvider provider) {
        provider.add(CDCreativeTabs.MISC.get(), "Create Delight | Misc");
        provider.add(CDCreativeTabs.COIN.get(), "Create Delight | Coin");
        provider.add(CDCreativeTabs.Fluid.get(), "Create Delight | Fluid");

        coin(provider, CDItems.IRON_COIN, "Iron Coin", "Iron");
        coin(provider, CDItems.COPPER_COIN, "Copper Coin", "Copper");
        coin(provider, CDItems.GOLD_COIN, "Gold Coin", "Gold");
        coin(provider, CDItems.EMERALD_COIN, "Emerald Coin", "Emerald");
        coin(provider, CDItems.NETHERITE_COIN, "Netherite Coin", "Netherite");

        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal", "%1$s became a part of the molten metal");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal.player", "%2$s thought that %1$s was a piece of unmelted metal");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream", "%1$s was frozen into a popsicle because of eating too much ice cream");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream.player", "%1$s was frozen into a popsicle because of eating too much ice cream");

        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing.fan", "Fan behind Powdered Snow");
        provider.add(CreateDelightCore.MODID + ".recipe.fan_freezing", "Bulk Freezing");
    }
}
