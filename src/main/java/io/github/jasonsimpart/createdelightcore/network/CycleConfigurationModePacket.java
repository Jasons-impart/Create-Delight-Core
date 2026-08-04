package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationMode;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public record CycleConfigurationModePacket(InteractionHand hand, int direction) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeByte(Integer.signum(direction));
    }

    public static CycleConfigurationModePacket decode(FriendlyByteBuf buffer) {
        return new CycleConfigurationModePacket(buffer.readEnum(InteractionHand.class), buffer.readByte());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || direction == 0) {
                return;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof ConfigurationModuleItem)) {
                return;
            }
            Optional<ConfigurationMode> selected = ConfigurationModuleManager.cycle(stack, direction);
            if (selected.isEmpty()) {
                player.displayClientMessage(Component.translatable(
                        "item.createdelightcore.kinetic_configuration_module.error.no_modes"), true);
                return;
            }
            Item target = ForgeRegistries.ITEMS.getValue(selected.get().target());
            player.displayClientMessage(Component.translatable(
                    "item.createdelightcore.kinetic_configuration_module.message.mode",
                    target == null ? selected.get().target().toString() : target.getDescription()), true);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        });
        context.setPacketHandled(true);
    }
}
