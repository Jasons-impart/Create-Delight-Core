package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.api.recipe.content.ContentModifier;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.machine.MBDPartMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import io.github.jasonsimpart.util.ModIds;

/** Legacy reactor thermodynamics and protection, independent of pack recipe definitions. */
final class MbdReactor {
    private MbdReactor() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdReactor::formed);
        NeoForge.EVENT_BUS.addListener(MbdReactor::tick);
        NeoForge.EVENT_BUS.addListener(MbdReactor::beforeModify);
        NeoForge.EVENT_BUS.addListener(MbdReactor::afterModify);
        NeoForge.EVENT_BUS.addListener(MbdReactor::beforeWorking);
        NeoForge.EVENT_BUS.addListener(MbdReactor::fuelModify);
        NeoForge.EVENT_BUS.addListener(MbdReactor::afterWorking);
        NeoForge.EVENT_BUS.addListener(MbdReactor::removed);
        NeoForge.EVENT_BUS.addListener(MbdReactor::invalid);
        NeoForge.EVENT_BUS.addListener(MbdReactor::stateChanged);
        NeoForge.EVENT_BUS.addListener(MbdReactor::placed);
        NeoForge.EVENT_BUS.addListener(MbdReactorUI::opened);
    }

    static boolean isReactor(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("fission_reactor"));
    }

    static boolean isControl(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("fission_reactor_controller"));
    }

    static double data(MBDMachine machine, String key) { return machine.getCustomData().getDouble(key); }

    static void set(MBDMachine machine, String key, double value) {
        if (Double.compare(data(machine, key), value) == 0) return;
        if (key.equals("burning_rate") || key.equals("multiplier")) {
            machine.getRecipeLogic().markLastRecipeDirty();
        }
        machine.getCustomData().putDouble(key, value);
        machine.getHolder().setChanged();
    }

    static void formed(MachineStructureFormedEvent event) {
        if (!isReactor(event.machine) || !(event.machine instanceof MBDMultiblockMachine machine)) return;
        int count = (int) machine.getParts().stream().filter(part -> part instanceof MBDMachine m
                && m.getDefinition().id().equals(MbdCompat.id("fission_fuel_assembly"))).count();
        machine.getCustomData().putInt("assembly_count", count);
        if (data(machine, "temperature") == 0) set(machine, "temperature", 298.15);
    }

    static double assemblies(MBDMachine machine) {
        return machine.getCustomData().getInt("assembly_count") * data(machine, "burning_rate");
    }

    static boolean active(MBDMachine machine) {
        return machine.getRecipeLogic().getFuelTime() > 0 && data(machine, "burning_rate") != 0;
    }

    static double heat(MBDMachine machine) {
        return active(machine) ? 0.08 * assemblies(machine) * Math.max(0.3, 1 - (data(machine, "temperature") - 298.15) * 0.0005) : 0;
    }

    static IFluidHandler fluids(MBDMachine machine) {
        return machine.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, machine.getPos(), null);
    }

    static double coolantEfficiency(MBDMachine machine) {
        var recipe = machine.getRecipeLogic().getLastRecipe();
        var fluids = fluids(machine);
        if (!machine.getMachineStateName().equals("working") || recipe == null
                || recipe.id.equals(MbdCompat.id("fission_react/empty")) || fluids == null) return 0;
        var fluid = BuiltInRegistries.FLUID.getKey(fluids.getFluidInTank(0).getFluid());
        if (fluid.equals(ResourceLocation.parse("minecraft:water"))) return 1;
        return fluid.equals(ResourceLocation.parse("netherexp:ectoplasm")) ? 1.5 : 0;
    }

    static double cooling(MBDMachine machine) { return 0.08 * coolantEfficiency(machine) * assemblies(machine) * 0.3; }
    static double ambient(MBDMachine machine) { return 0.05 + (data(machine, "temperature") - 298.15) * 0.0001; }
    static double amount(MBDMachine machine, double base, boolean input) {
        double multiplier = data(machine, "multiplier");
        return active(machine) ? base * (input && multiplier == 0 ? 1 : multiplier) * Math.pow(1.0415, assemblies(machine)) : 0;
    }

    static void tick(MachineTickEvent event) {
        var machine = event.machine;
        if (machine.getLevel().isClientSide || machine.getLevel().getGameTime() % 3 != 0) return;
        if (isControl(machine)) { control(machine); return; }
        if (!isReactor(machine)) return;
        updateTemperature(machine);
    }

    static void updateTemperature(MBDMachine machine) {
        double temp = data(machine, "temperature"), damage = data(machine, "degree_of_damage");
        double newTemp = Math.max(298.15, temp + 3 * (heat(machine) - cooling(machine) - ambient(machine)));
        double addedDamage = newTemp >= 1500 ? 0.03 * machine.getCustomData().getInt("assembly_count") : 0;
        long time = machine.getLevel().getGameTime();
        if (newTemp >= 1500 && damage > 50 && time >= machine.getCustomData().getLong("next_siren_tick")) {
            var siren = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("alexscavesup:nuclear_siren"));
            if (siren != null) machine.getLevel().playSound(null, machine.getPos(), siren, SoundSource.BLOCKS, 1, 1);
            machine.getCustomData().putLong("next_siren_tick", time + 100);
            machine.getHolder().setChanged();
        }
        set(machine, "temperature", newTemp);
        set(machine, "degree_of_damage", Math.max(0, damage + addedDamage - 0.015 * coolantEfficiency(machine) * assemblies(machine)));
        // The old script deliberately samples the previous temperature for this interval.
        set(machine, "multiplier", temp < 1000 ? 0 : temp <= 1500 ? 1 + 2 * (temp - 1000) / 500 : 3);
        if (damage >= 100 || newTemp >= 2500) explode(machine);
    }

    static void beforeModify(MachineRecipeModifyEvent.Before event) {
        if (isReactor(event.machine) && event.recipe != null) event.setRecipe(event.recipe.copy(
                ContentModifier.multiplier(Math.pow(1.0415, assemblies(event.machine))), false, IO.BOTH));
    }

    static void afterModify(MachineRecipeModifyEvent.After event) {
        if (!isReactor(event.machine) || event.recipe == null) return;
        double multiplier = data(event.machine, "multiplier");
        if (multiplier == 0) {
            // NeoForge SizedFluidIngredient forbids zero-sized stacks. Removing outputs
            // preserves the old cold-reactor behavior without constructing invalid fluid data.
            var cold = event.recipe.copy();
            cold.outputs.clear();
            event.setRecipe(cold);
        } else event.setRecipe(event.recipe.copy(ContentModifier.multiplier(multiplier), false, IO.BOTH));
    }

    static void beforeWorking(MachineBeforeRecipeWorkingEvent event) {
        if (isReactor(event.machine) && data(event.machine, "burning_rate") == 0) event.setCanceled(true);
    }

    static void fuelModify(MachineFuelRecipeModifyEvent event) {
        if (isReactor(event.machine) && data(event.machine, "burning_rate") == 0) event.setCanceled(true);
    }

    static void afterWorking(MachineAfterRecipeWorkingEvent event) {
        var machine = event.machine;
        if (!isReactor(machine) || !event.recipe.id.equals(MbdCompat.id("fission_react/empty"))) return;
        var fluids = fluids(machine);
        var energy = machine.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, machine.getPos(), null);
        if (fluids != null && fluids.getTanks() >= 2 && energy != null
                && !fluids.getFluidInTank(0).isEmpty() && fluids.getFluidInTank(0).getAmount() >= amount(machine, 20, true)
                && fluids.getFluidInTank(1).getAmount() + amount(machine, 20, false) <= fluids.getTankCapacity(1)
                && energy.getEnergyStored() + amount(machine, 40960, false) <= energy.getMaxEnergyStored()) {
            machine.getRecipeLogic().markLastRecipeDirty();
        }
    }

    static void removed(MachineRemovedEvent event) { if (isReactor(event.machine) && event.machine.getMachineStateName().equals("working")) explode(event.machine); }
    static void invalid(MachineStructureInvalidEvent event) { if (isReactor(event.machine) && event.machine.getMachineStateName().equals("working")) explode(event.machine); }
    static void stateChanged(MachineStateChangedEvent event) {
        if (isReactor(event.machine) && event.machine instanceof MBDMultiblockMachine machine
                && event.oldState.equals("working") && event.newState.equals("base") && !machine.isFormed()) explode(machine);
    }

    static void explode(MBDMachine machine) {
        if (machine.getLevel().isClientSide || machine.getCustomData().getBoolean("meltdown_started")) return;
        if (!ModList.get().isLoaded(ModIds.ALEXSCAVES)) return;
        machine.getCustomData().putBoolean("meltdown_started", true);
        NuclearExplosion.spawn(machine);
    }

    private static final class NuclearExplosion {
        static void spawn(MBDMachine machine) {
            var explosion = com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry.NUCLEAR_EXPLOSION.get().create(machine.getLevel());
            if (explosion == null) return;
            explosion.setPos(machine.getPos().getCenter());
            explosion.setSize((float) Math.sqrt(machine.getCustomData().getInt("assembly_count") / 9.0 + 1));
            machine.getLevel().addFreshEntity(explosion);
        }
    }

    static int signal(MBDMachine machine) {
        var facing = machine.getFrontFacing().orElse(Direction.NORTH);
        var pos = machine.getPos().relative(facing);
        return machine.getLevel().getBlockState(pos).getSignal(machine.getLevel(), pos, facing);
    }

    static void placed(MachinePlacedEvent event) {
        if (isControl(event.machine)) {
            event.machine.getCustomData().putInt("last_signal", signal(event.machine));
            event.machine.getHolder().setChanged();
        }
    }

    static void control(MBDMachine machine) {
        int state = machine.getCustomData().getInt("state"), power = 0;
        if (state != 0 && machine instanceof MBDPartMachine part && !part.getControllers().isEmpty()
                && part.getControllers().getFirst() instanceof MBDMachine reactor && isReactor(reactor)) {
            switch (state) {
                case 1 -> {
                    int current = signal(machine), previous = machine.getCustomData().getInt("last_signal");
                    if (current > 0 && previous == 0) set(reactor, "burning_rate", data(reactor, "burning_rate") == 0 ? 1 : 0);
                    if (current != previous) {
                        machine.getCustomData().putInt("last_signal", current);
                        machine.getHolder().setChanged();
                    }
                }
                case 2 -> power = data(reactor, "temperature") >= 1500 ? 15 : 0;
                case 3 -> {
                    var fluids = fluids(reactor);
                    if (fluids != null && fluids.getTanks() >= 2 && fluids.getFluidInTank(1).getAmount() + amount(reactor, 20, false) >= fluids.getTankCapacity(1)) power = 15;
                }
                case 4 -> {
                    var energy = reactor.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, reactor.getPos(), null);
                    if (energy != null && energy.getEnergyStored() + amount(reactor, 40960, false) >= energy.getMaxEnergyStored()) power = 15;
                }
                case 5 -> {
                    var items = reactor.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, reactor.getPos(), null);
                    if (items != null && items.getSlots() > 0 && items.getStackInSlot(0).isEmpty()) power = 15;
                }
            }
        }
        machine.setOutputSignal(power, machine.getFrontFacing().orElse(Direction.NORTH));
    }
}
