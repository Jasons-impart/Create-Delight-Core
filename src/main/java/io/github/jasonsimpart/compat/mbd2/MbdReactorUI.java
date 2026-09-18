package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineUIEvent;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleSupplier;

final class MbdReactorUI {
    private MbdReactorUI() {}

    static void opened(MachineUIEvent event) {
        var machine = event.machine;
        var ui = event.ui;
        if (ui == null) return;
        if (MbdReactor.isControl(machine)) {
            var modeGroup = new Toggle.ToggleGroup();
            var modes = new String[]{"off", "on", "high_temp", "output_fluid_choke", "output_energy_choke", "lack_of_fuel"};
            for (int i = 0; i < modes.length; i++) {
                int mode = i;
                ui.selectId(modes[i], Toggle.class).forEach(toggle -> {
                    toggle.setOn(machine.getCustomData().getInt("state") == mode, false).setToggleGroup(modeGroup);
                    toggle.bind(DataBindingBuilder.bool(
                        () -> machine.getCustomData().getInt("state") == mode,
                        pressed -> {
                            if (pressed) {
                                machine.getCustomData().putInt("state", mode);
                                machine.getHolder().setChanged();
                            }
                        }).build());
                });
            }
        }
        if (!MbdReactor.isReactor(machine)) return;
        // UITemplate persists elements, not Java callbacks: bind tab actions on the live UI.
        var tabs = new Toggle.ToggleGroup();
        for (int i = 0; i < 2; i++) {
            int selected = i;
            ui.selectId("legacy_tab_button_" + i, Toggle.class).forEach(button -> button.setToggleGroup(tabs).setOnToggleChanged(on -> {
                if (!on) return;
                for (int j = 0; j < 2; j++) {
                    boolean active = j == selected;
                    ui.selectId("legacy_tab_page_" + j).forEach(page -> page.setDisplay(active));
                    ui.selectId("legacy_tab_button_" + j, Toggle.class).forEach(toggle -> toggle.setOn(active, false));
                }
            }));
        }
        var controlRods = new Toggle.ToggleGroup();
        for (var id : new String[]{"reactor_on", "reactor_off"}) {
            boolean on = id.equals("reactor_on");
            ui.selectId(id, Toggle.class).forEach(toggle -> {
                toggle.setOn((MbdReactor.data(machine, "burning_rate") != 0) == on, false).setToggleGroup(controlRods);
                toggle.bind(DataBindingBuilder.bool(
                    () -> (MbdReactor.data(machine, "burning_rate") != 0) == on,
                    pressed -> { if (pressed) MbdReactor.set(machine, "burning_rate", on ? 1 : 0); }).build());
            });
        }
        ui.selectId("modify_burning_rate", TextField.class).forEach(field -> {
            field.setNumbersOnlyInt(0, 100);
            field.bind(DataBindingBuilder.string(() -> Long.toString(Math.round(MbdReactor.data(machine, "burning_rate") * 100)), text -> {
                try {
                    int percent = Integer.parseInt(text);
                    if (percent >= 0 && percent <= 100) MbdReactor.set(machine, "burning_rate", percent / 100.0);
                } catch (NumberFormatException ignored) { }
            }).build());
        });
        number(ui, "degree_of_damage", "degree_of_damage", () -> MbdReactor.data(machine, "degree_of_damage"));
        number(ui, "temperature", "temperature", () -> MbdReactor.data(machine, "temperature"));
        number(ui, "heatProduced", "heat_produced", () -> MbdReactor.heat(machine));
        number(ui, "cooling", "fluid_cooling", () -> MbdReactor.cooling(machine));
        number(ui, "ambientCooling", "ambient_cooling", () -> MbdReactor.ambient(machine));
        number(ui, "inputFluid", "input_fluid", () -> MbdReactor.amount(machine, 20, true));
        number(ui, "outputFluid", "output_fluid", () -> MbdReactor.amount(machine, 20, false));
        number(ui, "outputEnergy", "output_energy", () -> MbdReactor.amount(machine, 40960, false) / 1000);
        number(ui, "burningRate", "burning_rate", () -> MbdReactor.data(machine, "burning_rate") * 100);
        ui.selectId("switch", Label.class).forEach(label -> label.bind(DataBindingBuilder.componentS2C(() -> {
            double temp = MbdReactor.data(machine, "temperature");
            var status = temp >= 2000 ? "meltdown_temp" : temp >= 1500 ? "critical_temp"
                    : MbdReactor.data(machine, "burning_rate") != 0 ? "switch_on" : "switch_off";
            return Component.translatable("message.createdelight.fission_reactor.switch")
                    .append(Component.translatable("message.createdelight.fission_reactor." + status));
        }).build()));
    }

    private static void number(UI ui, String id, String key, DoubleSupplier value) {
        ui.selectId(id, Label.class).forEach(label -> label.bind(DataBindingBuilder.componentS2C(() ->
                Component.translatable("message.createdelight.fission_reactor." + key, Math.round(value.getAsDouble() * 100) / 100.0)).build()));
    }
}
