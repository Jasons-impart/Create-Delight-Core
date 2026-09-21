package io.github.jasonsimpart.mixin.traderefresh;

import com.llamalad7.mixinextras.sugar.Local;
import dev.xkmc.traderefresh.common.RestockEventHandler;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RestockEventHandler.class, remap = false)
public abstract class RestockEventHandlerMixin {
    @Unique
    private static final int CREATEDELIGHTCORE$COOLDOWN = 24000;

    @Inject(method = "onMobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;resetNumberOfRestocks()V"), cancellable = true)
    private static void createdelightcore$escalatingRestockCost(PlayerInteractEvent.EntityInteract event, CallbackInfo ci, @Local Villager villager) {
        var player = event.getEntity();
        var villagerData = villager.getPersistentData();
        int currentTick = player.tickCount;

        if (currentTick - villagerData.getInt("refresh_cooldown") >= CREATEDELIGHTCORE$COOLDOWN) {
            villagerData.putInt("refresh_cost", 1);
        }

        int refreshCost = villagerData.getInt("refresh_cost");
        if (refreshCost == 0) {
            refreshCost = 1;
            villagerData.putInt("refresh_cost", refreshCost);
        }

        if (event.getItemStack().getCount() < refreshCost - 1) {
            ci.cancel();
            return;
        }

        event.getItemStack().shrink(refreshCost - 1);
        villagerData.putInt("refresh_cooldown", currentTick);
        villagerData.putInt("refresh_cost", Math.min(event.getItemStack().getMaxStackSize(), refreshCost * 2));
    }
}
