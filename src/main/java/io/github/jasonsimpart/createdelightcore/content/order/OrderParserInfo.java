package io.github.jasonsimpart.createdelightcore.content.order;

import io.github.jasonsimpart.createdelightcore.content.order.data.OrderCustomerData;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDraftSealData;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderSpecData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class OrderParserInfo {
    public static final String DRAFT_ITEM_ID = "createdelight:unopened_order";
    public static final String SEAL_ITEM_ID = "createdelight:order_seal";

    private OrderParserInfo() {
    }

    public static boolean isParserStack(ItemStack stack) {
        return OrderInfo.isOrder(stack) || isItem(stack, DRAFT_ITEM_ID) || isItem(stack, SEAL_ITEM_ID);
    }

    public static List<OrderParserLine> describe(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of(line("createdelightcore.gui.parser.insert", "", 0x606060, true));
        }
        if (OrderInfo.isOrder(stack)) {
            return describeOrder(OrderInfo.fromStack(stack).orElse(null));
        }
        if (isItem(stack, DRAFT_ITEM_ID)) {
            return describeDraft(stack);
        }
        if (isItem(stack, SEAL_ITEM_ID)) {
            return describeSeal(readString(stack, "OrderSeal"));
        }
        return List.of(line("createdelightcore.gui.parser.unsupported", "", 0x803030, true));
    }

    private static List<OrderParserLine> describeOrder(OrderInfo order) {
        if (order == null) {
            return List.of(line("createdelightcore.gui.parser.no_data", "", 0x803030, true));
        }
        List<OrderParserLine> lines = new ArrayList<>();
        lines.add(line("createdelightcore.gui.parser.formal_order", order.type(), 0x303030, true));
        if (!order.ownerName().isBlank()) {
            lines.add(line("createdelightcore.gui.parser.owner", order.ownerName(), 0x404040, false));
        }
        lines.add(line("createdelightcore.gui.parser.reputation", Integer.toString(order.generatedReputationLevel()), 0x404040, false));
        lines.add(line("createdelightcore.gui.parser.order_entries", "", 0x303030, true));

        Map<String, EntrySummary> summaries = new LinkedHashMap<>();
        for (OrderEntry entry : order.entries()) {
            summaries.computeIfAbsent(entry.id(), ignored -> new EntrySummary()).add(entry);
        }
        summaries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> lines.add(categoryLine(entry.getKey(), entry.getValue().suffix())));
        return List.copyOf(lines);
    }

    private static List<OrderParserLine> describeDraft(ItemStack stack) {
        List<OrderParserLine> lines = new ArrayList<>();
        lines.add(line("createdelightcore.gui.parser.draft", "", 0x303030, true));
        CompoundTag draft = stack.hasTag() ? stack.getOrCreateTag().getCompound("OrderDraft") : new CompoundTag();
        String customerSeal = draft.getString("customerSeal");
        String categorySeal = draft.getString("categorySeal");
        if (customerSeal.isBlank() && categorySeal.isBlank()) {
            lines.add(line("createdelightcore.gui.parser.no_seal", "", 0x606060, false));
            return List.copyOf(lines);
        }
        if (!customerSeal.isBlank()) {
            appendSeal(lines, customerSeal);
        }
        if (!categorySeal.isBlank()) {
            appendSeal(lines, categorySeal);
        }
        return List.copyOf(lines);
    }

    private static List<OrderParserLine> describeSeal(String key) {
        List<OrderParserLine> lines = new ArrayList<>();
        if (key.isBlank()) {
            lines.add(line("createdelightcore.gui.parser.blank_seal", "", 0x606060, true));
            return List.copyOf(lines);
        }
        appendSeal(lines, key);
        return List.copyOf(lines);
    }

    private static void appendSeal(List<OrderParserLine> lines, String key) {
        Optional<OrderDraftSealData> optionalSeal = OrderDataManager.draftSeal(key);
        if (optionalSeal.isEmpty()) {
            lines.add(line("createdelightcore.gui.parser.unknown_seal", key, 0x803030, true));
            return;
        }

        OrderDraftSealData seal = optionalSeal.get();
        lines.add(new OrderParserLine(
                "createdelightcore.gui.parser.seal",
                seal.key(),
                "tooltip.createdelight.order_draft.seal." + seal.key(),
                "",
                0x303030,
                true
        ));
        OrderSpecData spec = seal.spec();
        if ("customer".equals(seal.type()) || !spec.customerGroups().isEmpty()) {
            lines.add(line("createdelightcore.gui.parser.possible_customers", "", 0x404040, true));
            appendCustomers(lines, spec);
        }
        if ("category".equals(seal.type()) || !spec.categoryGroups().isEmpty()) {
            lines.add(line("createdelightcore.gui.parser.possible_categories", "", 0x404040, true));
            appendCategories(lines, spec);
        }
        appendModifiers(lines, spec);
    }

    private static void appendCustomers(List<OrderParserLine> lines, OrderSpecData spec) {
        List<String> groups = spec.customerGroups();
        if (groups.isEmpty()) {
            lines.add(line("createdelightcore.gui.parser.no_data", "", 0x606060, false));
            return;
        }
        int startSize = lines.size();
        for (String group : groups) {
            String prefix = OrderDataManager.customerGroupPrefix(group);
            if (prefix == null || prefix.isBlank()) {
                continue;
            }
            OrderDataManager.customers().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> lines.add(customerLine(entry.getKey(), entry.getValue())));
        }
        if (lines.size() == startSize) {
            lines.add(line("createdelightcore.gui.parser.no_data", "", 0x606060, false));
        }
    }

    private static void appendCategories(List<OrderParserLine> lines, OrderSpecData spec) {
        List<String> groups = spec.categoryGroups();
        if (groups.isEmpty()) {
            lines.add(line("createdelightcore.gui.parser.no_data", "", 0x606060, false));
            return;
        }
        int startSize = lines.size();
        for (String group : groups) {
            Map<String, Double> categories = OrderDataManager.categoryGroup(group);
            categories.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> lines.add(weightedCategoryLine(entry.getKey(), weightSuffix(entry.getValue()))));
        }
        if (lines.size() == startSize) {
            lines.add(line("createdelightcore.gui.parser.no_data", "", 0x606060, false));
        }
    }

    private static void appendModifiers(List<OrderParserLine> lines, OrderSpecData spec) {
        if (spec.moneyMultiplier() != null) {
            lines.add(line("createdelightcore.gui.parser.money_multiplier", multiplier(spec.moneyMultiplier()), 0x606060, false));
        }
        if (spec.entryCountMultiplier() != null) {
            lines.add(line("createdelightcore.gui.parser.entry_count_multiplier", multiplier(spec.entryCountMultiplier()), 0x606060, false));
        }
        if (spec.countMultiplier() != null) {
            lines.add(line("createdelightcore.gui.parser.count_multiplier", multiplier(spec.countMultiplier()), 0x606060, false));
        }
        if (spec.minQualityBonus() != null) {
            lines.add(line("createdelightcore.gui.parser.min_quality_bonus", signed(spec.minQualityBonus()), 0x606060, false));
        }
    }

    private static OrderParserLine customerLine(String customerId, OrderCustomerData customer) {
        String suffix = Math.round(customer.chance() * 100.0D) + "%";
        return new OrderParserLine("", customerId, "tooltip.createdelight.order.customer." + customerId,
                suffix, 0x303030, false);
    }

    private static OrderParserLine categoryLine(String categoryId, String suffix) {
        return new OrderParserLine("", categoryId, "tooltip.createdelight.order.entries." + categoryId,
                suffix, 0x303030, false);
    }

    private static OrderParserLine weightedCategoryLine(String categoryId, String suffix) {
        return new OrderParserLine("", categoryId, "tooltip.createdelight.order.entries." + categoryId,
                suffix, "createdelightcore.gui.parser.weight", 0x303030, false);
    }

    private static OrderParserLine line(String labelKey, String value, int color, boolean heading) {
        return new OrderParserLine(labelKey, value, "", "", color, heading);
    }

    private static String readString(ItemStack stack, String key) {
        return stack.hasTag() && stack.getOrCreateTag().contains(key, Tag.TAG_STRING)
                ? stack.getOrCreateTag().getString(key)
                : "";
    }

    private static boolean isItem(ItemStack stack, String id) {
        return !stack.isEmpty() && id.equals(stack.getItem().builtInRegistryHolder().key().location().toString());
    }

    private static String weightSuffix(double value) {
        return trimNumber(value);
    }

    private static String multiplier(double value) {
        return "x" + trimNumber(value);
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    private static String trimNumber(double value) {
        if (Math.rint(value) == value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static final class EntrySummary {
        private int count;
        private int maxQuality;
        private int entries;

        private void add(OrderEntry entry) {
            count += Math.max(0, entry.count());
            maxQuality = Math.max(maxQuality, entry.minQuality());
            entries++;
        }

        private String suffix() {
            return "x" + count + "  Q" + maxQuality + (entries > 1 ? "  #" + entries : "");
        }
    }
}
