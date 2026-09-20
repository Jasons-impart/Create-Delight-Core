package io.github.jasonsimpart.compat.iceandfire;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.entity.DragonPartEntity;
import io.github.jasonsimpart.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Living-dragon blood collection; only loaded when Ice and Fire CE is present. */
public final class DragonBloodCollectionCompat {
    private DragonBloodCollectionCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(DragonBloodCollectionCompat::interactSpecific);
        NeoForge.EVENT_BUS.addListener(DragonBloodCollectionCompat::interact);
    }

    private static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (collect(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static void interact(PlayerInteractEvent.EntityInteract event) {
        if (collect(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static boolean collect(Player player, InteractionHand hand, Entity target) {
        ItemStack device = player.getMainHandItem();
        ItemStack bottle = player.getOffhandItem();
        if (hand != InteractionHand.MAIN_HAND || !device.is(ModItems.BLOOD_COLLECTION_DEVICE.get())
                || !bottle.is(Items.GLASS_BOTTLE)
                || !(target instanceof DragonBaseEntity || target instanceof DragonPartEntity)) {
            return false;
        }

        // CE resolves a part's parent only on the server. Intercept the client interaction
        // before CE sends its own multipart packet, then let the normal packet reach the server.
        if (player.level().isClientSide()) {
            return true;
        }
        Entity parent = target instanceof DragonPartEntity part ? part.getParent() : target;
        if (!(parent instanceof DragonBaseEntity dragon) || !dragon.isTame() || !dragon.isOwnedBy(player)
                || !dragon.isAlive() || dragon.isDeadOrDying() || dragon.isModelDead()) {
            return false;
        }
        // Both interact-at and ordinary interaction can be delivered for one click.
        if (player.getCooldowns().isOnCooldown(device.getItem())) {
            return true;
        }

        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType());
        String bloodPath = switch (type.toString()) {
            case "iceandfire:fire_dragon" -> "fire_dragon_blood";
            case "iceandfire:ice_dragon" -> "ice_dragon_blood";
            case "iceandfire:lightning_dragon" -> "lightning_dragon_blood";
            default -> null;
        };
        if (bloodPath == null) {
            return false;
        }
        Item blood = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("iceandfire", bloodPath));
        if (blood == Items.AIR) {
            return false;
        }
        // Preserve the old cost, including lethal collection and consumption in creative mode.
        float healthBefore = dragon.getHealth() + dragon.getAbsorptionAmount();
        if (!dragon.hurt(player.damageSources().genericKill(), Math.max(50.0F, dragon.getMaxHealth() * 0.1F))
                || dragon.getHealth() + dragon.getAbsorptionAmount() >= healthBefore) {
            // NeoForge can cancel LivingIncomingDamageEvent while hurt still returns true.
            return true;
        }
        player.getCooldowns().addCooldown(device.getItem(), 60);
        device.shrink(1);
        bottle.shrink(1);
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(blood));
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }
}
