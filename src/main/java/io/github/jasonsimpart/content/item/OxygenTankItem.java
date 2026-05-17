package io.github.jasonsimpart.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class OxygenTankItem extends Item {
    private final String oxygenTooltipKey;
    private final String environmentTooltipKey;

    public OxygenTankItem(Properties properties, String oxygenTooltipKey, String environmentTooltipKey) {
        super(properties.stacksTo(1));
        this.oxygenTooltipKey = oxygenTooltipKey;
        this.environmentTooltipKey = environmentTooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(oxygenTooltipKey).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(environmentTooltipKey).withStyle(ChatFormatting.YELLOW));
    }
}
