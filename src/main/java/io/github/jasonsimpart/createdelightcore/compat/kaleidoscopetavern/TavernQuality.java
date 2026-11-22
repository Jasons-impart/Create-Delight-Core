package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.resources.DrinkEffectDataReloadListener;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import com.mojang.datafixers.util.Pair;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.FoodUtils;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsCreateRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tavern drinks use QF quality directly, including ordinary drinks without a quality tag. */
public final class TavernQuality {
    public static final int NORMAL_BREW_LEVEL = 3;
    public static final TagKey<Item> EXTERNAL_FERMENTED_DRINKS = TagKey.create(Registries.ITEM,
            new ResourceLocation("createdelight", "tavern_fermented_drinks"));
    private static final String TEXT = "tooltip.createdelightcore.tavern.";

    private TavernQuality() {}

    public static boolean isQualityWine(ItemStack stack) {
        return isTavernItem(stack) && stack.getItem() instanceof DrinkBlockItem
                && !stack.is(ModItems.VINEGAR.get()) && !stack.is(ModItems.WATERMELON_JUICE.get());
    }

    public static boolean isBarrelProduct(ItemStack stack) {
        return isQualityWine(stack) || stack.is(ModItems.VINEGAR.get());
    }

    /** External wine keeps its own effects and never receives Tavern's native BrewLevel. */
    public static boolean canAge(ItemStack stack) {
        return isQualityWine(stack) || stack.is(EXTERNAL_FERMENTED_DRINKS);
    }

    public static boolean isBatchProduct(ItemStack stack) {
        return isBarrelProduct(stack) || stack.is(EXTERNAL_FERMENTED_DRINKS);
    }

    public static boolean supportsQuality(ItemStack stack) {
        return !stack.isEmpty() && (isBatchProduct(stack) || isTavernItem(stack) && stack.getItem() instanceof CocktailBlockItem);
    }

    private static boolean isTavernItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals("kaleidoscope_tavern");
    }

    public static ItemStack qualify(ItemStack template, Quality quality) {
        if (template.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = QualityFoodFluidsApi.clearItemQuality(template.copy());
        if (isBarrelProduct(result)) BottleBlockItem.setBrewLevel(result, NORMAL_BREW_LEVEL);
        return QualityFoodFluidsCreateRules.applyItemOutputQuality(quality, result);
    }

    public static Component qualityName(Quality quality) {
        return quality.getTranslation();
    }

    public static Component qualityLine(Quality quality) {
        return Component.translatable(TEXT + "quality", qualityName(quality));
    }

    public static List<Component> barrelTooltip(BarrelBlockEntity barrel) {
        var batch = ((TavernBarrelAccess) barrel).createdelightcore$getQualityBatch();
        if (batch == null) return List.of();
        var fluid = batch.fluidOutput();
        ItemStack output = fluid == null ? barrel.getOutput().getStackInSlot(0) : fluid.assembledOutput();
        if (output.isEmpty()) output = batch.pendingOutput();
        if (output.isEmpty()) return List.of();
        // The recipe preview is unqualified; QFF's hover-name prefix would conflict with the batch quality below.
        Component name = output.getHoverName();
        if (name.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("fluid.quality_food_fluids.with_quality")
                && contents.getArgs().length == 2 && contents.getArgs()[1] instanceof Component baseName) {
            name = baseName.copy().withStyle(name.getStyle());
        }
        boolean fluidReady = fluid != null && !batch.brewing();
        int count = fluidReady ? fluid.servings() : output.getCount();
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("jade.plugin_kaleidoscope_tavern.item_and_count", name, count));
        if (fluidReady) lines.add(Component.literal(fluid.amount() + " mB"));
        lines.add(qualityLine(batch.quality()));
        lines.add(agingLine(batch));
        return lines;
    }

    public static Component agingLine(TavernBarrelBatch batch) {
        if (batch.brewing()) return Component.translatable(TEXT + "brewing", time(batch.brewingTicks()));
        if (!batch.canAge()) return Component.translatable(TEXT + "brewed").withStyle(ChatFormatting.GOLD);
        if (batch.aged()) return Component.translatable(TEXT + "aged").withStyle(ChatFormatting.GOLD);
        return Component.translatable(TEXT + "aging", qualityName(batch.targetQuality()), time(batch.remainingTicks()));
    }

    private static String time(int ticks) {
        int seconds = (int) ((ticks + 19L) / 20);
        return String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    /** Build an effect carrier, without making the bottle edible or adding nutrition. QF owns all scaling. */
    public static List<Pair<MobEffectInstance, Float>> effects(ItemStack stack) {
        var data = DrinkEffectDataReloadListener.INSTANCE.get(stack.getItem());
        var base = new FoodProperties.Builder().nutrition(0).saturationMod(0);
        List<DrinkEffectData.Entry> entries = List.of();
        if (stack.getItem() instanceof SignatureCocktailBlockItem) {
            entries = SignatureCocktailBlockItem.getEffects(stack);
        } else if (data != null && !data.effects().isEmpty()) {
            int level = isBarrelProduct(stack) ? NORMAL_BREW_LEVEL : 1;
            entries = data.effects().get(Math.min(level, data.effects().size()) - 1);
        }
        for (var entry : entries) {
            base.effect(() -> new MobEffectInstance(entry.effect(), entry.duration() * 20, entry.amplifier()),
                    entry.probability());
        }
        return FoodUtils.handleFoodProperties(stack, base.build()).getEffects();
    }

    public static void drink(ItemStack stack, LivingEntity consumer) {
        if (consumer.level().isClientSide) return;
        for (var entry : effects(stack)) {
            if (consumer.getRandom().nextFloat() >= entry.getSecond()) continue;
            MobEffectInstance instance = entry.getFirst();
            if (instance.getEffect().isInstantenous()) {
                instance.getEffect().applyInstantenousEffect(consumer, consumer, consumer, instance.getAmplifier(), 1);
            } else {
                consumer.addEffect(new MobEffectInstance(instance));
            }
        }
    }

    public static void appendBottleTooltip(ItemStack stack, List<Component> tooltip) {
        ChatFormatting color = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
        if (color != ChatFormatting.RESET) {
            tooltip.add(Component.translatable("color.kaleidoscope_tavern.prefix").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("color.kaleidoscope_tavern." + color.getName()).withStyle(color)));
        }
        appendCocktailTooltip(stack, tooltip);
    }

    public static void appendCocktailTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(qualityLine(QualityUtils.getQuality(stack)).copy().withStyle(ChatFormatting.GRAY));
        List<MobEffectInstance> certain = new ArrayList<>();
        for (var entry : effects(stack)) if (entry.getSecond() >= 1) certain.add(entry.getFirst());
        if (!certain.isEmpty()) {
            tooltip.add(Component.empty());
            PotionUtils.addPotionTooltip(certain, tooltip, 1);
        }
    }
}
