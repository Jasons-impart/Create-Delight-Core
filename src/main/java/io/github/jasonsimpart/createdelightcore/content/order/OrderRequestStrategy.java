package io.github.jasonsimpart.createdelightcore.content.order;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record OrderRequestStrategy(OrderRequestMode mode, List<OrderRequestSelection> fixedSelections,
                                   List<OrderRequestRatioSelection> ratioSelections) {
    public OrderRequestStrategy {
        mode = mode == null ? OrderRequestMode.FIXED_COUNT : mode;
        fixedSelections = copyFixed(fixedSelections);
        ratioSelections = copyRatios(ratioSelections);
    }

    public static OrderRequestStrategy empty() {
        return new OrderRequestStrategy(OrderRequestMode.FIXED_COUNT, List.of(), List.of());
    }

    public boolean isEmpty() {
        return switch (mode) {
            case FIXED_COUNT -> fixedSelections.isEmpty();
            case RATIO -> ratioSelections.isEmpty();
        };
    }

    public OrderRequestStrategy withFixedSelections(List<OrderRequestSelection> selections) {
        return new OrderRequestStrategy(mode, selections, ratioSelections);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", mode.name());
        tag.put("FixedSelections", writeFixed(fixedSelections));
        tag.put("RatioSelections", writeRatios(ratioSelections));
        return tag;
    }

    public static OrderRequestStrategy read(CompoundTag tag) {
        OrderRequestMode mode = OrderRequestMode.byName(tag.getString("Mode"));
        return new OrderRequestStrategy(
                mode,
                readFixed(tag.getList("FixedSelections", Tag.TAG_COMPOUND)),
                readRatios(tag.getList("RatioSelections", Tag.TAG_COMPOUND))
        );
    }

    public void send(FriendlyByteBuf buf) {
        buf.writeEnum(mode);
        buf.writeVarInt(fixedSelections.size());
        for (OrderRequestSelection selection : fixedSelections) {
            selection.send(buf);
        }
        buf.writeVarInt(ratioSelections.size());
        for (OrderRequestRatioSelection selection : ratioSelections) {
            selection.send(buf);
        }
    }

    public static OrderRequestStrategy receive(FriendlyByteBuf buf) {
        OrderRequestMode mode = buf.readEnum(OrderRequestMode.class);
        int fixedCount = buf.readVarInt();
        List<OrderRequestSelection> fixed = new ArrayList<>();
        for (int i = 0; i < fixedCount; i++) {
            fixed.add(OrderRequestSelection.receive(buf));
        }
        int ratioCount = buf.readVarInt();
        List<OrderRequestRatioSelection> ratios = new ArrayList<>();
        for (int i = 0; i < ratioCount; i++) {
            ratios.add(OrderRequestRatioSelection.receive(buf));
        }
        return new OrderRequestStrategy(mode, fixed, ratios);
    }

    public static ListTag writeFixed(List<OrderRequestSelection> selections) {
        ListTag list = new ListTag();
        for (OrderRequestSelection selection : copyFixed(selections)) {
            list.add(selection.write());
        }
        return list;
    }

    public static List<OrderRequestSelection> readFixed(ListTag list) {
        List<OrderRequestSelection> selections = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag selectionTag = list.getCompound(i);
            if (selectionTag.contains("EntryId")) {
                selections.add(OrderRequestSelection.read(selectionTag));
            }
        }
        return copyFixed(selections);
    }

    public static ListTag writeRatios(List<OrderRequestRatioSelection> selections) {
        ListTag list = new ListTag();
        for (OrderRequestRatioSelection selection : copyRatios(selections)) {
            list.add(selection.write());
        }
        return list;
    }

    public static List<OrderRequestRatioSelection> readRatios(ListTag list) {
        List<OrderRequestRatioSelection> selections = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag selectionTag = list.getCompound(i);
            if (selectionTag.contains("EntryId")) {
                selections.add(OrderRequestRatioSelection.read(selectionTag));
            }
        }
        return copyRatios(selections);
    }

    private static List<OrderRequestSelection> copyFixed(List<OrderRequestSelection> selections) {
        List<OrderRequestSelection> copy = new ArrayList<>();
        if (selections != null) {
            for (OrderRequestSelection selection : selections) {
                if (selection != null && selection.isValid()) {
                    copy.add(new OrderRequestSelection(selection.entryId(), selection.stack(), selection.count(), selection.quality()));
                }
            }
        }
        return List.copyOf(copy);
    }

    private static List<OrderRequestRatioSelection> copyRatios(List<OrderRequestRatioSelection> selections) {
        List<OrderRequestRatioSelection> copy = new ArrayList<>();
        if (selections != null) {
            for (OrderRequestRatioSelection selection : selections) {
                if (selection != null && selection.isValid()) {
                    copy.add(new OrderRequestRatioSelection(selection.entryId(), selection.stack(), selection.weight(), selection.quality()));
                }
            }
        }
        return List.copyOf(copy);
    }
}
