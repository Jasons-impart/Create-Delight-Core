package io.github.jasonsimpart.compat.tetra;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;

/** Server-side pack effects, loaded only when Tetra is present. */
public final class TetraCombatCompat {
    private static final ItemEffect OVERWHELM = effect("overwhelm");
    private static final ItemEffect FROZEN = effect("frozen");
    private static final ItemEffect IRRADIATION = effect("irradiation");
    private static final ItemEffect ICE_RESTRAINT = effect("ice_dragon_restraint");
    private static final ItemEffect FIRE_RESTRAINT = effect("fire_dragon_restraint");
    private static final ItemEffect LIGHTNING_RESTRAINT = effect("lightning_dragon_restraint");
    private static final ResourceLocation FROZEN_STATUS = ResourceLocation.fromNamespaceAndPath("iceandfire", "frozen");
    private static final ResourceLocation RADIATION_STATUS = ResourceLocation.fromNamespaceAndPath("alexscavesup", "irradiated");

    private TetraCombatCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TetraCombatCompat::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(TetraCombatCompat::onPlayerTick);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IModularItem item)) {
            return;
        }

        int overwhelm = item.getEffectLevel(stack, OVERWHELM);
        if (overwhelm > 0 && event.getSource().getDirectEntity() == player) {
            // Preserve the old separate generic hit, including armour and invulnerability frames.
            // A source without a player also prevents recursive application of these effects.
            event.getEntity().hurt(player.damageSources().generic(), event.getEntity().getHealth() * overwhelm / 100.0F);
        }
        int frozen = item.getEffectLevel(stack, FROZEN);
        if (frozen > 0) {
            BuiltInRegistries.MOB_EFFECT.getHolder(FROZEN_STATUS).ifPresent(status ->
                    event.getEntity().addEffect(new MobEffectInstance(status, frozen * 20, 0, false, true)));
        }

        ResourceLocation target = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if ("iceandfire".equals(target.getNamespace())) {
            ItemEffect restraint = switch (target.getPath()) {
                case "ice_dragon" -> ICE_RESTRAINT;
                case "fire_dragon" -> FIRE_RESTRAINT;
                case "lightning_dragon" -> LIGHTNING_RESTRAINT;
                default -> null;
            };
            if (restraint != null) {
                event.setAmount(event.getAmount() + item.getEffectLevel(stack, restraint));
            }
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.level().getGameTime() % 600 != 0) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IModularItem item)) {
            return;
        }
        int irradiation = item.getEffectLevel(stack, IRRADIATION);
        if (irradiation > 0) {
            BuiltInRegistries.MOB_EFFECT.getHolder(RADIATION_STATUS).ifPresent(status -> {
                MobEffectInstance previous = player.getEffect(status);
                int duration = (int) Math.round(item.getEffectEfficiency(stack, IRRADIATION))
                        + (previous == null ? 0 : previous.getDuration());
                // The old effect uses the level as the zero-based amplifier, without subtracting one.
                player.addEffect(new MobEffectInstance(status, duration, irradiation, true, true));
            });
        }
    }

    private static ItemEffect effect(String name) {
        return ItemEffect.get(CreateDelightCore.MODID + ":" + name);
    }
}
