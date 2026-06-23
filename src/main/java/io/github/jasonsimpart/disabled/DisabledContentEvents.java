package io.github.jasonsimpart.disabled;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

public final class DisabledContentEvents {
    private DisabledContentEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DisabledContentEvents::hideCreativeEntries);
        NeoForge.EVENT_BUS.addListener(DisabledContentEvents::blockRightClickItem);
        NeoForge.EVENT_BUS.addListener(DisabledContentEvents::blockRightClickBlock);
        NeoForge.EVENT_BUS.addListener(DisabledContentEvents::blockAttack);
        NeoForge.EVENT_BUS.addListener(DisabledContentEvents::blockEquipment);
        NeoForge.EVENT_BUS.addListener(DisabledContentEvents::blockPlacedBlocks);
    }

    private static void hideCreativeEntries(BuildCreativeModeTabContentsEvent event) {
        for (ItemStack stack : List.copyOf(event.getParentEntries())) {
            if (DisabledContentManager.shouldHideFromCreative(stack)) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
        }

        for (ItemStack stack : List.copyOf(event.getSearchEntries())) {
            if (DisabledContentManager.shouldHideFromCreative(stack)) {
                event.remove(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        }
    }

    private static void blockRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (DisabledContentManager.shouldBlockUse(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static void blockRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (DisabledContentManager.shouldBlockUse(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static void blockAttack(AttackEntityEvent event) {
        if (DisabledContentManager.shouldBlockAttack(event.getEntity().getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    private static void blockEquipment(LivingEquipmentChangeEvent event) {
        if (DisabledContentManager.shouldBlockEquip(event.getTo())) {
            event.getEntity().setItemSlot(event.getSlot(), ItemStack.EMPTY);
        }
    }

    private static void blockPlacedBlocks(BlockEvent.EntityPlaceEvent event) {
        if (DisabledContentManager.shouldBlockPlace(event.getPlacedBlock())) {
            event.setCanceled(true);
        }
    }
}
