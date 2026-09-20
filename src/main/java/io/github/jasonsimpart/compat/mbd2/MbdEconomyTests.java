package io.github.jasonsimpart.compat.mbd2;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.jasonsimpart.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@PrefixGameTestTemplate(false)
public final class MbdEconomyTests {
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void openingOrderPreservesInputWhenDeliveryIsRejected(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ModItems.UNOPENED_ORDER.toStack(2));
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> reject = event -> {
            if (event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity) event.setCanceled(true);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(reject);
        try {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(
                    player, net.minecraft.world.InteractionHand.MAIN_HAND));
            helper.assertTrue(player.getMainHandItem().is(ModItems.UNOPENED_ORDER.get()) && player.getMainHandItem().getCount() == 2,
                    "Rejected overflow delivery must not consume an unopened order");
            player.getMainHandItem().setCount(1);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(
                    player, net.minecraft.world.InteractionHand.MAIN_HAND));
            helper.assertTrue(player.getMainHandItem().is(ModItems.ORDER.get()), "A single held order can be replaced even with a full inventory");
        } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(reject); }
        helper.succeed();
    }
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void frozenDaylightSchedulesOneAuction(GameTestHelper helper) {
        helper.assertTrue(MbdEconomyEvents.auctionDue(4800000), "First scheduled interval is eligible");
        for (int tick = 0; tick < 100; tick++)
            helper.assertTrue(!MbdEconomyEvents.auctionDue(4800000), "A frozen daylight clock must never flood auctions");
        helper.assertTrue(!MbdEconomyEvents.auctionDue(4800001) && MbdEconomyEvents.auctionDue(4812000),
                "Only the next half-day boundary permits another auction");
        helper.succeed();
    }
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void foodValueSyncPreservesServerPrices(GameTestHelper helper) {
        var payload = new io.github.jasonsimpart.network.SyncFoodValuesPayload(java.util.Map.of(
                ResourceLocation.parse("minecraft:apple"), 123,
                ResourceLocation.parse("minecraft:bread"), 5));
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            var codec = io.github.jasonsimpart.network.SyncFoodValuesPayload.STREAM_CODEC;
            codec.encode(buffer, payload);
            var decoded = codec.decode(buffer);
            helper.assertTrue(decoded.values().equals(payload.values()) && buffer.readableBytes() == 0,
                    "Remote tooltip prices must preserve the complete server valuation map");
        } finally { buffer.release(); }
        helper.succeed();
    }
    private static CompoundTag fruitOrder(int count) {
        var order = new CompoundTag();
        order.putString("type", "elven_tea_house");
        var entries = new ListTag();
        var entry = new CompoundTag();
        entry.putString("id", "fruit");
        entry.putInt("count", count);
        entry.putInt("minQuality", 1);
        entries.add(entry);
        order.put("entries", entries);
        return order;
    }
    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void orderQualityDiversityAndRepeatedRequirements(GameTestHelper helper) {
        var order = fruitOrder(4);
        var apple = new ItemStack(Items.APPLE, 4);
        helper.assertTrue(MbdOrders.score(order, List.of(apple)) == 1, "One fruit type at minimum quality scores one");
        helper.assertTrue(MbdOrders.score(order, List.of(new ItemStack(Items.APPLE, 2), new ItemStack(Items.SWEET_BERRIES, 2))) == 1.5,
                "Equal amounts of two fruit types must get the original 1.5 diversity bonus");
        helper.assertTrue(MbdOrders.score(order, List.of(new ItemStack(Items.APPLE, 3))) == 0 && apple.getCount() == 4,
                "Failed match must not modify submitted goods");
        var entries = order.getList("entries", net.minecraft.nbt.Tag.TAG_COMPOUND);
        entries.add(entries.getCompound(0).copy());
        helper.assertTrue(MbdOrders.score(order, List.of(apple)) == 0, "Repeated requirements must share one inventory, never double count");
        helper.assertTrue(MbdFoodEconomy.complexity(new ItemStack(Items.GOLDEN_APPLE)) == 4
                && Math.abs(MbdFoodEconomy.complexity(apple) - .52) < .0001, "Keep old complexity overrides and the exact 1.20.1 nutrition formula");
        helper.assertTrue(MbdOrders.reputationLevel(9) == 1 && MbdOrders.reputationLevel(10) == 2
                && MbdOrders.reputationLevel(99) == 5 && MbdOrders.reputationLevel(100) == 6, "Reputation thresholds remain exact");
        helper.assertTrue(Math.abs(MbdOrders.money(fruitOrder(4)) - 195.3125) < .0001, "Reward includes count/base, rarity and customer chance");
        helper.succeed();
    }

    @GameTest(batch = "mbd_single_machines", template = "mbd_single", templateNamespace = "createdelightcore")
    public static void sellBinPaysForConsumedItemsOnly(GameTestHelper helper) {
        var machine = MbdSingleMachineTests.place(helper, "sell_bin", new BlockPos(4, 2, 4));
        helper.runAfterDelay(2, () -> {
            var storage = ((com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait) machine.getTraitByName("item_slot")).storage;
            var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                    new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "SellBinTest"));
            player.getInventory().clearContent();
            storage.setStackInSlot(0, new ItemStack(Items.CARROT, 8));
            storage.setStackInSlot(1, new ItemStack(Items.CARROT, 1));
            storage.setStackInSlot(2, new ItemStack(Items.STONE, 8));
            helper.assertTrue(!MbdSellBin.sell(machine, player, (money, simulate) -> money) && storage.getStackInSlot(0).getCount() == 8,
                    "Failed payment is retryable and must retain the offered goods");
            helper.assertTrue(MbdSellBin.sell(machine, player), "A recovered payment handler permits a retry on the same day");
            helper.assertTrue(storage.getStackInSlot(0).isEmpty(), "Successful payment must consume sold food");
            helper.assertTrue(storage.getStackInSlot(1).getCount() == 1 && storage.getStackInSlot(2).getCount() == 8,
                    "Do not consume low-value stacks or non-food");
            long paid = 0;
            for (var coin : player.getInventory().items) {
                if (coin.isEmpty()) continue;
                var value = io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue.fromItemOrValue(coin.getItem(), coin.getCount());
                if (value instanceof io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue coins) paid += coins.getCoreValue() * coin.getCount();
            }
            helper.assertTrue(paid == 8, "Pay exactly eight base-value units, never pay for retained food; actual=" + paid);
            int coinCount = player.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
            MbdSellBin.sell(machine, player);
            helper.assertTrue(player.getInventory().items.stream().mapToInt(ItemStack::getCount).sum() == coinCount,
                    "A retained one-unit stack must not generate repeated free payouts");
            helper.succeed();
        });
    }

}
