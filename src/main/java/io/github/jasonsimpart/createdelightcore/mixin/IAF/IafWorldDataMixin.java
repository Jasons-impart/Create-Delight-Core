package io.github.jasonsimpart.createdelightcore.mixin.IAF;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.IafWorldData;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = IafWorldData.class, remap = false)
public abstract class IafWorldDataMixin extends SavedData {
    @Unique
    private static final String CREATEDELIGHTCORE$DATA_ID = IceAndFire.MODID + "_general";
    @Unique
    private static final int CREATEDELIGHTCORE$MAX_ENTRIES_PER_TYPE = 5_000;

    @Unique
    private EnumMap<IafWorldData.FeatureType, LinkedHashMap<String, BlockPos>> createdelightcore$lastGenerated;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true, require = 1)
    private static void createdelightcore$getDimensionData(Level world, CallbackInfoReturnable<IafWorldData> cir) {
        if (!(world instanceof ServerLevel serverLevel)) {
            cir.setReturnValue(null);
            return;
        }

        IafWorldData data = serverLevel.getDataStorage().computeIfAbsent(
                IafWorldData::new,
                IafWorldData::new,
                CREATEDELIGHTCORE$DATA_ID
        );
        cir.setReturnValue(data);
    }

    @Inject(
            method = "check(Lcom/github/alexthe666/iceandfire/world/IafWorldData$FeatureType;Lnet/minecraft/core/BlockPos;Ljava/lang/String;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void createdelightcore$checkAllPositions(
            IafWorldData.FeatureType type,
            BlockPos position,
            String id,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LinkedHashMap<String, BlockPos> entries = createdelightcore$data().get(type);
        double separation = IafConfig.dangerousWorldGenSeparationLimit;
        double separationSquared = separation * separation;
        boolean canGenerate = true;

        for (BlockPos previousPosition : entries.values()) {
            if (position.distSqr(previousPosition) <= separationSquared) {
                canGenerate = false;
                break;
            }
        }

        BlockPos storedPosition = position.immutable();
        entries.remove(id);
        entries.put(id, storedPosition);

        while (entries.size() > CREATEDELIGHTCORE$MAX_ENTRIES_PER_TYPE) {
            var iterator = entries.entrySet().iterator();
            iterator.next();
            iterator.remove();
        }

        setDirty();

        cir.setReturnValue(canGenerate);
    }

    @Inject(method = "load", at = @At("HEAD"), cancellable = true, require = 1)
    private void createdelightcore$loadBoundedData(CompoundTag tag, CallbackInfoReturnable<IafWorldData> cir) {
        EnumMap<IafWorldData.FeatureType, LinkedHashMap<String, BlockPos>> data = createdelightcore$data();

        for (IafWorldData.FeatureType type : IafWorldData.FeatureType.values()) {
            LinkedHashMap<String, BlockPos> entries = data.get(type);
            entries.clear();
            ListTag list = tag.getList(type.toString(), Tag.TAG_COMPOUND);
            LinkedHashMap<String, BlockPos> latestEntries = new LinkedHashMap<>();

            // Scan backwards so duplicate feature ids keep their newest position.
            for (int i = list.size() - 1;
                 i >= 0 && latestEntries.size() < CREATEDELIGHTCORE$MAX_ENTRIES_PER_TYPE;
                 i--) {
                CompoundTag entry = list.getCompound(i);
                String id = entry.getString("id");
                if (!latestEntries.containsKey(id)) {
                    latestEntries.put(id, NbtUtils.readBlockPos(entry.getCompound("position")));
                }
            }

            ArrayList<Map.Entry<String, BlockPos>> chronologicalEntries =
                    new ArrayList<>(latestEntries.entrySet());
            Collections.reverse(chronologicalEntries);
            for (Map.Entry<String, BlockPos> entry : chronologicalEntries) {
                entries.put(entry.getKey(), entry.getValue());
            }

            if (list.size() > entries.size()) {
                CreateDelightCore.LOGGER.warn(
                        "Compacted Ice and Fire {} worldgen records from {} to {} entries",
                        type,
                        list.size(),
                        entries.size()
                );
                setDirty();
            }
        }

        cir.setReturnValue((IafWorldData) (Object) this);
    }

    @Inject(method = {"save", "m_7176_"}, at = @At("HEAD"), cancellable = true, require = 1)
    private void createdelightcore$saveBoundedData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        EnumMap<IafWorldData.FeatureType, LinkedHashMap<String, BlockPos>> data = createdelightcore$data();

        for (IafWorldData.FeatureType type : IafWorldData.FeatureType.values()) {
            ListTag list = new ListTag();
            for (Map.Entry<String, BlockPos> entry : data.get(type).entrySet()) {
                CompoundTag subTag = new CompoundTag();
                subTag.putString("id", entry.getKey());
                subTag.put("position", NbtUtils.writeBlockPos(entry.getValue()));
                list.add(subTag);
            }
            tag.put(type.toString(), list);
        }

        cir.setReturnValue(tag);
    }

    @Unique
    private EnumMap<IafWorldData.FeatureType, LinkedHashMap<String, BlockPos>> createdelightcore$data() {
        if (createdelightcore$lastGenerated == null) {
            createdelightcore$lastGenerated = new EnumMap<>(IafWorldData.FeatureType.class);
            for (IafWorldData.FeatureType type : IafWorldData.FeatureType.values()) {
                createdelightcore$lastGenerated.put(type, new LinkedHashMap<>());
            }
        }
        return createdelightcore$lastGenerated;
    }
}
