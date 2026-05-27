package io.github.jasonsimpart.compat.cmr;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import static io.github.jasonsimpart.CreateDelightCore.MODID;

public final class DrainableFuelLoader {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "drainable_fuel_loader");
    private static final TagKey<Item> SNOWMAN_COOLER_FUEL_SPECIAL = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CmrCompat.MOD_ID, "snowman_cooler_fuel/special")
    );

    private DrainableFuelLoader() {
    }

    public static void load() {
        AbstractFurnaceBlockEntity.getFuel().forEach((item, burnTime) -> {
            if (burnTime <= 0) {
                return;
            }

            ItemStack stack = item.getDefaultInstance();
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler == null || handler.getTanks() != 1) {
                return;
            }

            FluidStack fluidStack = handler.getFluidInTank(0);
            if (fluidStack.isEmpty()) {
                return;
            }

            Pair<ResourceLocation, CoolerStomachHandler.LiquidCoolerFuel> existing =
                    CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.get(fluidStack.getFluid());
            if (existing != null && LiquidCoolerFuelJsonLoader.IDENTIFIER.equals(existing.getFirst())) {
                return;
            }

            boolean freezes = stack.is(SNOWMAN_COOLER_FUEL_SPECIAL);
            int amount = fluidStack.getAmount();
            int gcd = gcd(burnTime, amount);
            CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.put(
                    fluidStack.getFluid(),
                    Pair.of(
                            IDENTIFIER,
                            new CoolerStomachHandler.LiquidCoolerFuel(
                                    freezes ? 32 : burnTime / gcd,
                                    freezes,
                                    freezes ? 10 : amount / gcd
                            )
                    )
            );
        });
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
