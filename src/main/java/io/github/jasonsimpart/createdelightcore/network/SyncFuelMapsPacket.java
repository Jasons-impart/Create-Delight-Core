package io.github.jasonsimpart.createdelightcore.network;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.createdelightcore.compat.cmr.CoolerStomachHandler;
import io.github.jasonsimpart.createdelightcore.compat.fluidlogistics.BlazeCoolerFuels;
import net.minecraftforge.fml.ModList;
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
    private final Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> blazeCoolerData;

    /** Server-side constructor: snapshot current server maps. */
    public SyncFuelMapsPacket() {
        this.blazeCoolerData = ModList.get().isLoaded("fluidlogistics")
                ? BlazeCoolerFuels.snapshot() : Map.of();
        this.burnerData = new HashMap<>();
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.forEach((fluid, pair) -> {
            ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
            if (rl != null && pair != null && pair.getSecond() != null) {
                var t = pair.getSecond();
                burnerData.put(rl, Triplet.of(t.getFirst(), t.getSecond(), t.getThird()));
            }
        });

        this.coolerData = new HashMap<>();
        if (ModList.get().isLoaded("cmr")) {
            CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.forEach((fluid, pair) -> {
                ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(fluid);
                if (rl != null && pair != null && pair.getSecond() != null) {
                    var t = pair.getSecond();
                    coolerData.put(rl, Triplet.of(t.getFirst(), t.getSecond(), t.getThird()));
                }
            });
        }
    }

    private SyncFuelMapsPacket(Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData,
                              Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData,
                              Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> blazeCoolerData) {
        this.burnerData = burnerData;
        this.coolerData = coolerData;
        this.blazeCoolerData = blazeCoolerData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(blazeCoolerData, FriendlyByteBuf::writeResourceLocation, (buffer, fuel) -> {
            buffer.writeInt(fuel.getFirst());
            buffer.writeBoolean(fuel.getSecond());
            buffer.writeInt(fuel.getThird());
        });
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
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> blazeCoolerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                buffer -> Triplet.of(buffer.readInt(), buffer.readBoolean(), buffer.readInt()));
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> burnerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> Triplet.of(b.readInt(), b.readBoolean(), b.readInt())
        );
        Map<ResourceLocation, Triplet<Integer, Boolean, Integer>> coolerData = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                b -> Triplet.of(b.readInt(), b.readBoolean(), b.readInt())
        );
        return new SyncFuelMapsPacket(burnerData, coolerData, blazeCoolerData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFuelCache.BLAZE_COOLER_MAP.clear();
            blazeCoolerData.forEach((id, fuel) -> {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
                if (fluid != null) {
                    ClientFuelCache.BLAZE_COOLER_MAP.put(fluid, fuel);
                }
            });
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
