package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createcentralkitchen.content.contraptions.deployer.CuttingBoardDeployingRecipe;

/**
 * 砧板部署配方（{@link CuttingBoardDeployingRecipe}）加工后完整保留手上的切割工具。
 *
 * <p>实现方式：在 {@code activate} 入口处快照机械手主手物品，在出口处原样写回。
 * 这样无需 {@code @Redirect} 拦截 {@code hurtAndBreak} / {@code shrink} /
 * {@code getCraftingRemainingItem} 调用点——这些调用点同时被 ApoKinetics
 * 的 BeltDeployerCallbacksMixin 以 require=1 的 Redirect 占用，同一指令被两个
 * Redirect 竞争会导致后应用的一方注入失败并使游戏崩溃。</p>
 */
@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public class BeltDeployerCallbacksMixin {

    @Inject(method = "activate", at = @At("HEAD"))
    private static void create_Delight_Core$captureCuttingTool(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe,
            CallbackInfo ci,
            @Share("cdc$keptTool") LocalRef<ItemStack> keptTool) {
        if (!(recipe instanceof CuttingBoardDeployingRecipe))
            return;
        DeployerFakePlayer player = deployer.getPlayer();
        if (player == null)
            return;
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty())
            keptTool.set(held.copy());
    }

    @Inject(method = "activate", at = @At("RETURN"))
    private static void create_Delight_Core$restoreCuttingTool(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe,
            CallbackInfo ci,
            @Share("cdc$keptTool") LocalRef<ItemStack> keptTool) {
        ItemStack held = keptTool.get();
        if (held == null)
            return;
        DeployerFakePlayer player = deployer.getPlayer();
        if (player != null)
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
    }
}
