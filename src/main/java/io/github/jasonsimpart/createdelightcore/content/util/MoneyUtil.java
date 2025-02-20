package io.github.jasonsimpart.createdelightcore.content.util;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class MoneyUtil {
    public static Item getBaseCoinFromChain() {
        return CoinAPI.API.ChainData(CDConfig.moneyChain).getAllEntries(false).get(0).getCoin();
    }
    public static boolean playerCanAfford(Player player, MoneyValue coinValue) {
        return MoneyAPI.API.GetPlayersMoneyHandler(player).getStoredMoney().containsValue(coinValue);
    }

    public static MoneyValue baseCoinNumberToCoinValue(int number) {
        return  CoinValue.fromNumber(CDConfig.moneyChain, number);
    }

    public static int getTeleportCost(Player player, IWaystone waystone) {
        // 获取玩家当前的维度与传送点维度是否相同
        boolean isSameDimension = waystone.getDimension().equals(player.level().dimension());

        // 计算玩家与传送点的坐标距离
        BlockPos blockPos = waystone.getPos();
        Vec3 waystonePosition = new Vec3(blockPos.getX(), player.getY(), blockPos.getZ());
        double distance = player.position().distanceTo(waystonePosition);

        // 获取XP相关配置
        double blocksPerXpLevel = WaystonesConfig.getActive().xpCost.blocksPerXpLevel;
        double maximumBaseXpCost = WaystonesConfig.getActive().xpCost.maximumBaseXpCost;
        double dimensionalWarpXpCost = WaystonesConfig.getActive().xpCost.dimensionalWarpXpCost;
        double teleportCost = CDConfig.teleportCost;

        if (isSameDimension) {
            // 计算在相同维度内的传送费用
            double xpCost = distance / blocksPerXpLevel * teleportCost;
            double maxXpCost = Math.pow(maximumBaseXpCost, 2) * teleportCost;
            return (int) Math.min(xpCost, maxXpCost);
        } else {
            // 计算跨维度传送费用
            return (int) (teleportCost * Math.pow(dimensionalWarpXpCost, 2));
        }
    }
}
