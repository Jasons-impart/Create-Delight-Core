package io.github.jasonsimpart.createdelightcore.compat.fruitsdelight;

import com.gumillea.cosmopolitan.core.reg.CosmoEffects;
import dev.xkmc.fruitsdelight.init.food.EffectEntry;
import dev.xkmc.fruitsdelight.init.food.EffectFunc;
import dev.xkmc.fruitsdelight.init.food.FoodType;
import dev.xkmc.fruitsdelight.init.food.FruitType;
import dev.xkmc.fruitsdelight.init.food.IFDFood;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;

import java.util.List;

public final class LushConfitureFood {
    public static final String FRUIT_NAME = "LUSH_CONFITURE";

    public static final IFDFood FOOD = new IFDFood() {
        @Override
        public FruitType fruit() {
            return LushConfitureFood.fruit();
        }

        @Override
        public FoodType getType() {
            return FoodType.JELLY;
        }

        @Override
        public EffectEntry[] getEffects() {
            return new EffectEntry[0];
        }
    };
    public static final IFDFood JELLO_FOOD = new IFDFood() {
        @Override
        public FruitType fruit() {
            return LushConfitureFood.fruit();
        }

        @Override
        public FoodType getType() {
            return FoodType.JELLO;
        }

        @Override
        public EffectEntry[] getEffects() {
            return new EffectEntry[0];
        }
    };

    public static FruitType fruit() {
        return Holder.FRUIT;
    }

    public static IFDFood food() {
        return FOOD;
    }

    public static IFDFood jelloFood() {
        return JELLO_FOOD;
    }

    public static void bootstrap() {
        fruit();
    }

    private static class Holder {
        private static final FruitType FRUIT = CustomFDFruits.register(
                FRUIT_NAME,
                4,
                0xF0612E,
                CDBlocks.LUSH_CONFITURE::asItem,
                null,
                List.of(
                        new EffectFunc(CosmoEffects.PHOTOTAXIS, lv -> lv * 30),
                        new EffectFunc(CosmoEffects.TRACER, lv -> lv * 30)),
                CDBlocks.LUSH_CONFITURE::asItem,
                CDItems.LUSH_CONFITURE_JELLO::get);
    }

    private LushConfitureFood() {
    }
}
