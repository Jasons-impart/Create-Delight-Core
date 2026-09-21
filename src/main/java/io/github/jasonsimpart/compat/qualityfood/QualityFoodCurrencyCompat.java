package io.github.jasonsimpart.compat.qualityfood;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPending.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodJournal.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPendingStore.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodTransactions.*;

/** Quality Food × Lightman's Currency entry point: event hooks, admin commands and the public absorber API. */
public final class QualityFoodCurrencyCompat {
    public enum AbsorptionResult {
        SUCCESS,
        ROLLED_BACK_FAILURE,
        PENDING_RECOVERY
    }

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
        forgetPresence(event.getEntity().getUUID());
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
        QualityFoodPending.PendingOperation operation = readPendingIfRecoveryMayExist(player);
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
        QualityFoodPending.PendingOperation pending = readPendingIfRecoveryMayExist(player);
        if (pending != null && pending.escrowed()) {
            enforceEscrowedInventory(player, pending);
        }
        if (pending != null && player.tickCount % 20 == 0) {
            resolvePending(player);
        }
    }

    public static AbsorptionResult absorbInventoryQuality(Player player) {
        return QualityFoodTransactions.absorbInventoryQuality(player);
    }

    public static boolean hasPendingRecovery(Player player) {
        return hasPending(player);
    }
}
