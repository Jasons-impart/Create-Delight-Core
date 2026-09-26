package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * quality_food's LevelData iterates a plain {@link HashMap} during level save while other threads
 * can write to it; a concurrent resize makes {@code qualities.get(key)} return null for a live key
 * and crashes on {@code .ordinal()}. Serialization is guarded by copying the entry set first and
 * skipping transient null values, leaving every read/write path untouched.
 */
@Mixin(value = LevelData.class, remap = false)
public abstract class LevelDataMixin {
    @Shadow
    private HashMap<Long, Quality> qualities;
    @Shadow
    private HashMap<Long, List<ItemStack>> storedItemStacks;

    @Inject(method = "serializeNBT", at = @At("HEAD"), cancellable = true)
    private void create_Delight_Core$serializeNBT(CallbackInfoReturnable<CompoundTag> cir) {
        cir.setReturnValue(create_Delight_Core$serializeSafe());
    }

    @Unique
    private CompoundTag create_Delight_Core$serializeSafe() {
        CompoundTag tag = new CompoundTag();

        Map<Long, Quality> snapshot;
        try {
            snapshot = new HashMap<>(qualities);
        } catch (RuntimeException ignored) {
            snapshot = Map.of();
        }
        for (Map.Entry<Long, Quality> entry : snapshot.entrySet()) {
            Quality quality = entry.getValue();
            if (quality != null) {
                tag.putInt(String.valueOf(entry.getKey()), quality.ordinal());
            }
        }

        CompoundTag storedItems = new CompoundTag();
        try {
            storedItemStacks.forEach((key, value) -> {
                ListTag entryTag = new ListTag();
                value.forEach(stack -> entryTag.add(stack.save(new CompoundTag())));
                storedItems.put(String.valueOf(key), entryTag);
            });
        } catch (RuntimeException ignored) {
        }
        tag.put("stored_items", storedItems);

        return tag;
    }
}
