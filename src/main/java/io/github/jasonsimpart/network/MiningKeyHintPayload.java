package io.github.jasonsimpart.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A notification request only; cannot toggle combat or grant permissions. */
public record MiningKeyHintPayload() implements CustomPacketPayload {
    public static final Type<MiningKeyHintPayload> TYPE = new Type<>(ResourceLocation.parse("createdelightcore:mining_key_hint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiningKeyHintPayload> STREAM_CODEC = StreamCodec.unit(new MiningKeyHintPayload());
    @Override public Type<MiningKeyHintPayload> type() { return TYPE; }

    public static void handle(MiningKeyHintPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!ModList.get().isLoaded("bettercombat")) return;
            var data = context.player().getPersistentData();
            var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
            if (persisted.getBoolean("createdelightcore:mine_key_hint") || data.getBoolean("betterCombatMineKey")) return;
            context.player().sendSystemMessage(Component.translatable("message.createdelightcore.pack.mine_key"));
            persisted.putBoolean("createdelightcore:mine_key_hint", true);
            data.put(Player.PERSISTED_NBT_TAG, persisted);
        });
    }
}
