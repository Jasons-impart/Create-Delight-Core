package io.github.jasonsimpart.compat.waystones;

import io.github.jasonsimpart.Config;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class WaystoneMoneyRequirement implements WarpRequirement {
    private final int costUnits;

    public WaystoneMoneyRequirement(int costUnits) {
        this.costUnits = Math.max(0, costUnits);
    }

    @Override
    public boolean canAfford(Player player) {
        return isEmpty()
                || player.getAbilities().instabuild
                || extractFromPaymentSources(player, cost(), true).isEmpty();
    }

    @Override
    public void consume(Player player) {
        if (!isEmpty() && !player.getAbilities().instabuild) {
            MoneyValue price = cost();
            if (!extractFromPaymentSources(player, price, true).isEmpty()) {
                throw new IllegalStateException("Cannot afford Waystones money requirement");
            }

            MoneyValue remaining = extractFromPaymentSources(player, price, false);
            if (!remaining.isEmpty()) {
                MoneyValue extracted = price.subtractValue(remaining);
                if (!extracted.isEmpty()) {
                    insertIntoPaymentSources(player, extracted);
                }
                throw new IllegalStateException("Failed to consume full Waystones money requirement");
            }
        }
    }

    @Override
    public void rollback(Player player) {
        if (!isEmpty() && !player.getAbilities().instabuild) {
            insertIntoPaymentSources(player, cost());
        }
    }

    @Override
    public void appendHoverText(Player player, List<Component> tooltip) {
        if (!isEmpty()) {
            ChatFormatting color = canAfford(player) ? ChatFormatting.GREEN : ChatFormatting.RED;
            tooltip.add(Component.translatable("gui.createdelightcore.waystones_money_requirement", cost().getText()).withStyle(color));
        }
    }

    @Override
    public boolean isEmpty() {
        return costUnits <= 0;
    }

    public int costUnits() {
        return costUnits;
    }

    public MoneyValue cost() {
        long value = (long) costUnits * Config.WAYSTONES_TELEPORT_COST_PER_LEVEL.get();
        return CoinValue.fromNumber(Config.WAYSTONES_MONEY_CHAIN.get(), value);
    }

    private static MoneyValue extractFromPaymentSources(Player player, MoneyValue cost, boolean simulate) {
        MoneyValue remaining = inventoryMoneyHandler(player).extractMoney(cost, simulate);
        if (remaining.isEmpty()) {
            return remaining;
        }

        for (InventoryWallet wallet : inventoryWallets(player)) {
            remaining = wallet.extractMoney(remaining, simulate);
            if (remaining.isEmpty()) {
                return remaining;
            }
        }

        return MoneyAPI.getApi().GetPlayersMoneyHandler(player).extractMoney(remaining, simulate);
    }

    private static void insertIntoPaymentSources(Player player, MoneyValue cost) {
        MoneyValue remaining = inventoryMoneyHandler(player).insertMoney(cost, false);
        for (InventoryWallet wallet : inventoryWallets(player)) {
            if (remaining.isEmpty()) {
                return;
            }
            remaining = wallet.insertMoney(remaining);
        }
        if (!remaining.isEmpty()) {
            MoneyAPI.getApi().GetPlayersMoneyHandler(player).insertMoney(remaining, false);
        }
    }

    private static IMoneyHandler inventoryMoneyHandler(Player player) {
        return MoneyAPI.getApi().GetContainersMoneyHandler(player.getInventory(), player);
    }

    private static List<InventoryWallet> inventoryWallets(Player player) {
        List<InventoryWallet> wallets = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (WalletItem.isWallet(stack)) {
                wallets.add(new InventoryWallet(stack, player));
            }
        }
        return wallets;
    }

    private record InventoryWallet(ItemStack stack, Player player) {
        private MoneyValue insertMoney(MoneyValue cost) {
            WalletDataWrapper wallet = WalletItem.getDataWrapper(stack);
            Container contents = wallet.getContents();
            IMoneyHandler handler = walletMoneyHandler(contents);
            MoneyValue remaining = handler.insertMoney(cost, false);
            wallet.setContents(contents, player);
            return remaining;
        }

        private MoneyValue extractMoney(MoneyValue cost, boolean simulate) {
            WalletDataWrapper wallet = WalletItem.getDataWrapper(stack);
            Container contents = wallet.getContents();
            IMoneyHandler handler = walletMoneyHandler(contents);
            MoneyValue remaining = handler.extractMoney(cost, simulate);
            if (!simulate) {
                wallet.setContents(contents, player);
            }
            return remaining;
        }

        private IMoneyHandler walletMoneyHandler(Container contents) {
            return MoneyAPI.getApi().GetContainersMoneyHandler(
                    contents,
                    itemStack -> ItemHandlerHelper.giveItemToPlayer(player, itemStack),
                    IClientTracker.entityWrapper(player)
            );
        }
    }
}
