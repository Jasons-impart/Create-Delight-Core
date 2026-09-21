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
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodJournal.*;
import static io.github.jasonsimpart.compat.qualityfood.QualityFoodPendingStore.*;

    final class QualityFoodJournalData extends SavedData {
    private final CompoundTag pending = new CompoundTag();
    private final CompoundTag invalid = new CompoundTag();
    private final CompoundTag malformed = new CompoundTag();
    private boolean legacyMigrationComplete;

    static QualityFoodJournalData load(CompoundTag tag, HolderLookup.Provider registries) {
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

    QualityFoodJournalData() {
    }

    QualityFoodJournalData(
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

    boolean isLegacyMigrationComplete() {
        return legacyMigrationComplete;
    }

    void markLegacyMigrationComplete() {
        if (!legacyMigrationComplete) {
            legacyMigrationComplete = true;
            setDirty();
        }
    }

    void mergeMalformed(Tag value) {
        if (value instanceof CompoundTag compound) {
            malformed.merge(compound);
        } else {
            preserveMalformed(malformed, "RawMalformed", value);
        }
        setDirty();
    }

    boolean hasPending(String playerKey) {
        return pending.contains(playerKey);
    }

    Tag getPending(String playerKey) {
        return hasPending(playerKey) ? pending.get(playerKey).copy() : null;
    }

    void putPending(String playerKey, CompoundTag value) {
        putPendingTag(playerKey, value);
    }

    void putPendingTag(String playerKey, Tag value) {
        pending.put(playerKey, value.copy());
        setDirty();
    }

    void removePending(String playerKey) {
        pending.remove(playerKey);
        setDirty();
    }

    boolean hasInvalid(String playerKey) {
        return invalid.contains(playerKey);
    }

    Tag getInvalid(String playerKey) {
        return hasInvalid(playerKey) ? invalid.get(playerKey).copy() : null;
    }

    void putInvalid(String playerKey, Tag value) {
        invalid.put(playerKey, value.copy());
        setDirty();
    }

    void removeInvalid(String playerKey) {
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
