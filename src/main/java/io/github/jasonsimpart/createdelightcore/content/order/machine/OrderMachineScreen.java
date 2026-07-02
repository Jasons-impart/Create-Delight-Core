package io.github.jasonsimpart.createdelightcore.content.order.machine;

import io.github.jasonsimpart.createdelightcore.content.order.OrderCandidate;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntry;
import io.github.jasonsimpart.createdelightcore.content.order.OrderEntryCandidates;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestMode;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestEstimator;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestRatioSelection;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestResolver;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestSelection;
import io.github.jasonsimpart.createdelightcore.content.order.OrderRequestStrategy;
import io.github.jasonsimpart.createdelightcore.network.CDNetwork;
import io.github.jasonsimpart.createdelightcore.network.RequestOrderCandidatesPacket;
import io.github.jasonsimpart.createdelightcore.network.SetOrderRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class OrderMachineScreen<M extends OrderMachineMenu> extends AbstractContainerScreen<M> {
    private static final int PANEL_X = 64;
    private static final int PANEL_Y = 24;
    private static final int PANEL_WIDTH = 176;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 6;
    private static final int QUANTITY_AREA_WIDTH = 52;
    private static final int ORDER_BOX_X = 8;
    private static final int ORDER_BOX_Y = 20;
    private static final int ORDER_BOX_WIDTH = 52;
    private static final int ORDER_BOX_HEIGHT = 102;
    private static final int HELP_X = 116;
    private static final int HELP_Y = 7;
    private static final int HELP_SIZE = 10;

    private final List<OrderRequestSelection> selectedSelections = new ArrayList<>();
    private final List<OrderRequestRatioSelection> ratioSelections = new ArrayList<>();
    private final List<CandidateLine> visibleLines = new ArrayList<>();
    private int scrollOffset;
    private EditBox addressBox;
    private Button partialButton;
    private Button modeButton;
    private OrderRequestMode requestMode = OrderRequestMode.FIXED_COUNT;
    private boolean allowPartial;
    private ItemStack lastOrderStack = ItemStack.EMPTY;

    protected OrderMachineScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 248;
        imageHeight = 280;
        inventoryLabelX = 8;
        inventoryLabelY = 190;
    }

    @Override
    protected void init() {
        super.init();
        rememberOrderStack();
        addRenderableWidget(Button.builder(Component.translatable("createdelightcore.gui.refresh"),
                        button -> requestCandidates())
                .bounds(leftPos + 12, topPos + 92, 44, 20)
                .build());

        if (menu.isRequester()) {
            modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> toggleMode())
                    .bounds(leftPos + PANEL_X + 52, topPos + 3, 42, 16)
                    .build());

            addressBox = new EditBox(font, leftPos + PANEL_X, topPos + 150, 106, 16,
                    Component.translatable("createdelightcore.gui.address"));
            addressBox.setMaxLength(128);
            addressBox.setHint(Component.translatable("createdelightcore.gui.address"));
            addRenderableWidget(addressBox);

            partialButton = addRenderableWidget(Button.builder(partialModeLabel(), button -> togglePartial())
                    .bounds(leftPos + 176, topPos + 148, 64, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("createdelightcore.gui.save"),
                            button -> sendSelection(false))
                    .bounds(leftPos + PANEL_X, topPos + 171, 84, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("createdelightcore.gui.send"),
                            button -> sendSelection(true))
                    .bounds(leftPos + 156, topPos + 171, 84, 20)
                    .build());
        }

        requestCandidates();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (addressBox != null) {
            addressBox.tick();
        }
        if (!ItemStack.matches(lastOrderStack, menu.getSlot(0).getItem())) {
            rememberOrderStack();
            requestCandidates();
        }
    }

    public void acceptCandidateGroups(List<OrderEntryCandidates> groups) {
        acceptCandidateSync(groups, currentStrategy(), addressBox == null ? "" : addressBox.getValue(), allowPartial);
    }

    public void acceptCandidateSync(List<OrderEntryCandidates> groups, OrderRequestStrategy strategy,
                                    String targetAddress, boolean allowPartial) {
        OrderRequestStrategy incomingStrategy = strategy == null ? OrderRequestStrategy.empty() : strategy;
        menu.setCandidateGroups(groups);
        requestMode = incomingStrategy.mode();
        selectedSelections.clear();
        selectedSelections.addAll(sanitizeSelections(incomingStrategy.fixedSelections()));
        ratioSelections.clear();
        ratioSelections.addAll(incomingStrategy.ratioSelections());
        this.allowPartial = allowPartial;
        if (modeButton != null) {
            modeButton.setMessage(modeLabel());
        }
        if (addressBox != null) {
            addressBox.setValue(targetAddress == null ? "" : targetAddress);
        }
        if (partialButton != null) {
            partialButton.setMessage(partialModeLabel());
        }
        int maxScroll = Math.max(0, buildLines().size() - VISIBLE_ROWS);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    private void requestCandidates() {
        CDNetwork.CHANNEL.sendToServer(new RequestOrderCandidatesPacket(menu.getPos()));
    }

    private void rememberOrderStack() {
        lastOrderStack = menu.getSlot(0).getItem().copy();
    }

    private void togglePartial() {
        allowPartial = !allowPartial;
        if (partialButton != null) {
            partialButton.setMessage(partialModeLabel());
        }
    }

    private void toggleMode() {
        requestMode = requestMode == OrderRequestMode.FIXED_COUNT ? OrderRequestMode.RATIO : OrderRequestMode.FIXED_COUNT;
        if (modeButton != null) {
            modeButton.setMessage(modeLabel());
        }
    }

    private Component partialModeLabel() {
        return Component.translatable(allowPartial ? "createdelightcore.gui.partial" : "createdelightcore.gui.full");
    }

    private Component modeLabel() {
        return Component.translatable(requestMode == OrderRequestMode.RATIO
                ? "createdelightcore.gui.mode_ratio"
                : "createdelightcore.gui.mode_fixed");
    }

    private void sendSelection(boolean trigger) {
        CDNetwork.CHANNEL.sendToServer(new SetOrderRequestPacket(
                menu.getPos(),
                currentStrategy(),
                addressBox == null ? "" : addressBox.getValue(),
                allowPartial,
                trigger
        ));
    }

    private OrderRequestStrategy currentStrategy() {
        return new OrderRequestStrategy(requestMode, sanitizeSelections(selectedSelections), ratioSelections);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.isRequester() && handleCandidateClick(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleCandidateClick(double mouseX, double mouseY, int button) {
        CandidateLine line = getHoveredCandidateLine(mouseX, mouseY);
        if (line == null || line.isHeader() || line.isSummary() || line.isSummaryItem()) {
            return false;
        }

        if (button == 1) {
            if (requestMode == OrderRequestMode.RATIO) {
                removeRatio(line);
            } else {
                removeSelection(line);
            }
            return true;
        }

        if (button != 0) {
            return false;
        }

        if (requestMode == OrderRequestMode.RATIO) {
            if (line.candidate == null) {
                return false;
            }
            OrderRequestRatioSelection ratio = findRatio(line);
            if (ratio != null) {
                int localX = (int) mouseX - leftPos;
                if (isMinusButton(localX)) {
                    adjustRatio(line, -adjustStep());
                    return true;
                }
                if (isPlusButton(localX)) {
                    adjustRatio(line, adjustStep());
                    return true;
                }
            }
            addRatio(line);
            return true;
        }

        if (line.candidate == null) {
            return false;
        }
        OrderRequestSelection selection = findSelection(line);
        if (selection != null) {
            int localX = (int) mouseX - leftPos;
            if (isMinusButton(localX)) {
                adjustSelection(line, -adjustStep());
                return true;
            }
            if (isPlusButton(localX)) {
                adjustSelection(line, adjustStep());
                return true;
            }
        }

        addSelection(line);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, buildLines().size() - VISIBLE_ROWS);
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderCandidatePanel(graphics, mouseX, mouseY);
        renderHelpTooltip(graphics, mouseX, mouseY);
        renderQuantityTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        drawPanel(graphics, x, y, imageWidth, imageHeight);
        drawInset(graphics, x + ORDER_BOX_X, y + ORDER_BOX_Y, ORDER_BOX_WIDTH, ORDER_BOX_HEIGHT);
        drawInset(graphics, x + PANEL_X - 2, y + PANEL_Y - 2, PANEL_WIDTH + 4,
                ROW_HEIGHT * VISIBLE_ROWS + 4);

        for (Slot slot : menu.slots) {
            drawSlot(graphics, x + slot.x - 1, y + slot.y - 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, trim(title.getString(), 64), 8, 7, 0x404040, false);
        graphics.drawString(font, Component.translatable("createdelightcore.gui.order"), 12, 26, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        graphics.drawString(font, Component.translatable(menu.isRequester()
                        ? "createdelightcore.gui.select_candidates"
                        : "createdelightcore.gui.candidates"),
                PANEL_X, 7, 0x404040, false);
        if (menu.isRequester()) {
            graphics.drawString(font, "?", HELP_X + 3, HELP_Y + 1, 0x404040, false);
            graphics.drawString(font, Component.translatable("createdelightcore.gui.reward_score",
                            scoreLabel(estimateOrder())),
                    PANEL_X + 96, 7, 0x404040, false);
            graphics.drawString(font, Component.translatable("createdelightcore.gui.address"),
                    PANEL_X, 139, 0x404040, false);
        }
    }

    private void renderCandidatePanel(GuiGraphics graphics, int mouseX, int mouseY) {
        List<CandidateLine> lines = buildLines();
        List<OrderRequestSelection> ratioPreview = requestMode == OrderRequestMode.RATIO ? ratioPreviewSelections() : List.of();
        visibleLines.clear();
        int end = Math.min(lines.size(), scrollOffset + VISIBLE_ROWS);
        for (int i = scrollOffset; i < end; i++) {
            visibleLines.add(lines.get(i));
        }

        for (int row = 0; row < visibleLines.size(); row++) {
            CandidateLine line = visibleLines.get(row);
            int x = leftPos + PANEL_X;
            int y = topPos + PANEL_Y + row * ROW_HEIGHT;
            if (line.isSummary()) {
                renderSummaryHeading(graphics, x, y, mouseX, mouseY);
                continue;
            }
            if (line.isHeader()) {
                renderEntryHeading(graphics, line, x, y);
                continue;
            }

            if (line.candidate == null) {
                renderMissingRatioLine(graphics, line, ratioPreview, x, y, mouseX, mouseY);
                continue;
            }

            OrderRequestSelection selection = findSelection(line);
            OrderRequestRatioSelection ratio = findRatio(line);
            boolean selected = requestMode == OrderRequestMode.RATIO ? ratio != null : selection != null;
            int plannedCount = requestMode == OrderRequestMode.RATIO
                    ? selectedCount(line.entry.key(), line.candidate.stack(), ratioPreview)
                    : selection == null ? 0 : selection.count();
            int shortage = requestMode == OrderRequestMode.RATIO && selected
                    ? Math.max(0, plannedCount - availableForRatioLine(line, ratioPreview))
                    : 0;
            graphics.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT - 1,
                    shortage > 0 ? 0xFFFFD8D8 : selected ? 0xFFE0E0E0 : 0xFFB8B8B8);
            graphics.fill(x, y + ROW_HEIGHT - 1, x + PANEL_WIDTH, y + ROW_HEIGHT, 0xFF707070);
            if (shortage > 0) {
                graphics.fill(x, y, x + 3, y + ROW_HEIGHT - 1, 0xFFB03030);
            }
            ItemStack displayStack = selected && plannedCount > 0
                    ? line.candidate.stack().copyWithCount(plannedCount)
                    : line.candidate.stack();
            graphics.renderItem(displayStack, x + 2, y + 1);
            graphics.renderItemDecorations(font, displayStack, x + 2, y + 1);

            String label = line.candidate.stack().getHoverName().getString()
                    + " x" + line.candidate.count()
                    + " Q" + line.candidate.quality();
            String plannedLabel = selected && plannedCount > 0 && requestMode == OrderRequestMode.RATIO
                    ? Component.translatable(shortage > 0
                                    ? "createdelightcore.gui.shortage_count"
                                    : "createdelightcore.gui.planned_count",
                            shortage > 0 ? shortage : plannedCount).getString()
                    : "";
            int plannedWidth = plannedLabel.isEmpty() ? 0 : font.width(plannedLabel) + 4;
            graphics.drawString(font, trim(label, PANEL_WIDTH - 26 - (selected ? QUANTITY_AREA_WIDTH : 4) - plannedWidth),
                    x + 22, y + 5, 0x303030, false);
            if (!plannedLabel.isEmpty()) {
                graphics.drawString(font, plannedLabel,
                        x + PANEL_WIDTH - QUANTITY_AREA_WIDTH - font.width(plannedLabel) - 3,
                        y + 5, shortage > 0 ? 0xA03030 : 0x305030, false);
            }

            if (selected) {
                if (requestMode == OrderRequestMode.RATIO) {
                    renderAmountControls(graphics,
                            Component.translatable("createdelightcore.gui.ratio_parts_short", ratio.weight()).getString(),
                            x, y, 0x503020);
                } else {
                    renderAmountControls(graphics, Integer.toString(selection.count()), x, y, 0x205020);
                }
            }

            if (mouseX >= x && mouseX < x + PANEL_WIDTH - QUANTITY_AREA_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                graphics.renderTooltip(font, line.candidate.stack(), mouseX, mouseY);
            }
        }
    }

    private void renderAmountControls(GuiGraphics graphics, String amount, int x, int y, int color) {
        int minusX = x + PANEL_WIDTH - 50;
        int plusX = x + PANEL_WIDTH - 9;
        graphics.fill(minusX - 1, y + 3, minusX + 8, y + 15, 0xFFC6C6C6);
        graphics.fill(plusX - 1, y + 3, plusX + 8, y + 15, 0xFFC6C6C6);
        graphics.drawString(font, "-", minusX + 1, y + 5, 0x303030, false);
        graphics.drawString(font, "+", plusX, y + 5, 0x303030, false);
        graphics.drawString(font, amount, x + PANEL_WIDTH - 14 - font.width(amount), y + 5, color, false);
    }

    private void renderMissingRatioLine(GuiGraphics graphics, CandidateLine line, List<OrderRequestSelection> ratioPreview,
                                        int x, int y, int mouseX, int mouseY) {
        graphics.fill(x, y, x + PANEL_WIDTH, y + ROW_HEIGHT - 1, 0xFFD8B8B8);
        graphics.fill(x, y + ROW_HEIGHT - 1, x + PANEL_WIDTH, y + ROW_HEIGHT, 0xFF707070);
        int plannedCount = selectedCount(line.entry.key(), line.ratioOnly.stack(), ratioPreview);
        int shortage = Math.max(0, plannedCount - availableForRatioLine(line, ratioPreview));
        ItemStack displayStack = plannedCount > 0 ? line.ratioOnly.stack().copyWithCount(plannedCount) : line.ratioOnly.stack();
        graphics.renderItem(displayStack, x + 2, y + 1);
        graphics.renderItemDecorations(font, displayStack, x + 2, y + 1);

        String label = line.ratioOnly.stack().getHoverName().getString()
                + " " + Component.translatable("createdelightcore.gui.missing").getString();
        String plannedLabel = plannedCount > 0
                ? Component.translatable("createdelightcore.gui.shortage_count", shortage).getString()
                : "";
        int plannedWidth = plannedLabel.isEmpty() ? 0 : font.width(plannedLabel) + 4;
        graphics.drawString(font, trim(label, PANEL_WIDTH - 64 - plannedWidth), x + 22, y + 5, 0x803030, false);
        if (!plannedLabel.isEmpty()) {
            graphics.drawString(font, plannedLabel,
                    x + PANEL_WIDTH - QUANTITY_AREA_WIDTH - font.width(plannedLabel) - 3,
                    y + 5, 0x803030, false);
        }
        String parts = Component.translatable("createdelightcore.gui.ratio_parts_short", line.ratioOnly.weight()).getString();
        graphics.drawString(font, parts, x + PANEL_WIDTH - 14 - font.width(parts), y + 5, 0x503020, false);

        if (mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT) {
            graphics.renderTooltip(font, line.ratioOnly.stack(), mouseX, mouseY);
        }
    }

    private void renderEntryHeading(GuiGraphics graphics, CandidateLine line, int x, int y) {
        OrderRequestResolver.RatioResult ratioResult = requestMode == OrderRequestMode.RATIO ? ratioResult() : null;
        int selected = requestMode == OrderRequestMode.RATIO
                ? selectedCount(line.entry.key(), ratioPreviewSelections())
                : selectedCount(line.entry.key());
        boolean failed = ratioResult != null && ratioResult.failedEntry(line.entry.key());
        String heading = line.entry.id() + " " + selected + "/" + line.entry.count() + " Q" + line.entry.minQuality();
        int color = failed ? 0x803030 : selected >= line.entry.count() ? 0x305030 : 0x707000;
        graphics.drawString(font, trim(heading, 128), x + 2, y + 5, color, false);
        if (menu.isRequester()) {
            Component scoreText;
            if (failed) {
                scoreText = Component.translatable("createdelightcore.gui.estimate.incomplete");
            } else {
                OrderRequestEstimator.EntryScore score =
                        OrderRequestEstimator.estimateEntry(line.entry, currentDisplaySelections());
                scoreText = Component.literal(score.complete() ? scoreLabel(score.score()) : "--");
            }
            String text = scoreText.getString();
            graphics.drawString(font, text, x + PANEL_WIDTH - font.width(text) - 3, y + 5, 0x404040, false);
        }
    }

    private void renderSummaryHeading(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        SummaryStats stats = summaryStats();
        List<SummaryItem> items = summaryItems();
        String left = Component.translatable("createdelightcore.gui.summary_total").getString()
                + " " + Component.translatable("createdelightcore.gui.planned_count", stats.total()).getString();
        int reservedRight = stats.shortage() > 0
                ? font.width(Component.translatable("createdelightcore.gui.shortage_count", stats.shortage()).getString()) + 6
                : 0;
        graphics.drawString(font, trim(left, 64), x + 2, y + 5,
                stats.shortage() > 0 ? 0x803030 : 0x305030, false);

        int iconX = x + 66;
        int maxIconX = x + PANEL_WIDTH - reservedRight - 16;
        int shown = 0;
        for (SummaryItem item : items) {
            if (iconX > maxIconX) {
                break;
            }
            ItemStack displayStack = item.stack().copyWithCount(item.count());
            graphics.renderItem(displayStack, iconX, y + 1);
            graphics.renderItemDecorations(font, displayStack, iconX, y + 1);
            iconX += 18;
            shown++;
        }
        if (shown < items.size() && iconX <= maxIconX + 8) {
            String more = "+" + (items.size() - shown);
            graphics.drawString(font, more, iconX + 1, y + 5, 0x404040, false);
        }

        if (stats.shortage() > 0) {
            String shortage = Component.translatable("createdelightcore.gui.shortage_count", stats.shortage()).getString();
            graphics.drawString(font, shortage, x + PANEL_WIDTH - font.width(shortage) - 3, y + 5, 0x803030, false);
        }

        if (mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT) {
            renderSummaryTooltip(graphics, items, mouseX, mouseY);
        }
    }

    private void renderSummaryTooltip(GuiGraphics graphics, List<SummaryItem> items, int mouseX, int mouseY) {
        if (items.isEmpty()) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("createdelightcore.gui.summary_total"));
        for (SummaryItem item : items) {
            Component count = Component.translatable(item.shortage() > 0
                            ? "createdelightcore.gui.shortage_count"
                            : "createdelightcore.gui.planned_count",
                    item.shortage() > 0 ? item.shortage() : item.count());
            lines.add(Component.literal(item.stack().getHoverName().getString() + "  ").append(count));
        }
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    private List<CandidateLine> buildLines() {
        List<CandidateLine> lines = new ArrayList<>();
        List<OrderEntryCandidates> groups = menu.getCandidateGroups();
        if (menu.isRequester() && !groups.isEmpty()) {
            lines.add(CandidateLine.summaryLine());
        }
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            OrderEntryCandidates group = groups.get(groupIndex);
            lines.add(new CandidateLine(groupIndex, group.entry(), null, null, null, false));
            for (OrderCandidate candidate : group.candidates()) {
                lines.add(new CandidateLine(groupIndex, group.entry(), candidate, null, null, false));
            }
            if (requestMode == OrderRequestMode.RATIO) {
                for (OrderRequestRatioSelection ratio : ratioSelections) {
                    if (group.entry().id().equals(ratio.entryId()) && findCandidate(group, ratio) == null) {
                        lines.add(new CandidateLine(groupIndex, group.entry(), null, ratio, null, false));
                    }
                }
            }
        }
        return lines;
    }

    private CandidateLine getHoveredCandidateLine(double mouseX, double mouseY) {
        int localX = (int) mouseX - leftPos;
        int localY = (int) mouseY - topPos;
        if (localX < PANEL_X || localX >= PANEL_X + PANEL_WIDTH || localY < PANEL_Y
                || localY >= PANEL_Y + ROW_HEIGHT * VISIBLE_ROWS) {
            return null;
        }

        int row = (localY - PANEL_Y) / ROW_HEIGHT;
        return row >= 0 && row < visibleLines.size() ? visibleLines.get(row) : null;
    }

    private void addSelection(CandidateLine line) {
        if (findSelection(line) != null) {
            return;
        }
        int remaining = line.entry.count() - selectedCount(line.entry.key());
        int count = Math.min(Math.min(64, remaining), line.candidate.count());
        if (count <= 0) {
            return;
        }
        selectedSelections.add(new OrderRequestSelection(line.entry.key(), line.candidate.stack(), count, line.candidate.quality()));
    }

    private void addRatio(CandidateLine line) {
        if (findRatio(line) != null) {
            return;
        }
        ratioSelections.add(new OrderRequestRatioSelection(line.entry.id(), line.candidate.stack(), 1, line.candidate.quality()));
    }

    private void adjustSelection(CandidateLine line, int change) {
        OrderRequestSelection selection = findSelection(line);
        if (selection == null || change == 0) {
            return;
        }

        int max = Math.min(line.candidate.count(), line.entry.count() - selectedCountExcept(line.entry.key(), selection));
        int count = Math.max(1, Math.min(max, selection.count() + change));
        selectedSelections.set(selectedSelections.indexOf(selection),
                new OrderRequestSelection(selection.entryId(), selection.stack(), count, selection.quality()));
    }

    private void adjustRatio(CandidateLine line, int change) {
        OrderRequestRatioSelection ratio = findRatio(line);
        if (ratio == null || change == 0) {
            return;
        }
        int weight = Math.max(1, Math.min(999, ratio.weight() + change));
        ratioSelections.set(ratioSelections.indexOf(ratio),
                new OrderRequestRatioSelection(ratio.entryId(), ratio.stack(), weight, ratio.quality()));
    }

    private void removeSelection(CandidateLine line) {
        OrderRequestSelection selection = findSelection(line);
        if (selection != null) {
            selectedSelections.remove(selection);
        }
    }

    private void removeRatio(CandidateLine line) {
        OrderRequestRatioSelection ratio = line.ratioOnly != null ? line.ratioOnly : findRatio(line);
        if (ratio != null) {
            ratioSelections.remove(ratio);
        }
    }

    private OrderRequestSelection findSelection(CandidateLine line) {
        for (OrderRequestSelection selection : selectedSelections) {
            if (line.entry.key().equals(selection.entryId())
                    && ItemStack.isSameItemSameTags(line.candidate.stack(), selection.stack())) {
                return selection;
            }
        }
        return null;
    }

    private OrderRequestRatioSelection findRatio(CandidateLine line) {
        ItemStack stack = line.candidate != null ? line.candidate.stack() : line.ratioOnly != null ? line.ratioOnly.stack() : ItemStack.EMPTY;
        for (OrderRequestRatioSelection ratio : ratioSelections) {
            if (line.entry.id().equals(ratio.entryId())
                    && ItemStack.isSameItemSameTags(stack, ratio.stack())) {
                return ratio;
            }
        }
        return null;
    }

    private int selectedCount(String entryId) {
        int count = 0;
        for (OrderRequestSelection selection : selectedSelections) {
            if (entryId.equals(selection.entryId())) {
                count += selection.count();
            }
        }
        return count;
    }

    private int selectedCount(String entryId, List<OrderRequestSelection> selections) {
        int count = 0;
        for (OrderRequestSelection selection : selections) {
            if (entryId.equals(selection.entryId())) {
                count += selection.count();
            }
        }
        return count;
    }

    private int selectedCount(String entryId, ItemStack stack, List<OrderRequestSelection> selections) {
        int count = 0;
        for (OrderRequestSelection selection : selections) {
            if (entryId.equals(selection.entryId()) && ItemStack.isSameItemSameTags(stack, selection.stack())) {
                count += selection.count();
            }
        }
        return count;
    }

    private int availableForRatioLine(CandidateLine line, List<OrderRequestSelection> ratioPreview) {
        ItemStack stack = line.candidate != null ? line.candidate.stack() : line.ratioOnly != null ? line.ratioOnly.stack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            return 0;
        }

        int available = line.candidate == null ? 0 : line.candidate.count();
        for (OrderEntryCandidates group : menu.getCandidateGroups()) {
            if (group.entry().key().equals(line.entry.key())) {
                break;
            }
            int previousCount = selectedCount(group.entry().key(), stack, ratioPreview);
            if (previousCount > 0) {
                available -= previousCount;
            }
        }
        return Math.max(0, available);
    }

    private int selectedCountExcept(String entryId, OrderRequestSelection excluded) {
        int count = 0;
        for (OrderRequestSelection selection : selectedSelections) {
            if (selection != excluded && entryId.equals(selection.entryId())) {
                count += selection.count();
            }
        }
        return count;
    }

    private List<OrderRequestSelection> sanitizeSelections(List<OrderRequestSelection> selections) {
        List<OrderRequestSelection> sanitized = new ArrayList<>();
        for (OrderEntryCandidates group : menu.getCandidateGroups()) {
            int remaining = group.entry().count();
            for (OrderRequestSelection selection : selections) {
                if (remaining <= 0 || !group.entry().key().equals(selection.entryId())) {
                    continue;
                }
                OrderCandidate candidate = findCandidate(group, selection);
                if (candidate == null) {
                    continue;
                }
                int count = Math.min(remaining, Math.min(selection.count(), candidate.count()));
                if (count <= 0) {
                    continue;
                }
                sanitized.add(new OrderRequestSelection(group.entry().key(), candidate.stack(), count, candidate.quality()));
                remaining -= count;
            }
        }
        return sanitized;
    }

    private OrderCandidate findCandidate(OrderEntryCandidates group, OrderRequestSelection selection) {
        for (OrderCandidate candidate : group.candidates()) {
            if (ItemStack.isSameItemSameTags(candidate.stack(), selection.stack())) {
                return candidate;
            }
        }
        return null;
    }

    private OrderCandidate findCandidate(OrderEntryCandidates group, OrderRequestRatioSelection selection) {
        for (OrderCandidate candidate : group.candidates()) {
            if (ItemStack.isSameItemSameTags(candidate.stack(), selection.stack())) {
                return candidate;
            }
        }
        return null;
    }

    private double estimateOrder() {
        List<OrderEntry> entries = menu.getCandidateGroups().stream()
                .map(OrderEntryCandidates::entry)
                .toList();
        if (requestMode == OrderRequestMode.RATIO && !ratioResult().complete()) {
            return 0;
        }
        return OrderRequestEstimator.estimate(entries, currentDisplaySelections());
    }

    private List<OrderRequestSelection> currentDisplaySelections() {
        return requestMode == OrderRequestMode.RATIO ? ratioResult().selections() : selectedSelections;
    }

    private OrderRequestResolver.RatioResult ratioResult() {
        return OrderRequestResolver.resolveRatio(menu.getCandidateGroups(), ratioSelections);
    }

    private List<OrderRequestSelection> ratioPreviewSelections() {
        return OrderRequestResolver.previewRatio(menu.getCandidateGroups(), ratioSelections);
    }

    private SummaryStats summaryStats() {
        int total = 0;
        int shortage = 0;
        for (SummaryItem item : summaryItems()) {
            total += item.count();
            shortage += item.shortage();
        }
        return new SummaryStats(total, shortage);
    }

    private List<SummaryItem> summaryItems() {
        List<OrderRequestSelection> selections = requestMode == OrderRequestMode.RATIO ? ratioPreviewSelections() : selectedSelections;
        List<SummaryAccumulator> accumulators = new ArrayList<>();
        for (OrderRequestSelection selection : selections) {
            if (!selection.isValid()) {
                continue;
            }
            SummaryAccumulator accumulator = findSummaryAccumulator(accumulators, selection.stack());
            if (accumulator == null) {
                accumulator = new SummaryAccumulator(selection.stack());
                accumulators.add(accumulator);
            }
            accumulator.add(selection.count());
        }

        List<SummaryItem> items = new ArrayList<>();
        List<StockCounter> stockCounters = stockCounters();
        for (SummaryAccumulator accumulator : accumulators) {
            StockCounter counter = findStockCounter(stockCounters, accumulator.stack());
            int available = counter == null ? 0 : counter.remaining();
            int missing = Math.max(0, accumulator.count() - available);
            if (counter != null) {
                counter.consume(accumulator.count());
            }
            items.add(new SummaryItem(accumulator.stack(), accumulator.count(), missing));
        }
        return items;
    }

    private SummaryAccumulator findSummaryAccumulator(List<SummaryAccumulator> accumulators, ItemStack stack) {
        for (SummaryAccumulator accumulator : accumulators) {
            if (ItemStack.isSameItemSameTags(accumulator.stack(), stack)) {
                return accumulator;
            }
        }
        return null;
    }

    private List<StockCounter> stockCounters() {
        List<StockCounter> counters = new ArrayList<>();
        for (OrderEntryCandidates group : menu.getCandidateGroups()) {
            for (OrderCandidate candidate : group.candidates()) {
                StockCounter counter = findStockCounter(counters, candidate.stack());
                if (counter == null) {
                    counters.add(new StockCounter(candidate.stack(), candidate.count()));
                } else {
                    counter.setAvailable(Math.max(counter.available(), candidate.count()));
                }
            }
        }
        return counters;
    }

    private StockCounter findStockCounter(List<StockCounter> counters, ItemStack stack) {
        for (StockCounter counter : counters) {
            if (ItemStack.isSameItemSameTags(counter.stack(), stack)) {
                return counter;
            }
        }
        return null;
    }

    private String scoreLabel(double score) {
        return score <= 0 ? "--" : String.format(java.util.Locale.ROOT, "%.2f", score);
    }

    private boolean isMinusButton(int localX) {
        int x = PANEL_X + PANEL_WIDTH - 51;
        return localX >= x && localX < x + 11;
    }

    private boolean isPlusButton(int localX) {
        int x = PANEL_X + PANEL_WIDTH - 10;
        return localX >= x && localX < x + 11;
    }

    private int adjustStep() {
        return hasShiftDown() ? 16 : 1;
    }

    private void renderHelpTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (localX < HELP_X || localX >= HELP_X + HELP_SIZE || localY < HELP_Y || localY >= HELP_Y + HELP_SIZE) {
            return;
        }
        if (requestMode == OrderRequestMode.RATIO) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("createdelightcore.gui.help.ratio_select"),
                    Component.translatable("createdelightcore.gui.help.ratio_weight"),
                    Component.translatable("createdelightcore.gui.help.ratio_planned"),
                    Component.translatable("createdelightcore.gui.help.ratio_shortage"),
                    Component.translatable("createdelightcore.gui.help.ratio_missing"),
                    Component.translatable("createdelightcore.gui.help.score"),
                    Component.translatable("createdelightcore.gui.help.redstone")
            ), mouseX, mouseY);
        } else {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("createdelightcore.gui.help.select"),
                    Component.translatable("createdelightcore.gui.help.multi"),
                    Component.translatable("createdelightcore.gui.help.quantity"),
                    Component.translatable("createdelightcore.gui.help.cancel"),
                    Component.translatable("createdelightcore.gui.help.score")
            ), mouseX, mouseY);
        }
    }

    private void renderQuantityTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        CandidateLine line = getHoveredCandidateLine(mouseX, mouseY);
        if (line == null || line.candidate == null) {
            return;
        }
        if (requestMode == OrderRequestMode.RATIO && findRatio(line) == null) {
            return;
        }
        if (requestMode != OrderRequestMode.RATIO && findSelection(line) == null) {
            return;
        }
        int localX = mouseX - leftPos;
        if (!isMinusButton(localX) && !isPlusButton(localX)) {
            return;
        }
        graphics.renderTooltip(font, Component.translatable(requestMode == OrderRequestMode.RATIO
                ? "createdelightcore.gui.weight_hint"
                : "createdelightcore.gui.quantity_hint"), mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        graphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
    }

    private void drawInset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        graphics.fill(x, y, x + width, y + 1, 0xFF555555);
        graphics.fill(x, y, x + 1, y + height, 0xFF555555);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        graphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
    }

    private String trim(String text, int width) {
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(8, width - font.width("..."))) + "...";
    }

    private record CandidateLine(int groupIndex, OrderEntry entry, OrderCandidate candidate,
                                 OrderRequestRatioSelection ratioOnly, SummaryItem summaryItem, boolean summary) {
        private static CandidateLine summaryLine() {
            return new CandidateLine(-1, null, null, null, null, true);
        }

        private static CandidateLine summaryItemLine(SummaryItem item) {
            return new CandidateLine(-1, null, null, null, item, false);
        }

        private boolean isSummary() {
            return summary;
        }

        private boolean isSummaryItem() {
            return summaryItem != null;
        }

        private boolean isHeader() {
            return !summary && summaryItem == null && candidate == null && ratioOnly == null;
        }
    }

    private record SummaryStats(int total, int shortage) {
    }

    private record SummaryItem(ItemStack stack, int count, int shortage) {
        private SummaryItem {
            stack = stack == null ? ItemStack.EMPTY : stack.copyWithCount(1);
            count = Math.max(0, count);
            shortage = Math.max(0, shortage);
        }
    }

    private static final class SummaryAccumulator {
        private final ItemStack stack;
        private int count;

        private SummaryAccumulator(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
        }

        private ItemStack stack() {
            return stack;
        }

        private int count() {
            return count;
        }

        private void add(int amount) {
            count += Math.max(0, amount);
        }
    }

    private static final class StockCounter {
        private final ItemStack stack;
        private int available;
        private int used;

        private StockCounter(ItemStack stack, int available) {
            this.stack = stack.copyWithCount(1);
            this.available = available;
        }

        private ItemStack stack() {
            return stack;
        }

        private int available() {
            return available;
        }

        private void setAvailable(int available) {
            this.available = available;
        }

        private int remaining() {
            return Math.max(0, available - used);
        }

        private void consume(int count) {
            used += Math.max(0, count);
        }
    }
}
