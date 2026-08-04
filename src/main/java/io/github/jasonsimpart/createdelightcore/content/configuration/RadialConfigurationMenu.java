package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.SelectConfigurationModePacket;
import net.createmod.catnip.gui.element.GuiGameElement;
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
    private static final float OPEN_ANIMATION_TICKS = 8.0F;
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
        if (ticksOpen > 1 && !ConfigurationModuleClientInput.isToolbeltKeyPhysicallyDown(minecraft)) {
            finishSelection();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2;
        updateHoveredSlot(mouseX - centerX, mouseY - centerY);

        float progress = Mth.clamp((ticksOpen + partialTick) / OPEN_ANIMATION_TICKS, 0.0F, 1.0F);
        float remaining = 1.0F - progress;
        float easedProgress = 1.0F - remaining * remaining * remaining;
        float radius = SLOT_RADIUS * easedProgress;
        for (int index = 0; index < modes.size(); index++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / modes.size();
            float slotX = centerX + (float) Math.cos(angle) * radius - 11.0F;
            float slotY = centerY + (float) Math.sin(angle) * radius - 11.0F;
            ConfigurationModeSnapshot mode = modes.get(index);

            graphics.pose().pushPose();
            graphics.pose().translate(slotX, slotY, 0.0F);
            if (mode.id().equals(selectedMode)) {
                graphics.fill(-2, -2, 24, 24, 0xA0D89B24);
            }
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
            if (index == hoveredSlot) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
            }
            Item target = ForgeRegistries.ITEMS.getValue(mode.target());
            if (target != null) {
                GuiGameElement.of(new ItemStack(target)).at(3, 3).render(graphics);
            }
            graphics.pose().popPose();
        }

        graphics.pose().pushPose();
        graphics.pose().translate(centerX - 12.0F, centerY - 12.0F, 0.0F);
        AllGuiTextures.TOOLBELT_MAIN_SLOT.render(graphics, 0, 0);
        GuiGameElement.of(moduleStack).at(4, 4).render(graphics);
        graphics.pose().popPose();

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
