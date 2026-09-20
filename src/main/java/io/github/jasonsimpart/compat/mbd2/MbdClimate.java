package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineRemovedEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.teamtea.eclipticseasons.common.core.SolarHolders;
import com.teamtea.eclipticseasons.common.core.crop.HumidityControlProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;

/** Loaded only when Ecliptic Seasons is present. */
final class MbdClimate {
    private record Field(BlockPos owner, HumidityControlProvider provider) {}
    private static final java.util.Map<net.minecraft.world.level.Level, java.util.Map<BlockPos, Field>> FIELDS = new java.util.WeakHashMap<>();
    private static final java.util.Map<net.minecraft.world.level.Level, Long> CLEANED_AT = new java.util.WeakHashMap<>();
    private MbdClimate() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdClimate::tick);
        NeoForge.EVENT_BUS.addListener(MbdClimate::removed);
    }

    static boolean sprinkler(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("sprinkler"));
    }

    static boolean dryer(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("dryer"));
    }

    private static BlockPos anchor(MBDMachine machine) {
        if (dryer(machine)) return machine.getPos();
        for (int distance = 1; distance < 10; distance++) {
            var pos = machine.getPos().below(distance);
            if (machine.getLevel().isOutsideBuildHeight(pos)) break;
            if (!machine.getLevel().getBlockState(pos).isAir()) return pos;
        }
        return null;
    }

    private static void release(MBDMachine machine, BlockPos anchor) {
        var fields = FIELDS.get(machine.getLevel());
        var field = fields == null ? null : fields.get(anchor);
        if (field == null || !field.owner().equals(machine.getPos())) return;
        var data = SolarHolders.getSaveData(machine.getLevel());
        if (data != null && data.queryHumidityControlProvider(anchor) == field.provider()) data.removeHumidityControlProvider(anchor);
        fields.remove(anchor);
    }

    private static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (!(machine.getLevel() instanceof ServerLevel level) || (!sprinkler(machine) && !dryer(machine))) return;
        boolean drying = dryer(machine);
        boolean active = drying
                ? machine.getHolder() instanceof MBDKineticMachineBlockEntity kinetic && Math.abs(kinetic.getSpeed()) > 8
                : machine.getMachineStateName().equals("working");
        if (level.getGameTime() % 100 == 0) update(machine, active);
        if (active && level.getGameTime() % (drying ? 60 : 40) == 0) {
            var pos = machine.getPos();
            level.sendParticles(drying ? ParticleTypes.POOF : ParticleTypes.FALLING_WATER,
                    pos.getX() + .5, pos.getY() + (drying ? .5 : -.1), pos.getZ() + .5,
                    drying ? 3 : 20, .5, .5, .5, drying ? .2 : .1);
        }
    }

    static void update(MBDMachine machine, boolean active) {
        var data = SolarHolders.getSaveData(machine.getLevel());
        if (data == null) return;
        var pos = anchor(machine);
        var custom = machine.getCustomData();
        if (custom.contains("humidityAnchor")) {
            var previous = BlockPos.of(custom.getLong("humidityAnchor"));
            if (!previous.equals(pos)) {
                release(machine, previous);
                custom.remove("humidityAnchor");
                machine.getHolder().setChanged();
            }
        }
        if (pos == null) return;
        if (!active) {
            // Dryers stop refreshing; their humidity field expires naturally as in the old pack.
            if (sprinkler(machine) && machine.getMachineStateName().equals("base")) release(machine, pos);
            return;
        }
        var fields = FIELDS.computeIfAbsent(machine.getLevel(), ignored -> new java.util.HashMap<>());
        // Expired providers need no ownership entry, including machines in unloaded chunks.
        long cleanupInterval = machine.getLevel().getGameTime() / 100;
        if (!java.util.Objects.equals(CLEANED_AT.put(machine.getLevel(), cleanupInterval), cleanupInterval))
            fields.entrySet().removeIf(entry -> data.queryHumidityControlProvider(entry.getKey()) != entry.getValue().provider());
        var existing = data.queryHumidityControlProvider(pos);
        var owned = fields.get(pos);
        if (owned != null && owned.provider() != existing) { fields.remove(pos); owned = null; }
        if (existing != null && (owned == null || !owned.owner().equals(machine.getPos()))) return;
        if (existing == null) {
            var provider = new HumidityControlProvider(dryer(machine) ? -1 : 1, 4, 500);
            data.addHumidityControlProvider(pos, provider);
            fields.put(pos.immutable(), new Field(machine.getPos().immutable(), provider));
        } else if (existing.getRemainTime() <= 200) existing.addRemainTime(100);
        if (!custom.contains("humidityAnchor") || custom.getLong("humidityAnchor") != pos.asLong()) {
            custom.putLong("humidityAnchor", pos.asLong());
            machine.getHolder().setChanged();
        }
    }

    private static void removed(MachineRemovedEvent event) {
        var machine = event.machine;
        if (machine.getLevel().isClientSide || (!sprinkler(machine) && !dryer(machine))) return;
        if (machine.getCustomData().contains("humidityAnchor"))
            release(machine, BlockPos.of(machine.getCustomData().getLong("humidityAnchor")));
    }
}
