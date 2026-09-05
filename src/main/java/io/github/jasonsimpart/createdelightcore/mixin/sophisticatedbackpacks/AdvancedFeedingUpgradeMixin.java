package io.github.jasonsimpart.createdelightcore.mixin.sophisticatedbackpacks;

import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FeedingUpgradeWrapper.class, remap = false)
public abstract class AdvancedFeedingUpgradeMixin {
    @Shadow
    private boolean tryFeedingStack(Level level, int hungerToFeed, Player player, Integer slot, ItemStack stack,
                                    ITrackedContentsItemHandler storage) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean isEdible(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        throw new AssertionError();
    }

    @Shadow
    private boolean isHungryEnoughForFood(int hungerToFeed, ItemStack stack, Player player) {
        throw new AssertionError();
    }

    @Shadow
    public abstract FilterLogic getFilterLogic();

    @Shadow
    public abstract boolean shouldFeedImmediatelyWhenHurt();

    @Inject(method = "tryFeedingFoodFromStorage", at = @At("HEAD"), cancellable = true)
    private void createdelightcore$feedMostDiverseFood(Level level, int hungerToFeed, Player player,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (!createdelightcore$isAdvancedFeedingUpgrade()) {
            return;
        }

        FoodList foodList = FoodList.get(player);
        if (foodList == null) {
            return;
        }

        ITrackedContentsItemHandler storage =
                ((UpgradeWrapperBaseAccessor) (Object) this).createdelightcore$getStorageWrapper()
                        .getInventoryForUpgradeProcessing();
        boolean hurtAndNeedsFood = shouldFeedImmediatelyWhenHurt()
                && hungerToFeed > 0
                && player.getHealth() < player.getMaxHealth() - 0.1F;
        int bestSlot = -1;
        ItemStack bestStack = ItemStack.EMPTY;
        double bestDiversity = Double.NEGATIVE_INFINITY;

        for (int slot = 0; slot < storage.getSlots(); slot++) {
            ItemStack stack = storage.getStackInSlot(slot);
            if (!createdelightcore$isCandidate(stack, player, hungerToFeed, hurtAndNeedsFood)) {
                continue;
            }

            double diversity = foodList.simulateFoodAdd(stack.getItem());
            if (bestSlot == -1 || diversity > bestDiversity) {
                bestSlot = slot;
                bestStack = stack;
                bestDiversity = diversity;
            }
        }

        if (bestSlot != -1 && tryFeedingStack(level, hungerToFeed, player, bestSlot, bestStack, storage)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean createdelightcore$isAdvancedFeedingUpgrade() {
        ItemStack upgrade = ((UpgradeWrapperBaseAccessor) (Object) this).createdelightcore$getUpgrade();
        return upgrade.getItem() == ModItems.ADVANCED_FEEDING_UPGRADE.get();
    }

    @Unique
    private boolean createdelightcore$isCandidate(ItemStack stack, Player player, int hungerToFeed,
                                                  boolean hurtAndNeedsFood) {
        return !stack.isEmpty()
                && isEdible(stack, player)
                && getFilterLogic().matchesFilter(stack)
                && (isHungryEnoughForFood(hungerToFeed, stack, player) || hurtAndNeedsFood);
    }
}
