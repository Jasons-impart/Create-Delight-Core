package io.github.jasonsimpart.compat.lightmanscurrency;

import io.github.jasonsimpart.compat.tetra.TetraCombatCompat;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class MobCurrencyDrops {
    private MobCurrencyDrops() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MobCurrencyDrops::drops);
        NeoForge.EVENT_BUS.addListener(MobCurrencyDrops::entityLootTable);
    }

    private static void entityLootTable(net.neoforged.neoforge.event.LootTableLoadEvent event) {
        var id = event.getName();
        // Core replaces LC's entity/boss bonuses; chest/fishing/gift loot remains native.
        if (id.getNamespace().equals("lightmanscurrency")
                && id.getPath().matches("loot_addons/(entity|boss)/tier[1-6]")) event.setCanceled(true);
    }

    private static void drops(LivingDropsEvent event) {
        if (!event.getEntity().level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer
                || !(event.getEntity() instanceof Mob mob) || mob.getSpawnType() == MobSpawnType.SPAWNER
                || (!(mob instanceof Enemy) && !mob.isAggressive())) return;
        // The latest legacy pack fixes mobs (notably bosses) lacking ATTACK_DAMAGE.
        double fallback = mob instanceof EnderDragon ? 10 : mob instanceof WitherBoss ? 8 : 4.5;
        int greedy = ModList.get().isLoaded("tetra") ? TetraCombatCompat.greedyLevel(player.getMainHandItem()) : 0;
        long amount = value(mob.getMaxHealth(), attribute(mob, Attributes.ATTACK_DAMAGE, fallback),
                attribute(mob, Attributes.ARMOR, 0), attribute(mob, Attributes.ARMOR_TOUGHNESS, 0), greedy);
        if (amount <= 0) return;
        if (!(CoinValue.fromNumber("main", amount) instanceof CoinValue coins)) return;
        for (var stack : coins.getAsItemList()) {
            if (!stack.isEmpty()) event.getDrops().add(new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), stack));
        }
    }

    public static long value(double health, double damage, double armor, double toughness, int greedy) {
        double value = Math.sqrt(health / 20) * (damage / 4.5) * Math.sqrt(armor / 2 + toughness + 1) / 2;
        if (greedy > 0) value *= 1 + greedy / 100.0;
        // The old Rhino call to CoinValue.fromNumber(long) truncates fractional copper.
        return Double.isFinite(value) && value > 0 ? (long) value : 0;
    }

    private static double attribute(Mob mob, Holder<Attribute> attribute, double fallback) {
        var instance = mob.getAttribute(attribute);
        return instance == null ? fallback : instance.getValue();
    }
}
