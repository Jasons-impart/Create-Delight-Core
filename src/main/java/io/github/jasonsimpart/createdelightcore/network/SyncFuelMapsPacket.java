package io.github.jasonsimpart.createdelightcore.network;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.createdelightcore.compat.cmr.CoolerStomachHandler;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncFuelMapsPacket {

    private final Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData;
    private final Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData;

    /** Server-side constructor: snapshot current server maps. */
    public SyncFuelMapsPacket() {
        this.burnerData = new HashMap<>();
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null && pair != null && pair.getSecond() != null) {
                var t = pair.getSecond();
                burnerData.put(rl, Triplet.of(t.getFirst(), t.getSecond(), t.getThird()));
            }
        });

        this.coolerData = new HashMap<>();
        CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.forEach((fluid, pair) -> {
            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null && pair != null && pair.getSecond() != null) {
                var t = pair.getSecond();
                coolerData.put(rl, Triplet.of(t.getFirst(), t.getSecond(), t.getThird()));
            }
        });
    }

    private SyncFuelMapsPacket(Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData,
                                Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData) {
        this.burnerData = burnerData;
        this.coolerData = coolerData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(burnerData, FriendlyByteBuf::writeResourceLocation, (b, t) -> {
            b.writeInt(t.getFirst());
            b.writeBoolean(t.getSecond());
            b.writeInt(t.getThird());
        });
        buf.writeMap(coolerData, FriendlyByteBuf::writeResourceLocation, (b, t) -> {
            b.writeInt(t.getFirst());
            b.writeBoolean(t.getSecond());
            b.writeInt(t.getThird());
        });
    }

    public static SyncFuelMapsPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> Triplet.of(b.readInt(), b.readBoolean(), b.readInt())
        );
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> Triplet.of(b.readInt(), b.readBoolean(), b.readInt())
        );
        return new SyncFuelMapsPacket(burnerData, coolerData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFuelCache.BURNER_MAP.clear();
            burnerData.forEach((rl, triplet) -> {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                if (fluid != null) {
                    ClientFuelCache.BURNER_MAP.put(fluid, triplet);
                }
            });

            ClientFuelCache.COOLER_MAP.clear();
            coolerData.forEach((rl, triplet) -> {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                if (fluid != null) {
                    ClientFuelCache.COOLER_MAP.put(fluid, triplet);
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
