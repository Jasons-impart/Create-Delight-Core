package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineUseItemOnEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import io.github.jasonsimpart.compat.qualityfood.QualityFoodCompat;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;

final class MbdGrinding {
    private MbdGrinding() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdGrinding::tick);
        NeoForge.EVENT_BUS.addListener(MbdGrinding::use);
    }

    private static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (!MbdSmallProcessing.is(machine, "mechanic_grinding_wheel") || machine.getLevel().isClientSide
                || !(machine.getHolder() instanceof MBDKineticMachineBlockEntity kinetic)) return;
        float speed = Math.abs(kinetic.getSpeed());
        int interval = Math.max(5, (int) Math.floor((40 - 35 * speed / 256) / 5) * 5);
        if (speed == 0 || machine.getLevel().getGameTime() % interval != 0) return;
        transfer(machine);
    }

    static void transfer(MBDMachine machine) {
        var input = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_input_slot")).storage;
        var output = ((ItemSlotCapabilityTrait) machine.getTraitByName("item_output_slot")).storage;
        for (int slot = 0; slot < input.getSlots(); slot++) {
            var original = input.getStackInSlot(slot);
            if (original.isEmpty()) continue;
            var cleaned = original.copy();
            if (ModList.get().isLoaded("quality_food") && !QualityFoodCompat.clearQuality(cleaned).success()) continue;
            if (!ItemHandlerHelper.insertItemStacked(output, cleaned, true).isEmpty()) continue;
            // Simulate using the cleaned stack so differently graded food can merge correctly.
            ItemHandlerHelper.insertItemStacked(output, cleaned, false);
            input.setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
            machine.getHolder().setChanged();
        }
    }

    private static void use(MachineUseItemOnEvent event) {
        var machine = event.machine;
        if (!MbdSmallProcessing.is(machine, "mechanic_grinding_wheel")) return;
        var held = event.player.getItemInHand(event.hand);
        if (!held.isDamageableItem() || !(machine.getHolder() instanceof MBDKineticMachineBlockEntity kinetic)) return;
        event.setItemInteractionResult(ItemInteractionResult.SUCCESS);
        if (machine.getLevel().isClientSide) return;
        int damage = (int) Math.floor(Math.sqrt(Math.abs(kinetic.getSpeed())) / 4 + .5);
        if (ModList.get().isLoaded("tetra")) MbdTetraGrinding.hone(event.player, held, damage);
        held.hurtAndBreak(damage, event.player, event.hand == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        machine.getLevel().playSound(null, machine.getPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1, 1);
        event.player.swing(event.hand, true);
    }
}
