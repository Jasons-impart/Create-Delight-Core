package io.github.jasonsimpart.createdelightcore.content.order.board;

import io.github.jasonsimpart.createdelightcore.network.AcceptOrderBoardCandidatePacket;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.RequestOrderBoardPacket;
import io.github.jasonsimpart.createdelightcore.network.RerollOrderBoardPacket;
import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrderBoardScreen extends AbstractContainerScreen<OrderBoardMenu> {
    private static final int CARD_X = 10;
    private static final int CARD_Y = 26;
    private static final int CARD_WIDTH = 84;
    private static final int CARD_HEIGHT = 118;
    private static final int CARD_GAP = 6;

    private final List<Button> acceptButtons = new ArrayList<>();
    private Button rerollButton;
    private boolean waitingForSync = true;
    private boolean accepting;
    private boolean rerolling;
    private boolean refreshRequested;

    public OrderBoardScreen(OrderBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 278;
        imageHeight = 208;
    }

    @Override
    protected void init() {
        super.init();
        acceptButtons.clear();
        for (int i = 0; i < 3; i++) {
            int index = i;
            Button button = Button.builder(Component.translatable("createdelightcore.order_board.accept"),
                            ignored -> accept(index))
                    .bounds(leftPos + CARD_X + i * (CARD_WIDTH + CARD_GAP) + 8,
                            topPos + CARD_Y + CARD_HEIGHT - 24, CARD_WIDTH - 16, 18)
                    .build();
            button.active = false;
            acceptButtons.add(addRenderableWidget(button));
        }
        rerollButton = addRenderableWidget(Button.builder(rerollLabel(), ignored -> reroll())
                .bounds(leftPos + 69, topPos + 150, 140, 18)
                .build());
        rerollButton.active = false;
        requestSync();
    }

    public void acceptSync(List<OrderBoardCandidate> candidates, boolean accepted, int ticksUntilRefresh,
                           int rerollCost) {
        menu.setBoardState(candidates, accepted, ticksUntilRefresh, rerollCost);
        waitingForSync = false;
        accepting = false;
        rerolling = false;
        refreshRequested = false;
        updateButtons();
    }

    private void requestSync() {
        waitingForSync = true;
        updateButtons();
        CDNetwork.CHANNEL.sendToServer(new RequestOrderBoardPacket(menu.getPos()));
    }

    private void accept(int index) {
        if (accepting || rerolling || menu.isAccepted() || index < 0 || index >= menu.getCandidates().size()) {
            return;
        }
        accepting = true;
        updateButtons();
        CDNetwork.CHANNEL.sendToServer(new AcceptOrderBoardCandidatePacket(menu.getPos(), index));
    }

    private void reroll() {
        if (waitingForSync || accepting || rerolling || menu.isAccepted()) {
            return;
        }
        rerolling = true;
        updateButtons();
        CDNetwork.CHANNEL.sendToServer(new RerollOrderBoardPacket(menu.getPos()));
    }

    private Component rerollLabel() {
        return Component.translatable("createdelightcore.order_board.reroll",
                MoneyUtil.baseCoinNumberToCoinValue(menu.getRerollCost()).getText());
    }

    private void updateButtons() {
        for (int i = 0; i < acceptButtons.size(); i++) {
            acceptButtons.get(i).active = !waitingForSync && !accepting && !rerolling && !menu.isAccepted()
                    && i < menu.getCandidates().size();
        }
        if (rerollButton != null) {
            rerollButton.setMessage(rerollLabel());
            rerollButton.active = !waitingForSync && !accepting && !rerolling && !menu.isAccepted()
                    && menu.getCandidates().size() == 3;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int previousTicks = menu.getTicksUntilRefresh();
        menu.clientTick();
        if (previousTicks > 0 && menu.getTicksUntilRefresh() == 0 && !refreshRequested) {
            refreshRequested = true;
            requestSync();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFEEE2C6);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFF6E4C2B);
        graphics.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + imageHeight - 5, 0xFFF4E9D0);

        for (int i = 0; i < 3; i++) {
            int x = leftPos + CARD_X + i * (CARD_WIDTH + CARD_GAP);
            int y = topPos + CARD_Y;
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF5A3B22);
            graphics.fill(x + 2, y + 2, x + CARD_WIDTH - 2, y + CARD_HEIGHT - 2, 0xFFFFF8E8);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 8, 0x3C2616);
        if (waitingForSync) {
            graphics.drawCenteredString(font, Component.translatable("createdelightcore.order_board.loading"),
                    imageWidth / 2, 76, 0x6E5A44);
            return;
        }

        List<OrderBoardCandidate> candidates = menu.getCandidates();
        for (int i = 0; i < candidates.size() && i < 3; i++) {
            renderCandidate(graphics, candidates.get(i), CARD_X + i * (CARD_WIDTH + CARD_GAP), CARD_Y);
        }

        Component footer = menu.isAccepted()
                ? Component.translatable("createdelightcore.order_board.accepted_wait", formatTime(menu.getTicksUntilRefresh()))
                        .withStyle(ChatFormatting.DARK_GREEN)
                : Component.translatable("createdelightcore.order_board.refresh_in", formatTime(menu.getTicksUntilRefresh()));
        graphics.drawCenteredString(font, footer, imageWidth / 2, 176, 0x4A3521);
        graphics.drawCenteredString(font, Component.translatable("createdelightcore.order_board.hint"),
                imageWidth / 2, 190, 0x6E5A44);
    }

    private void renderCandidate(GuiGraphics graphics, OrderBoardCandidate candidate, int x, int y) {
        graphics.drawCenteredString(font,
                Component.translatable("createdelightcore.order_board.kind." + candidate.kind()),
                x + CARD_WIDTH / 2, y + 6, kindColor(candidate.kind()));
        drawTrimmed(graphics, Component.translatable("tooltip.createdelight.order_draft.seal." + candidate.customerSeal()),
                x + 6, y + 23, CARD_WIDTH - 12, 0x493523);
        Component direction = candidate.requiredCategories().isEmpty()
                ? Component.translatable("tooltip.createdelight.order_draft.seal." + candidate.categorySeal())
                : Component.translatable("createdelightcore.order_board.required",
                        candidate.requiredCategories().stream()
                                .map(category -> Component.translatable("tooltip.createdelight.order.entries." + category).getString())
                                .collect(Collectors.joining("/")));
        drawTrimmed(graphics, direction, x + 6, y + 35, CARD_WIDTH - 12, 0x493523);
        drawTrimmed(graphics, Component.translatable("createdelightcore.order_board.grade", candidate.grade(),
                        Component.translatable("tooltip.createdelight.order.grade." + candidate.grade())),
                x + 6, y + 49, CARD_WIDTH - 12, 0x5B3D86);
        drawTrimmed(graphics, Component.translatable("createdelightcore.order_board.total",
                        candidate.minTotal(), candidate.maxTotal()),
                x + 6, y + 62, CARD_WIDTH - 12, 0x493523);
        int coverage = Math.min(999, candidate.warehouseCount() * 100 / Math.max(1, candidate.maxTotal()));
        drawTrimmed(graphics, Component.translatable("createdelightcore.order_board.stock",
                        candidate.warehouseCount(), coverage),
                x + 6, y + 75, CARD_WIDTH - 12, coverage >= 100 ? 0x287A3D : 0x9A5A20);
        drawTrimmed(graphics, Component.translatable("createdelightcore.order_board.market",
                        String.format(Locale.ROOT, "%.2f", candidate.marketMultiplier())),
                x + 6, y + 88, CARD_WIDTH - 12, 0x9A6A11);
    }

    private void drawTrimmed(GuiGraphics graphics, Component component, int x, int y, int width, int color) {
        graphics.drawString(font, font.plainSubstrByWidth(component.getString(), width), x, y, color, false);
    }

    private int kindColor(String kind) {
        return switch (kind) {
            case "adapted" -> 0x2C6E49;
            case "expansion" -> 0x2D5D8A;
            default -> 0xA16B00;
        };
    }

    private String formatTime(int ticks) {
        int seconds = Math.max(0, ticks) / 20;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }
}
