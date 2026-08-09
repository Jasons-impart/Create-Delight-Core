package io.github.jasonsimpart.client.tetra;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

import java.util.List;

public final class TetraEnergyBarRenderer {
    private static final String TETRA_MOD_ID = "tetra";
    private static final int BAR_WIDTH = 13;
    private static final int BACKGROUND_COLOR = 0xFF000000;
    private static final int ENERGY_COLOR = 0xFFF03F3C;
    private static final List<ResourceLocation> ENERGY_ITEM_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_sword"),
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_double"),
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_bow"),
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_shield"),
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_crossbow"),
            ResourceLocation.fromNamespaceAndPath(TETRA_MOD_ID, "modular_single")
    );

    private TetraEnergyBarRenderer() {
    }

    public static void register(RegisterItemDecorationsEvent event) {
        if (!ModList.get().isLoaded(TETRA_MOD_ID)) {
            return;
        }

        ENERGY_ITEM_IDS.forEach(id -> BuiltInRegistries.ITEM.getOptional(id)
                .ifPresent(item -> event.register(item, TetraEnergyBarRenderer::render)));
    }

    @SuppressWarnings("deprecation")
    private static boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains("maxEnergy")) {
            return false;
        }

        CompoundTag customDataTag = customData.getUnsafe();
        int maxEnergy = customDataTag.getInt("maxEnergy");
        if (maxEnergy <= 0) {
            return false;
        }

        int energy = Mth.clamp(customDataTag.getInt("energy"), 0, maxEnergy);
        int filledWidth = Mth.clamp(Math.round((float) energy / maxEnergy * BAR_WIDTH), 0, BAR_WIDTH);
        int left = xOffset + 2;
        int top = yOffset + 11;

        guiGraphics.fill(RenderType.guiOverlay(), left, top, left + BAR_WIDTH, top + 2, BACKGROUND_COLOR);
        if (filledWidth > 0) {
            guiGraphics.fill(RenderType.guiOverlay(), left, top, left + filledWidth, top + 1, ENERGY_COLOR);
        }
        return false;
    }
}
