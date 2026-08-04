package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class ConfigurationModuleItemDecorator implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        return ConfigurationModuleManager.getSnapshotTarget(stack).map(target -> {
            guiGraphics.fill(xOffset + 8, yOffset, xOffset + 16, yOffset + 8, 0xA0000000);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(xOffset + 8.0F, yOffset, 250.0F);
            guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
            guiGraphics.renderItem(new ItemStack(target), 0, 0);
            guiGraphics.pose().popPose();
            return false;
        }).orElse(false);
    }
}
