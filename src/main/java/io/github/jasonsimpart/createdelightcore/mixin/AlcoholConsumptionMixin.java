package io.github.jasonsimpart.createdelightcore.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.alcohol.AlcoholDrinks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class AlcoholConsumptionMixin {
    @WrapMethod(method = "finishUsingItem")
    private ItemStack createdelightcore$consumeAlcohol(Level level, LivingEntity consumer,
                                                       Operation<ItemStack> original) {
        var effects = AlcoholDrinks.effects((ItemStack) (Object) this);
        ItemStack result = original.call(level, consumer);
        AlcoholDrinks.consume(effects, consumer);
        return result;
    }
}
