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

import static io.github.jasonsimpart.compat.qualityfood.QualityFoodJournal.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPendingStore.*;

final class QualityFoodPending {
    private QualityFoodPending() {
    }

    static final int MAX_QUALITY_LEVEL = Long.SIZE - 2;
    static final int MAX_INVALID_PENDING_HISTORY = 4;
    static final String ABSORB_MESSAGE = "message.createdelightcore.quality_absorber.absorbed";
    static final String PENDING_KEY = "createdelightcore:quality_absorber_pending";
    static final String STATE_KEY = "State";
    static final String STATE_INSERTING = "inserting";
    static final String STATE_ROLLING_BACK = "rolling_back";
    static final String STATE_ROLLBACK_UNKNOWN = "rollback_unknown";
    static final String STATE_ROLLBACK_COMPLETE = "rollback_complete";
    static final String STATE_COMMITTING = "committing";
    static final String STATE_COMMIT_COMPLETE = "commit_complete";
    static final String STATE_UNRESOLVED = "unresolved";
    static final String INVALID_PENDING_KEY = "createdelightcore:quality_absorber_pending_invalid";
    static final String MONEY_CHAIN_KEY = "MoneyChain";
    static final String AMOUNT_KEY = "Amount";
    static final String CREDITED_KEY = "Credited";
    static final String ENTRIES_KEY = "Entries";
    static final String ESCROWED_KEY = "Escrowed";
    static final String SLOT_KEY = "Slot";
    static final String ORIGINAL_KEY = "Original";
    static final String CLEARED_KEY = "Cleared";
    static final String WARNING_LOGGED_KEY = "WarningLogged";
    static final String RAW_PENDING_KEY = "RawPending";
    static final String SAVED_DATA_ROOT_KEY = "data";
    static final String LEGACY_MIGRATION_COMPLETE_KEY = "LegacyMigrationComplete";
    static final String JOURNAL_DATA_ID = "createdelightcore_quality_food_journal";
    static final String JOURNAL_PENDING_KEY = "Pending";
    static final String JOURNAL_INVALID_KEY = "Invalid";
    static final String JOURNAL_MALFORMED_KEY = "Malformed";
    static final String PLAYER_PENDING_COPY_KEY = "PlayerPending";
    static final String JOURNAL_PENDING_COPY_KEY = "JournalPending";

    static void copyPersistentEntry(CompoundTag source, CompoundTag target, String key) {
        if (source.contains(key)) {
            target.put(key, source.get(key).copy());
        }
    }

    static boolean isTerminalPendingTag(Tag tag) {
        return tag instanceof CompoundTag pending && isTerminalState(pending.getString(STATE_KEY));
    }

    static boolean isCompletePendingRecord(Player player, Tag tag) {
        if (!(tag instanceof CompoundTag pending)
                || pending.contains(RAW_PENDING_KEY)
                || pending.contains(PLAYER_PENDING_COPY_KEY)
                || pending.contains(JOURNAL_PENDING_COPY_KEY)) {
            return false;
        }
        try {
            return parsePendingRecord(player, pending) != null;
        } catch (LinkageError | RuntimeException exception) {
            return false;
        }
    }

    static boolean samePendingExceptWarning(Tag left, Tag right) {
        if (!(left instanceof CompoundTag leftPending)
                || !(right instanceof CompoundTag rightPending)) {
            return false;
        }
        CompoundTag leftCopy = leftPending.copy();
        CompoundTag rightCopy = rightPending.copy();
        leftCopy.remove(WARNING_LOGGED_KEY);
        rightCopy.remove(WARNING_LOGGED_KEY);
        return leftCopy.equals(rightCopy);
    }

