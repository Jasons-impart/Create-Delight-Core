package io.github.jasonsimpart.compat.qualityfood;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPending.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPendingStore.*;

final class QualityFoodJournal {
    private QualityFoodJournal() {
    }

    private static final long ABSENT_JOURNAL_RECHECK_TICKS = 200L;
    private static final Map<UUID, JournalPresence> JOURNAL_PRESENCE = new HashMap<>();
    private static final Map<Path, CompoundTag> LEGACY_JOURNAL_CACHE = new HashMap<>();

    static QualityFoodJournalData journalFor(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            throw new IllegalStateException("Quality Food compensation requires a server-backed player");
        }
        ServerLevel level = serverPlayer.getServer().overworld();
        QualityFoodJournalData journal = level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(QualityFoodJournalData::new, QualityFoodJournalData::load),
                journalDataId(player)
        );
        if (!journal.isLegacyMigrationComplete()) {
            migrateLegacyJournal(level, player, journal);
        }
        return journal;
    }

    static String journalPlayerKey(Player player) {
        return player.getUUID().toString();
    }

    static String journalDataId(Player player) {
        return JOURNAL_DATA_ID + "_" + journalPlayerKey(player);
    }

    static boolean hasPlayerJournalFile(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return false;
        }
        return Files.isRegularFile(
                serverPlayer.getServer().getWorldPath(LevelResource.ROOT)
                        .resolve("data")
                        .resolve(journalDataId(player) + ".dat")
        );
    }

    static Boolean legacyJournalContains(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return null;
        }
        Path legacyPath = serverPlayer.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(JOURNAL_DATA_ID + ".dat");
        if (!Files.isRegularFile(legacyPath)) {
            return false;
        }
        try {
            CompoundTag legacyData = readLegacyJournalData(serverPlayer.getServer().overworld());
            String playerKey = journalPlayerKey(player);
            return legacyData != null
                    && (journalEntry(legacyData, JOURNAL_PENDING_KEY, playerKey) != null
                    || journalEntry(legacyData, JOURNAL_INVALID_KEY, playerKey) != null);
        } catch (LinkageError | RuntimeException | java.io.IOException exception) {
            CreateDelightCore.LOGGER.error("Unable to inspect the legacy Quality Food transaction journal", exception);
            return null;
        }
    }

    static void migrateLegacyJournal(ServerLevel level, Player player, QualityFoodJournalData journal) {
        Path dataDirectory = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
        Path legacyPath = dataDirectory.resolve(JOURNAL_DATA_ID + ".dat");
        if (!Files.isRegularFile(legacyPath)) {
            journal.markLegacyMigrationComplete();
            return;
        }

        try {
            CompoundTag legacyData = readLegacyJournalData(level);
            if (legacyData == null) {
                journal.markLegacyMigrationComplete();
                return;
            }
            String playerKey = journalPlayerKey(player);
            Tag legacyPending = journalEntry(legacyData, JOURNAL_PENDING_KEY, playerKey);
            Tag legacyInvalid = journalEntry(legacyData, JOURNAL_INVALID_KEY, playerKey);
            if (legacyPending != null) {
                journal.putPendingTag(playerKey, legacyPending);
            }
            if (legacyInvalid != null) {
                journal.putInvalid(playerKey, legacyInvalid);
            }
            Tag legacyMalformed = legacyData.get(JOURNAL_MALFORMED_KEY);
            if (legacyMalformed != null) {
                journal.mergeMalformed(legacyMalformed);
            }
            journal.markLegacyMigrationComplete();
        } catch (LinkageError | RuntimeException | java.io.IOException exception) {
            CreateDelightCore.LOGGER.error("Unable to migrate the legacy Quality Food transaction journal", exception);
        }
    }

    static CompoundTag readLegacyJournalData(ServerLevel level) throws java.io.IOException {
        Path worldRoot = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        CompoundTag cached = LEGACY_JOURNAL_CACHE.get(worldRoot);
        if (cached != null) {
            return cached;
        }
        CompoundTag root = level.getDataStorage().readTagFromDisk(JOURNAL_DATA_ID, null, 0);
        CompoundTag legacyData = root.contains(SAVED_DATA_ROOT_KEY, Tag.TAG_COMPOUND)
                ? root.getCompound(SAVED_DATA_ROOT_KEY)
                : root;
        LEGACY_JOURNAL_CACHE.put(worldRoot, legacyData.copy());
        return legacyData;
    }

    static Tag journalEntry(CompoundTag root, String sectionKey, String playerKey) {
        if (!root.contains(sectionKey, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag section = root.getCompound(sectionKey);
        return section.contains(playerKey) ? section.get(playerKey).copy() : null;
    }

    static boolean persistTerminalPlayerState(Player player) {
        try {
            forcePlayerSave(player);
        } catch (LinkageError | RuntimeException exception) {
            try {
                forcePendingSave(player);
            } catch (LinkageError | RuntimeException journalException) {
                CreateDelightCore.LOGGER.error("Unable to retain the Quality Food terminal journal after player save failed", journalException);
            }
            CreateDelightCore.LOGGER.error("Unable to persist the Quality Food terminal player state", exception);
            return false;
        }
        try {
            forcePendingSave(player);
            return true;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to persist the Quality Food terminal journal after player save", exception);
            return false;
        }
    }

    static void forcePlayerSave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            throw new IllegalStateException("Quality Food compensation requires a server-backed player");
        }
        try {
            serverPlayer.getServer().getPlayerList().save(serverPlayer);
        } catch (LinkageError | RuntimeException exception) {
            throw new IllegalStateException("Unable to force-save the Quality Food player state", exception);
        }
    }

    static void forcePendingSave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            throw new IllegalStateException("Quality Food compensation requires a server-backed player");
        }
        try {
            QualityFoodJournalData journal = journalFor(player);
            journal.setDirty();
            journal.save(
                    serverPlayer.getServer().getWorldPath(LevelResource.ROOT)
                            .resolve("data")
                            .resolve(journalDataId(player) + ".dat")
                            .toFile(),
                    serverPlayer.getServer().registryAccess()
            );
        } catch (LinkageError | RuntimeException exception) {
            throw new IllegalStateException("Unable to force-save the Quality Food transaction journal", exception);
        }
    }

    static boolean recoveryMayExist(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(PENDING_KEY) || data.contains(INVALID_PENDING_KEY)) {
            return true;
        }

        UUID playerId = player.getUUID();
        long tick = player.tickCount;
        JournalPresence cached = JOURNAL_PRESENCE.get(playerId);
        if (cached != null && (cached.present() || tick < cached.nextProbeTick())) {
            return cached.present();
        }

        boolean present = hasPlayerJournalFile(player);
        if (!present) {
            Boolean legacyContainsPending = legacyJournalContains(player);
            present = legacyContainsPending == null || legacyContainsPending;
        }
        JOURNAL_PRESENCE.put(
                playerId,
                new JournalPresence(present, present ? Long.MAX_VALUE : tick + ABSENT_JOURNAL_RECHECK_TICKS)
        );
        return present;
    }

    static void markRecoveryPresent(Player player) {
        JOURNAL_PRESENCE.put(player.getUUID(), new JournalPresence(true, Long.MAX_VALUE));
    }

    static void markRecoveryAbsent(Player player) {
        JOURNAL_PRESENCE.put(
                player.getUUID(),
                new JournalPresence(false, player.tickCount + ABSENT_JOURNAL_RECHECK_TICKS)
        );
    }
    static void forgetPresence(UUID playerId) {
        JOURNAL_PRESENCE.remove(playerId);
    }

}
