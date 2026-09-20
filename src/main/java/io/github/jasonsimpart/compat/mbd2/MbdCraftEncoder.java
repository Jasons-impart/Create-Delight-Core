package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineNeighborChangedEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineUIEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Comparator;

/** Encodes ingredients and their grid positions into a Create package atomically. */
final class MbdCraftEncoder {
    private MbdCraftEncoder() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdCraftEncoder::tick);
        NeoForge.EVENT_BUS.addListener(MbdCraftEncoder::neighbor);
        NeoForge.EVENT_BUS.addListener(MbdCraftEncoder::ui);
    }

    private static boolean matches(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("mechanical_craft_encoder"));
    }

    private static void tick(MachineTickEvent event) {
        if (matches(event.machine) && event.machine.getLevel().getGameTime() % 30 == 0) powered(event.machine);
    }

    private static void neighbor(MachineNeighborChangedEvent event) {
        if (matches(event.machine)) powered(event.machine);
    }

    private static void powered(MBDMachine machine) {
        if (!machine.getLevel().isClientSide && machine.getLevel().hasNeighborSignal(machine.getPos())) encode(machine);
    }

    static int width(MBDMachine machine) {
        int value = machine.getCustomData().getInt("width");
        return value >= 1 && value <= 9 ? value : 5;
    }

    static boolean encode(MBDMachine machine) {
        net.minecraft.world.item.crafting.RecipeType<MechanicalCraftingRecipe> type = AllRecipeTypes.MECHANICAL_CRAFTING.getType();
        return encode(machine, machine.getLevel().getRecipeManager().getAllRecipesFor(type));
    }

    static boolean encode(MBDMachine machine, java.util.List<net.minecraft.world.item.crafting.RecipeHolder<MechanicalCraftingRecipe>> available) {
        var input = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_input_slot")).storage;
        var output = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_output_slot")).storage;
        var filter = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_filter_slot")).storage.getStackInSlot(0);
        int gridWidth = width(machine);
        var recipes = available.stream()
                .filter(holder -> holder.value().getWidth() <= gridWidth)
                .filter(holder -> filter.isEmpty() || ItemStack.isSameItem(filter,
                        holder.value().getResultItem(machine.getLevel().registryAccess())))
                .sorted(Comparator.<net.minecraft.world.item.crafting.RecipeHolder<MechanicalCraftingRecipe>>comparingInt(
                        holder -> gridWidth - holder.value().getWidth()).thenComparing(holder -> holder.id().toString()))
                .toList();
        for (var holder : recipes) {
            var recipe = holder.value();
            var remaining = new ItemStack[input.getSlots()];
            for (int slot = 0; slot < remaining.length; slot++) remaining[slot] = input.getStackInSlot(slot).copy();
            int[] required = recipe.getIngredients().stream().mapToInt(ingredient -> ingredient.isEmpty() ? 0 : 1).toArray();
            var allocation = io.github.jasonsimpart.util.ItemAllocation.allocate(java.util.Arrays.asList(remaining), required,
                    (entry, stack) -> recipe.getIngredients().get(entry).test(stack));
            if (allocation == null) continue;
            var grid = new ArrayList<BigItemStack>();
            var contents = new ItemStackHandler(81);
            boolean missing = false;
            for (int row = 0; row < recipe.getHeight() && !missing; row++) {
                for (int col = 0; col < gridWidth; col++) {
                    var stack = ItemStack.EMPTY;
                    if (col < recipe.getWidth()) {
                        var ingredient = recipe.getIngredients().get(row * recipe.getWidth() + col);
                        if (!ingredient.isEmpty()) {
                            for (int slot = 0; slot < remaining.length; slot++) {
                                if (allocation[row * recipe.getWidth() + col][slot] > 0) {
                                    stack = remaining[slot].copyWithCount(1);
                                    remaining[slot].shrink(1);
                                    break;
                                }
                            }
                            if (stack.isEmpty()) { missing = true; break; }
                            if (!ItemHandlerHelper.insertItemStacked(contents, stack, false).isEmpty()) {
                                missing = true;
                                break;
                            }
                        }
                    }
                    grid.add(new BigItemStack(stack));
                }
            }
            if (missing) continue;
            while (grid.size() < 9) grid.add(new BigItemStack(ItemStack.EMPTY));
            var parcel = PackageItem.containing(contents);
            PackageItem.addOrderContext(parcel, PackageOrderWithCrafts.singleRecipe(grid));
            if (!ItemHandlerHelper.insertItemStacked(output, parcel, true).isEmpty()) return false;
            for (int slot = 0; slot < remaining.length; slot++) input.setStackInSlot(slot, remaining[slot]);
            ItemHandlerHelper.insertItemStacked(output, parcel, false);
            machine.getHolder().setChanged();
            return true;
        }
        return false;
    }

    private static void ui(MachineUIEvent event) {
        if (!matches(event.machine) || event.ui == null) return;
        event.ui.selectId("width", TextField.class).forEach(field -> {
            field.setNumbersOnlyInt(1, 9);
            field.bind(DataBindingBuilder.string(() -> Integer.toString(width(event.machine)), text -> {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 1 || value > 9) return;
                    event.machine.getCustomData().putInt("width", value);
                    event.machine.getHolder().setChanged();
                } catch (NumberFormatException ignored) { }
            }).build());
        });
    }
}
