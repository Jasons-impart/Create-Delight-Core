package io.github.jasonsimpart.createdelightcore.content.order.data;

import java.util.Map;

public record OrderCustomerData(Map<String, EntryData> entries, int maxCount, double baseContinueRate,
                                String rarity, double chance, String reward, int rewardCount, int rewardMoney) {
    public OrderCustomerData {
        entries = entries == null ? Map.of() : Map.copyOf(entries);
        maxCount = Math.max(1, maxCount);
        rarity = rarity == null ? "COMMON" : rarity;
        reward = reward == null ? "" : reward;
        rewardCount = Math.max(0, rewardCount);
        rewardMoney = Math.max(0, rewardMoney);
    }

    public record EntryData(double weight, int minQuality) {
        public EntryData {
            minQuality = Math.max(0, minQuality);
        }
    }
}
