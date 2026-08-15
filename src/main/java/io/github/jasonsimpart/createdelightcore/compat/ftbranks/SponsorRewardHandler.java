package io.github.jasonsimpart.createdelightcore.compat.ftbranks;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Owns the sponsor list lifecycle and grants the one-time sponsor reward. */
public final class SponsorRewardHandler {
    private static final String MEDAL_GIVEN_TAG = "createdelightcore_sponsor_medal_given";
    private static final List<BiConsumer<MinecraftServer, SponsorTitleStore>> REFRESH_LISTENERS = new ArrayList<>();
    private static SponsorTitleStore sponsorTitleStore;

    private SponsorRewardHandler() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(SponsorRewardHandler.class);
    }

    static void registerRefreshListener(BiConsumer<MinecraftServer, SponsorTitleStore> listener) {
        synchronized (REFRESH_LISTENERS) {
            REFRESH_LISTENERS.add(listener);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        synchronized (SponsorRewardHandler.class) {
            if (sponsorTitleStore != null) {
                sponsorTitleStore.close();
                sponsorTitleStore = null;
            }
            if (!CDConfig.enableSponsorTitles) {
                return;
            }
            sponsorTitleStore = new SponsorTitleStore(event.getServer());
            sponsorTitleStore.refreshAsync();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        synchronized (SponsorRewardHandler.class) {
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

        tryGiveSponsorMedal(player, currentStore(player.getServer()));
    }

    static SponsorTitleStore currentStore(MinecraftServer server) {
        synchronized (SponsorRewardHandler.class) {
            if (!CDConfig.enableSponsorTitles) {
                return null;
            }
            if (sponsorTitleStore == null) {
                sponsorTitleStore = new SponsorTitleStore(server);
                sponsorTitleStore.refreshAsync();
            }
            return sponsorTitleStore;
        }
    }

    static void onStoreRefreshed(MinecraftServer server, SponsorTitleStore store) {
        if (!CDConfig.enableSponsorTitles) {
            return;
        }
        synchronized (SponsorRewardHandler.class) {
            if (sponsorTitleStore != store) {
                return;
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tryGiveSponsorMedal(player, store);
        }

        synchronized (REFRESH_LISTENERS) {
            for (BiConsumer<MinecraftServer, SponsorTitleStore> listener : REFRESH_LISTENERS) {
                listener.accept(server, store);
            }
        }
    }

    private static void tryGiveSponsorMedal(ServerPlayer player, SponsorTitleStore store) {
        if (store == null || !CDConfig.enableSponsorTitles
                || player.getPersistentData().getBoolean(MEDAL_GIVEN_TAG)
                || store.findTitle(player.getGameProfile().getName()).isEmpty()) {
            return;
        }

        ItemStack medal = new ItemStack(CDItems.SPONSOR_MEDAL.get());
        if (!player.addItem(medal)) {
            player.drop(medal, false);
        }
        player.getPersistentData().putBoolean(MEDAL_GIVEN_TAG, true);
        player.inventoryMenu.broadcastChanges();
    }
}
