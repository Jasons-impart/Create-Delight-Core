package io.github.jasonsimpart.createdelightcore.content.order.data;

public record OrderTypeData(double[] diversity, int baseCount) {
    public OrderTypeData {
        diversity = diversity == null ? new double[0] : diversity.clone();
        baseCount = Math.max(1, baseCount);
    }

    @Override
    public double[] diversity() {
        return diversity.clone();
    }
}
