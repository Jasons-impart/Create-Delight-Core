package io.github.jasonsimpart.client.waystones;

import io.github.jasonsimpart.compat.waystones.WaystoneMoneyRequirement;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.blay09.mods.waystones.client.requirement.RequirementClientRegistry;
import net.blay09.mods.waystones.client.requirement.RequirementRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WaystoneMoneyRequirementRenderer implements RequirementRenderer<WaystoneMoneyRequirement> {
    private WaystoneMoneyRequirementRenderer() {
    }

    public static void register() {
        RequirementClientRegistry.registerRenderer(WaystoneMoneyRequirement.class, new WaystoneMoneyRequirementRenderer());
    }

    @Override
    public void renderWidget(Player player, WaystoneMoneyRequirement requirement, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, int x, int y) {
        if (!requirement.canAfford(player)) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        }

        List<ItemStack> coins = coins(requirement);
        for (int i = 0; i < coins.size(); i++) {
            ItemStack coin = coins.get(i);
            int coinX = x + i * 16;
            guiGraphics.renderItem(coin, coinX, y, 16, 16);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, coin, coinX, y);
        }

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public int getWidth(Player player, WaystoneMoneyRequirement requirement) {
        return Math.max(16, coins(requirement).size() * 16);
    }

    @Override
    public int getOrder() {
        return 9;
    }

    private static List<ItemStack> coins(WaystoneMoneyRequirement requirement) {
        if (requirement.cost() instanceof CoinValue coinValue) {
            return coinValue.getAsItemList();
        }
        return List.of();
    }
}
