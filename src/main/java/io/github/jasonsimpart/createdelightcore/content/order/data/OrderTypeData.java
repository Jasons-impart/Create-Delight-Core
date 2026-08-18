package io.github.jasonsimpart.createdelightcore.content.order.data;

public record OrderTypeData(double[] diversity, int baseCount, double rewardWeight) {
    public OrderTypeData {
        diversity = diversity == null ? new double[0] : diversity.clone();
        baseCount = Math.max(1, baseCount);
        rewardWeight = Double.isFinite(rewardWeight) ? Math.max(0.1D, Math.min(4.0D, rewardWeight)) : 1.0D;
    }

    @Override
    public double[] diversity() {
        return diversity.clone();
    }
}
