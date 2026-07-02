package io.github.jasonsimpart.createdelightcore.content.order;

public enum OrderRequestMode {
    FIXED_COUNT,
    RATIO;

    public static OrderRequestMode byName(String name) {
        for (OrderRequestMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return FIXED_COUNT;
    }
}
