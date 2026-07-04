package io.github.jasonsimpart.createdelightcore.content.order.data;

public record OrderMarketSaturationData(String storageKey, double decayPerDay,
                                        double categoryPenalty, double customerPenalty, double maxPenalty,
                                        double categoryCompletionGain, double categoryCompletionScaleMax,
                                        double customerCompletionGain,
                                        double categoryCrossRecovery, double customerCrossRecovery) {
    public static final OrderMarketSaturationData DEFAULT = new OrderMarketSaturationData(
            "createdelight_order_market_saturation",
            0.72, 0.08, 0.05, 0.35,
            0.35, 2.0, 0.4,
            0.94, 0.94
    );

    public OrderMarketSaturationData {
        storageKey = storageKey == null || storageKey.isBlank()
                ? "createdelight_order_market_saturation"
                : storageKey;
    }
}
