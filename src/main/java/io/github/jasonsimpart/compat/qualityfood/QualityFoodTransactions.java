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
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPendingStore.*;

final class QualityFoodTransactions {
    private QualityFoodTransactions() {
    }

    public static AbsorptionResult absorbInventoryQuality(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return AbsorptionResult.ROLLED_BACK_FAILURE;
        }
        if (hasPending(player)) {
            resolvePending(player);
            return AbsorptionResult.PENDING_RECOVERY;
        }

        List<QualityEntry> entries = new ArrayList<>();
        try {
            long amount = 0L;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                QualityFoodCompat.QualityResult qualityResult = QualityFoodCompat.getQualityLevel(stack);
                if (!qualityResult.success()) {
                    return AbsorptionResult.ROLLED_BACK_FAILURE;
                }
                int level = qualityResult.level();
                if (level > 0) {
                    if (level > MAX_QUALITY_LEVEL) {
                        CreateDelightCore.LOGGER.warn("Quality Food level {} exceeds the supported currency range", level);
                        return AbsorptionResult.ROLLED_BACK_FAILURE;
                    }
                    ItemStack clearedStack = stack.copy();
                    QualityFoodCompat.QualityResult clearResult = QualityFoodCompat.clearQuality(clearedStack);
                    if (!clearResult.success() || clearResult.level() != level) {
                        return AbsorptionResult.ROLLED_BACK_FAILURE;
                    }
                    long value = valueFor(level, stack.getCount());
                    entries.add(new QualityEntry(slot, stack.copy(), clearedStack));
                    amount = Math.addExact(amount, value);
                }
            }

            if (amount <= 0L) {
                return AbsorptionResult.ROLLED_BACK_FAILURE;
            }

            String chain = moneyChain();
            MoneyValue money = CoinValue.fromNumber(chain, amount);
            if (money.isEmpty() || money.getCoreValue() != amount) {
                CreateDelightCore.LOGGER.warn("Quality Food currency chain '{}' cannot represent {} core units", chain, amount);
                return AbsorptionResult.ROLLED_BACK_FAILURE;
            }
            var moneyHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
            if (!moneyHandler.insertMoney(money, true).isEmpty()) {
                return AbsorptionResult.ROLLED_BACK_FAILURE;
            }

            long moneyBefore = storedCoreValue(moneyHandler, money);
            AbsorptionResult pendingResult = writePending(player, entries, amount, 0L, chain, STATE_INSERTING);
            if (pendingResult != AbsorptionResult.SUCCESS) {
                return pendingResult;
            }

            MoneyValue remaining;
            try {
                remaining = moneyHandler.insertMoney(money, false);
            } catch (LinkageError | RuntimeException exception) {
                InsertionObservation observation = insertedSince(moneyHandler, money, moneyBefore, exception);
                if (!observation.known()) {
                    updatePendingState(player, STATE_UNRESOLVED, 0L);
                    CreateDelightCore.LOGGER.error("Quality Food currency insertion could not be reconciled; compensation is held", exception);
                    return AbsorptionResult.PENDING_RECOVERY;
                }
                MoneyValue inserted = observation.inserted();
                if (inserted.isEmpty()) {
                    boolean cleared = discardPendingAfterRestore(player);
                    CreateDelightCore.LOGGER.warn("Quality Food currency insertion failed; compensation is pending", exception);
                    return cleared ? AbsorptionResult.ROLLED_BACK_FAILURE : AbsorptionResult.PENDING_RECOVERY;
                }
                if (inserted.getCoreValue() < money.getCoreValue()) {
                    boolean rolledBack = rollbackPending(player, moneyHandler, inserted, exception);
                    if (!rolledBack) {
                        CreateDelightCore.LOGGER.error("Quality Food currency rollback is pending after partial insertion", exception);
                    }
                    return rolledBack ? AbsorptionResult.ROLLED_BACK_FAILURE : AbsorptionResult.PENDING_RECOVERY;
                }
                return commitPending(player, money.getCoreValue(), money.getText(), entries, true, exception)
                        ? AbsorptionResult.SUCCESS
                        : AbsorptionResult.PENDING_RECOVERY;
            }

