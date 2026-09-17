package io.github.jasonsimpart.mixin.minecraft;

import io.github.jasonsimpart.network.ClientCreativeTabCache;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CreativeTabsScreenPage.class, remap = false)
public abstract class CreativeTabsScreenPageMixin {
    // Default tabs (search, hotbar, inventory, operator) are appended separately by NeoForge.
    @Inject(method = "getVisibleTabs", at = @At("RETURN"), cancellable = true, remap = false)
    private void cdc$filterVisibleTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        cir.setReturnValue(ClientCreativeTabCache.filter(cir.getReturnValue()));
    }
}
