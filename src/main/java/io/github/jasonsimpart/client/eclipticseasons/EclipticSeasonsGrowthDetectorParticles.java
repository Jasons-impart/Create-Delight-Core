package io.github.jasonsimpart.client.eclipticseasons;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class EclipticSeasonsGrowthDetectorParticles {
    private static final String ECLIPTIC_SEASONS_MODID = "eclipticseasons";
    private static final String ECLIPTIC_SEASONS_CROP_TAG_PREFIX = "crops/";
    private static final ResourceLocation GROWTH_DETECTOR_ID = ResourceLocation.fromNamespaceAndPath(ECLIPTIC_SEASONS_MODID, "growth_detector");
    private static final MethodHandle GET_GROW_CHANCE = getGrowChanceMethod();
    private static final int INTERVAL_TICKS = 60;
    private static final int HORIZONTAL_RADIUS = 5;
    private static final int VERTICAL_RADIUS = 2;
    private static final int PARTICLE_COUNT = 4;
    private static final int MAX_PARTICLE_BLOCKS = 32;
    private static final int MAX_GROWTH_EVALUATIONS = 64;
    private static final float PARTICLE_SCALE = 1.0F;
    private static boolean growthChanceFailureLogged;
    private static boolean growthChanceUnavailable;
    private static final int FAILED_POSITION_RETRY_TICKS = 600;
    private static final int TRANSIENT_FAILURE_RETRY_TICKS = INTERVAL_TICKS * 2;
    private static final int MAX_FAILED_POSITIONS = 64;
    private static final Map<BlockPos, FailedGrowthPosition> failedGrowthPositions = new HashMap<>();
    private static ClientLevel lastProcessedLevel;
    private static long lastProcessedGameTime = Long.MIN_VALUE;

    private EclipticSeasonsGrowthDetectorParticles() {
    }

    public static void register() {
        if (GET_GROW_CHANCE != null) {
            NeoForge.EVENT_BUS.addListener(EclipticSeasonsGrowthDetectorParticles::onClientTick);
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (lastProcessedLevel != level) {
            lastProcessedLevel = level;
            lastProcessedGameTime = Long.MIN_VALUE;
            growthChanceUnavailable = false;
            growthChanceFailureLogged = false;
            failedGrowthPositions.clear();
        }
        if (growthChanceUnavailable || level == null || player == null) {
            return;
        }

        long gameTime = level.getGameTime();
        if (lastProcessedGameTime != Long.MIN_VALUE && gameTime < lastProcessedGameTime) {
            failedGrowthPositions.clear();
            lastProcessedGameTime = Long.MIN_VALUE;
        }
        if (gameTime % INTERVAL_TICKS != 0 || gameTime == lastProcessedGameTime) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!isGrowthDetector(mainHand)) {
            return;
        }

        lastProcessedLevel = level;
        lastProcessedGameTime = gameTime;

        BlockPos playerPos = player.blockPosition();
        BlockPos min = playerPos.offset(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS);
        BlockPos max = playerPos.offset(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS);
        int particleBlocks = 0;
        int growthEvaluations = 0;
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            positions.add(pos.immutable());
        }
        int scanStart = level.random.nextInt(positions.size());
        for (int index = 0; index < positions.size(); index++) {
            BlockPos pos = positions.get((scanStart + index) % positions.size());
            BlockState state = level.getBlockState(pos);
            FailedGrowthPosition failure = failedGrowthPositions.get(pos);
            if (failure != null) {
                if (!failure.state().equals(state)) {
                    failedGrowthPositions.remove(pos);
                } else if (gameTime < failure.retryAt()) {
                    continue;
                }
            }
            if (hasEclipticSeasonsCropTag(state)) {
                if (growthEvaluations++ >= MAX_GROWTH_EVALUATIONS) {
                    break;
                }
                Float chance = growthChance(level, pos, state);
                if (growthChanceUnavailable) {
                    break;
                }
                if (chance == null) {
                    rememberGrowthFailure(pos, state, gameTime, TRANSIENT_FAILURE_RETRY_TICKS);
                    continue;
                }
                if (!Float.isFinite(chance)) {
                    if (!growthChanceFailureLogged) {
                        growthChanceFailureLogged = true;
                        CreateDelightCore.LOGGER.warn("Ecliptic Seasons growth detector API returned a non-finite growth chance; skipping this crop");
                    }
                    rememberGrowthFailure(pos, state, gameTime, FAILED_POSITION_RETRY_TICKS);
                    continue;
                }
                failedGrowthPositions.remove(pos);
                spawnGrowthChanceParticles(level, pos, chance);
                if (++particleBlocks >= MAX_PARTICLE_BLOCKS) {
                    break;
                }
            }
        }
    }

    private static boolean isGrowthDetector(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(GROWTH_DETECTOR_ID);
    }

    private static boolean hasEclipticSeasonsCropTag(BlockState state) {
        return state.getBlock().builtInRegistryHolder()
                .tags()
                .anyMatch(tag -> ECLIPTIC_SEASONS_MODID.equals(tag.location().getNamespace())
                        && tag.location().getPath().startsWith(ECLIPTIC_SEASONS_CROP_TAG_PREFIX));
    }

    private static Float growthChance(ClientLevel level, BlockPos pos, BlockState state) {
        try {
            return (float) GET_GROW_CHANCE.invokeExact((net.minecraft.world.level.Level) level, pos, state);
        } catch (Throwable throwable) {
            if (throwable instanceof LinkageError error) {
                growthChanceUnavailable = true;
                if (!growthChanceFailureLogged) {
                    growthChanceFailureLogged = true;
                    CreateDelightCore.LOGGER.warn("Ecliptic Seasons growth detector API became unavailable", error);
                }
                return null;
            }
            if (throwable instanceof Error error) {
                throw error;
            }
            if (!growthChanceFailureLogged) {
                growthChanceFailureLogged = true;
                CreateDelightCore.LOGGER.warn("Ecliptic Seasons growth detector API failed for a crop; skipping that position", throwable);
            }
            return null;
        }
    }

    private static void rememberGrowthFailure(BlockPos pos, BlockState state, long gameTime, int retryTicks) {
        failedGrowthPositions.entrySet().removeIf(entry -> gameTime >= entry.getValue().retryAt());
        if (failedGrowthPositions.size() >= MAX_FAILED_POSITIONS && !failedGrowthPositions.containsKey(pos)) {
            BlockPos oldest = failedGrowthPositions.entrySet().stream()
                    .min(Map.Entry.comparingByValue((left, right) -> Long.compare(left.retryAt(), right.retryAt())))
                    .orElseThrow()
                    .getKey();
            failedGrowthPositions.remove(oldest);
        }
        failedGrowthPositions.put(pos.immutable(), new FailedGrowthPosition(state, gameTime + retryTicks));
    }

    private static void spawnGrowthChanceParticles(ClientLevel level, BlockPos pos, float chance) {
        float clampedChance = Math.clamp(chance, 0.0F, 1.0F);
        float red;
        float green;
        if (clampedChance <= 0.5F) {
            red = 1.0F;
            green = clampedChance / 0.5F;
        } else {
            red = 1.0F - (clampedChance - 0.5F) / 0.5F;
            green = 1.0F;
        }

        DustParticleOptions particles = new DustParticleOptions(new Vector3f(red, green, 0.0F), PARTICLE_SCALE);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            level.addParticle(
                    particles,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    (level.random.nextDouble() - 0.5D) * 0.1D,
                    level.random.nextDouble() * -0.05D,
                    (level.random.nextDouble() - 0.5D) * 0.1D
            );
        }
    }

    private static MethodHandle getGrowChanceMethod() {
        try {
            Class<?> growthDetectorItem = Class.forName("com.teamtea.eclipticseasons.common.item.GrowthDetectorItem");
            return MethodHandles.publicLookup().findStatic(
                    growthDetectorItem,
                    "getGrowChance",
                    MethodType.methodType(float.class, net.minecraft.world.level.Level.class, BlockPos.class, BlockState.class)
            );
        } catch (ReflectiveOperationException | LinkageError exception) {
            CreateDelightCore.LOGGER.warn("Failed to bind Ecliptic Seasons growth detector API", exception);
            return null;
        }
    }

    private record FailedGrowthPosition(BlockState state, long retryAt) {
    }
}
