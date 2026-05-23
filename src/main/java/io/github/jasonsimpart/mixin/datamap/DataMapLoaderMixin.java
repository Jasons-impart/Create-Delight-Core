package io.github.jasonsimpart.mixin.datamap;

import com.mojang.datafixers.util.Either;
import io.github.jasonsimpart.Config;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.datamaps.DataMapEntry;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(DataMapLoader.class)
public abstract class DataMapLoaderMixin {
    private static final ResourceLocation DIMENSION_REGISTRY = ResourceLocation.withDefaultNamespace("dimension");
    private static final ResourceLocation ITEM_REGISTRY = ResourceLocation.withDefaultNamespace("item");
    private static final ResourceLocation BLUEPRINT_BIOME_SLICE_SIZES = ResourceLocation.fromNamespaceAndPath("blueprint", "modded_biome_slice_sizes");
    private static final ResourceLocation CREATE_ADDITIONAL_LOGISTICS_CURRENCY = ResourceLocation.fromNamespaceAndPath("createadditionallogistics", "currency");
    private static final ResourceLocation BLUEPRINT_EXAMPLE_DIMENSION = ResourceLocation.fromNamespaceAndPath("modid", "example");
    private static final String NUMISMATICS_NAMESPACE = "numismatics";

    @ModifyVariable(
            method = "buildDataMap",
            at = @At("HEAD"),
            argsOnly = true,
            index = 3
    )
    private <T, R> List<DataMapFile<T, R>> cdc$filterKnownMissingDataMapKeys(
            List<DataMapFile<T, R>> entries,
            Registry<R> registry,
            DataMapType<R, T> dataMapType
    ) {
        if (!Config.ENABLE_DATA_MAP_MISSING_KEY_FILTERS.get() || !cdc$shouldFilterDataMap(dataMapType)) {
            return entries;
        }

        List<DataMapFile<T, R>> filteredEntries = null;
        for (int index = 0; index < entries.size(); index++) {
            DataMapFile<T, R> entry = entries.get(index);
            Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> filteredValues = cdc$filterValues(dataMapType, entry.values());
            if (filteredValues != entry.values()) {
                if (filteredEntries == null) {
                    filteredEntries = new ArrayList<>(entries);
                }
                filteredEntries.set(index, new DataMapFile<>(entry.replace(), filteredValues, entry.removals()));
            }
        }

        return filteredEntries == null ? entries : filteredEntries;
    }

    private static boolean cdc$shouldFilterDataMap(DataMapType<?, ?> dataMapType) {
        ResourceLocation registryId = dataMapType.registryKey().location();
        ResourceLocation dataMapId = dataMapType.id();
        return DIMENSION_REGISTRY.equals(registryId) && BLUEPRINT_BIOME_SLICE_SIZES.equals(dataMapId)
                || ITEM_REGISTRY.equals(registryId) && CREATE_ADDITIONAL_LOGISTICS_CURRENCY.equals(dataMapId);
    }

    private static <T, R> Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> cdc$filterValues(
            DataMapType<R, T> dataMapType,
            Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values
    ) {
        Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> filteredValues = null;
        for (Map.Entry<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> entry : values.entrySet()) {
            if (!cdc$shouldRemoveKey(dataMapType, entry.getKey())) {
                continue;
            }

            if (filteredValues == null) {
                filteredValues = new LinkedHashMap<>(values);
            }
            filteredValues.remove(entry.getKey());
        }

        return filteredValues == null ? values : filteredValues;
    }

    private static <R> boolean cdc$shouldRemoveKey(DataMapType<R, ?> dataMapType, Either<TagKey<R>, ResourceKey<R>> key) {
        return key.map(tag -> false, registryKey -> cdc$shouldRemoveResourceKey(dataMapType, registryKey.location()));
    }

    private static boolean cdc$shouldRemoveResourceKey(DataMapType<?, ?> dataMapType, ResourceLocation key) {
        ResourceLocation registryId = dataMapType.registryKey().location();
        ResourceLocation dataMapId = dataMapType.id();
        if (DIMENSION_REGISTRY.equals(registryId) && BLUEPRINT_BIOME_SLICE_SIZES.equals(dataMapId)) {
            return BLUEPRINT_EXAMPLE_DIMENSION.equals(key);
        }

        return ITEM_REGISTRY.equals(registryId)
                && CREATE_ADDITIONAL_LOGISTICS_CURRENCY.equals(dataMapId)
                && NUMISMATICS_NAMESPACE.equals(key.getNamespace())
                && switch (key.getPath()) {
                    case "bevel", "cog", "crown", "sprocket", "spur", "sun" -> true;
                    default -> false;
                };
    }
}
