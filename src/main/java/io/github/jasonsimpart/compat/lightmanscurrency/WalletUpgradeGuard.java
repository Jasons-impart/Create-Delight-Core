package io.github.jasonsimpart.compat.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.ItemRequirement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

/** The legacy barter ignores wallet data; reject wallets whose contents/upgrades would be lost. */
public final class WalletUpgradeGuard {
    private WalletUpgradeGuard() {}

    public static void register() { NeoForge.EVENT_BUS.addListener(WalletUpgradeGuard::beforeTrade); }

    public static void beforeTrade(TradeEvent.PreTradeEvent event) {
        if (event.getTrader() == null || !"utility_trader".equals(event.getTrader().getPersistentID())
                || !(event.getTrade() instanceof ItemTradeData trade) || !trade.isBarter()
                || !(trade.getSellItem(0).getItem() instanceof WalletItem)
                || !(trade.getBarterItem(1).getItem() instanceof WalletItem)) return;
        var selected = event.getContext().getCollectableItems(new ItemRequirement[]{
                trade.getItemRequirement(2), trade.getItemRequirement(3)});
        if (selected == null) return;
        for (var stack : selected) {
            if (stack.getItem() instanceof WalletItem && !isSafeInput(stack)) {
                event.addDenial(Component.translatable("message.createdelightcore.wallet_upgrade_requires_empty"));
                event.setCanceled(true);
                return;
            }
        }
    }

    public static boolean isSafeInput(ItemStack stack) {
        var wallet = stack.getOrDefault(ModDataComponents.WALLET_DATA.get(), WalletData.EMPTY);
        return wallet.items().stream().allMatch(ItemStack::isEmpty) && wallet.bonusSlots() == 0
                && !stack.isEnchanted() && !stack.has(DataComponents.CUSTOM_NAME);
    }
}
