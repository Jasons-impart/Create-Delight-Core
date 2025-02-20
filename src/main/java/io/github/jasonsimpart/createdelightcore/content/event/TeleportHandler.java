package io.github.jasonsimpart.createdelightcore.content.event;

import com.mojang.logging.LogUtils;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.WaystoneTeleportEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * 监听传送事件
 *
 * @author PrefersMin
 * @version 1.1
 */
@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeleportHandler {

    /**
     * 监听方法
     *
     * @param event 传送事件
     */
    @SubscribeEvent
    public void onWayStoneTeleport(WaystoneTeleportEvent.Pre event) {

        if (CDConfig.useMoneyTeleport) {

            Entity teleportedEntity = event.getContext().getEntity();
            if (teleportedEntity instanceof Player player) {
                // 判断是否处于创造模式
                if (player.getAbilities().instabuild) {
                    return;
                }
                // 计算距离
                IWaystone waystone = event.getContext().getTargetWaystone();

                // 计算传送费用并判断余额是否足以支付传送费用
                int teleportCostNumber = MoneyUtil.getTeleportCost(player, waystone);
                MoneyValue moneyCost = MoneyUtil.baseCoinNumberToCoinValue(teleportCostNumber);
                boolean canAfford = MoneyUtil.playerCanAfford(player, moneyCost);
                // 执行消费或取消传送
                if (canAfford) {
                    MoneyAPI.API.GetPlayersMoneyHandler(player).extractMoney(moneyCost, false);
                    event.setXpCost(0);
                } else {
                    event.setCanceled(true);
                }
            }
        }
    }

}