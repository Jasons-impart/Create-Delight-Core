package io.github.jasonsimpart.createdelightcore.content.order.board;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public record OrderBoardCandidate(String kind, String customerSeal, String categorySeal, List<String> requiredCategories, int grade,
                                  int minTotal, int maxTotal, int warehouseCount, double marketMultiplier) {
    public static final String DRAFT_ITEM_ID = "createdelight:unopened_order";

    public OrderBoardCandidate {
        kind = kind == null ? "opportunity" : kind;
        customerSeal = customerSeal == null ? "" : customerSeal;
        categorySeal = categorySeal == null ? "" : categorySeal;
        requiredCategories = requiredCategories == null
                ? List.of()
                : List.copyOf(new LinkedHashSet<>(requiredCategories.stream()
                        .filter(category -> category != null && !category.isBlank())
                        .limit(3)
                        .toList()));
        grade = Math.max(1, Math.min(6, grade));
        minTotal = Math.max(1, minTotal);
        maxTotal = Math.max(minTotal, maxTotal);
        warehouseCount = Math.max(0, warehouseCount);
        marketMultiplier = Math.max(1.0D, marketMultiplier);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Kind", kind);
        tag.putString("CustomerSeal", customerSeal);
        tag.putString("CategorySeal", categorySeal);
        ListTag required = new ListTag();
        requiredCategories.forEach(category -> required.add(StringTag.valueOf(category)));
        tag.put("RequiredCategories", required);
        tag.putInt("Grade", grade);
        tag.putInt("MinTotal", minTotal);
        tag.putInt("MaxTotal", maxTotal);
        tag.putInt("WarehouseCount", warehouseCount);
        tag.putDouble("MarketMultiplier", marketMultiplier);
        return tag;
    }

    public static OrderBoardCandidate read(CompoundTag tag) {
        return new OrderBoardCandidate(
                tag.getString("Kind"),
                tag.getString("CustomerSeal"),
                tag.getString("CategorySeal"),
                readRequiredCategories(tag),
                tag.getInt("Grade"),
                tag.getInt("MinTotal"),
                tag.getInt("MaxTotal"),
                tag.getInt("WarehouseCount"),
                tag.getDouble("MarketMultiplier")
        );
    }

    public void send(FriendlyByteBuf buf) {
        buf.writeUtf(kind, 32);
        buf.writeUtf(customerSeal, 64);
        buf.writeUtf(categorySeal, 64);
        buf.writeVarInt(requiredCategories.size());
        requiredCategories.forEach(category -> buf.writeUtf(category, 64));
        buf.writeVarInt(grade);
        buf.writeVarInt(minTotal);
        buf.writeVarInt(maxTotal);
        buf.writeVarInt(warehouseCount);
        buf.writeDouble(marketMultiplier);
    }

    public static OrderBoardCandidate receive(FriendlyByteBuf buf) {
        return new OrderBoardCandidate(
                buf.readUtf(32),
                buf.readUtf(64),
                buf.readUtf(64),
                receiveRequiredCategories(buf),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readDouble()
        );
    }

    public ItemStack createDraftStack() {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("createdelight", "unopened_order"));
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);
        CompoundTag draft = new CompoundTag();
        draft.putInt("Revision", 1);
        draft.putString("customerSeal", customerSeal);
        draft.putString("categorySeal", categorySeal);
        if (!requiredCategories.isEmpty()) {
            ListTag required = new ListTag();
            requiredCategories.forEach(category -> required.add(StringTag.valueOf(category)));
            draft.put("requiredCategories", required);
        }
        draft.putInt("Grade", grade);
        draft.putString("BoardKind", kind);
        if ("opportunity".equals(kind) && marketMultiplier > 1.0D) {
            draft.putDouble("BoardMarketMultiplier", marketMultiplier);
        }
        stack.getOrCreateTag().put("OrderDraft", draft);
        return stack;
    }

    private static List<String> readRequiredCategories(CompoundTag tag) {
        List<String> result = new ArrayList<>();
        ListTag list = tag.getList("RequiredCategories", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }
        if (result.isEmpty() && tag.contains("RequiredCategory", Tag.TAG_STRING)) {
            result.add(tag.getString("RequiredCategory"));
        }
        return List.copyOf(result);
    }

    private static List<String> receiveRequiredCategories(FriendlyByteBuf buf) {
        int encodedSize = Math.max(0, Math.min(16, buf.readVarInt()));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < encodedSize; i++) {
            String category = buf.readUtf(64);
            if (result.size() < 3) {
                result.add(category);
            }
        }
        return List.copyOf(result);
    }
}
