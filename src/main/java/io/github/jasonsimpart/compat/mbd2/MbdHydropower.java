package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineStructureFormedEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineStructureInvalidEvent;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import net.minecraft.core.BlockPos;

/** The original hydro controller drives its fan parts at a fixed -32 RPM. */
final class MbdHydropower {
    private MbdHydropower() {}

    static void formed(MachineStructureFormedEvent event) {
        if (!(event.machine instanceof com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine machine)
                || !machine.getDefinition().id().equals(MbdCompat.id("hydropower_station"))) return;
        var positions = new java.util.ArrayList<Long>();
        for (var part : machine.getParts()) {
            if (machine.getLevel().getBlockEntity(part.getPos()) instanceof MBDKineticMachineBlockEntity fan) {
                fan.stopWorking();
                fan.scheduleWorkingRPM(-32, false);
                positions.add(part.getPos().asLong());
            }
        }
        machine.getCustomData().putLongArray("hydro_fans", positions);
    }

    static void invalid(MachineStructureInvalidEvent event) {
        var machine = event.machine;
        if (!machine.getDefinition().id().equals(MbdCompat.id("hydropower_station"))) return;
        // Parts are already detached by the event; retain the positions across save/load.
        for (long position : machine.getCustomData().getLongArray("hydro_fans")) {
            if (machine.getLevel().getBlockEntity(BlockPos.of(position)) instanceof MBDKineticMachineBlockEntity fan) {
                fan.stopWorking();
            }
        }
        machine.getCustomData().remove("hydro_fans");
    }
}
