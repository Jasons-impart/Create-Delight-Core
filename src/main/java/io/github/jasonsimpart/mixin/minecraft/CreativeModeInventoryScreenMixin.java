package io.github.jasonsimpart.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.jasonsimpart.network.ClientCreativeTabCache;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    @Shadow(remap = false)
    private CreativeTabsScreenPage currentPage;
    @Shadow
    private static CreativeModeTab selectedTab;
    @Shadow
    private List<Slot> originalSlots;

    // Filter before pagination, so disabled categories do not leave empty pages or gaps.
    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/common/CreativeModeTabRegistry;getSortedCreativeModeTabs()Ljava/util/List;", remap = false))
    private List<CreativeModeTab> cdc$filterTabs(List<CreativeModeTab> tabs) {
        return ClientCreativeTabCache.filter(tabs);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void cdc$closeIfAllTabsHidden(CallbackInfo ci) {
        // NeoForge init selects getVisibleTabs().get(0), which requires at least one visible tab.
        if (BuiltInRegistries.CREATIVE_MODE_TAB.stream()
                .noneMatch(tab -> tab.shouldDisplay() && !ClientCreativeTabCache.isHidden(tab))) {
            ((CreativeModeInventoryScreen) (Object) this).onClose();
            ci.cancel();
            return;
        }
        // init changes selectedTab before selectTab can restore the inventory tab's wrapped slots.
        if (originalSlots != null && ClientCreativeTabCache.isHidden(selectedTab)) {
            var menu = ((CreativeModeInventoryScreen) (Object) this).getMenu();
            menu.slots.clear();
            menu.slots.addAll(originalSlots);
            originalSlots = null;
        }
    }

    @ModifyVariable(method = "selectTab", at = @At("HEAD"), argsOnly = true)
    private CreativeModeTab cdc$avoidHiddenSelection(CreativeModeTab tab) {
        // Also covers vanilla's search shortcut and fallback to the default category.
        if (ClientCreativeTabCache.isHidden(tab)) {
            return currentPage.getVisibleTabs().stream().findFirst().orElse(tab);
        }
        return tab;
    }
}
