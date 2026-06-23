package io.github.jasonsimpart.server;

import io.github.jasonsimpart.Config;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

import java.util.Set;

public final class AlexCavesDimensionSpawnGuardEvents {
    private static final Set<ResourceKey<Level>> BLOCKED_DIMENSIONS = Set.of(
            dimension("ceres_dimension"),
            dimension("enceladus_dimension"),
            dimension("pluto_dimension")
    );
    private static final Set<String> BLOCKED_NAMESPACES = Set.of("minecraft", "quark");

    private AlexCavesDimensionSpawnGuardEvents() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()) {
            return;
        }

        if (shouldBlock(event.getEntity(), event.getLevel().dimension())) {
            event.setCanceled(true);
        }
    }

    public static void onMobPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (shouldBlock(event.getEntity(), event.getLevel().getLevel().dimension())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    private static boolean shouldBlock(Entity entity, ResourceKey<Level> dimension) {
        if (!Config.ENABLE_ALEXSCAVES_DIMENSION_MONSTER_SPAWN_GUARD.get()
                || entity.getType().getCategory() != MobCategory.MONSTER
                || !BLOCKED_DIMENSIONS.contains(dimension)) {
            return false;
        }

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && BLOCKED_NAMESPACES.contains(id.getNamespace());
    }

    private static ResourceKey<Level> dimension(String path) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path));
    }
}
