package io.github.jasonsimpart.compat.createliquidfuel;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.util.Triplet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import static io.github.jasonsimpart.CreateDelightCore.MODID;

public final class DrainableBurnerFuelLoader {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "drainable_burner_fuel_loader");
    private static final TagKey<Item> BLAZE_BURNER_FUEL_SPECIAL = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("create", "blaze_burner_fuel/special")
    );

    private DrainableBurnerFuelLoader() {
    }

    public static void load() {
        BuiltInRegistries.ITEM.forEach(item -> {
            ItemStack stack = item.getDefaultInstance();
            int burnTime = stack.getBurnTime(null);
            if (burnTime <= 0) {
                return;
            }

            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler == null || handler.getTanks() != 1) {
                return;
            }

            FluidStack fluidStack = handler.getFluidInTank(0);
            if (fluidStack.isEmpty()) {
                return;
            }

            Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>> existing =
                    BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.get(fluidStack.getFluid());
            if (existing != null && !IDENTIFIER.equals(existing.getFirst())) {
                return;
            }

            boolean superHeat = stack.is(BLAZE_BURNER_FUEL_SPECIAL);
            int amount = fluidStack.getAmount();
            int gcd = gcd(burnTime, amount);
            BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.put(
                    fluidStack.getFluid(),
                    Pair.of(
                            IDENTIFIER,
                            Triplet.of(
                                    superHeat ? 32 : burnTime / gcd,
                                    superHeat,
                                    superHeat ? 10 : amount / gcd
                            )
                    )
            );
        });
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
