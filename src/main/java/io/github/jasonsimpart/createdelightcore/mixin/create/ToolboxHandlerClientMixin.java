package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import io.github.jasonsimpart.createdelightcore.content.configuration.ConfigurationModuleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ToolboxHandlerClient.class, remap = false)
public class ToolboxHandlerClientMixin {
    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$preferConfigurationMenu(int key, boolean pressed, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !AllKeys.TOOLBELT.doesModifierAndCodeMatch(key)) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof ConfigurationModuleItem
                || player.getOffhandItem().getItem() instanceof ConfigurationModuleItem) {
            ci.cancel();
        }
    }
}
