package io.github.jasonsimpart.network;

import com.mojang.datafixers.util.Pair;
import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.compat.cmr.CoolerStomachHandler;
import io.github.jasonsimpart.compat.createliquidfuel.DrainableBurnerFuelLoader;
import io.github.jasonsimpart.network.ClientFuelCache.FuelData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncFuelMapsPayload(
        Map<ResourceLocation, FuelData> burnerData,
        Map<ResourceLocation, FuelData> coolerData
) implements CustomPacketPayload {
    public static final Type<SyncFuelMapsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "sync_fuel_maps"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFuelMapsPayload> STREAM_CODEC = StreamCodec.ofMember(SyncFuelMapsPayload::write, SyncFuelMapsPayload::read);

    @Override
    public Type<SyncFuelMapsPayload> type() {
        return TYPE;
    }

    public static SyncFuelMapsPayload snapshot() {
        DrainableBurnerFuelLoader.load();
        return new SyncFuelMapsPayload(snapshotBurnerData(), snapshotCoolerData());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeMap(burnerData, (buf, id) -> buf.writeResourceLocation(id), SyncFuelMapsPayload::writeFuelData);
        buffer.writeMap(coolerData, (buf, id) -> buf.writeResourceLocation(id), SyncFuelMapsPayload::writeFuelData);
    }

    private static SyncFuelMapsPayload read(RegistryFriendlyByteBuf buffer) {
        return new SyncFuelMapsPayload(
                buffer.readMap(FriendlyByteBuf::readResourceLocation, SyncFuelMapsPayload::readFuelData),
                buffer.readMap(FriendlyByteBuf::readResourceLocation, SyncFuelMapsPayload::readFuelData)
        );
    }

    public static void handle(SyncFuelMapsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFuelCache.BURNER_MAP.clear();
            payload.burnerData.forEach((id, data) -> BuiltInRegistries.FLUID.getOptional(id)
                    .ifPresent(fluid -> ClientFuelCache.BURNER_MAP.put(fluid, data)));

            ClientFuelCache.COOLER_MAP.clear();
            payload.coolerData.forEach((id, data) -> BuiltInRegistries.FLUID.getOptional(id)
                    .ifPresent(fluid -> ClientFuelCache.COOLER_MAP.put(fluid, data)));

            if (ClientFuelCache.onUpdate != null) {
                ClientFuelCache.onUpdate.run();
            }
        });
    }

    private static Map<ResourceLocation, FuelData> snapshotBurnerData() {
        Map<ResourceLocation, FuelData> data = new HashMap<>();
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            if (fluidId != null && pair != null && pair.getSecond() != null) {
                var fuel = pair.getSecond();
                data.put(fluidId, new FuelData(fuel.getFirst(), fuel.getSecond(), fuel.getThird()));
            }
        });
        return data;
    }

    private static Map<ResourceLocation, FuelData> snapshotCoolerData() {
        Map<ResourceLocation, FuelData> data = new HashMap<>();
        for (Map.Entry<Fluid, Pair<ResourceLocation, CoolerStomachHandler.LiquidCoolerFuel>> entry : CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.entrySet()) {
            CoolerStomachHandler.LiquidCoolerFuel fuel = entry.getValue().getSecond();
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(entry.getKey());
            if (fuel != null && fluidId != null) {
                data.put(fluidId, new FuelData(fuel.burnTime(), fuel.freezing(), fuel.amountConsumedPerTick()));
            }
        }
        return data;
    }

    private static void writeFuelData(FriendlyByteBuf buffer, FuelData data) {
        buffer.writeInt(data.burnTime());
        buffer.writeBoolean(data.strongHeat());
        buffer.writeInt(data.amountConsumed());
    }

    private static FuelData readFuelData(FriendlyByteBuf buffer) {
        return new FuelData(buffer.readInt(), buffer.readBoolean(), buffer.readInt());
    }
}
