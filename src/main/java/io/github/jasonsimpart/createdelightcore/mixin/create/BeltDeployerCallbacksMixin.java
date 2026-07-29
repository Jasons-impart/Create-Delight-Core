package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import plus.dragons.createcentralkitchen.content.contraptions.deployer.CuttingBoardDeployingRecipe;

import java.util.function.Consumer;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public class BeltDeployerCallbacksMixin {
    @Redirect(
            method = "activate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
                    remap = true
            )
    )
    private static void create_Delight_Core$keepCuttingToolDurability(
            ItemStack stack,
            int amount,
            LivingEntity entity,
            Consumer<LivingEntity> onBreak,
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe) {
        if (!(recipe instanceof CuttingBoardDeployingRecipe)) {
            stack.hurtAndBreak(amount, entity, onBreak);
        }
    }

    @Redirect(
            method = "activate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
                    ordinal = 1,
                    remap = true
            )
    )
    private static void create_Delight_Core$keepCuttingToolStack(
            ItemStack stack,
            int amount,
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe) {
        if (!(recipe instanceof CuttingBoardDeployingRecipe)) {
            stack.shrink(amount);
        }
    }

    @Redirect(
            method = "activate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getCraftingRemainingItem()Lnet/minecraft/world/item/ItemStack;",
                    remap = false
            )
    )
    private static ItemStack create_Delight_Core$skipCuttingToolRemainder(
            ItemStack stack,
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            Recipe<?> recipe) {
        if (recipe instanceof CuttingBoardDeployingRecipe) {
            return ItemStack.EMPTY;
        }
        return stack.getCraftingRemainingItem();
    }
}
