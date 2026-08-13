package io.github.jasonsimpart.createdelightcore.compat.ftbranks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/** Integrates the pack's sponsor title list with FTB Ranks when that mod is installed. */
public final class FTBRanksCompat {
    private static final Gson GSON = new Gson();
    private static final String DONATE_LIST_FILE = "donate_list.json";
    private static final String NAME_FORMAT_NODE = "ftbranks.name_format";

    private FTBRanksCompat() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(FTBRanksCompat.class);
        CreateDelightCore.LOGGER.info("FTB Ranks sponsor title compatibility enabled");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!CDConfig.enableSponsorTitles || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        try {
            Optional<String> title = readTitle(player);
            if (title.isEmpty()) {
                return;
            }

            FTBRanksAPI api = FTBRanksAPI.getInstance();
            if (api == null) {
                CreateDelightCore.LOGGER.warn("Cannot apply sponsor title for {}: FTB Ranks API is not ready", player.getGameProfile().getName());
                return;
            }

            RankManager manager = FTBRanksAPI.manager();
            Rank rank = findOrCreateRank(manager, player.getGameProfile().getName());
            String formattedName = "[" + title.get() + "]{name}";
            PermissionValue permission = api.parsePermissionValue(formattedName);
            rank.setPermission(NAME_FORMAT_NODE, permission);
            rank.add(player.getGameProfile());
        } catch (IOException | JsonParseException | IllegalStateException | IllegalArgumentException exception) {
            CreateDelightCore.LOGGER.error("Failed to apply sponsor title for {}", player.getGameProfile().getName(), exception);
        }
    }

    private static Optional<String> readTitle(ServerPlayer player) throws IOException {
        Path path = player.getServer().getServerDirectory().toPath().resolve(DONATE_LIST_FILE);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject list = GSON.fromJson(reader, JsonObject.class);
            if (list == null) {
                return Optional.empty();
            }

            JsonElement title = list.get(player.getGameProfile().getName());
            if (title == null || title.isJsonNull()) {
                return Optional.empty();
            }
            if (!title.isJsonPrimitive() || !title.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Sponsor title must be a string");
            }

            String value = title.getAsString();
            return value.isBlank() ? Optional.empty() : Optional.of(value);
        }
    }

    private static Rank findOrCreateRank(RankManager manager, String playerName) {
        String normalizedName = normalizeRankId(playerName);
        Optional<Rank> legacyRank = manager.getRank(normalizedName);
        if (legacyRank.isPresent() && legacyRank.get().getName().equalsIgnoreCase(playerName)) {
            return legacyRank.get();
        }

        String rankId = "createdelight_sponsor_" + normalizedName;
        return manager.getRank(rankId)
                .orElseGet(() -> manager.createRank(rankId, "Create Delight Sponsor " + playerName, 1));
    }

    private static String normalizeRankId(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replace("+", "_plus")
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_{2,}", "_");
    }
}
