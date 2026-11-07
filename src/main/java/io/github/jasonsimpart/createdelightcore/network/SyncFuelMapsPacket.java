package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.createliquidfuel.BlazeBurnerFuels;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerFuelManager;
import net.minecraftforge.fml.ModList;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

public class SyncFuelMapsPacket {

    private final Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData;
    private final Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData;

    /** Server-side constructor: snapshot current server maps. */
    public SyncFuelMapsPacket() {
        this.coolerData = SnowmanCoolerFuelManager.snapshot();
        this.burnerData = snapshotBurnerFuels();
    }

    private static Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> snapshotBurnerFuels() {
        if (!ModList.get().isLoaded("createliquidfuel")) {
            return Map.of();
        }
        try {
            return BlazeBurnerFuels.snapshot();
        } catch (Throwable t) {
            CreateDelightCore.LOGGER.warn("Skipping burner fuel sync because snapshot failed", t);
            return Map.of();
        }
    }

    private SyncFuelMapsPacket(Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData,
                              Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData) {
        this.burnerData = burnerData;
        this.coolerData = coolerData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(coolerData, FriendlyByteBuf::writeResourceLocation, (buffer, fuel) -> {
            buffer.writeInt(fuel.getFirst());
            buffer.writeBoolean(fuel.getSecond());
            buffer.writeInt(fuel.getThird());
        });
        buf.writeMap(burnerData, FriendlyByteBuf::writeResourceLocation, (b, t) -> {
            b.writeInt(t.getFirst());
            b.writeBoolean(t.getSecond());
            b.writeInt(t.getThird());
        });
    }

    public static SyncFuelMapsPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                buffer -> Triplet.of(buffer.readInt(), buffer.readBoolean(), buffer.readInt()));
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> Triplet.of(b.readInt(), b.readBoolean(), b.readInt())
        );
        return new SyncFuelMapsPacket(burnerData, coolerData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFuelCache.COOLER_MAP.clear();
            coolerData.forEach((id, fuel) -> {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
                if (fluid != null) {
                    ClientFuelCache.COOLER_MAP.put(fluid, fuel);
                }
            });
            ClientFuelCache.BURNER_MAP.clear();
            burnerData.forEach((rl, triplet) -> {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                if (fluid != null) {
                    ClientFuelCache.BURNER_MAP.put(fluid, triplet);
                }
            });

            // Notify JEI (if loaded and initialized) to add the freshly synced fuel recipes
            if (ClientFuelCache.onUpdate != null) {
                ClientFuelCache.onUpdate.run();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
