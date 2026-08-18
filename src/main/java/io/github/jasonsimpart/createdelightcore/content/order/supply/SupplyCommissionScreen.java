package io.github.jasonsimpart.createdelightcore.content.order.supply;

import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class SupplyCommissionScreen extends AbstractContainerScreen<SupplyCommissionMenu> {
    private Button submitButton;
    private Button previousButton;
    private Button nextButton;
    private Button queuePreviousButton;
    private Button queueNextButton;
    private Button cancelButton;
    private int selectedQueueRow = -1;

    public SupplyCommissionScreen(SupplyCommissionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 340;
        imageHeight = 254;
        inventoryLabelX = 8;
        inventoryLabelY = 160;
    }

    @Override
    protected void init() {
        super.init();
        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), ignored -> clickMenuButton(1))
                .bounds(leftPos + 51, topPos + 117, 20, 20)
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), ignored -> clickMenuButton(2))
                .bounds(leftPos + 73, topPos + 117, 20, 20)
                .build());
        submitButton = addRenderableWidget(Button.builder(
                        Component.translatable("createdelightcore.supply_commission.submit"),
                        ignored -> clickMenuButton(0))
                .bounds(leftPos + 96, topPos + 117, 76, 20)
                .build());
        queuePreviousButton = addRenderableWidget(Button.builder(Component.literal("<"), ignored -> cycleQueuePage(3))
                .bounds(leftPos + 296, topPos + 24, 17, 16)
                .build());
        queueNextButton = addRenderableWidget(Button.builder(Component.literal(">"), ignored -> cycleQueuePage(4))
                .bounds(leftPos + 315, topPos + 24, 17, 16)
                .build());
        cancelButton = addRenderableWidget(Button.builder(
                        Component.translatable("createdelightcore.supply_commission.cancel"),
                        ignored -> cancelSelectedQueue())
                .bounds(leftPos + 258, topPos + 117, 74, 20)
                .build());
        updateButtons();
    }

    private void cycleQueuePage(int id) {
        selectedQueueRow = -1;
        clickMenuButton(id);
    }

    private void cancelSelectedQueue() {
        if (selectedQueueRow < 0) {
            return;
        }
        clickMenuButton(5 + selectedQueueRow);
        selectedQueueRow = -1;
    }

    private void clickMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        ItemStack selected = menu.previewStack();
        if (previousButton != null && nextButton != null) {
            previousButton.active = menu.catalogCount() > 1;
            nextButton.active = menu.catalogCount() > 1;
        }
        if (submitButton != null) {
            submitButton.active = !selected.isEmpty();
        }
        if (queuePreviousButton != null && queueNextButton != null) {
            queuePreviousButton.active = menu.queuePages() > 1;
            queueNextButton.active = menu.queuePages() > 1;
        }
        List<SupplyCommissionRecord> records = menu.getBlockEntity() == null
                ? List.of() : menu.getBlockEntity().commissions();
        int selectedIndex = menu.queuePage() * 3 + selectedQueueRow;
        if (selectedQueueRow < 0 || selectedIndex >= records.size()) {
            selectedQueueRow = -1;
        }
        if (cancelButton != null) {
            cancelButton.active = selectedQueueRow >= 0
                    && minecraft != null && minecraft.player != null && minecraft.level != null
                    && records.get(menu.queuePage() * 3 + selectedQueueRow).owner().equals(minecraft.player.getUUID())
                    && minecraft.level.getGameTime() < records.get(menu.queuePage() * 3 + selectedQueueRow).dueTime();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int localX = (int) mouseX - leftPos;
            int localY = (int) mouseY - topPos;
            if (localX >= 179 && localX < 333 && localY >= 42 && localY < 108) {
                int row = (localY - 42) / 22;
                int index = menu.queuePage() * 3 + row;
                SupplyCommissionBlockEntity blockEntity = menu.getBlockEntity();
                if (blockEntity != null && index < blockEntity.commissions().size()) {
                    selectedQueueRow = row;
                    updateButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFFFFFFFF);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, 0xFFFFFFFF);
        graphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        for (Slot slot : menu.slots) {
            drawSlot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
        }
        drawPanel(graphics, leftPos + 50, topPos + 22, leftPos + 172, topPos + 112);
        drawPanel(graphics, leftPos + 176, topPos + 22, leftPos + 336, topPos + 112);
    }

    private void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, 0xFF8B8B8B);
        graphics.fill(left + 2, top + 2, right - 2, bottom - 2, 0xFFE8E8E8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, 0x404040, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.vouchers"), 8, 25, 0x404040, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.output"), 8, 69, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        renderPreview(graphics);
        graphics.drawString(font, trim(Component.translatable("createdelightcore.supply_commission.queue_header",
                menu.readyCount()).getString(), 68), 184, 28,
                menu.readyCount() > 0 ? 0x2F6B3A : 0x404040, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.queue_page",
                menu.queuePage() + 1, menu.queuePages()), 260, 28, 0x666666, false);
        renderQueue(graphics);
    }

    private void renderPreview(GuiGraphics graphics) {
        ItemStack selected = menu.previewStack();
        if (selected.isEmpty()) {
            graphics.drawString(font, trim(Component.translatable(
                    "createdelightcore.supply_commission.no_unlocked_catalog").getString(), 108),
                    58, 48, 0x777777, false);
            return;
        }
        graphics.renderItem(selected, 58, 26);
        graphics.renderItemDecorations(font, selected, 58, 26);
        graphics.drawString(font, trim(selected.getHoverName().getString(), 88), 78, 28, 0x303030, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.catalog_position",
                menu.selectedIndex() + 1, menu.catalogCount()), 78, 40, 0x666666, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.preview_batch_simple",
                menu.previewCount()), 58, 56, 0x404040, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.preview_cost",
                menu.previewTickets(), MoneyUtil.baseCoinNumberToCoinValue(menu.previewMoney()).getText()),
                58, 70, 0x6A4A18, false);
        graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.preview_time",
                formatTime(menu.previewDurationTicks())), 58, 84, 0x404040, false);
    }

    private void renderQueue(GuiGraphics graphics) {
        SupplyCommissionBlockEntity blockEntity = menu.getBlockEntity();
        List<SupplyCommissionRecord> records = blockEntity == null ? List.of() : blockEntity.commissions();
        if (records.isEmpty()) {
            graphics.drawString(font, Component.translatable("createdelightcore.supply_commission.queue_empty"),
                    184, 55, 0x777777, false);
        }
        long now = minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        int start = menu.queuePage() * 3;
        for (int row = 0; row < 3 && start + row < records.size(); row++) {
            SupplyCommissionRecord record = records.get(start + row);
            ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(record.targetItem()));
            int y = 43 + row * 22;
            if (row == selectedQueueRow) {
                graphics.fill(179, y - 2, 333, y + 19, 0x553B78A8);
            }
            graphics.renderItem(icon, 182, y);
            graphics.drawString(font, trim(icon.getHoverName().getString() + " ×" + record.count(), 126),
                    201, y, 0x303030, false);
            Component time = Component.translatable("createdelightcore.supply_commission.queue_remaining",
                    formatTime(record.dueTime() - now)).withStyle(ChatFormatting.DARK_GRAY);
            String detail = time.getString();
            if (minecraft != null && minecraft.player != null
                    && !record.owner().equals(minecraft.player.getUUID())) {
                detail = Component.translatable("createdelightcore.supply_commission.queue_owner",
                        record.ownerName(), time).getString();
            }
            graphics.drawString(font, trim(detail, 126), 201, y + 10, 0x666666, false);
        }
    }

    private String formatTime(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        long days = seconds / 1200L;
        long minutes = (seconds % 1200L) / 60L;
        if (days > 0L) {
            return String.format(Locale.ROOT, "%dd %02d:%02d", days, minutes, seconds % 60L);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds % 60L);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        graphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
    }

    private String trim(String value, int width) {
        return font.width(value) <= width ? value : font.plainSubstrByWidth(value, width - font.width("...")) + "...";
    }
}
