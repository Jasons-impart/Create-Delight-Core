package io.github.jasonsimpart.compat.jei.category;

import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import io.github.jasonsimpart.content.recipe.FanFreezingRecipe;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Blocks;

public class FanFreezingCategory extends ProcessingViaFanCategory.MultiOutput<FanFreezingRecipe> {
    public FanFreezingCategory(Info<FanFreezingRecipe> info) {
        super(info);
    }

    @Override
    protected void renderAttachedBlock(GuiGraphics graphics) {
        GuiGameElement.of(Blocks.POWDER_SNOW.defaultBlockState())
                .scale(SCALE)
                .atLocal(0, 0, 2)
                .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
                .render(graphics);
    }
}
