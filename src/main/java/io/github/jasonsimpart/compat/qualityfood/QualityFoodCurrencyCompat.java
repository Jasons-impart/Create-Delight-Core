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

public final class QualityFoodCurrencyCompat {
    public enum AbsorptionResult {
        SUCCESS,
        ROLLED_BACK_FAILURE,
        PENDING_RECOVERY
    }

    private static final int MAX_QUALITY_LEVEL = Long.SIZE - 2;
    private static final String ABSORB_MESSAGE = "message.createdelightcore.quality_absorber.absorbed";
    private static final String PENDING_KEY = "createdelightcore:quality_absorber_pending";
    private static final String STATE_KEY = "State";
    private static final String STATE_INSERTING = "inserting";
    private static final String STATE_ROLLING_BACK = "rolling_back";
    private static final String STATE_ROLLBACK_UNKNOWN = "rollback_unknown";
    private static final String STATE_ROLLBACK_COMPLETE = "rollback_complete";
    private static final String STATE_COMMITTING = "committing";
    private static final String STATE_COMMIT_COMPLETE = "commit_complete";
    private static final String STATE_UNRESOLVED = "unresolved";
    private static final int MAX_INVALID_PENDING_HISTORY = 4;
    private static final String INVALID_PENDING_KEY = "createdelightcore:quality_absorber_pending_invalid";
    private static final String MONEY_CHAIN_KEY = "MoneyChain";
    private static final String AMOUNT_KEY = "Amount";
    private static final String CREDITED_KEY = "Credited";
    private static final String ENTRIES_KEY = "Entries";
    private static final String ESCROWED_KEY = "Escrowed";
    private static final String SLOT_KEY = "Slot";
    private static final String ORIGINAL_KEY = "Original";
    private static final String CLEARED_KEY = "Cleared";
    private static final String WARNING_LOGGED_KEY = "WarningLogged";
    private static final String RAW_PENDING_KEY = "RawPending";
    private static final String SAVED_DATA_ROOT_KEY = "data";
    private static final String LEGACY_MIGRATION_COMPLETE_KEY = "LegacyMigrationComplete";
    private static final String JOURNAL_DATA_ID = "createdelightcore_quality_food_journal";
    private static final String JOURNAL_PENDING_KEY = "Pending";
    private static final String JOURNAL_INVALID_KEY = "Invalid";
    private static final String JOURNAL_MALFORMED_KEY = "Malformed";
    private static final String PLAYER_PENDING_COPY_KEY = "PlayerPending";
    private static final String JOURNAL_PENDING_COPY_KEY = "JournalPending";
    private static final long ABSENT_JOURNAL_RECHECK_TICKS = 200L;
    private static final Map<UUID, JournalPresence> JOURNAL_PRESENCE = new HashMap<>();
    private static final Map<Path, CompoundTag> LEGACY_JOURNAL_CACHE = new HashMap<>();

