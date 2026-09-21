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

import io.github.jasonsimpart.compat.qualityfood.QualityFoodCurrencyCompat.AbsorptionResult;

import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPending.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodJournal.*;

final class QualityFoodPendingStore {
    private QualityFoodPendingStore() {
    }

    static AbsorptionResult writePending(
            Player player,
            List<QualityEntry> entries,
            long amount,
            long credited,
            String moneyChain,
            String state
    ) {
        CompoundTag data = null;
        QualityFoodJournalData journal = null;
        Tag previousJournalPending = null;
        CompoundTag pending = null;
        String playerKey = null;
        List<QualityEntry> escrowedEntries = new ArrayList<>();
        boolean playerSnapshotAttempted = false;
        try {
            data = player.getPersistentData();
            journal = journalFor(player);
            playerKey = journalPlayerKey(player);
            if (data.contains(PENDING_KEY) || journal.hasPending(playerKey) || journal.hasInvalid(playerKey)) {
                return AbsorptionResult.PENDING_RECOVERY;
            }
            previousJournalPending = journal.getPending(playerKey);
            pending = new CompoundTag();
            pending.putString(STATE_KEY, state);
            pending.putString(MONEY_CHAIN_KEY, moneyChain);
            pending.putLong(AMOUNT_KEY, amount);
            pending.putLong(CREDITED_KEY, credited);
            pending.putBoolean(ESCROWED_KEY, true);
            ListTag serializedEntries = new ListTag();
            for (QualityEntry entry : entries) {
                CompoundTag serializedEntry = new CompoundTag();
                serializedEntry.putInt(SLOT_KEY, entry.slot());
                serializedEntry.put(ORIGINAL_KEY, entry.originalStack().save(player.registryAccess()));
                serializedEntry.put(CLEARED_KEY, entry.clearedStack().save(player.registryAccess()));
                serializedEntries.add(serializedEntry);
            }
            pending.put(ENTRIES_KEY, serializedEntries);

            for (QualityEntry entry : entries) {
                ItemStack current = player.getInventory().getItem(entry.slot());
                if (!sameStack(current, entry.originalStack())) {
                    throw new IllegalStateException("Quality Food inventory changed before escrow");
                }
            }
            for (QualityEntry entry : entries) {
                escrowedEntries.add(entry);
                player.getInventory().setItem(entry.slot(), ItemStack.EMPTY);
            }
            player.getInventory().setChanged();
            data.put(PENDING_KEY, pending);
            journal.putPending(playerKey, pending);
            forcePendingSave(player);
            playerSnapshotAttempted = true;
            forcePlayerSave(player);
            return AbsorptionResult.SUCCESS;
        } catch (LinkageError | RuntimeException exception) {
            boolean restored = true;
            boolean cleanupDurable = false;
            for (QualityEntry entry : escrowedEntries) {
                try {
                    player.getInventory().setItem(entry.slot(), entry.originalStack().copy());
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to restore a Quality Food stack after escrow failed", restoreException);
                }
            }
            if (!escrowedEntries.isEmpty()) {
                try {
                    player.getInventory().setChanged();
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to mark restored Quality Food inventory after escrow failed", restoreException);
                }
            }
            if (playerSnapshotAttempted) {
                if (data != null && pending != null) {
                    try {
                        data.put(PENDING_KEY, pending);
                    } catch (LinkageError | RuntimeException restoreException) {
                        CreateDelightCore.LOGGER.error("Unable to retain Quality Food compensation after player snapshot failed", restoreException);
                    }
                }
                if (journal != null && playerKey != null && pending != null) {
                    try {
                        journal.putPendingTag(playerKey, pending);
                        forcePendingSave(player);
                    } catch (LinkageError | RuntimeException journalException) {
                        CreateDelightCore.LOGGER.error("Unable to retain the Quality Food transaction after player snapshot failed", journalException);
                    }
                }
                CreateDelightCore.LOGGER.error("Quality Food escrowed player snapshot could not be durably saved; currency insertion was not attempted", exception);
                return AbsorptionResult.PENDING_RECOVERY;
            }
            if (restored && data != null) {
                try {
                    data.remove(PENDING_KEY);
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to restore Quality Food compensation state after a failed initial write", restoreException);
                }
            }
            if (restored && journal != null && playerKey != null) {
                try {
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to restore the Quality Food transaction journal after escrow failed", restoreException);
                }
            }
            if (restored) {
                try {
                    forcePendingSave(player);
                    cleanupDurable = true;
                } catch (LinkageError | RuntimeException saveException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to durably clear the Quality Food transaction after escrow failed", saveException);
                }
            }
            if (!restored) {
                if (data != null && pending != null) {
                    try {
                        data.put(PENDING_KEY, pending);
                    } catch (LinkageError | RuntimeException restoreException) {
                        CreateDelightCore.LOGGER.error("Unable to retain Quality Food compensation state after escrow restoration failed", restoreException);
                    }
                }
                if (journal != null && playerKey != null && pending != null) {
                    try {
                        journal.putPending(playerKey, pending);
                        forcePendingSave(player);
                    } catch (LinkageError | RuntimeException journalException) {
                        CreateDelightCore.LOGGER.error("Unable to retain the Quality Food transaction journal after escrow restoration failed", journalException);
                    }
                }
                CreateDelightCore.LOGGER.error("Quality Food escrow could not be fully restored; manual reconciliation may be required");
            }
            CreateDelightCore.LOGGER.error("Unable to persist Quality Food compensation state", exception);
            return cleanupDurable
                    ? AbsorptionResult.ROLLED_BACK_FAILURE
                    : AbsorptionResult.PENDING_RECOVERY;
        }
    }

