package io.github.jasonsimpart.compat.alexsmobs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Pack interactions, using registry lookups so Alex's Mobs stays optional. */
public final class PackBlockInteractions {
    private PackBlockInteractions() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PackBlockInteractions::interact);
    }

    private static void interact(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        if (event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE
                || event.getHand() != InteractionHand.MAIN_HAND || player.isSpectator()
                || !player.mayBuild() || !level.mayInteract(player, pos)) return;
        var state = level.getBlockState(pos);
        var held = event.getItemStack();
        var blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId.equals(ResourceLocation.parse("createdelightcore:fragment_of_border"))
                && held.is(state.getBlock().asItem())) {
            var type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("alexsmobsup:farseer"));
            if (type.isEmpty()) return;
            if (!level.isClientSide()) {
                var entity = type.get().create(level);
                if (entity == null) return;
                entity.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
                if (!level.addFreshEntity(entity)) return;
                if (!level.destroyBlock(pos, true, player)) {
                    entity.discard();
                    return;
                }
                // The legacy interaction consumes one held fragment even in creative mode.
                held.shrink(1);
            }
            success(event);
        } else if (blockId.equals(ResourceLocation.parse("alexsmobsup:capsid"))) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;
            var items = blockEntity.saveWithoutMetadata(level.registryAccess()).getList("Items", Tag.TAG_COMPOUND);
            if (items.isEmpty()) return;
            String stored = items.getCompound(0).getString("id");
            String heldId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
            if (!unsafePair(stored, heldId)) return;
            if (!level.isClientSide()) {
                // Preserve the old exploit guard: reset the capsid without dropping its contents.
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos, state);
            }
            success(event);
        }
    }

    private static boolean unsafePair(String stored, String held) {
        return stored.equals("create:minecart_contraption") && held.equals(stored)
                || stored.contains("present") && held.contains("present")
                || stored.contains("functionalstorage") && held.contains("functionalstorage");
    }

    private static void success(PlayerInteractEvent.RightClickBlock event) {
        event.getEntity().swing(event.getHand(), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