    static boolean samePendingExceptStateAndWarning(Tag left, Tag right) {
        if (!(left instanceof CompoundTag leftPending)
                || !(right instanceof CompoundTag rightPending)) {
            return false;
        }
        CompoundTag leftCopy = leftPending.copy();
        CompoundTag rightCopy = rightPending.copy();
        leftCopy.remove(STATE_KEY);
        leftCopy.remove(CREDITED_KEY);
        leftCopy.remove(WARNING_LOGGED_KEY);
        rightCopy.remove(STATE_KEY);
        rightCopy.remove(CREDITED_KEY);
        rightCopy.remove(WARNING_LOGGED_KEY);
        return leftCopy.equals(rightCopy);
    }

    static boolean isTerminalState(String state) {
        return STATE_COMMIT_COMPLETE.equals(state) || STATE_ROLLBACK_COMPLETE.equals(state);
    }

    static boolean terminalCanSupersede(String terminalState, String otherState) {
        return switch (terminalState) {
            case STATE_COMMIT_COMPLETE -> STATE_COMMITTING.equals(otherState)
                    || STATE_UNRESOLVED.equals(otherState);
            case STATE_ROLLBACK_COMPLETE -> STATE_ROLLING_BACK.equals(otherState)
                    || STATE_ROLLBACK_UNKNOWN.equals(otherState);
            default -> false;
        };
    }

    static boolean isValidTerminalCredit(CompoundTag pending) {
        if (!isValidStateCredit(pending)) {
            return false;
        }
        long amount = pending.getLong(AMOUNT_KEY);
        long credited = pending.getLong(CREDITED_KEY);
        return (STATE_COMMIT_COMPLETE.equals(pending.getString(STATE_KEY))
                ? credited == amount
                : STATE_ROLLBACK_COMPLETE.equals(pending.getString(STATE_KEY)) && credited == 0L);
    }

    static boolean isValidStateCredit(CompoundTag pending) {
        if (!pending.contains(STATE_KEY, Tag.TAG_STRING)
                || !pending.contains(AMOUNT_KEY, Tag.TAG_LONG)
                || !pending.contains(CREDITED_KEY, Tag.TAG_LONG)) {
            return false;
        }
        String state = pending.getString(STATE_KEY);
        long amount = pending.getLong(AMOUNT_KEY);
        long credited = pending.getLong(CREDITED_KEY);
        boolean invalidStateCredit = STATE_INSERTING.equals(state) && credited != 0L
                || STATE_COMMITTING.equals(state) && credited != amount
                || STATE_ROLLING_BACK.equals(state) && credited <= 0L
                || STATE_ROLLBACK_UNKNOWN.equals(state) && credited <= 0L
                || STATE_ROLLBACK_COMPLETE.equals(state) && credited != 0L
                || STATE_COMMIT_COMPLETE.equals(state) && credited != amount;
        return isKnownState(state)
                && amount > 0L
                && credited >= 0L
                && credited <= amount
                && !invalidStateCredit;
    }

    static Tag reconcilePendingCopies(Tag playerPending, Tag journalPending) {
        if (samePendingExceptWarning(playerPending, journalPending)) {
            CompoundTag selected = ((CompoundTag) journalPending).copy();
            if (playerPending instanceof CompoundTag player
                    && player.getBoolean(WARNING_LOGGED_KEY)
                    || journalPending instanceof CompoundTag journal
                    && journal.getBoolean(WARNING_LOGGED_KEY)) {
                selected.putBoolean(WARNING_LOGGED_KEY, true);
            }
            return selected;
        }
        if (!samePendingExceptStateAndWarning(playerPending, journalPending)) {
            return null;
        }

        CompoundTag player = (CompoundTag) playerPending;
        CompoundTag journal = (CompoundTag) journalPending;
        String playerState = player.getString(STATE_KEY);
        String journalState = journal.getString(STATE_KEY);
        CompoundTag selected;
        if (isTerminalState(playerState)
                && terminalCanSupersede(playerState, journalState)
                && isValidTerminalCredit(player)) {
            selected = player.copy();
        } else if (isTerminalState(journalState)
                && terminalCanSupersede(journalState, playerState)
                && isValidTerminalCredit(journal)) {
            selected = journal.copy();
        } else if (!isTerminalState(journalState) && isValidStateCredit(journal)) {
            selected = journal.copy();
        } else if (!isTerminalState(playerState) && isValidStateCredit(player)) {
            selected = player.copy();
        } else {
            return null;
        }
        if (player.getBoolean(WARNING_LOGGED_KEY) || journal.getBoolean(WARNING_LOGGED_KEY)) {
            selected.putBoolean(WARNING_LOGGED_KEY, true);
        }
        return selected;
    }

