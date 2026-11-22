package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Startup definitions for items whose full contents are independent of item NBT. */
public final class ItemFluidContainers {
    private static final Map<ResourceLocation, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static volatile Map<Item, Rule> rules = Map.of();
    private static boolean frozen;

    private ItemFluidContainers() {}

    public static void register(String itemId, String fluidId, double amount, String emptyContainerId) {
        register(itemId, fluidId, amount, emptyContainerId, false);
    }

    public static void registerIfPresent(String itemId, String fluidId, double amount, String emptyContainerId) {
        register(itemId, fluidId, amount, emptyContainerId, true);
    }

    private static synchronized void register(String itemId, String fluidId, double amount,
                                              String emptyContainerId, boolean optional) {
        var item = id(itemId);
        var fluid = id(fluidId);
        var container = id(emptyContainerId);
        if (item.equals(container) || !(amount > 0 && amount <= Integer.MAX_VALUE && amount == Math.floor(amount))) {
            throw new IllegalArgumentException("Invalid fixed fluid container for " + item + ": " + amount + " mB, empty " + container);
        }
        var definition = new Definition(fluid, (int) amount, container, optional);
        Definition previous = DEFINITIONS.get(item);
        if (previous != null && !previous.matches(definition)) {
            throw new IllegalArgumentException("Conflicting fixed fluid container for " + item + ": " + previous + " / " + definition);
        }
        if (frozen) {
            Item registeredItem = ForgeRegistries.ITEMS.getValue(item);
            if (previous != null && (optional || registeredItem != null && rules.containsKey(registeredItem))) return;
            throw new IllegalStateException("Fixed fluid container registration is closed; restart to change " + item);
        }
        if (previous == null || !optional) DEFINITIONS.put(item, definition);
    }

    private static ResourceLocation id(String value) {
        ResourceLocation id = value == null ? null : ResourceLocation.tryParse(value);
        if (id == null || id.getPath().isEmpty() || value.startsWith(":")) {
            throw new IllegalArgumentException("Invalid fixed fluid container resource ID: " + value);
        }
        return id;
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        // KubeJS records IDs during startup. Wait for every mod's load-complete listener before resolving them.
        event.enqueueWork(ItemFluidContainers::freeze);
    }

    private static synchronized void freeze() {
        Map<Item, Rule> resolved = new LinkedHashMap<>();
        DEFINITIONS.forEach((id, definition) -> {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            Item empty = ForgeRegistries.ITEMS.getValue(definition.empty);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(definition.fluid);
            if (item == null || item == Items.AIR || empty == null || empty == Items.AIR
                    || fluid == null || fluid == Fluids.EMPTY) {
                String message = "Unregistered item, fluid or empty container for " + id + ": " + definition;
                if (!definition.optional) throw new IllegalArgumentException(message);
                CreateDelightCore.LOGGER.info("Skipping optional fixed fluid container: {}", message);
                return;
            }
            resolved.put(item, new Rule(fluid, definition.amount, empty));
        });
        rules = Map.copyOf(resolved);
        frozen = true;
        resolved.keySet().forEach(item -> {
            var id = ForgeRegistries.ITEMS.getKey(item);
            checkNativeCapability(item, id, DEFINITIONS.get(id));
        });
    }

    private static void checkNativeCapability(Item item, ResourceLocation id, Definition definition) {
        // Probe a disposable stack using the same published mapping as the actual container.
        try {
            var probe = item.getDefaultInstance().copyWithCount(1);
            var handler = probe.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if (handler == null || handler instanceof FixedItemFluidHandler) return;
            FluidStack drained = handler.drain(definition.amount, FluidAction.EXECUTE);
            var returned = handler.getContainer();
            var fluid = ForgeRegistries.FLUIDS.getKey(drained.getFluid());
            var container = ForgeRegistries.ITEMS.getKey(returned.getItem());
            if (!definition.fluid.equals(fluid) || drained.getAmount() != definition.amount
                    || !definition.empty.equals(container) || returned.getCount() != 1) {
                CreateDelightCore.LOGGER.warn(
                        "Keeping existing fluid capability for {}: drained {} mB of {}, returned {}x {}; configured {} mB of {}, returning {}",
                        id, drained.getAmount(), fluid, returned.getCount(), container, definition.amount, definition.fluid, definition.empty);
            }
        } catch (RuntimeException e) {
            CreateDelightCore.LOGGER.warn("Could not probe the existing item fluid capability for {}", id, e);
        }
    }

    @Nullable
    public static Rule find(ItemStack item) {
        return item.isEmpty() ? null : findForItem(item.getItem());
    }

    @Nullable
    public static Rule findForItem(Item item) {
        return rules.get(item);
    }

    public record Rule(Fluid fluid, int amount, Item emptyContainer) {}

    private record Definition(ResourceLocation fluid, int amount, ResourceLocation empty, boolean optional) {
        boolean matches(Definition other) {
            return amount == other.amount && fluid.equals(other.fluid) && empty.equals(other.empty);
        }
    }
}
