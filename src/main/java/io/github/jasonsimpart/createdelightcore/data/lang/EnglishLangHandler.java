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
        addVirtualFluid(provider, CDFluids.MILK_SHAKE, "Milkshake");
        //jei
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid", "Blaze Burner Fluid");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsume", "Consume %s mb");
        //waystone
        provider.add("gui." + CreateDelightCore.MODID + ".need", "Need ");
        provider.add("gui." + CreateDelightCore.MODID + ".free", "Free");
        //tooltip
        provider.add("tooltip." + CreateDelightCore.MODID + ".jelly_block", "Sticky, but not connecting to other sticky blocks.");
        provider.add("tooltip." + CreateDelightCore.MODID + ".jello_block", "Slippery. Sticks to sticky blocks and same jello/jelly blocks.");
    }
}