    static PendingOperation parsePendingRecord(Player player, Tag tag) {
        if (!(tag instanceof CompoundTag pending)
                || !pending.contains(STATE_KEY, Tag.TAG_STRING)
                || !pending.contains(MONEY_CHAIN_KEY, Tag.TAG_STRING)
                || !pending.contains(AMOUNT_KEY, Tag.TAG_LONG)
                || !pending.contains(CREDITED_KEY, Tag.TAG_LONG)
                || !pending.contains(ESCROWED_KEY, Tag.TAG_BYTE)
                || !pending.contains(ENTRIES_KEY, Tag.TAG_LIST)
                || !pending.getBoolean(ESCROWED_KEY)) {
            return null;
        }

        String state = pending.getString(STATE_KEY);
        String moneyChain = pending.getString(MONEY_CHAIN_KEY);
        long amount = pending.getLong(AMOUNT_KEY);
        long credited = pending.getLong(CREDITED_KEY);
        boolean invalidStateCredit = STATE_INSERTING.equals(state) && credited != 0L
                || STATE_COMMITTING.equals(state) && credited != amount
                || STATE_ROLLING_BACK.equals(state) && credited <= 0L
                || STATE_ROLLBACK_UNKNOWN.equals(state) && credited <= 0L
                || STATE_ROLLBACK_COMPLETE.equals(state) && credited != 0L
                || STATE_COMMIT_COMPLETE.equals(state) && credited != amount;
        if (!isKnownState(state)
                || moneyChain.isBlank()
                || amount <= 0L
                || credited < 0L
                || credited > amount
                || invalidStateCredit) {
            return null;
        }

        ListTag serializedEntries = pending.getList(ENTRIES_KEY, Tag.TAG_COMPOUND);
        if (serializedEntries.isEmpty()) {
            return null;
        }
        List<QualityEntry> entries = new ArrayList<>(serializedEntries.size());
        Set<Integer> slots = new HashSet<>();
        long calculatedAmount = 0L;
        for (int index = 0; index < serializedEntries.size(); index++) {
            CompoundTag serializedEntry = serializedEntries.getCompound(index);
            if (!serializedEntry.contains(SLOT_KEY, Tag.TAG_INT)
                    || !serializedEntry.contains(ORIGINAL_KEY, Tag.TAG_COMPOUND)
                    || !serializedEntry.contains(CLEARED_KEY, Tag.TAG_COMPOUND)) {
                return null;
            }
            ItemStack original = ItemStack.parseOptional(player.registryAccess(), serializedEntry.getCompound(ORIGINAL_KEY));
            ItemStack cleared = ItemStack.parseOptional(player.registryAccess(), serializedEntry.getCompound(CLEARED_KEY));
            int slot = serializedEntry.getInt(SLOT_KEY);
            QualityFoodCompat.QualityResult qualityResult = QualityFoodCompat.getQualityLevel(original);
            ItemStack canonicalCleared = original.copy();
            if (!qualityResult.success()) {
                return null;
            }
            QualityFoodCompat.QualityResult clearResult = QualityFoodCompat.clearQuality(canonicalCleared);
            if (!clearResult.success()) {
                return null;
            }
            if (original.isEmpty()
                    || cleared.isEmpty()
                    || slot < 0
                    || slot >= player.getInventory().getContainerSize()
                    || !slots.add(slot)
                    || qualityResult.level() <= 0
                    || clearResult.level() != qualityResult.level()
                    || !sameStack(canonicalCleared, cleared)) {
                return null;
            }
            calculatedAmount = Math.addExact(calculatedAmount, valueFor(qualityResult.level(), original.getCount()));
            entries.add(new QualityEntry(slot, original, cleared));
        }
        if (calculatedAmount != amount) {
            return null;
        }
        return new PendingOperation(state, moneyChain, amount, credited, true, entries);
    }

