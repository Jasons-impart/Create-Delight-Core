package io.github.jasonsimpart.createdelightcore.content.order.board;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderCustomerData;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDataManager;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderDraftSealData;
import io.github.jasonsimpart.createdelightcore.content.order.data.OrderMarketSaturationData;
import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OrderBoardState {
    private static final String STORAGE_KEY = "createdelight_order_board";
    private static final int STATE_REVISION = 4;
    private static final int REROLL_BASE_COST = 64;
    private static final int[] REPUTATION_THRESHOLDS = {0, 10, 20, 40, 60, 100};
    private static final int[] GRADE_MAX_CUSTOMER_RARITY = {0, 1, 2, 2, 3, 3};
    private static final Set<String> BROAD_CATEGORIES = Set.of("food", "drink", "dessert", "staple_food");
    private static final GradeProfile[] GRADES = {
            new GradeProfile(32, 96),
            new GradeProfile(64, 160),
            new GradeProfile(128, 320),
            new GradeProfile(192, 512),
            new GradeProfile(320, 768),
            new GradeProfile(512, 1024)
    };
    private static final double[][][] GRADE_WEIGHTS = {
            {{1, 1.0D}},
            {{1, 0.45D}, {2, 0.55D}},
            {{2, 0.55D}, {3, 0.45D}},
            {{2, 0.20D}, {3, 0.50D}, {4, 0.30D}},
            {{3, 0.20D}, {4, 0.50D}, {5, 0.30D}},
            {{4, 0.20D}, {5, 0.50D}, {6, 0.30D}}
    };

    private OrderBoardState() {
    }

    public static Snapshot getOrCreate(ServerPlayer player, OrderBoardBlockEntity board) {
        long day = currentDay(player);
        int reputationLevel = reputationLevel(kubeJsPersistentData(player).getInt("order_reputation"));
        CompoundTag root = player.getPersistentData().getCompound(STORAGE_KEY);
        int rerollCount = root.getInt("Revision") == STATE_REVISION && root.getLong("Day") == day
                ? Math.max(0, root.getInt("RerollCount"))
                : 0;
        if (root.getInt("Revision") != STATE_REVISION
                || root.getLong("Day") != day
                || root.getLong("DataVersion") != OrderDataManager.version()
                || (!root.getBoolean("Accepted") && root.getInt("ReputationLevel") != reputationLevel)
                || !validCandidates(root, reputationLevel)) {
            root = generate(player, board, day, rerollCount);
            player.getPersistentData().put(STORAGE_KEY, root);
        }
        return snapshot(player, root);
    }

    public static AcceptResult accept(ServerPlayer player, OrderBoardBlockEntity board, int index) {
        Snapshot current = getOrCreate(player, board);
        if (current.accepted()) {
            return new AcceptResult(current, false, "createdelightcore.order_board.already_accepted");
        }
        if (index < 0 || index >= current.candidates().size()) {
            return new AcceptResult(current, false, "createdelightcore.order_board.invalid_candidate");
        }

        OrderBoardCandidate candidate = current.candidates().get(index);
        if (!isValidCandidate(candidate, reputationLevel(kubeJsPersistentData(player).getInt("order_reputation")))) {
            CompoundTag stored = player.getPersistentData().getCompound(STORAGE_KEY);
            CompoundTag regenerated = generate(player, board, currentDay(player),
                    Math.max(0, stored.getInt("RerollCount")));
            player.getPersistentData().put(STORAGE_KEY, regenerated);
            return new AcceptResult(snapshot(player, regenerated), false,
                    "createdelightcore.order_board.candidates_refreshed");
        }

        ItemStack draft = candidate.createDraftStack();
        if (draft.isEmpty()) {
            return new AcceptResult(current, false, "createdelightcore.order_board.missing_draft_item");
        }

        CompoundTag root = player.getPersistentData().getCompound(STORAGE_KEY);
        root.putBoolean("Accepted", true);
        root.putInt("AcceptedIndex", index);
        player.getPersistentData().put(STORAGE_KEY, root);
        if (!player.addItem(draft)) {
            player.drop(draft, false);
        }
        return new AcceptResult(snapshot(player, root), true, "createdelightcore.order_board.accepted");
    }

    public static RerollResult reroll(ServerPlayer player, OrderBoardBlockEntity board) {
        Snapshot current = getOrCreate(player, board);
        if (current.accepted()) {
            return new RerollResult(current, false,
                    Component.translatable("createdelightcore.order_board.reroll_after_accept"));
        }

        CompoundTag stored = player.getPersistentData().getCompound(STORAGE_KEY);
        int rerollCount = Math.max(0, stored.getInt("RerollCount"));
        int reputationLevel = reputationLevel(kubeJsPersistentData(player).getInt("order_reputation"));
        int cost = rerollCost(reputationLevel, rerollCount);
        MoneyValue moneyCost = MoneyUtil.baseCoinNumberToCoinValue(cost);
        if (!player.getAbilities().instabuild && !MoneyUtil.playerCanAfford(player, moneyCost)) {
            return new RerollResult(current, false,
                    Component.translatable("createdelightcore.order_board.reroll_insufficient", moneyCost.getText()));
        }

        CompoundTag next = generate(player, board, currentDay(player), rerollCount + 1);
        if (!validCandidates(next, reputationLevel)) {
            return new RerollResult(current, false,
                    Component.translatable("createdelightcore.order_board.reroll_unavailable"));
        }
        if (!player.getAbilities().instabuild) {
            MoneyAPI.getApi().GetPlayersMoneyHandler(player).extractMoney(moneyCost, false);
        }
        player.getPersistentData().put(STORAGE_KEY, next);
        return new RerollResult(snapshot(player, next), true,
                Component.translatable("createdelightcore.order_board.rerolled", moneyCost.getText()));
    }

    private static CompoundTag generate(ServerPlayer player, OrderBoardBlockEntity board, long day, int rerollCount) {
        int reputationLevel = reputationLevel(kubeJsPersistentData(player).getInt("order_reputation"));
        int safeRerollCount = Math.max(0, rerollCount);
        CompoundTag root = new CompoundTag();
        root.putInt("Revision", STATE_REVISION);
        root.putLong("Day", day);
        root.putLong("DataVersion", OrderDataManager.version());
        root.putInt("ReputationLevel", reputationLevel);
        root.putInt("RerollCount", safeRerollCount);
        root.putBoolean("Accepted", false);
        root.putInt("AcceptedIndex", -1);

        long seed = player.getUUID().getMostSignificantBits()
                ^ player.getUUID().getLeastSignificantBits()
                ^ day * 0x9E3779B97F4A7C15L
                ^ (long) safeRerollCount * 0xD1B54A32D192ED03L
                ^ (board == null ? 0L : board.getBlockPos().asLong());
        RandomSource random = RandomSource.create(seed);

        MarketSnapshot market = readMarket(player);
        InventorySummary warehouse = board == null ? null : board.getOrderBoardSummary();
        List<SealChoice> customers = sealChoices("customer", reputationLevel, board, warehouse, market);
        List<SealChoice> categories = sealChoices("category", reputationLevel, board, warehouse, market);
        List<OrderBoardCandidate> candidates = new ArrayList<>();
        if (!customers.isEmpty() && !categories.isEmpty()) {
            int adaptedGrade = chooseGrade(reputationLevel, random);
            List<FixedChoice> adaptedChoices = fixedChoices(reputationLevel, adaptedGrade, customers, categories,
                    board, warehouse, market, Set.of());
            if (!adaptedChoices.isEmpty()) {
                FixedChoice adapted = weightedFixedChoice(random, adaptedChoices, true);
                FixedSelection adaptedSelection = extendFixedSelection("adapted", adapted, adaptedChoices,
                        reputationLevel, adaptedGrade, random);
                candidates.add(createFixedCandidate("adapted", adaptedSelection, adaptedGrade));

                int expansionGrade = chooseGrade(reputationLevel, random);
                List<FixedChoice> expansionChoices = fixedChoices(reputationLevel, expansionGrade, customers, categories,
                        board, warehouse, market, Set.copyOf(adaptedSelection.categories()));
                if (expansionChoices.isEmpty()) {
                    expansionChoices = fixedChoices(reputationLevel, expansionGrade, customers, categories,
                            board, warehouse, market, Set.of());
                }
                if (!expansionChoices.isEmpty()) {
                    FixedChoice expansion = weightedFixedChoice(random, expansionChoices, false);
                    FixedSelection expansionSelection = extendFixedSelection("expansion", expansion, expansionChoices,
                            reputationLevel, expansionGrade, random);
                    candidates.add(createFixedCandidate("expansion", expansionSelection, expansionGrade));
                }
            }

            Pair opportunity = bestOpportunity(random, customers, categories);
            candidates.add(createMenuCandidate("opportunity", opportunity.customer(), opportunity.category(),
                    reputationLevel, random));
        }

        ListTag list = new ListTag();
        candidates.forEach(candidate -> list.add(candidate.write()));
        root.put("Candidates", list);
        return root;
    }

    private static List<SealChoice> sealChoices(String type, int reputationLevel, OrderBoardBlockEntity board,
                                                 InventorySummary warehouse, MarketSnapshot market) {
        List<SealChoice> choices = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        OrderDataManager.draftSeals().values().forEach(seal -> {
            if (!type.equals(seal.type()) || !seen.add(seal.key())) {
                return;
            }
            if ("customer".equals(type) && !hasUnlockedCustomer(seal, reputationLevel)) {
                return;
            }
            int warehouseCount = "category".equals(type) && board != null
                    ? board.countAvailable(seal.spec(), warehouse)
                    : 0;
            choices.add(new SealChoice(seal.key(), seal, warehouseCount, marketPressure(seal, market)));
        });
        choices.sort(Comparator.comparing(SealChoice::key));
        return choices;
    }

    private static boolean hasUnlockedCustomer(OrderDraftSealData seal, int reputationLevel) {
        for (String group : seal.spec().customerGroups()) {
            String prefix = OrderDataManager.customerGroupPrefix(group);
            if (prefix == null) {
                continue;
            }
            for (Map.Entry<String, OrderCustomerData> entry : OrderDataManager.customers().entrySet()) {
                if (entry.getKey().startsWith(prefix) && customerUnlockLevel(entry.getValue()) <= reputationLevel) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int customerUnlockLevel(OrderCustomerData customer) {
        return switch (customer.rarity()) {
            case "UNCOMMON" -> 2;
            case "RARE" -> 4;
            case "EPIC" -> 6;
            default -> 1;
        };
    }

    private static List<FixedChoice> fixedChoices(int reputationLevel, int grade,
                                                   List<SealChoice> customers, List<SealChoice> menus,
                                                   OrderBoardBlockEntity board, InventorySummary warehouse,
                                                   MarketSnapshot market, Set<String> excludedCategories) {
        List<FixedChoice> choices = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (SealChoice customer : customers) {
            for (SealChoice menu : menus) {
                for (String group : menu.data().spec().categoryGroups()) {
                    for (String category : OrderDataManager.categoryGroup(group).keySet()) {
                        if (excludedCategories.contains(category)
                                || BROAD_CATEGORIES.contains(category)
                                || !OrderDataManager.hasOrderType(category)
                                || !supportsRequiredCategories(customer.data(), List.of(category), reputationLevel, grade)) {
                            continue;
                        }
                        String identity = customer.key() + '\u0000' + menu.key() + '\u0000' + category;
                        if (!seen.add(identity)) {
                            continue;
                        }
                        int warehouseCount = board == null ? 0 : board.countAvailable(category, warehouse);
                        double pressure = market.categories().getOrDefault(category, 0.0D);
                        choices.add(new FixedChoice(customer, menu, category, warehouseCount, pressure));
                    }
                }
            }
        }
        choices.sort(Comparator.comparing((FixedChoice choice) -> choice.customer().key())
                .thenComparing(choice -> choice.menu().key())
                .thenComparing(FixedChoice::category));
        return choices;
    }

    private static FixedChoice weightedFixedChoice(RandomSource random, List<FixedChoice> choices, boolean preferStock) {
        if (choices.isEmpty()) {
            throw new IllegalStateException("Order board has no fixed category choices");
        }
        List<FixedChoice> pool = choices;
        if (preferStock && choices.stream().anyMatch(choice -> choice.warehouseCount() > 0)) {
            pool = choices.stream().filter(choice -> choice.warehouseCount() > 0).toList();
        }
        double total = 0.0D;
        List<Double> weights = new ArrayList<>();
        for (FixedChoice choice : pool) {
            double stockSignal = Math.min(3.0D, Math.log1p(choice.warehouseCount()) / Math.log(65.0D));
            double weight = preferStock ? 1.0D + stockSignal : 1.0D / (1.0D + stockSignal);
            weights.add(weight);
            total += weight;
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < pool.size(); i++) {
            roll -= weights.get(i);
            if (roll <= 0.0D) {
                return pool.get(i);
            }
        }
        return pool.get(pool.size() - 1);
    }

    private static FixedSelection extendFixedSelection(String kind, FixedChoice primary, List<FixedChoice> allChoices,
                                                       int reputationLevel, int grade, RandomSource random) {
        List<String> categories = new ArrayList<>();
        categories.add(primary.category());
        int target = requiredCategoryTarget(grade, random);
        while (categories.size() < target) {
            List<FixedChoice> compatible = allChoices.stream()
                    .filter(choice -> choice.customer().key().equals(primary.customer().key()))
                    .filter(choice -> choice.menu().key().equals(primary.menu().key()))
                    .filter(choice -> !categories.contains(choice.category()))
                    .filter(choice -> !("adapted".equals(kind) || "expansion".equals(kind))
                            || choice.warehouseCount() > 0)
                    .filter(choice -> {
                        List<String> proposed = new ArrayList<>(categories);
                        proposed.add(choice.category());
                        return supportsRequiredCategories(primary.customer().data(), proposed, reputationLevel, grade);
                    })
                    .toList();
            if (compatible.isEmpty()) {
                break;
            }
            categories.add(weightedFixedChoice(random, compatible, true).category());
        }
        int warehouseCount = allChoices.stream()
                .filter(choice -> choice.customer().key().equals(primary.customer().key()))
                .filter(choice -> choice.menu().key().equals(primary.menu().key()))
                .filter(choice -> categories.contains(choice.category()))
                .mapToInt(FixedChoice::warehouseCount)
                .sum();
        double marketPressure = categories.stream()
                .mapToDouble(category -> allChoices.stream()
                        .filter(choice -> choice.customer().key().equals(primary.customer().key()))
                        .filter(choice -> choice.menu().key().equals(primary.menu().key()))
                        .filter(choice -> choice.category().equals(category))
                        .mapToDouble(FixedChoice::categoryMarketPressure)
                        .findFirst()
                        .orElse(0.0D))
                .average()
                .orElse(0.0D);
        return new FixedSelection(primary.customer(), primary.menu(), List.copyOf(categories),
                warehouseCount, marketPressure);
    }

    private static int requiredCategoryTarget(int grade, RandomSource random) {
        if (grade <= 2) {
            return 1;
        }
        if (grade <= 4) {
            return random.nextDouble() < 0.30D ? 2 : 1;
        }
        double roll = random.nextDouble();
        if (roll < 0.15D) {
            return 3;
        }
        return roll < 0.75D ? 2 : 1;
    }

    private static boolean supportsRequiredCategories(OrderDraftSealData customerSeal, List<String> categories,
                                                      int reputationLevel, int grade) {
        int maxRarity = GRADE_MAX_CUSTOMER_RARITY[Math.max(0,
                Math.min(GRADE_MAX_CUSTOMER_RARITY.length - 1, grade - 1))];
        for (String group : customerSeal.spec().customerGroups()) {
            String prefix = OrderDataManager.customerGroupPrefix(group);
            if (prefix == null) {
                continue;
            }
            for (Map.Entry<String, OrderCustomerData> entry : OrderDataManager.customers().entrySet()) {
                OrderCustomerData customer = entry.getValue();
                if (entry.getKey().startsWith(prefix)
                        && customerUnlockLevel(customer) <= reputationLevel
                        && customerRarityRank(customer) <= maxRarity
                        && customer.maxCount() >= categories.size()
                        && categories.stream().allMatch(customer.entries()::containsKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int customerRarityRank(OrderCustomerData customer) {
        return switch (customer.rarity()) {
            case "UNCOMMON" -> 1;
            case "RARE" -> 2;
            case "EPIC" -> 3;
            default -> 0;
        };
    }

    private static Pair bestOpportunity(RandomSource random, List<SealChoice> customers,
                                        List<SealChoice> categories) {
        Pair best = null;
        double bestMultiplier = Double.NEGATIVE_INFINITY;
        int tied = 0;
        for (SealChoice customer : customers) {
            for (SealChoice category : categories) {
                double multiplier = marketMultiplier(customer.marketPressure(), category.marketPressure());
                if (multiplier > bestMultiplier + 1.0E-9D) {
                    best = new Pair(customer, category);
                    bestMultiplier = multiplier;
                    tied = 1;
                } else if (Math.abs(multiplier - bestMultiplier) <= 1.0E-9D
                        && random.nextInt(++tied) == 0) {
                    best = new Pair(customer, category);
                }
            }
        }
        return best;
    }

    private static OrderBoardCandidate createFixedCandidate(String kind, FixedSelection choice, int grade) {
        GradeProfile profile = GRADES[grade - 1];
        return new OrderBoardCandidate(
                kind,
                choice.customer().key(),
                choice.menu().key(),
                choice.categories(),
                grade,
                profile.minTotal(),
                profile.maxTotal(),
                choice.warehouseCount(),
                marketMultiplier(choice.customer().marketPressure(), choice.categoryMarketPressure())
        );
    }

    private static OrderBoardCandidate createMenuCandidate(String kind, SealChoice customer, SealChoice category,
                                                            int reputationLevel, RandomSource random) {
        int grade = chooseGrade(reputationLevel, random);
        GradeProfile profile = GRADES[grade - 1];
        return new OrderBoardCandidate(
                kind,
                customer.key(),
                category.key(),
                List.of(),
                grade,
                profile.minTotal(),
                profile.maxTotal(),
                category.warehouseCount(),
                marketMultiplier(customer.marketPressure(), category.marketPressure())
        );
    }

    private static int chooseGrade(int reputationLevel, RandomSource random) {
        double[][] weights = GRADE_WEIGHTS[Math.max(0, Math.min(GRADE_WEIGHTS.length - 1, reputationLevel - 1))];
        double total = 0.0D;
        for (double[] weight : weights) {
            total += weight[1];
        }
        double roll = random.nextDouble() * total;
        for (double[] weight : weights) {
            roll -= weight[1];
            if (roll <= 0.0D) {
                return (int) weight[0];
            }
        }
        return (int) weights[weights.length - 1][0];
    }

    private static double marketMultiplier(double customerPressure, double categoryPressure) {
        OrderMarketSaturationData config = OrderDataManager.marketSaturation();
        double rawConsumption = categoryPressure * config.categoryPenalty()
                + customerPressure * config.customerPenalty();
        double consumed = Math.min(config.maxBonus(), Math.max(0.0D, rawConsumption));
        return 1.0D + Math.max(0.0D, config.maxBonus() - consumed);
    }

    private static MarketSnapshot readMarket(ServerPlayer player) {
        OrderMarketSaturationData config = OrderDataManager.marketSaturation();
        String raw = kubeJsPersistentData(player).getString(config.storageKey());
        if (raw == null || raw.isBlank()) {
            return MarketSnapshot.EMPTY;
        }
        try {
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            long day = currentDay(player);
            long lastDay = root.has("lastDay") ? root.get("lastDay").getAsLong() : day;
            double decay = Math.pow(config.decayPerDay(), Math.max(0L, day - lastDay));
            return new MarketSnapshot(numberMap(root.get("categories"), decay), numberMap(root.get("customers"), decay));
        } catch (RuntimeException ignored) {
            return MarketSnapshot.EMPTY;
        }
    }

    private static Map<String, Double> numberMap(JsonElement element, double multiplier) {
        if (element == null || !element.isJsonObject()) {
            return Map.of();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        element.getAsJsonObject().entrySet().forEach(entry ->
                values.put(entry.getKey(), Math.max(0.0D, entry.getValue().getAsDouble() * multiplier)));
        return values;
    }

    private static double marketPressure(OrderDraftSealData seal, MarketSnapshot market) {
        List<Double> values = new ArrayList<>();
        if ("category".equals(seal.type())) {
            seal.spec().categoryGroups().forEach(group -> OrderDataManager.categoryGroup(group).keySet().forEach(category ->
                    values.add(market.categories().getOrDefault(category, 0.0D))));
        } else if ("customer".equals(seal.type())) {
            seal.spec().customerGroups().forEach(group -> {
                String prefix = OrderDataManager.customerGroupPrefix(group);
                if (prefix != null) {
                    OrderDataManager.customers().keySet().stream()
                            .filter(customer -> customer.startsWith(prefix))
                            .forEach(customer -> values.add(market.customers().getOrDefault(customer, 0.0D)));
                }
            });
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0D);
    }

    private static boolean validCandidates(CompoundTag root, int reputationLevel) {
        ListTag list = root.getList("Candidates", Tag.TAG_COMPOUND);
        if (list.size() != 3) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            OrderBoardCandidate candidate = OrderBoardCandidate.read(list.getCompound(i));
            if (!isValidCandidate(candidate, reputationLevel)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidSeal(String type, String key) {
        return OrderDataManager.draftSeals().values().stream()
                .anyMatch(seal -> type.equals(seal.type()) && key.equals(seal.key()));
    }

    private static boolean isValidCandidate(OrderBoardCandidate candidate, int reputationLevel) {
        if (!isValidSeal("customer", candidate.customerSeal())
                || !isValidSeal("category", candidate.categorySeal())) {
            return false;
        }
        boolean fixedCategory = "adapted".equals(candidate.kind()) || "expansion".equals(candidate.kind());
        if (!fixedCategory) {
            return candidate.requiredCategories().isEmpty();
        }
        if (candidate.requiredCategories().isEmpty()
                || candidate.requiredCategories().stream().anyMatch(category -> !OrderDataManager.hasOrderType(category))) {
            return false;
        }
        OrderDraftSealData categorySeal = findSeal("category", candidate.categorySeal());
        OrderDraftSealData customerSeal = findSeal("customer", candidate.customerSeal());
        if (categorySeal == null || customerSeal == null
                || candidate.requiredCategories().stream().anyMatch(category -> !sealContainsCategory(categorySeal, category))) {
            return false;
        }
        return supportsRequiredCategories(customerSeal, candidate.requiredCategories(), reputationLevel, candidate.grade());
    }

    private static OrderDraftSealData findSeal(String type, String key) {
        return OrderDataManager.draftSeals().values().stream()
                .filter(seal -> type.equals(seal.type()) && key.equals(seal.key()))
                .findFirst()
                .orElse(null);
    }

    private static boolean sealContainsCategory(OrderDraftSealData seal, String category) {
        return seal.spec().categoryGroups().stream()
                .anyMatch(group -> OrderDataManager.categoryGroup(group).containsKey(category));
    }

    private static Snapshot snapshot(ServerPlayer player, CompoundTag root) {
        List<OrderBoardCandidate> candidates = new ArrayList<>();
        ListTag list = root.getList("Candidates", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            candidates.add(OrderBoardCandidate.read(list.getCompound(i)));
        }
        int reputationLevel = reputationLevel(kubeJsPersistentData(player).getInt("order_reputation"));
        int nextRerollCost = rerollCost(reputationLevel, Math.max(0, root.getInt("RerollCount")));
        return new Snapshot(List.copyOf(candidates), root.getBoolean("Accepted"), ticksUntilRefresh(player),
                nextRerollCost);
    }

    private static int reputationLevel(int value) {
        int safe = Math.max(0, value);
        for (int i = REPUTATION_THRESHOLDS.length - 1; i >= 0; i--) {
            if (safe >= REPUTATION_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    static int rerollCost(int reputationLevel, int rerollCount) {
        long cost = (long) REROLL_BASE_COST
                * Math.max(1, Math.min(REPUTATION_THRESHOLDS.length, reputationLevel))
                * (Math.max(0, rerollCount) + 1L);
        return (int) Math.min(Integer.MAX_VALUE, cost);
    }

    private static CompoundTag kubeJsPersistentData(ServerPlayer player) {
        try {
            Object value = player.getClass().getMethod("kjs$getPersistentData").invoke(player);
            if (value instanceof CompoundTag tag) {
                return tag;
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
        }
        return player.getPersistentData();
    }

    private static long currentDay(ServerPlayer player) {
        return Math.floorDiv(player.serverLevel().getDayTime(), 24000L);
    }

    private static int ticksUntilRefresh(ServerPlayer player) {
        long time = Math.floorMod(player.serverLevel().getDayTime(), 24000L);
        return (int) Math.max(0L, 24000L - time);
    }

    public record Snapshot(List<OrderBoardCandidate> candidates, boolean accepted, int ticksUntilRefresh,
                           int rerollCost) {
    }

    public record AcceptResult(Snapshot snapshot, boolean success, String messageKey) {
    }

    public record RerollResult(Snapshot snapshot, boolean success, Component message) {
    }

    private record GradeProfile(int minTotal, int maxTotal) {
    }

    private record SealChoice(String key, OrderDraftSealData data, int warehouseCount, double marketPressure) {
    }

    private record FixedChoice(SealChoice customer, SealChoice menu, String category,
                               int warehouseCount, double categoryMarketPressure) {
    }

    private record FixedSelection(SealChoice customer, SealChoice menu, List<String> categories,
                                  int warehouseCount, double categoryMarketPressure) {
    }

    private record Pair(SealChoice customer, SealChoice category) {
    }

    private record MarketSnapshot(Map<String, Double> categories, Map<String, Double> customers) {
        private static final MarketSnapshot EMPTY = new MarketSnapshot(Map.of(), Map.of());
    }
}
