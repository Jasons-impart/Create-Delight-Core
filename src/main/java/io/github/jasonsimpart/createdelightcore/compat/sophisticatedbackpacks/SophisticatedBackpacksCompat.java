package io.github.jasonsimpart.createdelightcore.compat.sophisticatedbackpacks;

import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.refill.RefillUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Bridges configuration-module charge to Sophisticated Backpacks' refill upgrades. */
public final class SophisticatedBackpacksCompat {
    private static final long ACTIVE_UPGRADE_GRACE_TICKS = 6;
    private static final Map<UUID, List<ActiveRefillUpgrade>> ACTIVE_REFILL_UPGRADES = new HashMap<>();

    private SophisticatedBackpacksCompat() {
    }

    /** Called only from Sophisticated Backpacks' own active refill-upgrade tick. */
    public static void onRefillUpgradeTick(Player player, RefillUpgradeWrapper upgrade, IStorageWrapper storage) {
        long expiresAt = player.level().getGameTime() + ACTIVE_UPGRADE_GRACE_TICKS;
        List<ActiveRefillUpgrade> activeUpgrades = ACTIVE_REFILL_UPGRADES.computeIfAbsent(player.getUUID(),
                ignored -> new ArrayList<>());
        activeUpgrades.removeIf(active -> active.isExpired(player.level().getGameTime()) || active.upgrade() == upgrade);
        activeUpgrades.add(new ActiveRefillUpgrade(upgrade, storage, expiresAt));
    }

    /**
     * Finds an enabled native refill upgrade which has recently ticked for this player, then prepares a refill plan.
     * No items are extracted until {@link RefillReservation#commit(ItemStack)} is called after placement succeeds.
     */
    public static Optional<RefillReservation> reserveRefillForUse(Player player, ItemStack moduleStack,
                                                                    InteractionHand hand, int chargeCost,
                                                                    IItemHandler playerInventory) {
        long gameTime = player.level().getGameTime();
        List<ActiveRefillUpgrade> activeUpgrades = ACTIVE_REFILL_UPGRADES.get(player.getUUID());
        if (activeUpgrades == null) {
            return Optional.empty();
        }
        activeUpgrades.removeIf(active -> active.isExpired(gameTime));
        if (activeUpgrades.isEmpty()) {
            ACTIVE_REFILL_UPGRADES.remove(player.getUUID());
            return Optional.empty();
        }

        ModuleSlot module = new ModuleSlot(moduleStack,
                hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1,
                hand == InteractionHand.OFF_HAND);
        for (ActiveRefillUpgrade active : activeUpgrades) {
            if (!canRefill(active.upgrade(), player, module)) {
                continue;
            }
            IItemHandler inventory = active.storage().getInventoryForUpgradeProcessing();
            Optional<ConfigurationModuleItem.RefillPlan> plan =
                    ConfigurationModuleItem.planRefillToCapacityWithoutOverflow(player.level(),
                            List.of(playerInventory, inventory), moduleStack);
            if (plan.isPresent() && plan.get().chargeAdded() >= chargeCost
                    - ConfigurationModuleManager.getCharge(moduleStack)) {
                return Optional.of(new RefillReservation(plan.get()));
            }
        }
        return Optional.empty();
    }

    private static boolean canRefill(RefillUpgradeWrapper upgrade, Player player, ModuleSlot module) {
        if (!upgrade.isEnabled() || !upgrade.getFilterLogic().matchesFilter(module.stack())) {
            return false;
        }
        if (!upgrade.allowsTargetSlotSelection()) {
            return true;
        }
        return upgrade.getTargetSlots().isEmpty() || upgrade.getTargetSlots().values().stream()
                .anyMatch(target -> matchesTargetSlot(target, player, module));
    }

    private static boolean matchesTargetSlot(RefillUpgradeWrapper.TargetSlot target, Player player, ModuleSlot module) {
        if (target == RefillUpgradeWrapper.TargetSlot.ANY) {
            return true;
        }
        if (target == RefillUpgradeWrapper.TargetSlot.MAIN_HAND) {
            return !module.offhand() && module.inventorySlot() == player.getInventory().selected;
        }
        if (target == RefillUpgradeWrapper.TargetSlot.OFF_HAND) {
            return module.offhand();
        }
        int hotbarSlot = target.ordinal() - RefillUpgradeWrapper.TargetSlot.TOOLBAR_1.ordinal();
        return !module.offhand() && hotbarSlot >= 0 && hotbarSlot < 9 && module.inventorySlot() == hotbarSlot;
    }

    private record ModuleSlot(ItemStack stack, int inventorySlot, boolean offhand) {
    }

    private record ActiveRefillUpgrade(RefillUpgradeWrapper upgrade, IStorageWrapper storage, long expiresAt) {
        private boolean isExpired(long gameTime) {
            return gameTime > expiresAt;
        }
    }

    public record RefillReservation(ConfigurationModuleItem.RefillPlan plan) {
        public int commit(ItemStack moduleStack) {
            return ConfigurationModuleItem.applyRefillPlan(moduleStack, plan);
        }
    }
}
