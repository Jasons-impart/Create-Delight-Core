package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class SmartHumidityRegulatorClient {
    private SmartHumidityRegulatorClient() { }

    public static void open(ScrollValueBehaviour behaviour, BlockHitResult hit, Player player) {
        ScreenOpener.open(new ValueSettingsScreen(hit.getBlockPos(), behaviour.createBoard(player, hit),
                behaviour.getValueSettings(), behaviour::newSettingHovered, behaviour.netId()));
    }
}
