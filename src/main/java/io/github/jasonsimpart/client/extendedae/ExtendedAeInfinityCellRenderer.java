package io.github.jasonsimpart.client.extendedae;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import com.glodblock.github.extendedae.common.items.ItemInfinityCell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

public final class ExtendedAeInfinityCellRenderer {
    private ExtendedAeInfinityCellRenderer() {
    }

    public static void register(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemInfinityCell infinityCell) {
                event.register(item, (guiGraphics, font, stack, xOffset, yOffset) ->
                        render(guiGraphics, infinityCell.getRecord(), xOffset, yOffset));
            }
        }
    }

    private static boolean render(GuiGraphics guiGraphics, AEKey record, int xOffset, int yOffset) {
        if (record == null) {
            return false;
        }

        // ExtendedAE can be used with other AE2 key providers. Do not let an
        // unknown key type make the item decorator crash the client.
        if (AEKeyRendering.get(record.getType()) == null) {
            return false;
        }

        guiGraphics.pose().pushPose();
        try {
            guiGraphics.pose().translate(xOffset + 8.0F, yOffset + 7.0F, 10.0F);
            guiGraphics.pose().scale(0.65F, 0.65F, 1.0F);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, 0, 0, record);
        } finally {
            guiGraphics.pose().popPose();
        }
        return false;
    }
}
