package io.github.jasonsimpart.createdelightcore.compat.cmr;

import com.mojang.datafixers.util.Pair;
import fr.iglee42.cmr.init.CMRTags;
import io.github.jasonsimpart.createdelightcore.util.MathUtil;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import static io.github.jasonsimpart.createdelightcore.CreateDelightCore.MODID;

public class DrainableFuelLoader {
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "drainable_fuel_loader");
    public static void load() {
        ForgeHooks.updateBurns();

        ForgeRegistries.ITEMS.forEach(item -> {
            ItemStack stack = item.getDefaultInstance();

            int burnTime = ForgeHooks.getBurnTime(stack, null);
            if (burnTime <= 0)
                return;

            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
                if (handler.getTanks() != 1)
                    return;

                boolean freezes = CMRTags.CMRItemTags.SNOWMAN_COOLER_FUEL_SPECIAL.matches(stack);

                int amount = handler.getFluidInTank(0).getAmount();

                if (CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.get(handler.getFluidInTank(0).getFluid()) != null
                && CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.get(handler.getFluidInTank(0).getFluid()).getFirst().equals(LiquidCoolerFuelJsonLoader.IDENTIFIER))
                    return;

                // JSON can override drainable fuel values

                CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.put(
                        handler.getFluidInTank(0).getFluid(),
                        Pair.of(
                                IDENTIFIER,
                                Triplet.of(
                                        freezes ? 32 : burnTime / MathUtil.gcd(burnTime, amount),
                                        CMRTags.CMRItemTags.SNOWMAN_COOLER_FUEL_SPECIAL.matches(stack),
                                        freezes ? 10 : amount / MathUtil.gcd(burnTime, amount)
                                )
                        )
                );
            });
        });
    }
}
