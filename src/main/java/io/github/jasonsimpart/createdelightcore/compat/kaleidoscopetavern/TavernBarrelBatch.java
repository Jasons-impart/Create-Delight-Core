package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import de.cadentem.quality_food.core.Quality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Brewing precedes the optional, single QF aging upgrade of a barrel batch. */
public final class TavernBarrelBatch {
    public static final String NBT_KEY = "createdelightcore:barrel_quality";
    public static final int BASE_AGING_TICKS = 30 * 60 * 20;
    private final int initialLevel;
    private final boolean canAge;
    private int brewingTicks;
    private int remainingTicks;
    private boolean aged;
    private ItemStack pendingOutput = ItemStack.EMPTY;

    public TavernBarrelBatch(Quality rolled) {
        this(rolled, 0, true);
    }

    public TavernBarrelBatch(Quality rolled, int brewingTicks, boolean canAge) {
        this.initialLevel = Math.max(0, Math.min(canAge ? 2 : 3, rolled.level()));
        this.canAge = canAge;
        this.brewingTicks = Math.max(0, brewingTicks);
        this.remainingTicks = durationTicks();
    }

    private TavernBarrelBatch(int initialLevel, int brewingTicks, int remainingTicks, boolean canAge, boolean aged) {
        this.initialLevel = initialLevel;
        this.canAge = canAge;
        this.brewingTicks = brewingTicks;
        this.aged = canAge && aged && brewingTicks == 0;
        this.remainingTicks = this.aged || !canAge ? 0 : remainingTicks;
    }

    public Quality quality() { return Quality.get(initialLevel + (aged ? 1 : 0)); }
    public Quality targetQuality() { return Quality.get(Math.min(3, initialLevel + 1)); }
    public int durationTicks() { return canAge ? BASE_AGING_TICKS * (initialLevel + 1) : 0; }
    public int brewingTicks() { return brewingTicks; }
    public boolean brewing() { return brewingTicks > 0; }
    public boolean canAge() { return canAge; }
    public boolean finished() { return !brewing() && (!canAge || aged); }
    public int processingTicks() { return brewing() ? brewingTicks : remainingTicks; }
    public int remainingTicks() { return remainingTicks; }
    public boolean aged() { return aged; }

    /** A recipe snapshot for persistence and display, never exposed as an inventory or fluid tank. */
    public ItemStack pendingOutput() { return pendingOutput.copy(); }
    public boolean hasPendingOutput() { return !pendingOutput.isEmpty(); }
    public void deferOutput(ItemStack template) { pendingOutput = template.copy(); }

    public ItemStack takeCompletedOutput() {
        if (brewing()) return ItemStack.EMPTY;
        ItemStack result = pendingOutput;
        pendingOutput = ItemStack.EMPTY;
        return result;
    }

    /** Returns true when brewing completes or this batch's single aging upgrade completes. */
    public boolean tick() {
        if (brewing()) {
            brewingTicks--;
            return brewingTicks == 0;
        }
        if (finished()) return false;
        if (remainingTicks > 0) remainingTicks--;
        if (remainingTicks > 0) return false;
        aged = true;
        return true;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putInt("version", 3);
        tag.putInt("initial_quality", initialLevel);
        tag.putInt("brewing_ticks", brewingTicks);
        tag.putBoolean("can_age", canAge);
        tag.putInt("remaining_ticks", remainingTicks);
        tag.putBoolean("aged", aged);
        if (hasPendingOutput()) tag.put("pending_output", pendingOutput.save(new CompoundTag()));
        return tag;
    }

    public static TavernBarrelBatch load(CompoundTag tag) {
        int version = tag.getInt("version");
        if (version < 1 || version > 3) return null;
        boolean canAge = version == 1 || tag.getBoolean("can_age");
        int initial = tag.getInt("initial_quality");
        if (initial < 0 || initial > (canAge ? 2 : 3)) return null;
        int brewing = version == 1 ? 0 : Math.max(0, tag.getInt("brewing_ticks"));
        int remaining = canAge ? Math.max(0, Math.min(BASE_AGING_TICKS * (initial + 1), tag.getInt("remaining_ticks"))) : 0;
        var batch = new TavernBarrelBatch(initial, brewing, remaining, canAge, tag.getBoolean("aged"));
        if (version >= 3) batch.pendingOutput = ItemStack.of(tag.getCompound("pending_output"));
        return batch;
    }
}
