package io.github.jasonsimpart.compat.lightmanscurrency;

import com.mojang.brigadier.arguments.LongArgumentType;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.PlayerListing;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** New progression can call this API or the permission-2 command without old quest IDs. */
public final class TraderWhitelist {
    private TraderWhitelist() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TraderWhitelist::commands);
    }

    public static int unlock(ServerPlayer player, TraderData trader, Item item) {
        if (trader == null) return 0;
        int changed = 0;
        for (var trade : trader.getTradeData()) {
            if (!(trade instanceof ItemTradeData items)
                    || (!items.getSellItem(0).is(item) && !items.getSellItem(1).is(item))) continue;
            for (var rule : trade.getRules()) {
                // Never turn a blacklist into a whitelist (LC's addToWhitelist clears it).
                if (rule instanceof PlayerListing listing && listing.isWhitelistMode() && listing.addToWhitelist(player)) changed++;
            }
        }
        if (changed > 0) trader.markTradeRulesDirty();
        return changed;
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("createdelightcore")
                .then(Commands.literal("trader").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("trader", LongArgumentType.longArg(0))
                                                .then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                                        .executes(context -> {
                                                            var trader = TraderAPI.getApi().GetTrader(false, LongArgumentType.getLong(context, "trader"));
                                                            if (trader == null) {
                                                                context.getSource().sendFailure(Component.translatable("message.createdelightcore.trader.missing"));
                                                                return 0;
                                                            }
                                                            int count = unlock(EntityArgument.getPlayer(context, "player"), trader,
                                                                    ItemArgument.getItem(context, "item").getItem());
                                                            context.getSource().sendSuccess(() -> Component.translatable("message.createdelightcore.trader.unlocked", count), false);
                                                            return count;
                                                        })))))));
    }
}
