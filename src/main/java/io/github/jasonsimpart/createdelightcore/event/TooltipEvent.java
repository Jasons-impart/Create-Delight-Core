package io.github.jasonsimpart.createdelightcore.event;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.cmr.CoolerStomachHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipEvent {
    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof BucketItem bucket){
            var fluid = bucket.getFluid();
            // 烈焰人燃料桶提示
            if(BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.containsKey(fluid)){
                var infoPair = BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.get(fluid);
                if(infoPair != null){
                    var info = infoPair.getSecond();
                    if(info != null){
                        Integer burnTime = info.getFirst();
                        Boolean isSuperHeat = info.getSecond();
                        Integer amountConsume = info.getThird();
                        if(burnTime != null && isSuperHeat != null && amountConsume != null){
                            if( Screen.hasShiftDown()) {
                                var heatType = Component.translatable(CreateDelightCore.MODID + ".tooltip.heatType." + (isSuperHeat ? "superHeat" : "normalHeat"));

                                var heatColor = isSuperHeat ? ChatFormatting.BLUE : ChatFormatting.YELLOW;
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.burnTime", burnTime).withStyle(ChatFormatting.GRAY));
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.amountConsume", amountConsume).withStyle(ChatFormatting.GRAY));
                                event.getToolTip().add(heatType.withStyle(heatColor));}
                            else{
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.shiftHeat").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                            }
                        }
                    }
                }
            }
            // 雪傀儡冷却剂桶提示
            else if(CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.containsKey(fluid)){
                var infoPair = CoolerStomachHandler.LIQUID_COOLER_FUEL_MAP.get(fluid);
                if(infoPair != null){
                    var info = infoPair.getSecond();
                    if(info != null){
                        Integer coolTime = info.getFirst();
                        Boolean isSuperCool = info.getSecond();
                        Integer amountConsume = info.getThird();
                        if(coolTime != null && isSuperCool != null && amountConsume != null){
                            if( Screen.hasShiftDown()) {
                                var heatType = Component.translatable(CreateDelightCore.MODID + ".tooltip.coolType." + (isSuperCool ? "superCool" : "normalCool"));
                                var heatColor = isSuperCool ? ChatFormatting.BLUE : ChatFormatting.YELLOW;
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.coolTime", coolTime).withStyle(ChatFormatting.GRAY));
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.amountConsume", amountConsume).withStyle(ChatFormatting.GRAY));
                                event.getToolTip().add(heatType.withStyle(heatColor));}
                            else{
                                event.getToolTip().add(Component.translatable(CreateDelightCore.MODID + ".tooltip.shiftCool").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                            }
                        }
                    }
                }
            }
        }
    }
}
