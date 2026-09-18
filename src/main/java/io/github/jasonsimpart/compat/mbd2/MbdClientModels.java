package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.common.machine.definition.MBDMachineDefinition;
import com.lowdragmc.mbd2.common.machine.definition.config.MachineState;
import com.lowdragmc.mbd2.integration.create.machine.CreateMachineState;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/** Register rotating models before the first atlas bake, not on first placement. */
@OnlyIn(Dist.CLIENT)
final class MbdClientModels {
    // Flywheel's PartialModel registry has weak values. Keep the models alive until
    // its model-loading listener has registered/baked them, and across reloads.
    private static final Set<PartialModel> MODELS = new HashSet<>();

    private MbdClientModels() {}

    static void register(MBDMachineDefinition definition) {
        register(definition.stateMachine().getRootState());
    }

    private static void register(MachineState state) {
        if (state instanceof CreateMachineState kinetic) MODELS.add(kinetic.getRotationPartialModel());
        state.getChildren().forEach(MbdClientModels::register);
    }
}
