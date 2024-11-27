package io.github.jasonsimpart.createdelightcore.jei.category;

import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import io.github.jasonsimpart.createdelightcore.recipe.BatchFreezingRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class FanBatchFreezingCategory extends CDProcessingViaFanCategory.MultiOutput<BatchFreezingRecipe> {
    public FanBatchFreezingCategory(Info<BatchFreezingRecipe> info) {
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
