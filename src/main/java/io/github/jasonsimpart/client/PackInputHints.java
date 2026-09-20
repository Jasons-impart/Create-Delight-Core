package io.github.jasonsimpart.client;

import io.github.jasonsimpart.network.MiningKeyHintPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

public final class PackInputHints {
    private static boolean mineDown;
    private static final Set<String> CASINGS = Set.of("create:andesite_casing", "create:brass_casing");
    private static final Set<String> UNCASABLE = Set.of("create:belt",
            "create:andesite_encased_shaft", "create:brass_encased_shaft");
    private PackInputHints() {}

    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !mc.isWindowActive()) { mineDown = false; return; }
        boolean down = false;
        if (ModList.get().isLoaded("bettercombat")) {
            for (var mapping : mc.options.keyMappings)
                if (mapping.getName().equals("keybinds.bettercombat.toggle_mine_with_weapons")) down = mapping.isDown();
            if (down && !mineDown) PacketDistributor.sendToServer(new MiningKeyHintPayload());
        }
        mineDown = down;
        if (mc.player.tickCount % 10 != 0 || Screen.hasAltDown() || !(mc.hitResult instanceof BlockHitResult hit)) return;
        var block = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(hit.getBlockPos()).getBlock()).toString();
        var hand = BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString();
        if (CASINGS.contains(hand) && (block.equals("create:shaft") || block.equals("create:belt")))
            mc.player.displayClientMessage(Component.translatable("message.createdelightcore.pack.chained_casing"), true);
        else if (hand.equals("create:wrench") && UNCASABLE.contains(block)
                && (block.equals("create:belt") ? !mc.player.isShiftKeyDown() : mc.player.isShiftKeyDown()))
            mc.player.displayClientMessage(Component.translatable("message.createdelightcore.pack.chained_uncasing"), true);
    }
}
