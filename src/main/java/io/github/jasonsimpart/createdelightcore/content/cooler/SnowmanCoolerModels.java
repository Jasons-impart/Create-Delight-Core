// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.content.cooler;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;

public final class SnowmanCoolerModels {
    public static final PartialModel SNOWMAN_INERT = model("snowman/inert");
    public static final PartialModel SNOWMAN_IDLE = model("snowman/idle");
    public static final PartialModel SNOWMAN_ACTIVE = model("snowman/active");
    public static final PartialModel SNOWMAN_SUPER = model("snowman/super");
    public static final PartialModel SNOWMAN_SUPER_ACTIVE = model("snowman/super_active");
    public static final PartialModel SNOWMAN_FLAME = model("flame");
    public static final SpriteShiftEntry COOLER_FLAME = SpriteShifter.get(
            CreateDelightCore.id("block/snowman_flame"), CreateDelightCore.id("block/snowman_flame_scroll"));
    public static final SpriteShiftEntry SUPER_COOLER_FLAME = SpriteShifter.get(
            CreateDelightCore.id("block/snowman_flame"), CreateDelightCore.id("block/snowman_flame_super_scroll"));

    private static PartialModel model(String name) {
        return PartialModel.of(CreateDelightCore.id("block/snowman_cooler/" + name));
    }

    public static void init() {}
}
