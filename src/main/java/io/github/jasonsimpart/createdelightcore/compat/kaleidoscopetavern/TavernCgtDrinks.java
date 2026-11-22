package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/** CGT is an optional runtime dependency of Core, so bind its public drink API only when present. */
final class TavernCgtDrinks {
    private static final String PACKAGE = "com.yision.creategearsandtavern.content.fluids.drink.";
    private static final Api API = ModList.get().isLoaded("creategearsandtavern") ? bind() : null;

    private TavernCgtDrinks() {}

    @Nullable
    static Serving serving(Item item) throws ReflectiveOperationException {
        if (API == null) return null;
        Object definition = API.byItem.invoke(null, item);
        return definition == null ? null
                : new Serving((int) API.servingAmount.invoke(definition), (Item) API.containerItem.invoke(definition));
    }

    static FluidStack fromItem(ItemStack item, int amount, int brewLevel) throws ReflectiveOperationException {
        return API == null ? FluidStack.EMPTY : (FluidStack) API.fromItem.invoke(null, item, amount, brewLevel);
    }

    @Nullable
    private static Api bind() {
        try {
            Class<?> catalog = Class.forName(PACKAGE + "CGTDrinkCatalog");
            Class<?> definition = Class.forName(PACKAGE + "CGTDrinkDefinition");
            Class<?> conversions = Class.forName(PACKAGE + "DrinkFluidConversions");
            return new Api(catalog.getMethod("byItem", Item.class), definition.getMethod("servingAmount"),
                    definition.getMethod("containerItem"), conversions.getMethod("fromItem", ItemStack.class, int.class, int.class));
        } catch (ReflectiveOperationException e) {
            CreateDelightCore.LOGGER.warn("Could not bind CGT drink conversions", e);
            return null;
        }
    }

    record Serving(int amount, Item container) {}

    private record Api(Method byItem, Method servingAmount, Method containerItem, Method fromItem) {}
}
