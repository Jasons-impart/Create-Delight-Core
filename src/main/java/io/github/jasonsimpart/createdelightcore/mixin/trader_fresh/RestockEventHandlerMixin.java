package io.github.jasonsimpart.createdelightcore.mixin.trader_fresh;

import com.llamalad7.mixinextras.sugar.Local;
import dev.xkmc.traderefresh.common.RestockEventHandler;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RestockEventHandler.class)
public abstract class RestockEventHandlerMixin {

    @Unique
    private static final int create_Delight_Core$COOLDOWN = 24000;
    @Inject(method = "onMobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;resetNumberOfRestocks()V"), remap = false, cancellable = true, require = 1)
    private static void onMobInteractMixin(PlayerInteractEvent.EntityInteract event, CallbackInfo ci, @Local Villager villager) {
        var player = event.getEntity();
        var villagerData = villager.getPersistentData();

        // 获取冷却时间和当前时间
        int lastRefreshTime = villagerData.getInt("refresh_cooldown");
        int currentTick = player.tickCount;

        // 如果超过冷却时间，刷新为1
        if (currentTick - lastRefreshTime >= create_Delight_Core$COOLDOWN) {
            villagerData.putInt("refresh_cost", 1);
        }

        // 获取刷新成本并初始化为1（如果尚未设置）
        int refreshCost = villagerData.getInt("refresh_cost");
        if (refreshCost == 0) {
            refreshCost = 1;
            villagerData.putInt("refresh_cost", refreshCost);
        }

        // 如果物品数量不足，取消操作
        int availableCount = event.getItemStack().getCount();
        if (availableCount < refreshCost - 1) {
            ci.cancel();
            return;
        }

        // 执行刷新逻辑
        // 减少物品数量
        event.getItemStack().shrink(refreshCost - 1);

        // 更新冷却时间
        villagerData.putInt("refresh_cooldown", currentTick);
        // 更新刷新成本，确保不会超过物品堆叠最大值
        int newCost = Math.min(event.getItemStack().getMaxStackSize(), refreshCost * 2);
        villagerData.putInt("refresh_cost", newCost);
    }
}
