package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.mbd2.api.pattern.BlockPattern;
import com.lowdragmc.mbd2.api.pattern.MultiblockShapeInfo;
import com.lowdragmc.mbd2.api.pattern.TraceabilityPredicate;
import com.lowdragmc.mbd2.api.pattern.predicates.PatternPredicate;
import com.lowdragmc.mbd2.api.pattern.predicates.PredicateBlocks;
import com.lowdragmc.mbd2.api.pattern.util.RelativeDirection;
import com.lowdragmc.mbd2.common.machine.definition.MultiblockMachineDefinition;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;

final class MbdMachinePattern {
    private MbdMachinePattern() {}

    static void configure(MultiblockMachineDefinition definition, CompoundTag data) {
        var predicates = new HashMap<String, PatternPredicate>();
        var resources = data.getCompound("predicates");
        for (var key : resources.getAllKeys()) {
            var tag = resources.getCompound(key).copy();
            if (MBDRegistries.PATTERN_PREDICATES.get(tag.getString("type")) == null) {
                throw new IllegalStateException("Unknown structure predicate " + tag.getString("type"));
            }
            boolean missing = false;
            for (var field : java.util.List.of("blocks", "fluids")) {
                var entries = tag.getList(field, Tag.TAG_STRING);
                int originalCount = entries.size();
                entries.removeIf(raw -> {
                    var id = ResourceLocation.parse(raw.getAsString());
                    boolean absent = field.equals("blocks") ? !BuiltInRegistries.BLOCK.containsKey(id) : !BuiltInRegistries.FLUID.containsKey(id);
                    if (absent) CreateDelightCore.LOGGER.warn("Missing structure {} alternative {} in {}", field, id, definition.id());
                    return absent;
                });
                if (originalCount > 0 && entries.isEmpty()) missing = true;
            }
            var states = tag.getList("states", Tag.TAG_COMPOUND);
            int stateCount = states.size();
            states.removeIf(raw -> {
                var id = ResourceLocation.parse(((CompoundTag) raw).getString("Name"));
                if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                    CreateDelightCore.LOGGER.warn("Missing structure state alternative {} in {}", id, definition.id());
                    return true;
                }
                return false;
            });
            if (stateCount > 0 && states.isEmpty()) missing = true;
            if (missing) {
                // Never let registry fallback turn an absent optional block into air or ANY.
                predicates.put(key, new PatternPredicate(state -> false, () -> new com.lowdragmc.lowdraglib2.utils.data.BlockInfo[0]));
                continue;
            }
            predicates.put(key, PatternPredicate.CODEC.parse(
                    Platform.getFrozenRegistry().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow());
        }
        var size = data.getIntArray("size");
        var cells = data.getIntArray("cells");
        var holders = data.getList("holders", Tag.TAG_COMPOUND);
        var axis = Direction.Axis.valueOf(data.getString("axis"));
        int a = axis.ordinal();
        // Keep the original editor's X/Y/Z indexing and slice orientation.
        int rowAxis = a == 2 ? 1 : 2;
        int heightAxis = a == 0 ? 1 : 0;
        var traces = new TraceabilityPredicate[size[a]][size[heightAxis]][size[rowAxis]];
        var repetitions = data.getIntArray("repetitions");
        var ranges = new int[size[a]][2];
        for (int i = 0; i < ranges.length; i++) {
            ranges[i][0] = repetitions[i * 2];
            ranges[i][1] = repetitions[i * 2 + 1];
        }
        int[] center = null;
        Direction facing = Direction.NORTH;
        int index = 0;
        for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    int[] pos = {x, y, z};
                    var holder = holders.getCompound(cells[index++]);
                    TraceabilityPredicate trace = null;
                    for (var raw : holder.getList("predicates", Tag.TAG_STRING)) {
                        var predicate = predicates.get(raw.getAsString());
                        if (predicate == null) throw new IllegalStateException("Missing predicate " + raw);
                        var next = new TraceabilityPredicate(predicate);
                        trace = trace == null ? next : trace.or(next);
                    }
                    if (holder.getBoolean("controller")) {
                        if (center != null) throw new IllegalStateException("Multiple controllers in " + definition.id());
                        int min = 0, max = 0;
                        for (int i = 0; i < pos[a]; i++) { min += ranges[i][0]; max += ranges[i][1]; }
                        center = new int[]{pos[rowAxis], pos[heightAxis], pos[a], min, max};
                        facing = Direction.from3DDataValue(holder.getInt("facing"));
                        var self = new TraceabilityPredicate(new PredicateBlocks(definition.block()));
                        trace = trace == null ? self : self.or(trace);
                    }
                    if (trace == null) throw new IllegalStateException("Empty predicate in " + definition.id());
                    traces[pos[a]][pos[heightAxis]][pos[rowAxis]] = trace;
                }
            }
        }
        if (center == null || index != cells.length) throw new IllegalStateException("Invalid structure " + definition.id());
        if (facing.getAxis() == Direction.Axis.Y) facing = Direction.NORTH;
        var directions = new RelativeDirection[]{RelativeDirection.getSliceYDirection(axis, facing),
                RelativeDirection.getSliceXDirection(axis, facing), RelativeDirection.getAisleDirection(axis, facing)};
        var pattern = new BlockPattern(traces, directions, ranges, center);
        // BlockPattern keeps mutable counters/cache; controllers must not share it.
        var origin = center;
        definition.blockPatternFactory(machine -> new BlockPattern(traces, directions, ranges, origin) {
            @Override
            public LongOpenHashSet collectTrackedPositions(BlockPos pos, Direction front) {
                // MBD2 21.0.6 captures only non-ANY cells at maximum repetition.
                // Shorter coils move the cap into omitted cells, so the async check
                // rejects a structure that passes a live check. Capture the union
                // of required cells across all legal shifted layer positions.
                var positions = new LongOpenHashSet();
                int prefixMin = 0, prefixMax = 0;
                for (int layer = 0; layer < traces.length; layer++) {
                    for (int z = prefixMin - origin[4]; z < prefixMax + ranges[layer][1] - origin[3]; z++) {
                        for (int row = 0; row < traces[layer].length; row++) {
                            for (int column = 0; column < traces[layer][row].length; column++) {
                                if (traces[layer][row][column].isAny()) continue;
                                int[] offsets = {column - origin[0], row - origin[1], z};
                                var cell = pos;
                                for (int i = 0; i < 3; i++) cell = cell.relative(directions[i].getActualFacing(front), offsets[i]);
                                positions.add(cell.asLong());
                            }
                        }
                    }
                    prefixMin += ranges[layer][0];
                    prefixMax += ranges[layer][1];
                }
                return positions;
            }

            @Override
            public LongOpenHashSet collectNbtSensitivePositions(BlockPos pos, Direction front) {
                boolean sensitive = predicates.values().stream().anyMatch(p -> p.nbt != null && !p.nbt.isEmpty());
                return sensitive ? collectTrackedPositions(pos, front) : new LongOpenHashSet();
            }
        });
        definition.shapeInfoFactory(ignored -> {
            var previews = new ArrayList<MultiblockShapeInfo>();
            var repeat = java.util.Arrays.stream(ranges).mapToInt(range -> range[0]).toArray();
            previews.add(new MultiblockShapeInfo(pattern.getPreview(repeat)));
            for (int layer = 0; layer < ranges.length; layer++) {
                for (int count = ranges[layer][0] + 1; count <= ranges[layer][1]; count++) {
                    repeat[layer] = count;
                    previews.add(new MultiblockShapeInfo(pattern.getPreview(repeat)));
                }
                repeat[layer] = ranges[layer][0];
            }
            return previews.toArray(MultiblockShapeInfo[]::new);
        });
    }
}