    private QualityFoodCurrencyCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, QualityFoodCurrencyCompat::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, QualityFoodCurrencyCompat::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, QualityFoodCurrencyCompat::onPlayerSave);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, QualityFoodCurrencyCompat::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(QualityFoodCurrencyCompat::registerCommands);
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        JOURNAL_PRESENCE.remove(event.getEntity().getUUID());
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalData = event.getOriginal().getPersistentData();
        CompoundTag replacementData = event.getEntity().getPersistentData();
        copyPersistentEntry(originalData, replacementData, PENDING_KEY);
        copyPersistentEntry(originalData, replacementData, INVALID_PENDING_KEY);
        Tag journalPending = null;
        Tag journalInvalid = null;
        if (event.isWasDeath() && event.getOriginal() instanceof ServerPlayer original) {
            try {
                QualityFoodJournalData journal = journalFor(original);
                journalPending = journal.getPending(journalPlayerKey(original));
                journalInvalid = journal.getInvalid(journalPlayerKey(original));
            } catch (LinkageError | RuntimeException exception) {
                CreateDelightCore.LOGGER.error("Unable to inspect Quality Food compensation during player death", exception);
            }
        }
        if (event.isWasDeath() && journalInvalid != null) {
            replacementData.put(INVALID_PENDING_KEY, journalInvalid);
            replacementData.remove(PENDING_KEY);
        }
        Tag deathPending = journalPending != null ? journalPending : replacementData.contains(PENDING_KEY)
                ? replacementData.get(PENDING_KEY)
                : null;
        boolean clearOriginalPending = deathPending != null || journalInvalid != null;
        if (event.isWasDeath() && deathPending instanceof CompoundTag pendingTag) {
            CompoundTag pending = pendingTag.copy();
            String state = pending.getString(STATE_KEY);
            if (isTerminalState(state)) {
                replacementData.remove(PENDING_KEY);
            } else {
                if (STATE_INSERTING.equals(state) || STATE_COMMITTING.equals(state)) {
                    pending.putString(STATE_KEY, STATE_UNRESOLVED);
                }
                replacementData.put(PENDING_KEY, pending);
                if (STATE_INSERTING.equals(state) || STATE_COMMITTING.equals(state)) {
                    CreateDelightCore.LOGGER.warn("Quality Food compensation was moved to explicit recovery after player death");
                }
            }
        }
        boolean replacementJournalSynchronized = false;
        if (event.getEntity() instanceof ServerPlayer replacement
                && (replacementData.contains(PENDING_KEY)
                || replacementData.contains(INVALID_PENDING_KEY)
                || journalPending != null
                || journalInvalid != null)) {
            try {
                QualityFoodJournalData journal = journalFor(replacement);
                String playerKey = journalPlayerKey(replacement);
                if (replacementData.contains(PENDING_KEY)) {
                    journal.putPendingTag(playerKey, replacementData.get(PENDING_KEY));
                } else {
                    journal.removePending(playerKey);
                }
                if (replacementData.contains(INVALID_PENDING_KEY)) {
                    journal.putInvalid(playerKey, replacementData.get(INVALID_PENDING_KEY));
                } else {
                    journal.removeInvalid(playerKey);
                }
                forcePendingSave(replacement);
                replacementJournalSynchronized = true;
            } catch (LinkageError | RuntimeException exception) {
                CreateDelightCore.LOGGER.error("Unable to synchronize Quality Food compensation after player clone", exception);
            }
        }
        if (event.isWasDeath() && clearOriginalPending && replacementJournalSynchronized) {
            CompoundTag originalDataAfterSync = event.getOriginal().getPersistentData();
            originalDataAfterSync.remove(PENDING_KEY);
            if (journalInvalid != null) {
                originalDataAfterSync.remove(INVALID_PENDING_KEY);
            }
        } else if (event.isWasDeath() && clearOriginalPending && !replacementJournalSynchronized) {
            CreateDelightCore.LOGGER.error("Unable to clear the original Quality Food compensation after player death");
        }
    }

    private static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();
        boolean terminalSnapshot = data.contains(PENDING_KEY, Tag.TAG_COMPOUND)
                && isTerminalState(data.getCompound(PENDING_KEY).getString(STATE_KEY));
        if (!terminalSnapshot) {
            return;
        }
        PendingOperation operation = readPendingIfRecoveryMayExist(player);
        if (operation == null || !isTerminalState(operation.state())) {
            return;
        }

        try {
            QualityFoodJournalData journal = journalFor(player);
            String playerKey = journalPlayerKey(player);
            if (journal.hasInvalid(playerKey)) {
                return;
            }
            if (!clearPendingDurably(player)) {
                CreateDelightCore.LOGGER.error("Quality Food completed compensation remains pending after player save");
            }
        } catch (LinkageError | RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Unable to finalize Quality Food compensation after player save", exception);
        }
    }

    private static void copyPersistentEntry(CompoundTag source, CompoundTag target, String key) {
        if (source.contains(key)) {
            target.put(key, source.get(key).copy());
        }
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var retryCommand = Commands.literal("retry")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!hasPending(player)) {
                                context.getSource().sendFailure(Component.literal("No Quality Food compensation is pending for that player"));
                                return 0;
                            }
                            resolvePending(player);
                            if (hasPending(player)) {
                                context.getSource().sendFailure(Component.literal("Quality Food compensation is still pending; inspect the player state before retrying"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Retried Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var rollbackCommand = Commands.literal("rollback")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!rollbackPendingRecord(player)) {
                                context.getSource().sendFailure(Component.literal("The pending Quality Food compensation is not safely rollbackable yet"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Rolled back Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var forceCommitCommand = Commands.literal("force_commit_unresolved")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!forceCommitUnresolved(player)) {
                                context.getSource().sendFailure(Component.literal("No unresolved Quality Food compensation can be force-committed for that player"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Force-committed unresolved Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var forceCommitUncertainCommand = Commands.literal("force_commit_uncertain")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!forceCommitUncertain(player)) {
                                context.getSource().sendFailure(Component.literal("No uncertain Quality Food compensation can be force-committed for that player"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Force-committed uncertain Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var discardCommand = Commands.literal("discard_unresolved")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!discardUnresolved(player)) {
                                context.getSource().sendFailure(Component.literal("No unresolved Quality Food compensation exists for that player"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Discarded unresolved Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var discardInvalidCommand = Commands.literal("discard_invalid")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!discardInvalid(player)) {
                                context.getSource().sendFailure(Component.literal("No quarantined Quality Food compensation exists for that player"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Discarded quarantined Quality Food compensation for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var retryInvalidCommand = Commands.literal("retry_invalid")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            if (!retryInvalid(player)) {
                                context.getSource().sendFailure(Component.literal("No quarantined Quality Food compensation can be retried for that player"));
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Restored quarantined Quality Food compensation for retry for " + player.getGameProfile().getName()),
                                    true
                            );
                            return 1;
                        }));
        var qualityAbsorberCommand = Commands.literal("quality_absorber")
                .then(retryCommand)
                .then(rollbackCommand)
                .then(forceCommitCommand)
                .then(forceCommitUncertainCommand)
                .then(discardCommand)
                .then(discardInvalidCommand)
                .then(retryInvalidCommand);
        event.getDispatcher().register(Commands.literal("createdelightcore")
                .requires(source -> source.hasPermission(2))
                .then(qualityAbsorberCommand));
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        PendingOperation pending = readPendingIfRecoveryMayExist(player);
        if (pending != null && pending.escrowed()) {
            enforceEscrowedInventory(player, pending);
        }
        if (pending != null && player.tickCount % 20 == 0) {
            resolvePending(player);
        }
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

    private static AbsorptionResult failureResult(Player player) {
        return hasPending(player) ? AbsorptionResult.PENDING_RECOVERY : AbsorptionResult.ROLLED_BACK_FAILURE;
    }

    public static boolean hasPendingRecovery(Player player) {
        return hasPending(player);
    }

    private static void resolvePending(Player player) {
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

    private static boolean recoverJournalOnlyTerminal(Player player, PendingOperation pending) {
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

    private static boolean prepareTerminalPlayerSnapshot(Player player) {
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

    private static boolean rollbackPendingRecord(Player player) {
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

    private static boolean forceCommitUnresolved(Player player) {
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

    private static boolean forceCommitUncertain(Player player) {
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

    private static boolean discardUnresolved(Player player) {
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

    private static boolean discardInvalid(Player player) {
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

    private static boolean retryInvalid(Player player) {
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

    private static boolean commitPending(
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

    private static boolean inventoryMatches(Player player, List<QualityEntry> entries, boolean original) {
        for (QualityEntry entry : entries) {
            ItemStack expected = original ? entry.originalStack() : entry.clearedStack();
            if (!sameStack(player.getInventory().getItem(entry.slot()), expected)) {
                return false;
            }
        }
        return true;
    }

    private static List<QualityEntry> reconcileEntries(Player player, List<QualityEntry> entries, boolean escrowed) {
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

    private static boolean commitInventory(Player player, List<QualityEntry> entries, boolean escrowed) {
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

    private static boolean rollbackPending(
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

    private static void enforceEscrowedInventory(Player player, PendingOperation pending) {
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

    private static boolean restorePendingInventory(Player player, List<QualityEntry> entries, boolean escrowed) {
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

    private static boolean finishRollback(Player player, PendingOperation pending) {
        if (STATE_ROLLBACK_COMPLETE.equals(pending.state())) {
            return true;
        }
        if (!restorePendingInventory(player, pending.entries(), pending.escrowed())) {
            CreateDelightCore.LOGGER.error("Quality Food rollback completed financially but inventory restoration remains pending");
            return false;
        }
        return clearPendingDurably(player);
    }

    private static boolean discardPendingAfterRestore(Player player) {
        PendingOperation pending = readPending(player);
        return pending != null && finishRollback(player, pending);
    }

    private static AbsorptionResult writePending(
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

    private static boolean updatePendingState(Player player, String state, long credited) {
        return updatePendingState(player, state, credited, true);
    }

    private static boolean updatePendingState(Player player, String state, long credited, boolean durable) {
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

    private static PendingOperation readPending(Player player) {
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

    private static boolean isTerminalPendingTag(Tag tag) {
        return tag instanceof CompoundTag pending && isTerminalState(pending.getString(STATE_KEY));
    }

    private static boolean isCompletePendingRecord(Player player, Tag tag) {
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

    private static PendingOperation readPendingIfRecoveryMayExist(Player player) {
        return recoveryMayExist(player) ? readPending(player) : null;
    }

    private static boolean recoveryMayExist(Player player) {
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

    private static void markRecoveryPresent(Player player) {
        JOURNAL_PRESENCE.put(player.getUUID(), new JournalPresence(true, Long.MAX_VALUE));
    }

    private static void markRecoveryAbsent(Player player) {
        JOURNAL_PRESENCE.put(
                player.getUUID(),
                new JournalPresence(false, player.tickCount + ABSENT_JOURNAL_RECHECK_TICKS)
        );
    }

    private static boolean samePendingExceptWarning(Tag left, Tag right) {
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

    private static boolean samePendingExceptStateAndWarning(Tag left, Tag right) {
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

    private static boolean isTerminalState(String state) {
        return STATE_COMMIT_COMPLETE.equals(state) || STATE_ROLLBACK_COMPLETE.equals(state);
    }

    private static boolean terminalCanSupersede(String terminalState, String otherState) {
        return switch (terminalState) {
            case STATE_COMMIT_COMPLETE -> STATE_COMMITTING.equals(otherState)
                    || STATE_UNRESOLVED.equals(otherState);
            case STATE_ROLLBACK_COMPLETE -> STATE_ROLLING_BACK.equals(otherState)
                    || STATE_ROLLBACK_UNKNOWN.equals(otherState);
            default -> false;
        };
    }

    private static boolean isValidTerminalCredit(CompoundTag pending) {
        if (!isValidStateCredit(pending)) {
            return false;
        }
        long amount = pending.getLong(AMOUNT_KEY);
        long credited = pending.getLong(CREDITED_KEY);
        return (STATE_COMMIT_COMPLETE.equals(pending.getString(STATE_KEY))
                ? credited == amount
                : STATE_ROLLBACK_COMPLETE.equals(pending.getString(STATE_KEY)) && credited == 0L);
    }

    private static boolean isValidStateCredit(CompoundTag pending) {
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

    private static Tag reconcilePendingCopies(Tag playerPending, Tag journalPending) {
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

    private static PendingOperation parsePendingRecord(Player player, Tag tag) {
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

    private static boolean isKnownState(String state) {
        return STATE_INSERTING.equals(state)
                || STATE_ROLLING_BACK.equals(state)
                || STATE_ROLLBACK_UNKNOWN.equals(state)
                || STATE_ROLLBACK_COMPLETE.equals(state)
                || STATE_COMMITTING.equals(state)
                || STATE_COMMIT_COMPLETE.equals(state)
                || STATE_UNRESOLVED.equals(state);
    }

    private static boolean hasPending(Player player) {
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

    private static boolean clearPendingDurably(Player player) {
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

    private static boolean sameStack(ItemStack left, ItemStack right) {
        return left.getCount() == right.getCount() && ItemStack.matches(left, right);
    }

    private static boolean markWarningLogged(Player player) {
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

    private static void logPendingInventoryFailure(Player player, Throwable cause) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food inventory update is pending retry", cause);
        }
    }

    private static void logPendingUnresolved(Player player) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food currency compensation is unresolved; automatic conversion is paused");
        }
    }

    private static void logPendingRollback(Player player) {
        if (markWarningLogged(player)) {
            CreateDelightCore.LOGGER.error("Quality Food currency rollback is held for explicit administrator reconciliation");
        }
    }

    private static boolean rollbackMoney(Player player, IMoneyHandler moneyHandler, MoneyValue amount, Throwable cause) {
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

    private static boolean completeRollback(Player player) {
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

    private static long remainingCore(MoneyValue requested, MoneyValue remaining, long before, IMoneyHandler moneyHandler) {
        if (!remaining.isEmpty() && remaining.sameType(requested)) {
            return Math.min(requested.getCoreValue(), remaining.getCoreValue());
        }
        long after = storedCoreValue(moneyHandler, requested);
        return Math.max(0L, requested.getCoreValue() - Math.max(0L, before - after));
    }

    private static InsertionObservation insertedSince(IMoneyHandler moneyHandler, MoneyValue requested, long before, Throwable cause) {
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

    private static long storedCoreValue(IMoneyHandler moneyHandler, MoneyValue requested) {
        MoneyValue stored = moneyHandler.getStoredMoney().valueOf(requested.getUniqueName());
        if (stored.isEmpty() || !stored.sameType(requested)) {
            return 0L;
        }
        return stored.getCoreValue();
    }

    private static String moneyChain() {
        return Config.QUALITY_FOOD_MONEY_CHAIN.get();
    }

    private static MoneyValue moneyForPending(PendingOperation pending) {
        MoneyValue money = CoinValue.fromNumber(pending.moneyChain(), pending.amount());
        if (money.isEmpty() || money.getCoreValue() != pending.amount()) {
            throw new IllegalStateException("Quality Food compensation currency chain no longer represents the recorded amount");
        }
        return money;
    }

    private static QualityFoodJournalData journalFor(Player player) {
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

    private static String journalPlayerKey(Player player) {
        return player.getUUID().toString();
    }

    private static String journalDataId(Player player) {
        return JOURNAL_DATA_ID + "_" + journalPlayerKey(player);
    }

    private static boolean hasPlayerJournalFile(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return false;
        }
        return Files.isRegularFile(
                serverPlayer.getServer().getWorldPath(LevelResource.ROOT)
                        .resolve("data")
                        .resolve(journalDataId(player) + ".dat")
        );
    }

    private static Boolean legacyJournalContains(Player player) {
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

    private static void migrateLegacyJournal(ServerLevel level, Player player, QualityFoodJournalData journal) {
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

    private static CompoundTag readLegacyJournalData(ServerLevel level) throws java.io.IOException {
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

    private static Tag journalEntry(CompoundTag root, String sectionKey, String playerKey) {
        if (!root.contains(sectionKey, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag section = root.getCompound(sectionKey);
        return section.contains(playerKey) ? section.get(playerKey).copy() : null;
    }

    private static boolean persistTerminalPlayerState(Player player) {
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

    private static void forcePlayerSave(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            throw new IllegalStateException("Quality Food compensation requires a server-backed player");
        }
        try {
            serverPlayer.getServer().getPlayerList().save(serverPlayer);
        } catch (LinkageError | RuntimeException exception) {
            throw new IllegalStateException("Unable to force-save the Quality Food player state", exception);
        }
    }

    private static void forcePendingSave(Player player) {
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

    private static ListTag copyInvalidHistory(CompoundTag data) {
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

    private static void appendInvalidHistory(ListTag history, Tag existing) {
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

    private static void appendQuarantinedPending(ListTag history, Tag pending) {
        if (pending instanceof CompoundTag compound) {
            history.add(compound.copy());
        } else {
            CompoundTag wrappedPending = new CompoundTag();
            wrappedPending.put(RAW_PENDING_KEY, pending.copy());
            history.add(wrappedPending);
        }
    }

    private static void appendPendingCopies(ListTag history, Tag playerPending, Tag journalPending) {
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

    private static void quarantineInvalidState(
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

    private static void quarantineBothPendingCopies(Player player, Tag playerPending, Tag journalPending) {
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

    private static void quarantinePending(Player player) {
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

    private record QualityEntry(int slot, ItemStack originalStack, ItemStack clearedStack) {
    }

    private record PendingOperation(
            String state,
            String moneyChain,
            long amount,
            long credited,
            boolean escrowed,
            List<QualityEntry> entries
    ) {
    }

    private record InsertionObservation(boolean known, MoneyValue inserted) {
    }

    private record JournalPresence(boolean present, long nextProbeTick) {
    }

    private static final class QualityFoodJournalData extends SavedData {
        private final CompoundTag pending = new CompoundTag();
        private final CompoundTag invalid = new CompoundTag();
        private final CompoundTag malformed = new CompoundTag();
        private boolean legacyMigrationComplete;

        private static QualityFoodJournalData load(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag pending = new CompoundTag();
            CompoundTag invalid = new CompoundTag();
            CompoundTag malformed = new CompoundTag();
            if (tag.contains(JOURNAL_MALFORMED_KEY, Tag.TAG_COMPOUND)) {
                malformed.merge(tag.getCompound(JOURNAL_MALFORMED_KEY));
            } else if (tag.contains(JOURNAL_MALFORMED_KEY)) {
                preserveMalformed(malformed, "RawMalformedRoot", tag.get(JOURNAL_MALFORMED_KEY));
            }
            copyJournalSection(tag, JOURNAL_PENDING_KEY, pending, malformed);
            copyJournalSection(tag, JOURNAL_INVALID_KEY, invalid, malformed);
            return new QualityFoodJournalData(
                    pending,
                    invalid,
                    malformed,
                    tag.getBoolean(LEGACY_MIGRATION_COMPLETE_KEY)
            );
        }

        private static void preserveMalformed(CompoundTag malformed, String key, Tag value) {
            String uniqueKey = key;
            int suffix = 1;
            while (malformed.contains(uniqueKey)) {
                uniqueKey = key + suffix++;
            }
            malformed.put(uniqueKey, value.copy());
        }

        private static void copyJournalSection(
                CompoundTag source,
                String key,
                CompoundTag destination,
                CompoundTag malformed
        ) {
            if (!source.contains(key)) {
                return;
            }
            Tag raw = source.get(key);
            if (raw instanceof CompoundTag section) {
                destination.merge(section);
            } else if (raw != null) {
                preserveMalformed(malformed, "Raw" + key, raw);
            }
        }

        private QualityFoodJournalData() {
        }

        private QualityFoodJournalData(
                CompoundTag pending,
                CompoundTag invalid,
                CompoundTag malformed,
                boolean legacyMigrationComplete
        ) {
            this.pending.merge(pending);
            this.invalid.merge(invalid);
            this.malformed.merge(malformed);
            this.legacyMigrationComplete = legacyMigrationComplete;
        }

        private boolean isLegacyMigrationComplete() {
            return legacyMigrationComplete;
        }

        private void markLegacyMigrationComplete() {
            if (!legacyMigrationComplete) {
                legacyMigrationComplete = true;
                setDirty();
            }
        }

        private void mergeMalformed(Tag value) {
            if (value instanceof CompoundTag compound) {
                malformed.merge(compound);
            } else {
                preserveMalformed(malformed, "RawMalformed", value);
            }
            setDirty();
        }

        private boolean hasPending(String playerKey) {
            return pending.contains(playerKey);
        }

        private Tag getPending(String playerKey) {
            return hasPending(playerKey) ? pending.get(playerKey).copy() : null;
        }

        private void putPending(String playerKey, CompoundTag value) {
            putPendingTag(playerKey, value);
        }

        private void putPendingTag(String playerKey, Tag value) {
            pending.put(playerKey, value.copy());
            setDirty();
        }

        private void removePending(String playerKey) {
            pending.remove(playerKey);
            setDirty();
        }

        private boolean hasInvalid(String playerKey) {
            return invalid.contains(playerKey);
        }

        private Tag getInvalid(String playerKey) {
            return hasInvalid(playerKey) ? invalid.get(playerKey).copy() : null;
        }

        private void putInvalid(String playerKey, Tag value) {
            invalid.put(playerKey, value.copy());
            setDirty();
        }

        private void removeInvalid(String playerKey) {
            invalid.remove(playerKey);
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.put(JOURNAL_PENDING_KEY, pending.copy());
            tag.put(JOURNAL_INVALID_KEY, invalid.copy());
            tag.put(JOURNAL_MALFORMED_KEY, malformed.copy());
            tag.putBoolean(LEGACY_MIGRATION_COMPLETE_KEY, legacyMigrationComplete);
            return tag;
        }
    }

    private static long valueFor(int level, int count) {
        if (level <= 0 || level > MAX_QUALITY_LEVEL || count <= 0) {
            throw new IllegalArgumentException("Unsupported Quality Food value: level=" + level + ", count=" + count);
        }

        long unitValue = 1L << (level - 1);
        return Math.multiplyExact(unitValue, (long) count);
    }
}
