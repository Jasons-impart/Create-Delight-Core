package io.github.jasonsimpart.compat.mbd2;

import io.github.jasonsimpart.registry.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import io.github.jasonsimpart.util.ModIds;

@EventBusSubscriber(modid = "createdelightcore", value = Dist.CLIENT)
public final class MbdEconomyTooltip {
    private MbdEconomyTooltip() {}
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!ModList.get().isLoaded(ModIds.MBD2) || !ModList.get().isLoaded(ModIds.LIGHTMANS_CURRENCY)) return;
        var stack = event.getItemStack();
        var lines = event.getToolTip();
        double price = MbdFoodEconomy.price(stack, true);
        if (price > 0) {
            boolean total = Screen.hasShiftDown();
            double amount = price * (total ? stack.getCount() : 1);
            Component money = amount < 1
                    ? Component.literal(String.format(java.util.Locale.ROOT, "%.1f ", amount)).append(MbdFoodEconomy.text(MbdFoodEconomy.money(1)))
                    : MbdFoodEconomy.text(MbdFoodEconomy.money(amount));
            lines.add(Component.translatable(total ? "tooltip.createdelight.total_price" : "tooltip.createdelight.single_price", money));
        }
        if (itemId(stack).equals("createdelightcore:sell_bin")) {
            lines.add(Component.translatable(Screen.hasControlDown() ? "tooltip.createdelight.hold_ctrl" : "tooltip.createdelight.hold_ctrl_to_see_more_info"));
            if (Screen.hasControlDown()) lines.add(Component.translatable("tooltip.createdelight.ctrl_sell_bin", MbdFoodEconomy.text(MbdFoodEconomy.money(1))));
        }
        var categories = Component.empty();
        stack.getTags().filter(tag -> tag.location().getNamespace().equals("createdelightcore") && tag.location().getPath().startsWith("order/"))
                .forEach(tag -> {
                    String category = tag.location().getPath().substring(6);
                    if (!MbdOrders.DATA.getAsJsonObject("categories").has(category)) return;
                    categories.append(Component.translatable("tooltip.createdelight.order.entries." + category)).append("-")
                            .append(Component.translatable("tooltip.createdelight.order.tier." + MbdOrders.quality(stack, category))).append(" ");
                });
        if (!categories.getSiblings().isEmpty()) lines.add(categories);
        if (!stack.is(ModItems.ORDER.get())) return;
        var info = MbdOrders.info(stack);
        var customer = MbdOrders.customer(info);
        if (customer == null) return;
        lines.add(Component.translatable("tooltip.createdelight.order.title", Component.translatable("tooltip.createdelight.order.customer." + info.getString("type"))));
        String rarity = customer.get("rarity").getAsString();
        lines.add(Component.translatable("rarity." + rarity.toLowerCase(java.util.Locale.ROOT)).withStyle(Rarity.valueOf(rarity).color()));
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.createdelight.order.require.title"));
        var entries = info.getList("entries", Tag.TAG_COMPOUND);
        for (var raw : entries) {
            var entry = (CompoundTag) raw;
            String id = entry.getString("id");
            var category = MbdOrders.DATA.getAsJsonObject("categories").getAsJsonObject(id);
            if (category == null) continue;
            int count = entry.getInt("count"), base = category.get("base_count").getAsInt();
            var name = Component.translatable("tooltip.createdelight.order.entries." + id);
            var quality = Component.translatable("tooltip.createdelight.order.tier." + entry.getInt("minQuality"));
            lines.add(Screen.hasShiftDown()
                    ? Component.translatable("tooltip.createdelight.order.require.entry_shift", name, count, quality, base, String.format(java.util.Locale.ROOT, "%.2f", count / (double) base))
                    : Component.translatable("tooltip.createdelight.order.require.entry", name, count, quality));
        }
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.createdelight.order.reward.title"));
        var reward = customer.getAsJsonArray("reward");
        String path = reward == null ? "orders/" + info.getString("type") : reward.get(0).getAsString().split(":", 2)[1];
        lines.add(Component.translatable("tooltip.createdelight.order.reward.entry", entries.size() * (reward == null ? 1 : reward.get(1).getAsInt()),
                Component.translatable("tooltip.createdelight.order.reward." + path)));
        lines.add(MbdFoodEconomy.text(MbdFoodEconomy.money(MbdOrders.money(info))));
    }
    private static String itemId(net.minecraft.world.item.ItemStack stack) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
