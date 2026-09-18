package io.github.jasonsimpart.compat.mbd2;

import com.lance5057.butchercraft.workstations.hook.MeatHookBlockEntity;
import com.lance5057.butchercraft.workstations.hook.MeatHookBlock;
import com.lance5057.butchercraft.workstations.butcherblock.ButcherBlockBlockEntity;
import com.lance5057.butchercraft.workstations.butcherblock.ButcherBlockBlock;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Display-only workstations: the controller recipe owns every consumed/produced item. */
final class MbdButchery {
    private static final String DISPLAY_OWNER = "createdelightcore:butchery_display_owner";

    private MbdButchery() {}

    static void register(IEventBus bus) {
        // Register before Butchercraft's normal-priority providers. Always wrap, so
        // automation that cached the capability before processing is guarded too.
        bus.addListener(EventPriority.HIGH, MbdButchery::capabilities);
        NeoForge.EVENT_BUS.addListener(MbdButchery::tick);
        NeoForge.EVENT_BUS.addListener(MbdButchery::removed);
        NeoForge.EVENT_BUS.addListener(MbdButchery::invalid);
        NeoForge.EVENT_BUS.addListener(MbdButchery::interact);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                com.lance5057.butchercraft.ButchercraftBlockEntities.MEAT_HOOK.get(),
                (block, side) -> new DisplayInventory(block, block.getHandler()));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                com.lance5057.butchercraft.ButchercraftBlockEntities.BUTCHER_BLOCK.get(),
                (block, side) -> new DisplayInventory(block, block.getHandler()));
    }

    private record DisplayInventory(BlockEntity block, IItemHandlerModifiable inventory) implements IItemHandlerModifiable {
        private boolean display() { return block.getPersistentData().contains(DISPLAY_OWNER); }
        @Override public int getSlots() { return inventory.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return display() ? ItemStack.EMPTY : inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return display() ? stack : inventory.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return display() ? ItemStack.EMPTY : inventory.extractItem(slot, amount, simulate);
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return !display() && inventory.isItemValid(slot, stack); }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (!display()) inventory.setStackInSlot(slot, stack);
        }
    }

    private static boolean matches(MBDMachine machine) {
        return !machine.isRemote() && machine.getDefinition().id().equals(MbdCompat.id("butchery_room"));
    }

    private static MeatHookBlockEntity hook(MBDMachine machine) {
        return hook(machine, machine.getFrontFacing().orElse(Direction.NORTH));
    }

    private static MeatHookBlockEntity hook(MBDMachine machine, Direction facing) {
        var block = machine.getLevel().getBlockEntity(machine.getPos().relative(facing.getOpposite()).above(3)
                .relative(facing.getCounterClockWise()));
        return block instanceof MeatHookBlockEntity hook ? hook : null;
    }

    private static ButcherBlockBlockEntity table(MBDMachine machine) {
        return table(machine, machine.getFrontFacing().orElse(Direction.NORTH));
    }

    private static ButcherBlockBlockEntity table(MBDMachine machine, Direction facing) {
        var block = machine.getLevel().getBlockEntity(machine.getPos().relative(facing.getOpposite()).above());
        return block instanceof ButcherBlockBlockEntity table ? table : null;
    }

    private static void clear(MBDMachine machine) {
        // Rotation can update the controller facing before invalidation is dispatched.
        for (var facing : Direction.Plane.HORIZONTAL) {
            clear(hook(machine, facing), machine);
            clear(table(machine, facing), machine);
        }
    }

    private static void clear(BlockEntity block, MBDMachine machine) {
        if (!owned(block, machine)) return;
        block.getPersistentData().remove(DISPLAY_OWNER);
        if (block instanceof MeatHookBlockEntity hook) hook.finishRecipe();
        if (block instanceof ButcherBlockBlockEntity table) table.finishRecipe();
        sync(block);
    }

    private static boolean owned(BlockEntity block, MBDMachine machine) {
        return block != null && block.getPersistentData().contains(DISPLAY_OWNER, net.minecraft.nbt.Tag.TAG_LONG)
                && block.getPersistentData().getLong(DISPLAY_OWNER) == machine.getPos().asLong();
    }

    private static void markDisplay(BlockEntity block, MBDMachine machine) {
        block.getPersistentData().putLong(DISPLAY_OWNER, machine.getPos().asLong());
        block.setChanged();
    }

    private static void interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        // Both workstations forward hits on their dummy blocks to the inventory root.
        if (state.getBlock() instanceof MeatHookBlock) pos = pos.above(state.getValue(MeatHookBlock.DUMMY));
        else if (state.getBlock() instanceof ButcherBlockBlock) {
            if (state.getValue(ButcherBlockBlock.DUMMY)) pos = pos.below();
        } else return;
        var block = level.getBlockEntity(pos);
        if (block != null && block.getPersistentData().contains(DISPLAY_OWNER)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    private static void sync(net.minecraft.world.level.block.entity.BlockEntity block) {
        // Butchercraft updateInventory only marks the BE dirty; automation must send its packet.
        var state = block.getBlockState();
        block.getLevel().sendBlockUpdated(block.getBlockPos(), state, state, 3);
    }

    static void removed(MachineRemovedEvent event) {
        if (matches(event.machine)) clear(event.machine);
    }

    static void invalid(MachineStructureInvalidEvent event) {
        if (matches(event.machine)) clear(event.machine);
    }

    static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (!matches(machine) || machine.getLevel().getGameTime() % 20 != 0) return;
        var recipe = machine.getRecipeLogic().getLastRecipe();
        if (!"working".equals(machine.getMachineStateName()) || recipe == null) {
            clear(machine);
            return;
        }
        updateDisplay(machine, recipe);
    }

    static void updateDisplay(MBDMachine machine, com.lowdragmc.mbd2.api.recipe.MBDRecipe recipe) {
        var contents = recipe.getInputContents(ItemRecipeCapability.CAP);
        if (contents.isEmpty()) { clear(machine); return; }
        var stacks = ItemRecipeCapability.CAP.of(contents.getLast().content).getItems();
        if (stacks.length == 0) { clear(machine); return; }
        var stack = stacks[0].copyWithCount(1);
        var path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int maximum = path.equals("cow_carcass") ? 6 : path.equals("pig_carcass") ? 5
                : path.endsWith("_carcass") && !path.equals("chicken_carcass") ? 4 : 0;
        boolean advance = machine.getLevel().getGameTime() % 40 == 0;
        if (stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse("butchercraft:big_carcass")))) {
            clear(table(machine), machine);
            var hook = hook(machine);
            if (hook == null) return;
            if (!owned(hook, machine) && !hook.isEmpty()) return;
            if (!ItemStack.isSameItemSameComponents(hook.getInsertedItem(), stack) || hook.stage == maximum) {
                hook.finishRecipe();
                hook.insertItem(stack);
                markDisplay(hook, machine);
                hook.stage = 0;
            }
            if (advance && hook.stage < maximum) hook.stage++;
            hook.updateInventory();
            sync(hook);
        } else {
            clear(hook(machine), machine);
            var table = table(machine);
            if (table == null) return;
            if (!owned(table, machine) && !table.isEmpty()) return;
            if (!ItemStack.isSameItemSameComponents(table.getInsertedItem(), stack) || table.stage == maximum) {
                table.finishRecipe();
                table.insertItem(stack);
                markDisplay(table, machine);
                table.stage = 0;
            }
            if (advance && table.stage < maximum - 1) table.stage++;
            if (advance && path.equals("chicken_carcass") && table.stage < 5) table.stage += 4;
            table.updateInventory();
            sync(table);
        }
        machine.getLevel().playSound(null, machine.getPos(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 1, 1);
    }
}
