package io.github.jasonsimpart.createdelightcore.content.util;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public final class QualityHarvestAutomationContext {
    private static final ThreadLocal<HarvestData> CURRENT = new ThreadLocal<>();

    private QualityHarvestAutomationContext() {
    }

    public static @Nullable HarvestData get() {
        return CURRENT.get();
    }

    public static @Nullable HarvestData push(MovementContext context, BlockPos pos, BlockState state) {
        HarvestData previous = CURRENT.get();
        CURRENT.set(new HarvestData(context, pos.immutable(), state));
        return previous;
    }

    public static void pop(@Nullable HarvestData previous) {
        if (previous == null) {
            CURRENT.remove();
            return;
        }
        CURRENT.set(previous);
    }

    public static void applyQuality(ItemStack stack) {
        HarvestData harvestData = CURRENT.get();
        if (harvestData == null || stack.isEmpty() || QualityUtils.getQuality(stack).level() > 0) {
            return;
        }

        if (!harvestData.isRelevantCrop() || !harvestData.ensureActive()) {
            return;
        }

        QualityUtils.applyQuality(
                stack,
                harvestData.state(),
                LevelData.get(harvestData.level(), harvestData.pos()),
                null,
                harvestData.level().getBlockState(harvestData.pos().below()));
    }

    public record Settings(int tier, int lifeMatterCost, int maxQuality, float multiplier) {
    }

    public static final class HarvestData {
        private final MovementContext context;
        private final BlockPos pos;
        private final BlockState state;
        private boolean consumptionTried;
        private boolean active;
        private Settings settings;

        private HarvestData(MovementContext context, BlockPos pos, BlockState state) {
            this.context = context;
            this.pos = pos;
            this.state = state;
        }

        public Level level() {
            return context.world;
        }

        public BlockPos pos() {
            return pos;
        }

        public BlockState state() {
            return state;
        }

        public boolean isActive() {
            return active;
        }

        public Settings settings() {
            return settings;
        }

        private boolean isRelevantCrop() {
            return QualityUtils.isRelevantCrop(state) || CDTags.AllBlockTags.QUALITY_CROPS.matches(state);
        }

        private boolean ensureActive() {
            if (active) {
                return true;
            }
            if (consumptionTried) {
                return false;
            }

            consumptionTried = true;
            settings = findSettings();
            if (settings == null || !consumeLifeMatter(settings.lifeMatterCost())) {
                settings = null;
                return false;
            }

            active = true;
            return true;
        }

        private @Nullable Settings findSettings() {
            IItemHandlerModifiable items = context.contraption.getStorage().getAllItems();
            int tier = 0;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.is(CDTags.AllItemTags.QUALITY_HARVEST_CALIBRATORS_TIER_3.tag)) {
                    tier = Math.max(tier, 3);
                } else if (stack.is(CDTags.AllItemTags.QUALITY_HARVEST_CALIBRATORS_TIER_2.tag)) {
                    tier = Math.max(tier, 2);
                } else if (stack.is(CDTags.AllItemTags.QUALITY_HARVEST_CALIBRATORS_TIER_1.tag)) {
                    tier = Math.max(tier, 1);
                }
            }

            return switch (tier) {
                case 3 -> new Settings(3, 4, 3, 0.75F);
                case 2 -> new Settings(2, 2, 2, 0.55F);
                case 1 -> new Settings(1, 1, 1, 0.35F);
                default -> null;
            };
        }

        private boolean consumeLifeMatter(int amount) {
            IItemHandlerModifiable items = context.contraption.getStorage().getAllItems();
            int available = 0;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.is(CDTags.AllItemTags.LIFE_MATTER.tag)) {
                    available += stack.getCount();
                    if (available >= amount) {
                        break;
                    }
                }
            }
            if (available < amount) {
                return false;
            }

            int remaining = amount;
            for (int slot = 0; slot < items.getSlots() && remaining > 0; slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (!stack.is(CDTags.AllItemTags.LIFE_MATTER.tag)) {
                    continue;
                }
                ItemStack extracted = items.extractItem(slot, remaining, false);
                remaining -= extracted.getCount();
            }

            return remaining <= 0;
        }
    }
}
