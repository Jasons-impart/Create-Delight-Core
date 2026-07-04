package io.github.jasonsimpart.createdelightcore.mixin.fruitsdelight;

import dev.xkmc.fruitsdelight.init.food.EffectFunc;
import dev.xkmc.fruitsdelight.init.food.FruitType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Supplier;

@Mixin(value = FruitType.class, remap = false)
public interface FruitTypeAccessor {
    @Invoker("<init>")
    static FruitType createdelightcore$create(String name, int ordinal, int jellyCost, int color,
                                              Supplier<Item> fruit, TagKey<Item> tag,
                                              List<EffectFunc> effects) {
        throw new AssertionError();
    }
}
