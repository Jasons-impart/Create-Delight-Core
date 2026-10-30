package io.github.jasonsimpart.createdelightcore.content.cooler;

import com.simibubi.create.content.processing.recipe.HeatCondition;

public final class CoolerHeat {
    public static boolean isCooling(HeatCondition condition) {
        return condition.name().equals("COOLED") || isFreezing(condition);
    }

    public static boolean isFreezing(HeatCondition condition) {
        return condition.name().equals("FROZEN");
    }
}
