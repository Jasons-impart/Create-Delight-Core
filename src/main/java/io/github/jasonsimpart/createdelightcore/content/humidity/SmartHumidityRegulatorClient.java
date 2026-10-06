package io.github.jasonsimpart.createdelightcore.content.humidity;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class SmartHumidityRegulatorClient {
    private SmartHumidityRegulatorClient() { }

    public static void rotateBottomLabel(com.mojang.blaze3d.vertex.PoseStack poseStack) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return;
        float angle = net.createmod.catnip.math.AngleHelper.horizontalAngle(player.getDirection()) + 180;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
    }

    public static void registerTooltip(net.minecraft.world.item.Item item) {
        com.simibubi.create.foundation.item.TooltipModifier.REGISTRY.register(item,
                new com.simibubi.create.foundation.item.ItemDescription.Modifier(item,
                        net.createmod.catnip.lang.FontHelper.Palette.STANDARD_CREATE)
                        .andThen(com.simibubi.create.foundation.item.KineticStats.create(item)));
    }

    public static void open(ScrollValueBehaviour behaviour, BlockHitResult hit, Player player) {
        ScreenOpener.open(new ValueSettingsScreen(hit.getBlockPos(), behaviour.createBoard(player, hit),
                behaviour.getValueSettings(), behaviour::newSettingHovered, behaviour.netId()) {
            @Override
            public com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings getClosestCoordinate(int mouseX, int mouseY) {
                var closest = super.getClosestCoordinate(mouseX, mouseY);
                return new com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings(0,
                        SmartHumidityRegulatorBlockEntity.guiHumidity(closest.value())
                                * SmartHumidityRegulatorBlockEntity.GUI_LEVEL_SPACING);
            }
        });
    }
}
