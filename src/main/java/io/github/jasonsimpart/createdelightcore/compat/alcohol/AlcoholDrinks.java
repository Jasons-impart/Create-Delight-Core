package io.github.jasonsimpart.createdelightcore.compat.alcohol;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Startup configuration shared by consumption, tooltips and client-only JEED recipes. */
public final class AlcoholDrinks {
    public static final int MAX_AMPLIFIER = 4;
    private static final Map<String, Profile> PROFILES = new LinkedHashMap<>();
    private static final Set<String> FLUIDS = new LinkedHashSet<>();

    private AlcoholDrinks() {}

    /** Sources are item IDs or #item tags. Capture native drunk/tipsy before FoodBuilder removes them. */
    public static void registerDrink(String source, int duration, int amplifier) {
        id(source.startsWith("#") ? source.substring(1) : source);
        if (duration <= 0 || amplifier < 0 || amplifier > MAX_AMPLIFIER) {
            throw new IllegalArgumentException("Invalid alcohol dose for " + source);
        }
        if (PROFILES.containsKey(source)) return;
        if (!source.startsWith("#")) {
            Item item = ForgeRegistries.ITEMS.getValue(id(source));
            if (item == null) throw new IllegalArgumentException("Unknown alcohol item: " + source);
            var food = item.getFoodProperties();
            if (food != null) {
                for (String effect : List.of("youkaishomecoming:drunk", "brewinandchewin:tipsy")) {
                    var original = food.getEffects().stream()
                            .map(pair -> pair.getFirst())
                            .filter(instance -> id(effect).equals(ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect())))
                            .findFirst();
                    if (original.isPresent()) {
                        duration = original.get().getDuration();
                        amplifier = Math.min(MAX_AMPLIFIER, original.get().getAmplifier());
                        break;
                    }
                }
            }
        }
        PROFILES.put(source, new Profile(source, duration, amplifier));
    }

    public static void registerBonus(String source, String effect, int duration, int amplifier) {
        Profile profile = PROFILES.get(source);
        if (profile == null) throw new IllegalArgumentException("Register drink before its bonus: " + source);
        if (duration <= 0 || amplifier < 0) throw new IllegalArgumentException("Invalid bonus for " + source);
        var bonus = new Bonus(id(effect), duration, amplifier);
        profile.bonuses.removeIf(existing -> existing.effect.equals(bonus.effect));
        profile.bonuses.add(bonus);
    }

    public static void registerFluid(String fluid) {
        FLUIDS.add(id(fluid).toString());
    }

    /** Describes effects applied by a native drink action; consumption must not apply them again. */
    public static void registerTooltipEffect(String source, String effect, int duration, int amplifier) {
        Profile profile = PROFILES.get(source);
        if (profile == null) throw new IllegalArgumentException("Register drink before its tooltip: " + source);
        if (duration <= 0 || amplifier < 0) throw new IllegalArgumentException("Invalid tooltip effect for " + source);
        var tooltip = new Bonus(id(effect), duration, amplifier);
        profile.nativeTooltips.removeIf(existing -> existing.effect.equals(tooltip.effect));
        profile.nativeTooltips.add(tooltip);
    }

    public static Map<String, List<String>> tooltipEffectSources() {
        Map<String, List<String>> sources = new LinkedHashMap<>();
        PROFILES.forEach((source, profile) -> profile.nativeTooltips.forEach(tooltip ->
                sources.computeIfAbsent(tooltip.effect.toString(), key -> new ArrayList<>()).add(source)));
        return sources;
    }

    public static List<String> sources() { return List.copyOf(PROFILES.keySet()); }
    public static List<String> fluids() { return List.copyOf(FLUIDS); }

    /** New instances are essential: native effect application may mutate instances. */
    public static List<MobEffectInstance> effects(ItemStack stack) {
        Profile profile = profile(stack);
        MobEffect drunk = effect("youkaishomecoming:drunk");
        if (profile == null || drunk == null) return List.of();
        List<MobEffectInstance> result = new ArrayList<>();
        result.add(new MobEffectInstance(drunk, profile.duration, profile.amplifier));
        for (Bonus bonus : profile.bonuses) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(bonus.effect);
            if (effect != null) result.add(new MobEffectInstance(effect, bonus.duration, bonus.amplifier));
        }
        return result;
    }

    public static List<MobEffectInstance> tooltipEffects(ItemStack stack) {
        List<MobEffectInstance> result = new ArrayList<>(effects(stack));
        Profile profile = profile(stack);
        if (profile != null) for (Bonus tooltip : profile.nativeTooltips) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(tooltip.effect);
            if (effect != null) result.add(new MobEffectInstance(effect, tooltip.duration, tooltip.amplifier));
        }
        return result;
    }

    private static Profile profile(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Profile profile = null;
        // Native wine tags define the complete fixed profile, including companion effects.
        for (Profile candidate : PROFILES.values()) {
            if (candidate.tag != null && stack.is(candidate.tag)) {
                profile = candidate;
                break;
            }
        }
        if (profile == null) profile = PROFILES.get(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        return profile;
    }

    /** Called exactly once, after the native drink has applied its other effects and returned its container. */
    public static void consume(List<MobEffectInstance> effects, LivingEntity consumer) {
        if (consumer.level().isClientSide || effects.isEmpty()) return;
        MobEffectInstance dose = effects.get(0);
        MobEffect drunk = dose.getEffect();
        MobEffect sober = effect("youkaishomecoming:sober");
        MobEffectInstance current = consumer.getEffect(drunk);
        if ((sober == null || !consumer.hasEffect(sober))
                && (current == null || current.getAmplifier() < MAX_AMPLIFIER)) {
            int duration = current == null || current.isInfiniteDuration() ? dose.getDuration()
                    : Math.min(dose.getDuration(), current.getDuration());
            int amplifier = current == null ? dose.getAmplifier()
                    : Math.min(MAX_AMPLIFIER, current.getAmplifier() + dose.getAmplifier() + 1);
            // Remove hidden lower-level effects as well; they must never resume after a short higher dose.
            if (current != null) consumer.removeEffect(drunk);
            consumer.addEffect(new MobEffectInstance(drunk, duration, amplifier));
        }
        for (int i = 1; i < effects.size(); i++) consumer.addEffect(new MobEffectInstance(effects.get(i)));
    }

    private static ResourceLocation id(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new IllegalArgumentException("Invalid alcohol resource ID: " + value);
        return id;
    }

    private static MobEffect effect(String value) { return ForgeRegistries.MOB_EFFECTS.getValue(id(value)); }

    private static final class Profile {
        final TagKey<Item> tag;
        final int duration;
        final int amplifier;
        final List<Bonus> bonuses = new ArrayList<>();
        final List<Bonus> nativeTooltips = new ArrayList<>();

        Profile(String source, int duration, int amplifier) {
            this.tag = source.startsWith("#") ? TagKey.create(Registries.ITEM, id(source.substring(1))) : null;
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }

    private record Bonus(ResourceLocation effect, int duration, int amplifier) {}
}
