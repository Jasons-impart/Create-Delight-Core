package io.github.jasonsimpart.createdelightcore.compat.jei.utils;

import java.math.BigDecimal;

/** Displays the exact duration provided by one fluid fuel batch. */
public final class FuelTimeFormatter {
    private FuelTimeFormatter() {
    }

    public static String format(int ticks) {
        int minutes = ticks / 1200;
        int remainingTicks = ticks % 1200;
        if (minutes > 0 && remainingTicks == 0) return minutes + " m";
        String seconds = BigDecimal.valueOf(remainingTicks).divide(BigDecimal.valueOf(20))
                .stripTrailingZeros().toPlainString() + " s";
        return minutes > 0 ? minutes + " m " + seconds : seconds;
    }
}