            if (!remaining.isEmpty()) {
                MoneyValue accepted = money.subtractValue(remaining);
                if (accepted.isEmpty()) {
                    boolean cleared = discardPendingAfterRestore(player);
                    CreateDelightCore.LOGGER.warn("Quality Food currency insertion did not accept the requested value");
                    return cleared ? AbsorptionResult.ROLLED_BACK_FAILURE : AbsorptionResult.PENDING_RECOVERY;
                }
                boolean rolledBack = rollbackPending(player, moneyHandler, accepted, null);
                if (!rolledBack) {
                    CreateDelightCore.LOGGER.error("Quality Food currency rollback is pending after partial insertion");
                }
                CreateDelightCore.LOGGER.warn("Quality Food currency insertion accepted only part of the requested value");
                return rolledBack ? AbsorptionResult.ROLLED_BACK_FAILURE : AbsorptionResult.PENDING_RECOVERY;
            }

            return commitPending(player, money.getCoreValue(), money.getText(), entries, true, null)
                    ? AbsorptionResult.SUCCESS
                    : AbsorptionResult.PENDING_RECOVERY;
        } catch (LinkageError exception) {
            CreateDelightCore.LOGGER.warn("Quality Food currency compat is unavailable", exception);
            return failureResult(player);
        } catch (ArithmeticException | IllegalArgumentException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food inventory value exceeds the supported currency range", exception);
            return failureResult(player);
        } catch (RuntimeException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food currency compat failed", exception);
            return failureResult(player);
        }
    }

    static AbsorptionResult failureResult(Player player) {
        return hasPending(player) ? AbsorptionResult.PENDING_RECOVERY : AbsorptionResult.ROLLED_BACK_FAILURE;
    }

    static void resolvePending(Player player) {
        CompoundTag data = player.getPersistentData();
        boolean terminalSnapshot = data.contains(PENDING_KEY, Tag.TAG_COMPOUND)
                && isTerminalState(data.getCompound(PENDING_KEY).getString(STATE_KEY));
        PendingOperation pending = readPending(player);
        if (pending == null) {
            return;
        }

        try {
            switch (pending.state()) {
                case STATE_INSERTING -> {
                    updatePendingState(player, STATE_UNRESOLVED, pending.credited());
                    logPendingUnresolved(player);
                }
                case STATE_ROLLING_BACK -> {
                    logPendingRollback(player);
                }
                case STATE_ROLLBACK_UNKNOWN -> {
                    logPendingRollback(player);
                }
                case STATE_COMMITTING -> {
                    MoneyValue money = moneyForPending(pending);
                    commitPending(
                            player,
                            pending.amount(),
                            money.getText(),
                            pending.entries(),
                            pending.escrowed(),
                            null
                    );
                }
                case STATE_COMMIT_COMPLETE, STATE_ROLLBACK_COMPLETE -> {
                    if (!terminalSnapshot) {
                        recoverJournalOnlyTerminal(player, pending);
                    }
                }
                case STATE_UNRESOLVED -> {
                    // Do not infer a later balance change as this transaction's credit.
                }
                default -> CreateDelightCore.LOGGER.error("Unknown Quality Food compensation state: {}", pending.state());
            }
        } catch (LinkageError | RuntimeException exception) {
            quarantinePending(player);
            CreateDelightCore.LOGGER.warn("Quality Food compensation retry failed", exception);
        }
    }

    static boolean recoverJournalOnlyTerminal(Player player, PendingOperation pending) {
        boolean rollback = STATE_ROLLBACK_COMPLETE.equals(pending.state());
        if (inventoryMatches(player, pending.entries(), rollback)) {
            return prepareTerminalPlayerSnapshot(player) && persistTerminalPlayerState(player);
        }

        for (QualityEntry entry : pending.entries()) {
            ItemStack current = player.getInventory().getItem(entry.slot());
            ItemStack source = rollback ? entry.clearedStack() : entry.originalStack();
            if (!sameStack(current, source)) {
                return false;
            }
        }

        List<QualityEntry> appliedEntries = new ArrayList<>();
        try {
            for (QualityEntry entry : pending.entries()) {
                ItemStack target = rollback ? entry.originalStack() : entry.clearedStack();
                appliedEntries.add(entry);
                player.getInventory().setItem(entry.slot(), target.copy());
            }
            player.getInventory().setChanged();
            return prepareTerminalPlayerSnapshot(player) && persistTerminalPlayerState(player);
        } catch (LinkageError | RuntimeException exception) {
            for (QualityEntry entry : appliedEntries) {
                ItemStack source = rollback ? entry.clearedStack() : entry.originalStack();
                try {
                    player.getInventory().setItem(entry.slot(), source.copy());
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore Quality Food terminal recovery state", restoreException);
                }
            }
            try {
                player.getInventory().setChanged();
            } catch (LinkageError | RuntimeException restoreException) {
                CreateDelightCore.LOGGER.error("Unable to mark restored Quality Food terminal recovery state as changed", restoreException);
            }
            CreateDelightCore.LOGGER.error("Unable to recover the journal-only Quality Food terminal state", exception);
            return false;
        }
    }

    static boolean prepareTerminalPlayerSnapshot(Player player) {
        CompoundTag data = player.getPersistentData();
        if (isTerminalPendingTag(data.get(PENDING_KEY))) {
            return true;
        }
        try {
            QualityFoodJournalData journal = journalFor(player);
            Tag journalPending = journal.getPending(journalPlayerKey(player));
            if (!isTerminalPendingTag(journalPending)) {
                return false;
            }
            data.put(PENDING_KEY, journalPending.copy());
            return true;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to prepare the Quality Food terminal player snapshot", exception);
            return false;
        }
    }

    static boolean rollbackPendingRecord(Player player) {
        PendingOperation pending = readPending(player);
        if (pending == null
                || STATE_INSERTING.equals(pending.state())
                || STATE_COMMITTING.equals(pending.state())
                || STATE_ROLLBACK_UNKNOWN.equals(pending.state())
                || STATE_ROLLBACK_COMPLETE.equals(pending.state())
                || STATE_UNRESOLVED.equals(pending.state())) {
            return false;
        }

        try {
            MoneyValue money = moneyForPending(pending);
            MoneyValue credited = money.fromCoreValue(pending.credited());
            var moneyHandler = MoneyAPI.getApi().GetPlayersMoneyHandler(player);
            boolean rolledBack = rollbackPending(player, moneyHandler, credited, null);
            return rolledBack;
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food compensation rollback command failed", exception);
            return false;
        }
    }

    static boolean forceCommitUnresolved(Player player) {
        PendingOperation pending = readPending(player);
        if (pending == null
                || !STATE_UNRESOLVED.equals(pending.state())
                || pending.credited() < pending.amount()) {
            return false;
        }
        try {
            MoneyValue money = moneyForPending(pending);
            return commitPending(player, pending.amount(), money.getText(), pending.entries(), pending.escrowed(), null);
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food unresolved compensation commit failed", exception);
            return false;
        }
    }

    static boolean forceCommitUncertain(Player player) {
        PendingOperation pending = readPending(player);
        if (pending == null
                || !STATE_UNRESOLVED.equals(pending.state())
                || pending.credited() >= pending.amount()) {
            return false;
        }
        try {
            MoneyValue money = moneyForPending(pending);
            return commitPending(player, pending.amount(), money.getText(), pending.entries(), pending.escrowed(), null);
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food uncertain compensation commit failed", exception);
            return false;
        }
    }

    static boolean discardUnresolved(Player player) {
        PendingOperation pending = readPending(player);
        if (pending == null
                || (!STATE_UNRESOLVED.equals(pending.state())
                && !STATE_ROLLBACK_UNKNOWN.equals(pending.state())
                && !STATE_ROLLBACK_COMPLETE.equals(pending.state()))) {
            return false;
        }
        if (STATE_ROLLBACK_COMPLETE.equals(pending.state())) {
            if (!inventoryMatches(player, pending.entries(), true)
                    || !clearPendingDurably(player)) {
                return false;
            }
            CreateDelightCore.LOGGER.error("Quality Food completed rollback was explicitly discarded; currency and quality may require manual reconciliation");
            return true;
        }
        if (!restorePendingInventory(player, pending.entries(), pending.escrowed())) {
            return false;
        }
        if (!clearPendingDurably(player)) {
            return false;
        }
        CreateDelightCore.LOGGER.error("Quality Food unresolved compensation was explicitly discarded; currency and quality may require manual reconciliation");
        return true;
    }

    static boolean discardInvalid(Player player) {
        CompoundTag data = player.getPersistentData();
        QualityFoodJournalData journal = null;
        Tag previousJournalInvalid = null;
        try {
            journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            if (!data.contains(INVALID_PENDING_KEY) && journal.hasInvalid(playerKey)) {
                data.put(INVALID_PENDING_KEY, journal.getInvalid(playerKey));
            }
            previousJournalInvalid = journal.getInvalid(playerKey);
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to inspect the quarantined Quality Food journal", exception);
            return false;
        }
        if (!data.contains(INVALID_PENDING_KEY)) {
            return false;
        }
        Tag previousHistory = data.get(INVALID_PENDING_KEY).copy();
        data.remove(INVALID_PENDING_KEY);
        try {
            journal.removeInvalid(journalPlayerKey(player));
        } catch (LinkageError | RuntimeException exception) {
            data.put(INVALID_PENDING_KEY, previousHistory);
            return false;
        }
        try {
            forcePendingSave(player);
            if (!data.contains(PENDING_KEY)) {
                markRecoveryAbsent(player);
            }
            CreateDelightCore.LOGGER.error("Quarantined Quality Food compensation was explicitly discarded; currency and quality may require manual reconciliation");
            return true;
        } catch (LinkageError | RuntimeException exception) {
            if (previousHistory != null) {
                data.put(INVALID_PENDING_KEY, previousHistory);
            }
            if (journal != null) {
                try {
                    if (previousJournalInvalid == null) {
                        journal.removeInvalid(journalPlayerKey(player));
                    } else {
                        journal.putInvalid(journalPlayerKey(player), previousJournalInvalid);
                    }
                } catch (LinkageError | RuntimeException restoreException) {
                    CreateDelightCore.LOGGER.error("Unable to restore the quarantined Quality Food journal", restoreException);
                }
            }
            CreateDelightCore.LOGGER.error("Unable to persist quarantined Quality Food compensation discard", exception);
            return false;
        }
    }

    static boolean retryInvalid(Player player) {
        CompoundTag data = player.getPersistentData();
        QualityFoodJournalData journal;
        String playerKey;
        try {
            journal = journalFor(player);
            playerKey = journalPlayerKey(player);
            if (!data.contains(INVALID_PENDING_KEY) && journal.hasInvalid(playerKey)) {
                data.put(INVALID_PENDING_KEY, journal.getInvalid(playerKey));
            }
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to inspect the quarantined Quality Food journal", exception);
            return false;
        }
        if (data.contains(PENDING_KEY)
                || journal.hasPending(playerKey)
                || !data.contains(INVALID_PENDING_KEY)) {
            return false;
        }

        Tag previousHistory = data.get(INVALID_PENDING_KEY).copy();
        CompoundTag pending;
        ListTag remainingHistory = null;
        if (previousHistory instanceof ListTag history) {
            if (history.isEmpty()) {
                return false;
            }
            Tag candidate = history.get(history.size() - 1);
            if (!(candidate instanceof CompoundTag candidateCompound)) {
                return false;
            }
            pending = candidateCompound.copy();
            remainingHistory = history.copy();
            remainingHistory.remove(remainingHistory.size() - 1);
        } else if (previousHistory instanceof CompoundTag compound) {
            pending = compound.copy();
        } else {
            return false;
        }

        if (!isCompletePendingRecord(player, pending)) {
            return false;
        }

        if (remainingHistory == null || remainingHistory.isEmpty()) {
            data.remove(INVALID_PENDING_KEY);
        } else {
            data.put(INVALID_PENDING_KEY, remainingHistory);
        }
        data.put(PENDING_KEY, pending);
        try {
            journal.putPending(playerKey, pending);
            if (data.contains(INVALID_PENDING_KEY)) {
                journal.putInvalid(playerKey, data.get(INVALID_PENDING_KEY));
            } else {
                journal.removeInvalid(playerKey);
            }
            forcePendingSave(player);
            return true;
        } catch (LinkageError | RuntimeException exception) {
            data.remove(PENDING_KEY);
            data.put(INVALID_PENDING_KEY, previousHistory);
            try {
                journal.removePending(playerKey);
                journal.putInvalid(playerKey, previousHistory);
            } catch (LinkageError | RuntimeException restoreException) {
                CreateDelightCore.LOGGER.error("Unable to restore the quarantined Quality Food journal after retry failed", restoreException);
            }
            CreateDelightCore.LOGGER.error("Unable to persist quarantined Quality Food compensation retry", exception);
            return false;
        }
    }

    static boolean commitPending(
            Player player,
            long amount,
            Object currencyDisplay,
            List<QualityEntry> entries,
            boolean escrowed,
            Throwable cause
    ) {
        if (!updatePendingState(player, STATE_COMMITTING, amount, true)) {
            return false;
        }

        List<QualityEntry> reconciledEntries = reconcileEntries(player, entries, escrowed);
        if (reconciledEntries == null || !commitInventory(player, reconciledEntries, escrowed)) {
            updatePendingState(player, STATE_UNRESOLVED, amount);
            logPendingInventoryFailure(player, cause);
            return false;
        }

        // The inventory is now committed. Persist a terminal tombstone before
        // clearing the transaction so a stale player NBT copy cannot replay the
        // non-terminal COMMITTING branch after a crash.
        if (!updatePendingState(player, STATE_COMMIT_COMPLETE, amount, false)) {
            CreateDelightCore.LOGGER.error("Quality Food inventory commit completed but its terminal state could not be journaled", cause);
            return false;
        }
        if (!persistTerminalPlayerState(player)) {
            return false;
        }
        try {
            player.sendSystemMessage(Component.translatable(ABSORB_MESSAGE, currencyDisplay));
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.warn("Quality Food currency conversion succeeded, but its notification failed", exception);
        }
        return true;
    }

    static boolean inventoryMatches(Player player, List<QualityEntry> entries, boolean original) {
        for (QualityEntry entry : entries) {
            ItemStack expected = original ? entry.originalStack() : entry.clearedStack();
            if (!sameStack(player.getInventory().getItem(entry.slot()), expected)) {
                return false;
            }
        }
        return true;
    }

    static List<QualityEntry> reconcileEntries(Player player, List<QualityEntry> entries, boolean escrowed) {
        List<QualityEntry> reconciled = new ArrayList<>(entries.size());
        for (QualityEntry entry : entries) {
            ItemStack current = player.getInventory().getItem(entry.slot());
            if (escrowed
                    ? (!current.isEmpty()
                    && !sameStack(current, entry.clearedStack())
                    && !sameStack(current, entry.originalStack()))
                    : !sameStack(current, entry.originalStack())) {
                return null;
            }
            reconciled.add(new QualityEntry(entry.slot(), entry.originalStack(), entry.clearedStack()));
        }
        return reconciled;
    }

    static boolean commitInventory(Player player, List<QualityEntry> entries, boolean escrowed) {
        List<QualityEntry> appliedEntries = new ArrayList<>();
        try {
            for (QualityEntry entry : entries) {
                ItemStack current = player.getInventory().getItem(entry.slot());
                if (escrowed
                        ? (!current.isEmpty()
                        && !sameStack(current, entry.clearedStack())
                        && !sameStack(current, entry.originalStack()))
                        : !sameStack(current, entry.originalStack())) {
                    return false;
                }
            }

            for (QualityEntry entry : entries) {
                ItemStack current = player.getInventory().getItem(entry.slot());
                if (sameStack(current, entry.clearedStack())) {
                    continue;
                }
                if (escrowed
                        ? (!current.isEmpty() && !sameStack(current, entry.originalStack()))
                        : !sameStack(current, entry.originalStack())) {
                    throw new IllegalStateException("Quality Food inventory changed during compensation commit");
                }
                appliedEntries.add(entry);
                player.getInventory().setItem(entry.slot(), entry.clearedStack().copy());
            }
            player.getInventory().setChanged();
            return true;
        } catch (LinkageError | RuntimeException exception) {
            boolean restored = true;
            for (QualityEntry entry : appliedEntries) {
                try {
                    player.getInventory().setItem(
                            entry.slot(),
                            escrowed ? ItemStack.EMPTY : entry.originalStack().copy()
                    );
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to restore a Quality Food stack after a failed compensation commit", restoreException);
                }
            }
            if (restored) {
                try {
                    player.getInventory().setChanged();
                } catch (LinkageError | RuntimeException restoreException) {
                    restored = false;
                    CreateDelightCore.LOGGER.error("Unable to mark restored Quality Food inventory state as changed", restoreException);
                }
            }
            if (!restored) {
                CreateDelightCore.LOGGER.error("Quality Food inventory compensation could not be fully restored; manual reconciliation may be required");
            }
            CreateDelightCore.LOGGER.warn("Quality Food inventory update failed; compensation will be retried", exception);
            return false;
        }
    }

    static boolean rollbackPending(
            Player player,
            IMoneyHandler moneyHandler,
            MoneyValue amount,
            Throwable cause
    ) {
        if (amount.isEmpty()) {
            PendingOperation pending = readPending(player);
            return pending != null && finishRollback(player, pending);
        }
        return rollbackMoney(player, moneyHandler, amount, cause);
    }

    static void enforceEscrowedInventory(Player player, PendingOperation pending) {
        boolean conflict = false;
        for (QualityEntry entry : pending.entries()) {
            ItemStack current = player.getInventory().getItem(entry.slot());
            if (current.isEmpty()
                    || sameStack(current, entry.clearedStack())
                    || sameStack(current, entry.originalStack())) {
                continue;
            }
            conflict = true;
        }
        if (conflict && !STATE_UNRESOLVED.equals(pending.state())) {
            if (updatePendingState(player, STATE_UNRESOLVED, pending.credited())) {
                logPendingUnresolved(player);
            } else {
                CreateDelightCore.LOGGER.error("Quality Food inventory escrow conflict could not be journaled");
            }
        }
    }

    static boolean restorePendingInventory(Player player, List<QualityEntry> entries, boolean escrowed) {
        if (!escrowed) {
            return true;
        }
        boolean restored = true;
        boolean changed = false;
        for (QualityEntry entry : entries) {
            ItemStack current = player.getInventory().getItem(entry.slot());
            if (sameStack(current, entry.originalStack())) {
                continue;
            }
            if (!current.isEmpty() && !sameStack(current, entry.clearedStack())) {
                restored = false;
                CreateDelightCore.LOGGER.error("A Quality Food escrow slot contains an unexpected stack; manual reconciliation is required");
                continue;
            }
            try {
                player.getInventory().setItem(entry.slot(), entry.originalStack().copy());
                changed = true;
            } catch (LinkageError | RuntimeException exception) {
                restored = false;
                CreateDelightCore.LOGGER.error("Unable to restore a Quality Food escrowed stack", exception);
            }
        }
        if (changed) {
            try {
                player.getInventory().setChanged();
            } catch (LinkageError | RuntimeException exception) {
                restored = false;
                CreateDelightCore.LOGGER.error("Unable to mark restored Quality Food escrow as changed", exception);
            }
        }
        return restored;
    }

    static boolean finishRollback(Player player, PendingOperation pending) {
        if (STATE_ROLLBACK_COMPLETE.equals(pending.state())) {
            return true;
        }
        if (!restorePendingInventory(player, pending.entries(), pending.escrowed())) {
            CreateDelightCore.LOGGER.error("Quality Food rollback completed financially but inventory restoration remains pending");
            return false;
        }
        return clearPendingDurably(player);
    }

    static boolean discardPendingAfterRestore(Player player) {
        PendingOperation pending = readPending(player);
        return pending != null && finishRollback(player, pending);
    }

    static boolean rollbackMoney(Player player, IMoneyHandler moneyHandler, MoneyValue amount, Throwable cause) {
        long before = storedCoreValue(moneyHandler, amount);
        if (!updatePendingState(player, STATE_ROLLBACK_UNKNOWN, amount.getCoreValue())) {
            CreateDelightCore.LOGGER.error("Quality Food currency rollback could not be journaled before extraction", cause);
            return false;
        }
        try {
            MoneyValue remaining = moneyHandler.extractMoney(amount, false);
            if (!remaining.isEmpty()) {
                long remainder = remainingCore(amount, remaining, before, moneyHandler);
                if (remainder <= 0L || !updatePendingState(player, STATE_ROLLING_BACK, remainder)) {
                    CreateDelightCore.LOGGER.error("Quality Food currency rollback remainder could not be durably recorded", cause);
                }
                CreateDelightCore.LOGGER.error("Quality Food currency rollback left a remainder", cause);
                return false;
            }
            return completeRollback(player);
        } catch (LinkageError | RuntimeException exception) {
            try {
                long after = storedCoreValue(moneyHandler, amount);
                long removed = Math.max(0L, before - after);
                long remainder = Math.max(0L, amount.getCoreValue() - removed);
                if (remainder == 0L) {
                    return completeRollback(player);
                } else {
                    updatePendingState(player, STATE_ROLLING_BACK, remainder);
                }
            } catch (LinkageError | RuntimeException ignored) {
                CreateDelightCore.LOGGER.error("Quality Food rollback outcome is unknown; retaining the non-retryable rollback journal", ignored);
            }
            CreateDelightCore.LOGGER.error("Quality Food currency rollback failed", exception);
            return false;
        }
    }

    static boolean completeRollback(Player player) {
        PendingOperation pending = readPending(player);
        if (pending == null || !restorePendingInventory(player, pending.entries(), pending.escrowed())) {
            CreateDelightCore.LOGGER.error("Quality Food currency rollback completed but inventory restoration remains pending");
            return false;
        }
        if (!updatePendingState(player, STATE_ROLLBACK_COMPLETE, 0L, false)) {
            CreateDelightCore.LOGGER.error("Quality Food currency rollback completed but its completion state could not be journaled");
            return false;
        }
        return persistTerminalPlayerState(player);
    }

    static long remainingCore(MoneyValue requested, MoneyValue remaining, long before, IMoneyHandler moneyHandler) {
        if (!remaining.isEmpty() && remaining.sameType(requested)) {
            return Math.min(requested.getCoreValue(), remaining.getCoreValue());
        }
        long after = storedCoreValue(moneyHandler, requested);
        return Math.max(0L, requested.getCoreValue() - Math.max(0L, before - after));
    }

    static InsertionObservation insertedSince(IMoneyHandler moneyHandler, MoneyValue requested, long before, Throwable cause) {
        try {
            long after = storedCoreValue(moneyHandler, requested);
            long inserted = after - before;
            if (inserted <= 0L) {
                return new InsertionObservation(true, MoneyValue.empty());
            }
            return new InsertionObservation(true, requested.fromCoreValue(Math.min(inserted, requested.getCoreValue())));
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to determine currency inserted before the failure", cause);
            return new InsertionObservation(false, MoneyValue.empty());
        }
    }

    static long storedCoreValue(IMoneyHandler moneyHandler, MoneyValue requested) {
        MoneyValue stored = moneyHandler.getStoredMoney().valueOf(requested.getUniqueName());
        if (stored.isEmpty() || !stored.sameType(requested)) {
            return 0L;
        }
        return stored.getCoreValue();
    }

    static String moneyChain() {
        return Config.QUALITY_FOOD_MONEY_CHAIN.get();
    }

    static MoneyValue moneyForPending(PendingOperation pending) {
        MoneyValue money = CoinValue.fromNumber(pending.moneyChain(), pending.amount());
        if (money.isEmpty() || money.getCoreValue() != pending.amount()) {
            throw new IllegalStateException("Quality Food compensation currency chain no longer represents the recorded amount");
        }
        return money;
    }

    record InsertionObservation(boolean known, MoneyValue inserted) {
    }
}
