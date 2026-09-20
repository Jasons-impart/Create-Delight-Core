package io.github.jasonsimpart.compat.improvedmobs;

import io.github.flemmli97.improvedmobs.neoforge.AttachmentsRegister;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** 0488 numerical progression; the later legacy tier/quest system is not enabled. */
public final class ImprovedMobsCompat {
    private ImprovedMobsCompat() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ImprovedMobsCompat::death);
        NeoForge.EVENT_BUS.addListener(ImprovedMobsCompat::drops);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ImprovedMobsCompat::migrateLegacyData);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ImprovedMobsCompat::clonePlayer);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) sync(player);
        });
    }

    public static double difficulty(ServerPlayer player) {
        return ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).getDifficultyLevel();
    }

    public static double changeDifficulty(ServerPlayer player, double delta) {
        if (!Double.isFinite(delta) || player.getPersistentData().getBoolean("disableRankChange")) return 0;
        double before = difficulty(player);
        if (!Double.isFinite(before)) return 0;
        double after = Math.max(0, before + delta);
        if (!Double.isFinite(after)) return 0;
        ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player).setDifficultyLevel(after);
        sync(player);
        double changed = after - before;
        if (changed != 0) player.sendSystemMessage(Component.translatable(
                "message.createdelightcore.difficulty." + (changed < 0 ? "decreased" : "increased"),
                java.math.BigDecimal.valueOf(Math.abs(changed)).stripTrailingZeros().toPlainString()));
        return changed;
    }

    private static void sync(ServerPlayer player) {
        io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff.INSTANCE.sendClientboundPacket(
                io.github.flemmli97.improvedmobs.common.network.PacketHandler.createDifficultyPacket(
                        io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData.get(player.server), player), player);
    }

    private static void migrateLegacyData(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // IM's join handler re-imports the obsolete tenshilib:player_difficulty value.
        // Import only when no current improvedmobs:player_difficulty exists, then retire it.
        var legacy = player.getExistingData(AttachmentsRegister.PLAYER_DIFFICULTY);
        if (legacy.isEmpty()) return;
        var active = ImprovedMobsAttachments.PLAYER_DIFFICULTY.get();
        if (active.getOptional(player).isEmpty()) {
            active.get(player).read(legacy.get().write(player.registryAccess()), player.registryAccess());
        }
        player.removeData(AttachmentsRegister.PLAYER_DIFFICULTY);
    }

    private static void clonePlayer(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        // TenshiLib copies the active IM attachment, including pause/index, itself.
        var original = event.getOriginal().getPersistentData();
        if (original.contains("disableRankChange")) {
            event.getEntity().getPersistentData().putBoolean("disableRankChange", original.getBoolean("disableRankChange"));
        }
    }

    private static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            changeDifficulty(player, -DifficultyRules.deathPenalty(difficulty(player)));
        }
    }

    private static void drops(LivingDropsEvent event) {
        if (!event.getEntity().level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (!id.getNamespace().equals("iceandfire")) return;
        double difficulty = difficulty(player);
        var rule = DifficultyLootRules.RULES.stream().filter(value -> value.entity().equals(id.getPath())).findFirst();
        if (rule.isEmpty() || !Double.isFinite(difficulty) || difficulty < rule.get().difficulty()
                || event.getEntity().getRandom().nextDouble() >= rule.get().chance()) return;
        BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("iceandfire", rule.get().item())).ifPresent(value -> {
            var entity = event.getEntity();
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(value)));
        });
    }
}
