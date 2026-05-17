package io.github.jasonsimpart.content.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class OptionalEffectFoodItem extends Item {
    private final List<OptionalEffect> optionalEffects;
    private final ItemLike returnedItem;
    private final boolean foiled;

    public OptionalEffectFoodItem(Properties properties, List<OptionalEffect> optionalEffects) {
        this(properties, optionalEffects, null, false);
    }

    public OptionalEffectFoodItem(Properties properties, List<OptionalEffect> optionalEffects, boolean foiled) {
        this(properties, optionalEffects, null, foiled);
    }

    public OptionalEffectFoodItem(Properties properties, List<OptionalEffect> optionalEffects, ItemLike returnedItem) {
        this(properties, optionalEffects, returnedItem, false);
    }

    public OptionalEffectFoodItem(Properties properties, List<OptionalEffect> optionalEffects, ItemLike returnedItem, boolean foiled) {
        super(properties);
        this.optionalEffects = List.copyOf(optionalEffects);
        this.returnedItem = returnedItem;
        this.foiled = foiled;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level instanceof ServerLevel serverLevel) {
            Registry<MobEffect> effects = serverLevel.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
            optionalEffects.forEach(optionalEffect -> effects.getHolder(optionalEffect.key())
                    .ifPresent(effect -> livingEntity.addEffect(new MobEffectInstance(effect, optionalEffect.duration(), optionalEffect.amplifier()))));
            if (returnedItem != null && livingEntity instanceof Player player && !player.isCreative()) {
                ItemStack returnStack = new ItemStack(returnedItem);
                if (!player.getInventory().add(returnStack)) {
                    player.drop(returnStack, false);
                }
            }
        }
        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return foiled || super.isFoil(stack);
    }

    public record OptionalEffect(ResourceKey<MobEffect> key, int duration, int amplifier) {
        public static OptionalEffect of(String namespace, String path, int duration, int amplifier) {
            return new OptionalEffect(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(namespace, path)), duration, amplifier);
        }
    }
}
