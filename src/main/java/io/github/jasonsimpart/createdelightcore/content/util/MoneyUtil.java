package io.github.jasonsimpart.createdelightcore.content.util;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Objects;

public class MoneyUtil {
    public static Item getBaseCoinFromChain() {
        return CoinAPI.getApi().ChainData(CDConfig.moneyChain).getAllEntries(false).get(0).getCoin();
    }
    public static boolean playerCanAfford(Player player, MoneyValue coinValue) {
        return MoneyAPI.getApi().GetPlayersMoneyHandler(player).getStoredMoney().containsValue(coinValue);
    }

    public static boolean extractPlayerMoney(Player player, MoneyValue coinValue) {
        if (!playerCanAfford(player, coinValue)) {
            return false;
        }
        MoneyValue remainder = MoneyAPI.getApi().GetPlayersMoneyHandler(player).extractMoney(coinValue, false);
        return remainder != null && remainder.isEmpty();
    }

    public static void insertPlayerMoney(Player player, MoneyValue coinValue) {
        MoneyAPI.getApi().GetPlayersMoneyHandler(player).insertMoney(coinValue, false);
    }

    public static MoneyValue baseCoinNumberToCoinValue(int number) {
        return  CoinValue.fromNumber(CDConfig.moneyChain, number);
    }
}
