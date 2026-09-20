package io.github.jasonsimpart.compat.fruitsdelight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

/** Acting-player sound/swing feedback from 0488; native cauldron recipes own all mutations. */
public final class CauldronFeedback {
    private static final Map<String, String> SPECIAL_FRUIT = Map.of(
            "chorus", "minecraft:chorus_fruit", "melon", "minecraft:melon_slice",
            "apple", "minecraft:apple", "glowberry", "minecraft:glow_berries",
            "sweetberry", "minecraft:sweet_berries", "durian", "fruitsdelight:durian_flesh",
            "pineapple", "fruitsdelight:pineapple_slice", "hamimelon", "fruitsdelight:hamimelon_slice");
    private static final java.util.Set<String> FRUITS = java.util.Set.of(
            "chorus", "melon", "durian", "pineapple", "hamimelon", "apple", "lemon", "pear",
            "hawberry", "lychee", "mango", "persimmon", "peach", "orange", "mangosteen",
            "bayberry", "kiwi", "fig", "blueberry", "cranberry", "glowberry", "sweetberry");
    private CauldronFeedback() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CauldronFeedback::interact);
    }

    private static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator() || !player.mayBuild() || !event.getLevel().mayInteract(player, event.getPos())) return;
        if (event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE) return;
        boolean secondaryUse = player.isSecondaryUseActive()
                && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
                && !(player.getMainHandItem().doesSneakBypassUse(event.getLevel(), event.getPos(), player)
                && player.getOffhandItem().doesSneakBypassUse(event.getLevel(), event.getPos(), player));
        if (secondaryUse && event.getUseBlock() != net.neoforged.neoforge.common.util.TriState.TRUE) return;
        boolean heated = event.getLevel().getBlockState(event.getPos().below()).is(
                TagKey.create(Registries.BLOCK, ResourceLocation.parse("farmersdelight:heat_sources")));
        SoundEvent sound = feedback(event.getLevel().getBlockState(event.getPos()), event.getItemStack(), heated);
        if (sound == null) return;
        player.swing(event.getHand(), true);
        player.playNotifySound(sound, SoundSource.BLOCKS, 1, 1);
    }

    public static SoundEvent feedback(BlockState state, ItemStack stack, boolean heated) {
        String block = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (block.equals("minecraft:water_cauldron")) {
            if (item.equals("fruitsdelight:lemon_slice")) return SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT;
            for (String fruit : FRUITS) if (item.equals("fruitsdelight:" + fruit + "_jelly")) return SoundEvents.HONEY_BLOCK_PLACE;
        }
        for (String fruit : FRUITS) {
            String raw = SPECIAL_FRUIT.getOrDefault(fruit, "fruitsdelight:" + fruit);
            String prefix = "fruitsdelight:" + fruit;
            if (block.equals("fruitsdelight:lemonade_cauldron") && heated && item.equals(raw))
                return SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT;
            if (block.equals(prefix + "_cauldron")) {
                var property = state.getBlock().getStateDefinition().getProperty("level");
                if (!(property instanceof IntegerProperty levelProperty)) return null;
                int level = state.getValue(levelProperty);
                if (heated && item.equals(raw) && level < 12) return SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT;
                if (heated && item.equals("minecraft:sugar") && level == 12
                        || item.equals(prefix + "_jelly") && level < 12) return SoundEvents.HONEY_BLOCK_PLACE;
            }
            if (block.equals(prefix + "_jam_cauldron")) {
                if (heated && item.equals("minecraft:slime_ball")) return SoundEvents.SLIME_BLOCK_PLACE;
                if (item.equals("minecraft:glass_bottle")) return SoundEvents.HONEY_BLOCK_PLACE;
            }
            if (block.equals(prefix + "_jello_cauldron") && item.equals("minecraft:bowl"))
                return SoundEvents.SLIME_BLOCK_PLACE;
        }
        return null;
    }
}
