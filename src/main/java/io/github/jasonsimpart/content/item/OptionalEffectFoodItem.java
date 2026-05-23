package io.github.jasonsimpart.content.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendOptionalEffectTooltip(optionalEffects, context, tooltip);
    }

    public static void appendOptionalEffectTooltip(List<OptionalEffect> optionalEffects, TooltipContext context, List<Component> tooltip) {
        HolderLookup.Provider registries = context.registries();
        if (registries == null || optionalEffects.isEmpty()) {
            return;
        }

        List<MobEffectInstance> effects = new ArrayList<>();
        HolderLookup.RegistryLookup<MobEffect> effectRegistry = registries.lookupOrThrow(Registries.MOB_EFFECT);
        optionalEffects.forEach(optionalEffect -> effectRegistry.get(optionalEffect.key())
                .ifPresent(effect -> effects.add(new MobEffectInstance(effect, optionalEffect.duration(), optionalEffect.amplifier()))));
        if (!effects.isEmpty()) {
            PotionContents.addPotionTooltip(effects, tooltip::add, 1.0F, 20.0F);
        }
    }

    public record OptionalEffect(ResourceKey<MobEffect> key, int duration, int amplifier) {
        public static OptionalEffect of(String namespace, String path, int duration, int amplifier) {
            return new OptionalEffect(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(namespace, path)), duration, amplifier);
        }
    }
}
