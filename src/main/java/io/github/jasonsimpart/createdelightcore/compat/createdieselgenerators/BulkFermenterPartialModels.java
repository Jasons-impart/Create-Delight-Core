package io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;

public class BulkFermenterPartialModels {
    public static final PartialModel FILTER_HOLDER = PartialModel.of(
            CreateDelightCore.id("block/bulk_fermenter/filter_holder"));

    public static void init() {
        // Load partial models during client construction, before model baking.
    }
}
