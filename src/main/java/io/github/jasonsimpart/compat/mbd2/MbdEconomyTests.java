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
    public static void deliveryCatchesUpAfterMissedDeadline(GameTestHelper helper) {
        var pos = new BlockPos(4, 2, 4);
        var machine = MbdSingleMachineTests.place(helper, "order_deliverer", pos);
        var tablePos = pos.east();
        helper.setBlock(tablePos.below(), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(tablePos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:white_table_cloth")).defaultBlockState()
                .setValue(com.simibubi.create.content.logistics.tableCloth.TableClothBlock.HAS_BE, true));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getGameTime() % 20 == 0,
                "Wait for the bounded settlement retry interval")).thenExecute(() -> {
            var cloth = (TableClothBlockEntity) helper.getBlockEntity(tablePos);
            var order = ModItems.ORDER.toStack();
            var data = new CompoundTag();
            data.put("createdelightOrderInfo", fruitOrder(4));
            order.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            cloth.manuallyAddedItems.add(order);
            cloth.manuallyAddedItems.add(PackageItem.containing(List.of(new ItemStack(Items.APPLE, 4))));
            helper.setBlock(pos.west().below(), net.minecraft.world.level.block.Blocks.STONE);
            helper.setBlock(pos.west(), helper.getBlockState(tablePos));
            var pendingCloth = (TableClothBlockEntity) helper.getBlockEntity(pos.west());
            var pendingOrder = ModItems.ORDER.toStack();
            var owned = fruitOrder(4);
            owned.putString("ownerUUID", java.util.UUID.randomUUID().toString());
            var pendingData = new CompoundTag();
            pendingData.put("createdelightOrderInfo", owned);
            pendingOrder.set(DataComponents.CUSTOM_DATA, CustomData.of(pendingData));
            pendingCloth.manuallyAddedItems.add(pendingOrder);
            pendingCloth.manuallyAddedItems.add(PackageItem.containing(List.of(new ItemStack(Items.APPLE, 4))));
            long previous = helper.getLevel().getDayTime();
            try {
                helper.getLevel().setDayTime(2000);
                var tick = new com.lowdragmc.mbd2.common.machine.definition.config.event.MachineTickEvent(machine);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(tick);
                helper.assertTrue(!machine.getCustomData().contains("settledDay") && !cloth.manuallyAddedItems.contains(order)
                        && pendingCloth.manuallyAddedItems.contains(pendingOrder),
                        "A ready segment settles after the deadline without preventing an offline segment from retrying");
                var replenished = order.copy();
                cloth.manuallyAddedItems.clear();
                cloth.manuallyAddedItems.add(replenished);
                cloth.manuallyAddedItems.add(PackageItem.containing(List.of(new ItemStack(Items.APPLE, 4))));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(tick);
                helper.assertTrue(cloth.manuallyAddedItems.contains(replenished) && !machine.getCustomData().contains("settledDay"),
                        "Replenishing a completed row must not pay it again while another row is pending");
                pendingData.put("createdelightOrderInfo", fruitOrder(4));
                pendingOrder.set(DataComponents.CUSTOM_DATA, CustomData.of(pendingData));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(tick);
                helper.assertTrue(machine.getCustomData().contains("settledDay") && !pendingCloth.manuallyAddedItems.contains(pendingOrder),
                        "A previously pending segment can complete on the same day");
                cloth.manuallyAddedItems.add(ModItems.ORDER.toStack());
                int size = cloth.manuallyAddedItems.size();
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(tick);
                helper.assertTrue(cloth.manuallyAddedItems.size() == size, "A successful day's delivery must not repeat");
            } finally { helper.getLevel().setDayTime(previous); }
            helper.succeed();
        });
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
    public static void deliveryPreservesFailedOrderAndReplacesSuccessfulParcels(GameTestHelper helper) {
        var pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:white_table_cloth")).defaultBlockState().setValue(com.simibubi.create.content.logistics.tableCloth.TableClothBlock.HAS_BE, true));
        helper.runAfterDelay(2, () -> {
            var cloth = (TableClothBlockEntity) helper.getBlockEntity(pos);
            var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ModItems.ORDER_DELIVERER_ITEM.toStack());
            var denied = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player,
                    net.minecraft.world.InteractionHand.MAIN_HAND, helper.absolutePos(pos),
                    new net.minecraft.world.phys.BlockHitResult(helper.absolutePos(pos).getCenter(), net.minecraft.core.Direction.UP,
                            helper.absolutePos(pos), false));
            denied.setUseBlock(net.neoforged.neoforge.common.util.TriState.FALSE);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(denied);
            helper.assertTrue(helper.getBlockEntity(pos) == cloth && player.getMainHandItem().getCount() == 1,
                    "Denied block use preserves both the table cloth and deliverer item");
            var order = ModItems.ORDER.toStack();
            var data = new CompoundTag();
            data.put("createdelightOrderInfo", fruitOrder(4));
            order.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            var parcel = PackageItem.containing(List.of(new ItemStack(Items.APPLE, 3)));
            cloth.manuallyAddedItems.add(order);
            cloth.manuallyAddedItems.add(parcel);
            helper.assertTrue(!MbdOrderDelivery.settleSegment(helper.getLevel(), List.of(cloth), order), "Missing goods must reject settlement");
            helper.assertTrue(cloth.manuallyAddedItems.size() == 2 && cloth.manuallyAddedItems.get(0) == order,
                    "Rejected settlement must preserve the order and parcels");
            cloth.manuallyAddedItems.set(1, PackageItem.containing(List.of(new ItemStack(Items.APPLE, 7), new ItemStack(Items.DIAMOND, 2))));
            PackageItem.addAddress(cloth.manuallyAddedItems.get(1), "Return address");
            var decoration = new ItemStack(Items.EMERALD);
            var otherOrder = ModItems.ORDER.toStack();
            cloth.manuallyAddedItems.add(decoration);
            cloth.manuallyAddedItems.add(otherOrder);
            var owned = fruitOrder(4);
            owned.putString("ownerUUID", java.util.UUID.randomUUID().toString());
            data.put("createdelightOrderInfo", owned);
            order.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            helper.assertTrue(!MbdOrderDelivery.settleSegment(helper.getLevel(), List.of(cloth), order)
                    && cloth.manuallyAddedItems.size() == 4, "An offline owner's order must wait without consuming goods or reputation");
            data.put("createdelightOrderInfo", fruitOrder(4));
            order.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            helper.assertTrue(MbdOrderDelivery.settleSegment(helper.getLevel(), List.of(cloth), order), "Complete order must settle");
            helper.assertTrue(cloth.manuallyAddedItems.contains(decoration) && cloth.manuallyAddedItems.contains(otherOrder),
                    "Unrelated display items and orders must survive successful settlement");
            var leftovers = PackageItem.getContents(cloth.manuallyAddedItems.getFirst());
            helper.assertTrue(PackageItem.getAddress(cloth.manuallyAddedItems.getFirst()).equals("Return address"),
                    "Partially consumed parcels retain their original address");
            int apples = 0, diamonds = 0;
            for (int slot = 0; slot < leftovers.getSlots(); slot++) {
                var stack = leftovers.getStackInSlot(slot);
                if (stack.is(Items.APPLE)) apples += stack.getCount();
                if (stack.is(Items.DIAMOND)) diamonds += stack.getCount();
            }
            helper.assertTrue(apples == 3 && diamonds == 2, "Return surplus goods and unrelated parcel contents exactly");
            // A full first display routes rewards to entities; cancellation must preserve all submitted goods.
            cloth.manuallyAddedItems.clear();
            for (int i = 0; i < 4; i++) cloth.manuallyAddedItems.add(new ItemStack(Items.EMERALD));
            var nextPos = pos.east();
            helper.setBlock(nextPos.below(), net.minecraft.world.level.block.Blocks.STONE);
            helper.setBlock(nextPos, helper.getBlockState(pos));
            var next = (TableClothBlockEntity) helper.getBlockEntity(nextPos);
            var overflowOrder = ModItems.ORDER.toStack();
            overflowOrder.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            var submitted = PackageItem.containing(List.of(new ItemStack(Items.APPLE, 4)));
            next.manuallyAddedItems.add(overflowOrder);
            next.manuallyAddedItems.add(submitted);
            java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof com.simibubi.create.content.logistics.box.PackageEntity) event.setCanceled(true);
            };
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(reject);
            try {
                helper.assertTrue(!MbdOrderDelivery.settleSegment(helper.getLevel(), List.of(cloth, next), overflowOrder),
                        "Rejected overflow reward spawning must reject settlement");
                helper.assertTrue(next.manuallyAddedItems.contains(overflowOrder) && next.manuallyAddedItems.contains(submitted)
                        && cloth.manuallyAddedItems.size() == 4, "Payout failure preserves original orders, parcels, and displays");
            } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(MbdOrderDelivery.settleSegment(helper.getLevel(), List.of(cloth, next), overflowOrder),
                    "Settlement may retry once overflow reward spawning is permitted");
            helper.succeed();
        });
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
