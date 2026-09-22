package io.github.jasonsimpart.content.quality.harvest;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.jasonsimpart.registry.ModBlocks;
import io.github.jasonsimpart.registry.ModTags;
import io.github.jasonsimpart.util.ModIds;
import io.github.jasonsimpart.util.OptionalMods;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QualityHarvestAutomationContext {
    private static final ThreadLocal<HarvestData> CURRENT = new ThreadLocal<>();

    private QualityHarvestAutomationContext() {
    }

    public static @Nullable HarvestData get() {
        return CURRENT.get();
    }

    /**
     * Reflection contract for {@code de.cadentem.quality_food.compat.HarvestAutomationCompat}
     * in the Quality Food fork: keep this name and signature stable. Called once per
     * harvested crop drop; returns {@link Settings} when an active harvest context exists,
     * consuming life matter lazily on first use.
     */
    public static @Nullable Settings getAutomationSettings(BlockState state) {
        HarvestData data = CURRENT.get();
        if (data == null || !data.ensureActive(state.is(ModTags.Blocks.QUALITY_CROPS))) {
            return null;
        }
        return data.settings;
    }

    /**
     * Reflection contract for Quality Food (see {@link #getAutomationSettings}):
     * season-based grow chance for automated harvests, without the manual player bonus.
     */
    public static float getAutomatedGrowChance(Level level, BlockPos pos, BlockState state, int sourceRank) {
        if (!OptionalMods.isLoaded(ModIds.ECLIPTIC_SEASONS)) {
            return 1.0F;
        }
        return getEclipticGrowChance(level, pos, state);
    }

    /** Reflection contract for Quality Food: extra blocks treated as quality crops (createdelightcore:quality_crops tag). */
    public static boolean isExtraCrop(BlockState state) {
        return state.is(ModTags.Blocks.QUALITY_CROPS);
    }

    private static Method eclipticGrowChance;
    private static boolean eclipticLookupAttempted;
    private static boolean eclipticInvokeFailed;

    private static float getEclipticGrowChance(Level level, BlockPos pos, BlockState state) {
        if (eclipticInvokeFailed) {
            return 1.0F;
        }
        try {
            if (eclipticGrowChance == null) {
                if (eclipticLookupAttempted) {
                    return 1.0F;
                }
                eclipticLookupAttempted = true;
                Class<?> detector = Class.forName("com.teamtea.eclipticseasons.common.item.GrowthDetectorItem");
                eclipticGrowChance = detector.getMethod("getGrowChance", Level.class, BlockPos.class, BlockState.class);
            }
            return (float) eclipticGrowChance.invoke(null, level, pos, state);
        } catch (Throwable throwable) {
            eclipticInvokeFailed = true;
            return 1.0F;
        }
    }

    public static @Nullable HarvestData push(MovementContext context, BlockPos pos, BlockState state) {
        HarvestData previous = CURRENT.get();
        CURRENT.set(new HarvestData(context));
        return previous;
    }

    public static void pop(@Nullable HarvestData previous) {
        if (previous == null) {
            CURRENT.remove();
            return;
        }
        CURRENT.set(previous);
    }

    public record Settings(int tier, int lifeMatterCost, int maxQuality, float multiplier) {
    }

    public static final class HarvestData {
        private final MovementContext context;
        private boolean consumptionTried;
        private boolean active;
        private Settings settings;

        private HarvestData(MovementContext context) {
            this.context = context;
        }

        public Level level() {
            return context.world;
        }

        public boolean isActive() {
            return active;
        }

        public Settings settings() {
            return settings;
        }

        /**
         * Called once per harvested crop drop by the Quality Food hook. Consumes life matter
         * from the on-board controller (preferred) or the contraption inventory on first use.
         */
        public boolean ensureActive(boolean relevantCrop) {
            if (!relevantCrop) {
                return false;
            }
            if (active) {
                return true;
            }
            if (consumptionTried) {
                return false;
            }

            consumptionTried = true;
            ControllerSelection controller = findControllerSelection();
            settings = controller != null ? controller.settings() : findStorageSettings();
            if (settings == null || !consumeLifeMatter(controller, settings.lifeMatterCost())) {
                settings = null;
                return false;
            }

            active = true;
            return true;
        }

        private @Nullable Settings findStorageSettings() {
            if (hasController()) {
                return null;
            }

            IItemHandlerModifiable items = context.contraption.getStorage().getAllItems();
            int tier = 0;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_3)) {
                    tier = Math.max(tier, 3);
                } else if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_2)) {
                    tier = Math.max(tier, 2);
                } else if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_1)) {
                    tier = Math.max(tier, 1);
                }
            }

            return settingsForTier(tier);
        }

        private @Nullable Settings settingsForTier(int tier) {
            return switch (tier) {
                case 3 -> new Settings(3, 5, 3, 0.70F);
                case 2 -> new Settings(2, 3, 2, 0.45F);
                case 1 -> new Settings(1, 2, 1, 0.25F);
                default -> null;
            };
        }

        private boolean consumeLifeMatter(@Nullable ControllerSelection controller, int amount) {
            if (controller != null) {
                consumeController(controller);
                return true;
            }
            if (hasController()) {
                return false;
            }

            IItemHandlerModifiable items = context.contraption.getStorage().getAllItems();
            int available = 0;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.is(ModTags.Items.LIFE_MATTER)) {
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
                if (!stack.is(ModTags.Items.LIFE_MATTER)) {
                    continue;
                }
                ItemStack extracted = items.extractItem(slot, remaining, false);
                remaining -= extracted.getCount();
            }

            return remaining <= 0;
        }

        private boolean hasController() {
            for (StructureTemplate.StructureBlockInfo info : context.contraption.getBlocks().values()) {
                if (isController(info.state())) {
                    return true;
                }
            }
            return false;
        }

        private @Nullable ControllerSelection findControllerSelection() {
            List<ControllerSelection> candidates = new ArrayList<>();
            for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : context.contraption.getBlocks().entrySet()) {
                StructureTemplate.StructureBlockInfo info = entry.getValue();
                if (!isController(info.state())) {
                    continue;
                }

                CompoundTag tag = info.nbt() == null ? new CompoundTag() : info.nbt();
                ItemStack calibrator = QualityHarvestControllerBlockEntity.getCalibrator(tag, level().registryAccess());
                Settings settings = settingsForTier(tierOf(calibrator));
                if (settings == null) {
                    continue;
                }

                int stored = QualityHarvestControllerBlockEntity.getLifeMatterStored(tag);
                if (stored >= settings.lifeMatterCost()) {
                    candidates.add(new ControllerSelection(entry.getKey(), info, tag, settings, stored));
                }
            }

            ControllerSelection best = null;
            for (ControllerSelection candidate : candidates) {
                if (best == null || candidate.settings().tier() > best.settings().tier()) {
                    best = candidate;
                }
            }
            return best;
        }

        private boolean isController(BlockState state) {
            return state.is(ModBlocks.QUALITY_HARVEST_CONTROLLER.get())
                    || state.is(ModTags.Blocks.QUALITY_HARVEST_CONTROLLERS);
        }

        private int tierOf(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_3)) {
                return 3;
            }
            if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_2)) {
                return 2;
            }
            if (stack.is(ModTags.Items.QUALITY_HARVEST_CALIBRATORS_TIER_1)) {
                return 1;
            }
            return 0;
        }

        private void consumeController(ControllerSelection controller) {
            CompoundTag updatedTag = QualityHarvestControllerBlockEntity.setLifeMatterStored(
                    controller.tag(), controller.storedLifeMatter() - controller.settings().lifeMatterCost());
            StructureTemplate.StructureBlockInfo updatedInfo =
                    new StructureTemplate.StructureBlockInfo(controller.info().pos(), controller.info().state(), updatedTag);
            context.contraption.getBlocks().put(controller.key(), updatedInfo);
        }

        private record ControllerSelection(BlockPos key, StructureTemplate.StructureBlockInfo info, CompoundTag tag,
                                           Settings settings, int storedLifeMatter) {
        }
    }
}