    static boolean updatePendingState(Player player, String state, long credited) {
        return updatePendingState(player, state, credited, true);
    }

    static boolean updatePendingState(Player player, String state, long credited, boolean durable) {
        CompoundTag data = null;
        CompoundTag previousPending = null;
        QualityFoodJournalData journal = null;
        Tag previousJournalPending = null;
        try {
            data = player.getPersistentData();
            if (!data.contains(PENDING_KEY, Tag.TAG_COMPOUND)) {
                return false;
            }
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            previousPending = data.getCompound(PENDING_KEY).copy();
            previousJournalPending = journal.getPending(playerKey);
            CompoundTag pending = previousPending.copy();
            pending.putString(STATE_KEY, state);
            pending.putLong(CREDITED_KEY, credited);
            data.put(PENDING_KEY, pending);
            journal.putPending(playerKey, pending);
            if (durable) {
                forcePendingSave(player);
            }
            return true;
        } catch (LinkageError | RuntimeException exception) {
            if (data != null && previousPending != null) {
                try {
                    data.put(PENDING_KEY, previousPending);
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore Quality Food compensation state after a failed update", restoreException);
                }
            }
            if (journal != null) {
                try {
                    String playerKey = journalPlayerKey(player);
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore the Quality Food transaction journal after a failed update", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to update Quality Food compensation state", exception);
            return false;
        }
    }

    static PendingOperation readPending(Player player) {
        try {
            CompoundTag data = player.getPersistentData();
            Tag playerPending = data.contains(PENDING_KEY) ? data.get(PENDING_KEY) : null;
            if (playerPending == null && !hasPlayerJournalFile(player)) {
                Boolean legacyContainsPending = legacyJournalContains(player);
                if (Boolean.FALSE.equals(legacyContainsPending)) {
                    return null;
                }
            }
            QualityFoodJournalData journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            Tag playerInvalid = data.contains(INVALID_PENDING_KEY) ? data.get(INVALID_PENDING_KEY) : null;
            Tag journalInvalid = journal.getInvalid(playerKey);
            Tag journalPending = journal.getPending(playerKey);
            if (playerInvalid != null || journalInvalid != null) {
                if (playerPending == null
                        && journalPending == null
                        && playerInvalid != null
                        && playerInvalid.equals(journalInvalid)) {
                    markRecoveryPresent(player);
                    return null;
                }
                quarantineInvalidState(player, playerPending, journalPending, playerInvalid, journalInvalid);
                return null;
            }
            Tag selectedPending;
            if (playerPending != null && journalPending != null && !playerPending.equals(journalPending)) {
                selectedPending = reconcilePendingCopies(playerPending, journalPending);
                if (selectedPending == null) {
                    quarantineBothPendingCopies(player, playerPending, journalPending);
                    return null;
                }
            } else {
                selectedPending = journalPending != null ? journalPending : playerPending;
            }
            if (selectedPending == null) {
                markRecoveryAbsent(player);
                return null;
            }

            boolean deferTerminalJournalSync = isTerminalPendingTag(journalPending)
                    && journalPending.equals(selectedPending)
                    && !isTerminalPendingTag(playerPending);
            if (playerPending == null && !deferTerminalJournalSync) {
                data.put(PENDING_KEY, selectedPending.copy());
            }
            PendingOperation pending = parsePendingRecord(player, selectedPending);
            if (pending == null) {
                if (deferTerminalJournalSync) {
                    data.put(PENDING_KEY, selectedPending.copy());
                }
                quarantinePending(player);
                return null;
            }
            if (!deferTerminalJournalSync && (playerPending == null || !playerPending.equals(selectedPending))) {
                data.put(PENDING_KEY, selectedPending.copy());
            }
            if (!deferTerminalJournalSync && (journalPending == null || !journalPending.equals(selectedPending))) {
                journal.putPendingTag(playerKey, selectedPending);
                forcePendingSave(player);
            }
            markRecoveryPresent(player);
            return pending;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to read Quality Food compensation state", exception);
            quarantinePending(player);
            return null;
        }
    }

    static PendingOperation readPendingIfRecoveryMayExist(Player player) {
        return recoveryMayExist(player) ? readPending(player) : null;
    }

    static boolean hasPending(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(PENDING_KEY) || data.contains(INVALID_PENDING_KEY)) {
            return true;
        }
        if (!recoveryMayExist(player)) {
            return false;
        }
        try {
            QualityFoodJournalData journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            boolean pending = journal.hasPending(playerKey) || journal.hasInvalid(playerKey);
            if (pending) {
                markRecoveryPresent(player);
            } else {
                markRecoveryAbsent(player);
            }
            return pending;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to inspect the Quality Food transaction journal", exception);
            return true;
        }
    }

