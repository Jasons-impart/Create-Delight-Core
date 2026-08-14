package io.github.jasonsimpart.createdelightcore.compat.ftbranks;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;
import java.util.Optional;

/** Integrates the pack's sponsor title list with FTB Ranks when that mod is installed. */
public final class FTBRanksCompat {
    private static final String NAME_FORMAT_NODE = "ftbranks.name_format";
    private static SponsorTitleStore sponsorTitleStore;

    private FTBRanksCompat() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(FTBRanksCompat.class);
        CreateDelightCore.LOGGER.info("FTB Ranks sponsor title compatibility enabled");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!CDConfig.enableSponsorTitles) {
            return;
        }
        synchronized (FTBRanksCompat.class) {
            if (sponsorTitleStore != null) {
                sponsorTitleStore.close();
            }
            sponsorTitleStore = new SponsorTitleStore(event.getServer());
            sponsorTitleStore.refreshAsync();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        synchronized (FTBRanksCompat.class) {
            if (sponsorTitleStore != null) {
                sponsorTitleStore.close();
                sponsorTitleStore = null;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!CDConfig.enableSponsorTitles || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        applyTitle(player, currentStore(player.getServer()));
    }

    static void applyTitlesToOnlinePlayers(MinecraftServer server, SponsorTitleStore store) {
        synchronized (FTBRanksCompat.class) {
            if (sponsorTitleStore != store) {
                return;
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyTitle(player, store);
        }
    }

    private static SponsorTitleStore currentStore(MinecraftServer server) {
        synchronized (FTBRanksCompat.class) {
            if (sponsorTitleStore == null) {
                sponsorTitleStore = new SponsorTitleStore(server);
                sponsorTitleStore.refreshAsync();
            }
            return sponsorTitleStore;
        }
    }

    private static void applyTitle(ServerPlayer player, SponsorTitleStore store) {
        Optional<String> title = store.findTitle(player.getGameProfile().getName());
        if (title.isEmpty()) {
            return;
        }

        try {
            FTBRanksAPI api = FTBRanksAPI.getInstance();
            if (api == null) {
                CreateDelightCore.LOGGER.debug("Cannot apply sponsor title for {}: FTB Ranks API is not ready", player.getGameProfile().getName());
                return;
            }

            RankManager manager = FTBRanksAPI.manager();
            Rank rank = findOrCreateRank(manager, player.getGameProfile().getName());
            String formattedName = "[" + title.get() + "]{name}";
            PermissionValue permission = api.parsePermissionValue(formattedName);
            rank.setPermission(NAME_FORMAT_NODE, permission);
            rank.add(player.getGameProfile());
        } catch (RuntimeException exception) {
            CreateDelightCore.LOGGER.error("Failed to apply sponsor title for {}", player.getGameProfile().getName(), exception);
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