    static boolean isKnownState(String state) {
        return STATE_INSERTING.equals(state)
                || STATE_ROLLING_BACK.equals(state)
                || STATE_ROLLBACK_UNKNOWN.equals(state)
                || STATE_ROLLBACK_COMPLETE.equals(state)
                || STATE_COMMITTING.equals(state)
                || STATE_COMMIT_COMPLETE.equals(state)
                || STATE_UNRESOLVED.equals(state);
    }

    static boolean sameStack(ItemStack left, ItemStack right) {
        return left.getCount() == right.getCount() && ItemStack.matches(left, right);
    }

    static ListTag copyInvalidHistory(CompoundTag data) {
        ListTag history = new ListTag();
        if (!data.contains(INVALID_PENDING_KEY)) {
            return history;
        }
        Tag existingHistory = data.get(INVALID_PENDING_KEY);
        if (existingHistory instanceof ListTag list
                && (list.isEmpty() || list.getElementType() == Tag.TAG_COMPOUND)) {
            history.addAll(list);
        } else if (existingHistory != null) {
            CompoundTag wrappedHistory = new CompoundTag();
            wrappedHistory.put(RAW_PENDING_KEY, existingHistory.copy());
            history.add(wrappedHistory);
        }
        return history;
    }

    static void appendInvalidHistory(ListTag history, Tag existing) {
        if (existing == null) {
            return;
        }
        if (existing instanceof ListTag list
                && (list.isEmpty() || list.getElementType() == Tag.TAG_COMPOUND)) {
            for (int index = 0; index < list.size(); index++) {
                history.add(list.get(index).copy());
            }
            return;
        }
        appendQuarantinedPending(history, existing);
    }

    static void appendQuarantinedPending(ListTag history, Tag pending) {
        if (pending instanceof CompoundTag compound) {
            history.add(compound.copy());
        } else {
            CompoundTag wrappedPending = new CompoundTag();
            wrappedPending.put(RAW_PENDING_KEY, pending.copy());
            history.add(wrappedPending);
        }
    }

    static void appendPendingCopies(ListTag history, Tag playerPending, Tag journalPending) {
        if (playerPending == null && journalPending == null) {
            return;
        }
        if (playerPending != null && journalPending != null && !playerPending.equals(journalPending)) {
            CompoundTag copies = new CompoundTag();
            copies.put(PLAYER_PENDING_COPY_KEY, playerPending.copy());
            copies.put(JOURNAL_PENDING_COPY_KEY, journalPending.copy());
            history.add(copies);
        } else {
            appendQuarantinedPending(history, playerPending != null ? playerPending : journalPending);
        }
    }

    record QualityEntry(int slot, ItemStack originalStack, ItemStack clearedStack) {
    }

    record PendingOperation(
            String state,
            String moneyChain,
            long amount,
            long credited,
            boolean escrowed,
            List<QualityEntry> entries
    ) {
    }

    record JournalPresence(boolean present, long nextProbeTick) {
    }

    static long valueFor(int level, int count) {
        if (level <= 0 || level > MAX_QUALITY_LEVEL || count <= 0) {
            throw new IllegalArgumentException("Unsupported Quality Food value: level=" + level + ", count=" + count);
        }

        long unitValue = 1L << (level - 1);
        return Math.multiplyExact(unitValue, (long) count);
    }
}