    static boolean clearPendingDurably(Player player) {
        CompoundTag data = null;
        Tag previousPending = null;
        QualityFoodJournalData journal = null;
        Tag previousJournalPending = null;
        try {
            data = player.getPersistentData();
            previousPending = data.contains(PENDING_KEY)
                    ? data.get(PENDING_KEY).copy()
                    : null;
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            previousJournalPending = journal.getPending(playerKey);
            data.remove(PENDING_KEY);
            journal.removePending(playerKey);
            forcePendingSave(player);
            markRecoveryAbsent(player);
            return true;
        } catch (LinkageError | RuntimeException exception) {
            if (data != null && previousPending != null) {
                try {
                    data.put(PENDING_KEY, previousPending);
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore Quality Food compensation after a failed durable clear", restoreException);
                }
            }
            if (journal != null) {
                try {
                    String playerKey = journalPlayerKey(player);
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore the Quality Food transaction journal after a failed durable clear", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to persist Quality Food compensation completion", exception);
            return false;
        }
    }

    static boolean markWarningLogged(Player player) {
        try {
            CompoundTag data = player.getPersistentData();
            if (!data.contains(PENDING_KEY, Tag.TAG_COMPOUND)) {
                return false;
            }
            CompoundTag pending = data.getCompound(PENDING_KEY).copy();
            if (pending.getBoolean(WARNING_LOGGED_KEY)) {
                return false;
            }
            QualityFoodJournalData journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            Tag journalPending = journal.getPending(playerKey);
            if (!(journalPending instanceof CompoundTag journalPendingTag)) {
                return false;
            }
            pending.putBoolean(WARNING_LOGGED_KEY, true);
            journalPendingTag.putBoolean(WARNING_LOGGED_KEY, true);
            data.put(PENDING_KEY, pending);
            journal.putPending(playerKey, journalPendingTag);
            forcePendingSave(player);
            return true;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to mark Quality Food compensation warning state", exception);
            return false;
        }
    }

    static void logPendingInventoryFailure(Player player, Throwable cause) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food inventory update is pending retry", cause);
        }
    }

    static void logPendingUnresolved(Player player) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food currency compensation is unresolved; automatic conversion is paused");
        }
    }

    static void logPendingRollback(Player player) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food currency rollback is held for explicit administrator reconciliation");
        }
    }

    static void quarantineInvalidState(
            Player player,
            Tag playerPending,
            Tag journalPending,
            Tag playerInvalid,
            Tag journalInvalid
    ) {
        QualityFoodJournalData journal = null;
        Tag previousPlayerPending = null;
        Tag previousPlayerInvalid = null;
        Tag previousJournalPending = null;
        Tag previousJournalInvalid = null;
        try {
            CompoundTag data = player.getPersistentData();
            previousPlayerPending = data.contains(PENDING_KEY) ? data.get(PENDING_KEY).copy() : null;
            previousPlayerInvalid = data.contains(INVALID_PENDING_KEY) ? data.get(INVALID_PENDING_KEY).copy() : null;
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            previousJournalPending = journal.getPending(playerKey);
            previousJournalInvalid = journal.getInvalid(playerKey);

            ListTag history = new ListTag();
            appendInvalidHistory(history, playerInvalid);
            if (journalInvalid != null && (playerInvalid == null || !journalInvalid.equals(playerInvalid))) {
                appendInvalidHistory(history, journalInvalid);
            }
            appendPendingCopies(history, playerPending, journalPending);
            while (history.size() > MAX_INVALID_PENDING_HISTORY) {
                history.remove(0);
            }

            data.put(INVALID_PENDING_KEY, history);
            data.remove(PENDING_KEY);
            journal.removePending(playerKey);
            journal.putInvalid(playerKey, history);
            forcePendingSave(player);
            markRecoveryPresent(player);
            CreateDelightCore.LOGGER.error("Quality Food pending and quarantined copies were preserved for manual recovery");
        } catch (LinkageError | RuntimeException exception) {
            CompoundTag data = player.getPersistentData();
            try {
                if (previousPlayerPending == null) {
                    data.remove(PENDING_KEY);
                } else {
                    data.put(PENDING_KEY, previousPlayerPending);
                }
                if (previousPlayerInvalid == null) {
                    data.remove(INVALID_PENDING_KEY);
                } else {
                    data.put(INVALID_PENDING_KEY, previousPlayerInvalid);
                }
            } catch (LinkageError | RuntimeException restoreException) {
                CreateDelightCore.LOGGER.error("Unable to restore player Quality Food state after invalid precedence quarantine failed", restoreException);
            }
            if (journal != null) {
                try {
                    String playerKey = journalPlayerKey(player);
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                    if (previousJournalInvalid == null) {
                        journal.removeInvalid(playerKey);
                    } else {
                        journal.putInvalid(playerKey, previousJournalInvalid);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore journal Quality Food state after invalid precedence quarantine failed", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to preserve conflicting Quality Food invalid state", exception);
        }
    }

    static void quarantineBothPendingCopies(Player player, Tag playerPending, Tag journalPending) {
        QualityFoodJournalData journal = null;
        Tag previousPlayerPending = null;
        Tag previousPlayerInvalid = null;
        Tag previousJournalPending = null;
        Tag previousJournalInvalid = null;
        try {
            CompoundTag data = player.getPersistentData();
            previousPlayerPending = data.contains(PENDING_KEY) ? data.get(PENDING_KEY).copy() : null;
            previousPlayerInvalid = data.contains(INVALID_PENDING_KEY) ? data.get(INVALID_PENDING_KEY).copy() : null;
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            previousJournalPending = journal.getPending(playerKey);
            previousJournalInvalid = journal.getInvalid(playerKey);
            ListTag history = copyInvalidHistory(data);
            while (history.size() >= MAX_INVALID_PENDING_HISTORY) {
                history.remove(0);
            }
            CompoundTag copies = new CompoundTag();
            copies.put(PLAYER_PENDING_COPY_KEY, playerPending.copy());
            copies.put(JOURNAL_PENDING_COPY_KEY, journalPending.copy());
            history.add(copies);
            data.put(INVALID_PENDING_KEY, history);
            data.remove(PENDING_KEY);
            journal.removePending(playerKey);
            journal.putInvalid(playerKey, history);
            forcePendingSave(player);
            CreateDelightCore.LOGGER.error("Mismatched Quality Food compensation copies were quarantined for manual recovery");
        } catch (LinkageError | RuntimeException exception) {
            CompoundTag data = player.getPersistentData();
            try {
                if (previousPlayerPending == null) {
                    data.remove(PENDING_KEY);
                } else {
                    data.put(PENDING_KEY, previousPlayerPending);
                }
                if (previousPlayerInvalid == null) {
                    data.remove(INVALID_PENDING_KEY);
                } else {
                    data.put(INVALID_PENDING_KEY, previousPlayerInvalid);
                }
            } catch (LinkageError | RuntimeException restoreException) {
                CreateDelightCore.LOGGER.error("Unable to restore player Quality Food copies after quarantine failed", restoreException);
            }
            if (journal != null) {
                try {
                    String playerKey = journalPlayerKey(player);
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                    if (previousJournalInvalid == null) {
                        journal.removeInvalid(playerKey);
                    } else {
                        journal.putInvalid(playerKey, previousJournalInvalid);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore journal Quality Food copies after quarantine failed", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to quarantine mismatched Quality Food compensation copies", exception);
        }
    }

    static void quarantinePending(Player player) {
        QualityFoodJournalData journal = null;
        Tag previousPlayerPending = null;
        Tag previousPlayerInvalid = null;
        Tag previousJournalPending = null;
        Tag previousJournalInvalid = null;
        try {
            CompoundTag data = player.getPersistentData();
            if (!data.contains(PENDING_KEY)) {
                return;
            }
            previousPlayerPending = data.get(PENDING_KEY).copy();
            previousPlayerInvalid = data.contains(INVALID_PENDING_KEY) ? data.get(INVALID_PENDING_KEY).copy() : null;
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            previousJournalPending = journal.getPending(playerKey);
            previousJournalInvalid = journal.getInvalid(playerKey);

            ListTag history = copyInvalidHistory(data);
            while (history.size() >= MAX_INVALID_PENDING_HISTORY) {
                history.remove(0);
            }

            Tag pendingTag = data.get(PENDING_KEY).copy();
            if (pendingTag instanceof CompoundTag pending) {
                history.add(pending);
            } else {
                CompoundTag wrappedPending = new CompoundTag();
                wrappedPending.put(RAW_PENDING_KEY, pendingTag);
                history.add(wrappedPending);
            }
            data.put(INVALID_PENDING_KEY, history);
            data.remove(PENDING_KEY);
            journal.removePending(playerKey);
            journal.putInvalid(playerKey, history);
            forcePendingSave(player);
            CreateDelightCore.LOGGER.error("Invalid Quality Food compensation state was quarantined for manual recovery");
        } catch (LinkageError | RuntimeException exception) {
            CompoundTag data = player.getPersistentData();
            try {
                if (previousPlayerPending == null) {
                    data.remove(PENDING_KEY);
                } else {
                    data.put(PENDING_KEY, previousPlayerPending);
                }
                if (previousPlayerInvalid == null) {
                    data.remove(INVALID_PENDING_KEY);
                } else {
                    data.put(INVALID_PENDING_KEY, previousPlayerInvalid);
                }
            } catch (LinkageError | RuntimeException restoreException) {
                CreateDelightCore.LOGGER.error("Unable to restore player Quality Food state after quarantine failed", restoreException);
            }
            if (journal != null) {
                try {
                    String playerKey = journalPlayerKey(player);
                    if (previousJournalPending == null) {
                        journal.removePending(playerKey);
                    } else {
                        journal.putPendingTag(playerKey, previousJournalPending);
                    }
                    if (previousJournalInvalid == null) {
                        journal.removeInvalid(playerKey);
                    } else {
                        journal.putInvalid(playerKey, previousJournalInvalid);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore the Quality Food transaction journal after quarantine failed", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to quarantine invalid Quality Food compensation state", exception);
        }
    }
}
