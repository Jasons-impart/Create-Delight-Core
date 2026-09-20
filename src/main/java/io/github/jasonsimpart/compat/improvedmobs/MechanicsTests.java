package io.github.jasonsimpart.compat.improvedmobs;

import com.mojang.authlib.GameProfile;
import io.github.flemmli97.improvedmobs.neoforge.AttachmentsRegister;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import io.github.jasonsimpart.compat.lightmanscurrency.MobCurrencyDrops;
import io.github.jasonsimpart.compat.lightmanscurrency.TraderWhitelist;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerListing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PrefixGameTestTemplate(false)
public final class MechanicsTests {
    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void deathAndRespawn(GameTestHelper helper) {
        double[] input = {0, 10, 50, 100, 150, 200, 250, 500};
        int[] loss = {0, 0, 15, 25, 30, 30, 25, 50};
        for (int i = 0; i < input.length; i++) helper.assertTrue(DifficultyRules.deathPenalty(input[i]) == loss[i], "0488 death curve at " + input[i]);
        var player = player(helper);
        try {
            ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(150);
            var canceled = new LivingDeathEvent(player, player.damageSources().generic());
            canceled.setCanceled(true);
            NeoForge.EVENT_BUS.post(canceled);
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 150, "Canceled deaths keep difficulty");
            player.getPersistentData().putBoolean("disableRankChange", true);
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(player, player.damageSources().generic()));
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 150, "Opt-out survives death handler");
            player.getPersistentData().putBoolean("disableRankChange", false);
            ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setPaused(true);
            player.die(player.damageSources().generic());
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 120, "Actual death deducts exactly 30");
            player = player.server.getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 120, "Difficulty survives actual respawn");
            helper.assertTrue(ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).paused(), "Respawn preserves difficulty pause state");
            ImprovedMobsCompat.changeDifficulty(player, -1000);
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 0, "Difficulty clamps to zero");
            ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(250);
            player.getPersistentData().putBoolean("disableRankChange", true);
            player.die(player.damageSources().generic());
            player = player.server.getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 250
                    && player.getPersistentData().getBoolean("disableRankChange"), "Opt-out survives actual death and respawn");
        } finally { player.server.getPlayerList().remove(player); }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void nativeCommandKillAndLegacyMigration(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var previousType = io.github.flemmli97.improvedmobs.common.config.Config.CommonConfig.difficultyType;
        try {
            io.github.flemmli97.improvedmobs.common.config.Config.CommonConfig.difficultyType =
                    io.github.flemmli97.improvedmobs.common.config.Config.DifficultyType.PLAYERMEAN;
            player = player.server.getPlayerList().respawn(player, true, Entity.RemovalReason.DISCARDED);
            var commands = player.server.getCommands().getDispatcher();
            commands.execute("improvedmobs difficulty player @a[name=mechanics-test,limit=1] set 114514", player.createCommandSourceStack().withPermission(2));
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 114514, "Core reads the native command's attachment");
            // Reproduce old Core's stale zero alongside the native command's current value.
            player.getData(AttachmentsRegister.PLAYER_DIFFICULTY).setDifficultyLevel(0);
            commands.execute("kill @s", player.createCommandSourceStack().withPermission(2));
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 103064, "Native kill deducts 11450");
            player = player.server.getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 103064, "Native command value survives real respawn");
            // A reconnect/dimension join must not re-import an old shadow value either.
            player.getData(AttachmentsRegister.PLAYER_DIFFICULTY).setDifficultyLevel(0);
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(player, helper.getLevel()));
            helper.assertTrue(ImprovedMobsCompat.difficulty(player) == 103064
                    && !player.hasData(AttachmentsRegister.PLAYER_DIFFICULTY), "Active value wins over legacy on join");
            var packet = io.github.flemmli97.improvedmobs.common.network.PacketHandler.createDifficultyPacket(
                    io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData.get(player.server), player);
            helper.assertTrue(packet.difficulty() == 103064, "Native HUD packet agrees with server storage");
            var saved = player.saveWithoutId(new CompoundTag());
            var restored = player(helper);
            restored.load(saved);
            helper.assertTrue(ImprovedMobsCompat.difficulty(restored) == 103064, "Current difficulty survives NBT save/load");
            // Preserve valid old-version saves which have only the legacy attachment.
            var legacyOnly = player(helper);
            legacyOnly.getData(AttachmentsRegister.PLAYER_DIFFICULTY).setDifficultyLevel(75);
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(legacyOnly, helper.getLevel()));
            helper.assertTrue(ImprovedMobsCompat.difficulty(legacyOnly) == 75
                    && !legacyOnly.hasData(AttachmentsRegister.PLAYER_DIFFICULTY), "Legacy-only saves migrate once");
        } finally {
            io.github.flemmli97.improvedmobs.common.config.Config.CommonConfig.difficultyType = previousType;
            player.server.getPlayerList().remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void currencyEligibility(GameTestHelper helper) {
        helper.assertTrue(MobCurrencyDrops.value(20, 4.5, 0, 0, 0) == 0, "Fractional copper truncates");
        helper.assertTrue(MobCurrencyDrops.value(80, 9, 6, 1, 0) == 4, "Attributes determine payout");
        helper.assertTrue(MobCurrencyDrops.value(80, 9, 6, 1, 100) == 8, "Greed applies before truncation");
        var player = player(helper);
        try {
            var zombie = new Zombie(helper.getLevel());
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);
            zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(9);
            zombie.getAttribute(Attributes.ARMOR).setBaseValue(6);
            zombie.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(1);
            var drops = drops(zombie, player);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 4
                    && BuiltInRegistries.ITEM.getKey(drops.getFirst().getItem().getItem()).toString().equals("lightmanscurrency:coin_copper"), "Real player receives four copper");
            helper.assertTrue(drops(zombie, FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "test-fake"))).isEmpty(), "Fake players earn no coins");
            helper.assertTrue(drops(new Cow(net.minecraft.world.entity.EntityType.COW, helper.getLevel()), player).isEmpty(), "Passive mobs earn no coins");
            zombie.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.ZERO), MobSpawnType.SPAWNER, null);
            helper.assertTrue(drops(zombie, player).isEmpty(), "Spawner mobs earn no coins");
        } finally { player.server.getPlayerList().remove(player); }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void disabledMobLootPreventsCoinsOnDeath(GameTestHelper helper) {
        var player = player(helper);
        var rule = helper.getLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT);
        boolean previous = rule.get();
        try {
            rule.set(false, helper.getLevel().getServer());
            var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(4, 2, 4));
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);
            zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(9);
            zombie.getAttribute(Attributes.ARMOR).setBaseValue(6);
            zombie.hurt(player.damageSources().playerAttack(player), 1000);
            helper.assertTrue(zombie.isDeadOrDying(), "Exercise a real eligible mob death");
            var loot = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(2));
            helper.assertTrue(loot.isEmpty(), "Disabled mob loot must produce neither vanilla drops nor currency");
            ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(200);
            var cockatrice = (LivingEntity) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("iceandfire:cockatrice")).create(helper.getLevel());
            for (int attempt = 0; attempt < 100; attempt++)
                helper.assertTrue(drops(cockatrice, player).isEmpty(), "Difficulty-gated bonus loot must also honor doMobLoot");
        } finally {
            rule.set(previous, helper.getLevel().getServer());
            player.server.getPlayerList().remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void difficultyLootThresholds(GameTestHelper helper) {
        var player = player(helper);
        try {
            for (var path : List.of("cockatrice", "dread_lich", "dread_knight", "dread_thrall", "dread_ghoul")) {
                var mob = (LivingEntity) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("iceandfire:" + path)).create(helper.getLevel());
                helper.assertTrue(mob != null, "Source entity exists: " + path);
                int threshold = path.equals("cockatrice") ? 100 : 150;
                String item = "iceandfire:" + (threshold == 100 ? "cockatrice_eye" : "dragonsteel_ice_ingot");
                ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(threshold - 1);
                for (int i = 0; i < 100; i++) helper.assertTrue(drops(mob, player).stream().noneMatch(d -> BuiltInRegistries.ITEM.getKey(d.getItem().getItem()).toString().equals(item)), "No difficulty loot below threshold");
                ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(threshold);
                mob.getRandom().setSeed(12345);
                int count = 0;
                for (int i = 0; i < 1000; i++) count += (int) drops(mob, player).stream().filter(d -> BuiltInRegistries.ITEM.getKey(d.getItem().getItem()).toString().equals(item)).count();
                double chance = threshold == 100 ? .5 : path.equals("dread_lich") || path.equals("dread_knight") ? .1 : .05;
                helper.assertTrue(Math.abs(count - 1000 * chance) < 50, "Seeded drop chance for " + path + ": " + count);
            }
        } finally { player.server.getPlayerList().remove(player); }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void whitelistIsolation(GameTestHelper helper) {
        var player = player(helper);
        try {
            var trader = new ItemTraderData(3, helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
            for (int i = 0; i < 3; i++) {
                trader.getTrade(i).setItem(new ItemStack(i == 1 ? Items.GOLD_INGOT : Items.IRON_INGOT), 0);
                trader.getTrade(i).setRules(List.of(PlayerListing.TYPE.createNew()));
            }
            var blacklist = (PlayerListing) trader.getTrade(2).getRules().getFirst();
            var tag = new CompoundTag(); tag.putBoolean("WhitelistMode", false);
            blacklist.loadPersistentData(tag, helper.getLevel().registryAccess());
            helper.assertTrue(TraderWhitelist.unlock(player, trader, Items.IRON_INGOT) == 1, "Only matching whitelist changes");
            helper.assertTrue(TraderWhitelist.unlock(player, trader, Items.IRON_INGOT) == 0, "Repeated unlock is idempotent");
            helper.assertTrue(((PlayerListing) trader.getTrade(1).getRules().getFirst()).getPlayerList().isEmpty(), "Other products stay locked");
            helper.assertTrue(blacklist.isBlacklistMode() && blacklist.getPlayerList().isEmpty(), "Blacklist remains unchanged");
            helper.assertTrue(TraderWhitelist.unlock(player, null, Items.IRON_INGOT) == 0, "Missing trader is harmless");
        } finally { player.server.getPlayerList().remove(player); }
        helper.succeed();
    }

    @GameTest(template = "mbd_single", templateNamespace = "createdelightcore")
    public static void nativeCoinLootReplaced(GameTestHelper helper) {
        var mob = new Zombie(helper.getLevel());
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(helper.getLevel())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, mob.position())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, mob)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, mob.damageSources().generic())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        for (var kind : List.of("entity", "boss")) for (int tier = 1; tier <= 6; tier++) {
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                    ResourceLocation.parse("lightmanscurrency:loot_addons/" + kind + "/tier" + tier));
            var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            var stacks = new ArrayList<ItemStack>();
            for (int roll = 0; roll < 50; roll++) table.getRandomItemsRaw(params, stacks::add);
            helper.assertTrue(stacks.isEmpty(), "LC native coin addon is empty: " + key.location());
        }
        var vanilla = helper.getLevel().getServer().reloadableRegistries().getLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.SIMPLE_DUNGEON);
        var stacks = new ArrayList<ItemStack>();
        vanilla.getRandomItemsRaw(params, stacks::add);
        helper.assertTrue(!stacks.isEmpty(), "Unrelated loot tables remain intact");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "mechanics-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        // GameTest's built-in mock logs in as vanilla and rejects every mod payload.
        // This server fixture retains the real player/respawn path, with an inert transport.
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) {}
        };
        return player;
    }

    private static List<ItemEntity> drops(LivingEntity mob, ServerPlayer player) {
        var drops = new ArrayList<ItemEntity>();
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(mob, player.damageSources().playerAttack(player), drops, true));
        return drops;
    }
}
