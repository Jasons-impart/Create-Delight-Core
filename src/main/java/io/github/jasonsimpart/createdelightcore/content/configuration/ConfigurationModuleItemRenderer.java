package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ConfigurationModuleItemRenderer extends BlockEntityWithoutLevelRenderer {
    public ConfigurationModuleItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ConfigurationModuleManager.getSnapshotTarget(stack).ifPresent(target ->
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        new ItemStack(target), displayContext, packedLight, packedOverlay,
                        poseStack, buffer, Minecraft.getInstance().level, 0));
    }
}
