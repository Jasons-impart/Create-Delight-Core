package io.github.jasonsimpart.createdelightcore.compat.fruitsdelight;

import dev.xkmc.fruitsdelight.init.food.EffectFunc;
import dev.xkmc.fruitsdelight.init.food.FruitType;
import io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight.FruitTypeAccessor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class CustomFDFruits {
    private static final Map<String, Entry> BY_NAME = new LinkedHashMap<>();
    private static final Map<FruitType, Entry> BY_TYPE = new IdentityHashMap<>();
    private static int nextOrdinal = FruitType.values().length;
    private static boolean bootstrapped;

    public static FruitType register(String name, int jellyCost, int color, Supplier<Item> fruit, TagKey<Item> tag,
                                     List<EffectFunc> effects, Supplier<Item> jelly, Supplier<Item> jello) {
        Entry existing = BY_NAME.get(name);
        if (existing != null) {
            return existing.fruit();
        }

        FruitType fruitType = FruitTypeAccessor.createdelightcore$create(
                name,
                nextOrdinal++,
                jellyCost,
                color,
                fruit,
                tag,
                effects);
        Entry entry = new Entry(fruitType, jelly, jello);
        BY_NAME.put(name, entry);
        BY_TYPE.put(fruitType, entry);
        return fruitType;
    }

    public static Optional<FruitType> getFruit(String name) {
        bootstrap();
        Entry entry = BY_NAME.get(name);
        return entry == null ? Optional.empty() : Optional.of(entry.fruit());
    }

    public static Optional<Item> getJelly(FruitType fruit) {
        bootstrap();
        Entry entry = BY_TYPE.get(fruit);
        return entry == null ? Optional.empty() : Optional.of(entry.jelly().get());
    }

    public static Optional<Item> getJello(FruitType fruit) {
        bootstrap();
        Entry entry = BY_TYPE.get(fruit);
        return entry == null ? Optional.empty() : Optional.of(entry.jello().get());
    }

    private static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        LushConfitureFood.bootstrap();
    }

    private record Entry(FruitType fruit, Supplier<Item> jelly, Supplier<Item> jello) {
    }

    private CustomFDFruits() {
    }
}
