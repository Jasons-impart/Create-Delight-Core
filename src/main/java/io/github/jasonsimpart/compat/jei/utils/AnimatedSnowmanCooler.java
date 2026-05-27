package io.github.jasonsimpart.compat.jei.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import fr.iglee42.cmr.cooler.SnowmanCoolerBlock.HeatLevel;
import fr.iglee42.cmr.init.CMRPartials;
import fr.iglee42.cmr.init.CMRRegistries;
import fr.iglee42.cmr.init.CMRSpriteShifts;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class AnimatedSnowmanCooler extends AnimatedKinetics {
    private HeatLevel heatLevel;

    public AnimatedSnowmanCooler withHeat(HeatLevel heatLevel) {
        this.heatLevel = heatLevel;
        return this;
    }

    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(xOffset, yOffset, 200);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5f));
        pose.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        float offset = (Mth.sin(AnimationTickHolder.getRenderTime() / 16f) + 0.5f) / 16f;

        blockElement(CMRRegistries.SNOWMAN_COOLER.getDefaultState()).atLocal(0, 1.65, 0)
                .scale(scale)
                .render(graphics);

        PartialModel snowman = heatLevel == HeatLevel.FREEZING ? CMRPartials.SNOWMAN_SUPER_ACTIVE : CMRPartials.SNOWMAN_ACTIVE;

        blockElement(snowman).atLocal(1, 1.8, 1)
                .rotate(0, 180, 0)
                .scale(scale)
                .render(graphics);

        pose.scale(scale, -scale, scale);
        pose.translate(0, -1.8, 0);

        SpriteShiftEntry spriteShift = heatLevel == HeatLevel.FREEZING ? CMRSpriteShifts.SUPER_COOLER_FLAME : CMRSpriteShifts.COOLER_FLAME;
        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();
        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        float time = AnimationTickHolder.getRenderTime(Minecraft.getInstance().level);
        float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

        double vScroll = speed * time;
        vScroll = vScroll - Math.floor(vScroll);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.cutoutMipped());
        CachedBuffers.partial(CMRPartials.SNOWMAN_FLAME, Blocks.AIR.defaultBlockState())
                .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(pose, buffer);
        pose.popPose();
    }
}
