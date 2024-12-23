package io.github.jasonsimpart.createdelightcore.compat.jei.category;

import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import io.github.jasonsimpart.createdelightcore.content.recipe.FanFreezingRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class FanFreezingCategory extends CDProcessingViaFanCategory.MultiOutput<FanFreezingRecipe> {
    public FanFreezingCategory(Info<FanFreezingRecipe> info) {
        super(info);
    }

    @Override
    protected void renderAttachedBlock(@NotNull GuiGraphics graphics) {
        GuiGameElement.of(Blocks.POWDER_SNOW.defaultBlockState())
                .scale(SCALE)
                .atLocal(0, 0, 2)
                .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
                .render(graphics);
    }
}
