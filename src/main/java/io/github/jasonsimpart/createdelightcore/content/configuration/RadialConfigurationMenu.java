package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.SelectConfigurationModePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RadialConfigurationMenu extends Screen {
    private static final float SLOT_RADIUS = 52.0F;
    private static final double INNER_SELECTION_RADIUS_SQUARED = 18.0D * 18.0D;
    private static final double OUTER_SELECTION_RADIUS_SQUARED = 96.0D * 96.0D;

    private final InteractionHand hand;
    private final ItemStack moduleStack;
    private final List<ConfigurationModeSnapshot> modes;
    private final ResourceLocation selectedMode;
    private int ticksOpen;
    private int hoveredSlot = -1;
    private boolean closing;

    public RadialConfigurationMenu(InteractionHand hand, ItemStack moduleStack,
                                   List<ConfigurationModeSnapshot> modes, ResourceLocation selectedMode) {
        super(Component.translatable("menu.createdelightcore.configuration_module.title"));
        this.hand = hand;
        this.moduleStack = moduleStack.copyWithCount(1);
        this.modes = List.copyOf(modes);
        this.selectedMode = selectedMode;
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (ticksOpen > 1 && !isSelectionKeyPhysicallyDown()) {
            finishSelection();
        }
    }

    private boolean isSelectionKeyPhysicallyDown() {
        InputConstants.Key key = AllKeys.TOOLBELT.getKeybind().getKey();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return AllKeys.isMouseButtonDown(key.getValue());
        }
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key.getValue());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2;
        updateHoveredSlot(mouseX - centerX, mouseY - centerY);

        float progress = Mth.clamp((ticksOpen + partialTick) / 5.0F, 0.0F, 1.0F);
        float radius = SLOT_RADIUS * progress;
        for (int index = 0; index < modes.size(); index++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / modes.size();
            int slotX = centerX + Mth.floor(Math.cos(angle) * radius);
            int slotY = centerY + Mth.floor(Math.sin(angle) * radius);
            ConfigurationModeSnapshot mode = modes.get(index);

            if (mode.id().equals(selectedMode)) {
                graphics.fill(slotX - 13, slotY - 13, slotX + 13, slotY + 13, 0xA0D89B24);
            }
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, slotX - 12, slotY - 12);
            if (index == hoveredSlot) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, slotX - 13, slotY - 13);
            }
            Item target = ForgeRegistries.ITEMS.getValue(mode.target());
            if (target != null) {
                graphics.renderItem(new ItemStack(target), slotX - 8, slotY - 8);
            }
        }

        graphics.fill(centerX - 12, centerY - 12, centerX + 12, centerY + 12, 0xA0202020);
        graphics.renderItem(moduleStack, centerX - 8, centerY - 8);

        ConfigurationModeSnapshot displayed = getDisplayedMode();
        if (displayed != null) {
            Item target = ForgeRegistries.ITEMS.getValue(displayed.target());
            Component name = target == null ? Component.literal(displayed.target().toString()) : target.getDescription();
            Component cost = Component.translatable(
                    "item.createdelightcore.configuration_module.tooltip.cost", displayed.chargeCost());
            drawCenteredLabel(graphics, name, centerX, centerY + 78, 0xFFE8C66A);
            drawCenteredLabel(graphics, cost, centerX, centerY + 90, 0xFF7FD7FF);
        }
        drawCenteredLabel(graphics, Component.translatable(
                "menu.createdelightcore.configuration_module.release_to_select",
                AllKeys.TOOLBELT.getKeybind().getTranslatedKeyMessage()), centerX, centerY + 106, 0xFFB0B0B0);
    }

    private void drawCenteredLabel(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        int textWidth = font.width(text);
        graphics.fill(centerX - textWidth / 2 - 3, y - 2,
                centerX + textWidth / 2 + 3, y + font.lineHeight + 1, 0x90000000);
        graphics.drawCenteredString(font, text, centerX, y, color);
    }

    private void updateHoveredSlot(double mouseX, double mouseY) {
        double distanceSquared = mouseX * mouseX + mouseY * mouseY;
        if (distanceSquared < INNER_SELECTION_RADIUS_SQUARED
                || distanceSquared > OUTER_SELECTION_RADIUS_SQUARED || modes.isEmpty()) {
            hoveredSlot = -1;
            return;
        }
        double step = Math.PI * 2.0D / modes.size();
        double angle = Math.atan2(mouseY, mouseX) + Math.PI / 2.0D;
        if (angle < 0.0D) {
            angle += Math.PI * 2.0D;
        }
        hoveredSlot = Math.floorMod((int) Math.floor((angle + step / 2.0D) / step), modes.size());
    }

    private ConfigurationModeSnapshot getDisplayedMode() {
        if (hoveredSlot >= 0 && hoveredSlot < modes.size()) {
            return modes.get(hoveredSlot);
        }
        return modes.stream().filter(mode -> mode.id().equals(selectedMode)).findFirst().orElse(null);
    }

    private void finishSelection() {
        if (closing) {
            return;
        }
        closing = true;
        if (hoveredSlot >= 0 && hoveredSlot < modes.size()) {
            CDNetwork.CHANNEL.sendToServer(new SelectConfigurationModePacket(hand, modes.get(hoveredSlot).id()));
        }
        minecraft.setScreen(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot >= 0) {
            finishSelection();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            closing = true;
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
