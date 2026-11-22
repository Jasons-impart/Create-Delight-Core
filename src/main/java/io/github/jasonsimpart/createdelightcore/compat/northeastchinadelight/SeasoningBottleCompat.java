package io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight;

import com.gunmu.northeast_china_delight.item.ModItems;
import io.github.jasonsimpart.createdelightcore.content.item.SeasoningBottleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

public final class SeasoningBottleCompat {
    public static final int COOKING_DOSE = 50;
    public static final int GRILL_DOSE = 10;

    private static final String NORTHEAST = "northeast_china_delight";

    private SeasoningBottleCompat() {}

    public static boolean isBottle(ItemStack stack) {
        return stack.getItem() instanceof SeasoningBottleItem;
    }

    public static boolean hasAmount(ItemStack stack, int amount) {
        return SeasoningBottleItem.contents(stack).getAmount() >= amount;
    }

    public static ItemStack asIngredient(ItemStack stack) {
        return asIngredient(stack, SeasoningBottleItem.CAPACITY);
    }

    /** Gives native recipes an ingredient identity, optionally limited to the amount deposited. */
    public static ItemStack asIngredient(ItemStack stack, int maxAmount) {
        if (!isBottle(stack)) {
            return stack;
        }
        FluidStack fluid = SeasoningBottleItem.contents(stack);
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
        if (fluidId == null) {
            return ItemStack.EMPTY;
        }
        String ingredient = switch (fluidId.toString()) {
            case "northeast_china_delight:soy_sauce", "youkaishomecoming:soy_sauce" -> "soy_sauce";
            case "northeast_china_delight:vinegar", "createdelight:vinegar" -> "vinegar";
            case "northeast_china_delight:vegetable_oil", "createdieselgenerators:plant_oil" -> "cooking_oil";
            default -> NORTHEAST.equals(fluidId.getNamespace()) ? fluidId.getPath() : "";
        };
        if (ingredient.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(NORTHEAST, ingredient));
        if (!(item instanceof com.gunmu.northeast_china_delight.item.SeasoningBottleItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack nativeStack = new ItemStack(item);
        nativeStack.setDamageValue(SeasoningBottleItem.CAPACITY - Math.min(maxAmount, fluid.getAmount()));
        return nativeStack;
    }

    public static int cookingPotAmount(ItemStack ingredient, ItemStack result) {
        return result.getItem() instanceof com.gunmu.northeast_china_delight.item.SeasoningBottleItem
                || (ingredient.is(ModItems.DEEP_FRY_OILS) && result.is(ModItems.DEEP_FRIED_DISHES))
                ? SeasoningBottleItem.CAPACITY : COOKING_DOSE;
    }

    public static void consume(ItemStack stack, int amount) {
        FluidUtil.getFluidHandler(stack).ifPresent(handler -> {
            if (handler.drain(amount, FluidAction.SIMULATE).getAmount() == amount) {
                handler.drain(amount, FluidAction.EXECUTE);
            }
        });
    }

    public static ItemStack remainder(ItemStack stack, int amount) {
        ItemStack remainder = stack.copyWithCount(1);
        consume(remainder, amount);
        return remainder;
    }
}
