package io.github.jasonsimpart.createdelightcore.event;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.network.ClientFuelCache;
import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateDelightCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipEvent {
    public static String formatTime(int ticks) {
        if (ticks >= 20 * 60)
            return (ticks / (20 * 60)) + " m";
        if (ticks >= 20)
            return (ticks / 20) + " s";
        return (ticks) + " t";
    }
    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof BucketItem bucket){
            Fluid fluid = bucket.getFluid();
            // 烈焰人燃料桶提示
            Triplet<Integer, Boolean, Integer> burnerInfo = ClientFuelCache.BURNER_MAP.get(fluid);
            if(burnerInfo != null){
                Integer burnTime = burnerInfo.getFirst();
                Boolean isSuperHeat = burnerInfo.getSecond();
                Integer amountConsume = burnerInfo.getThird();
                if(burnTime != null && isSuperHeat != null && amountConsume != null){
                    if( Screen.hasShiftDown()) {
                        var heatType = isSuperHeat
                                ? Component.translatable("tooltip." + CreateDelightCore.MODID + ".superHeat").withStyle(ChatFormatting.BLUE)
                                : Component.translatable("tooltip." + CreateDelightCore.MODID + ".Heat").withStyle(ChatFormatting.GOLD);
                        var burnTimeComponent = Component.literal(formatTime(burnTime)).withStyle(ChatFormatting.GOLD);
                        var amountConsumeComponent = Component.literal(amountConsume.toString() + " mB").withStyle(ChatFormatting.GOLD);
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".holdShiftHeat"));
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".burnTime").append(burnTimeComponent));
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".amountConsume").append(amountConsumeComponent));
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".heatType").append(heatType));
                    }
                    else{
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".holdShiftToSeeHeat"));
                    }
                }
            }
            // 雪傀儡冷却剂桶提示
            Triplet<Integer, Boolean, Integer> coolerInfo = ClientFuelCache.COOLER_MAP.get(fluid);
            if(coolerInfo != null){
                Integer coolTime = coolerInfo.getFirst();
                Boolean isSuperCool = coolerInfo.getSecond();
                Integer amountConsume = coolerInfo.getThird();
                if(coolTime != null && isSuperCool != null && amountConsume != null){
                    if( Screen.hasControlDown()) {
                        var coolType = isSuperCool
                                ? Component.translatable("tooltip." + CreateDelightCore.MODID + ".Frozen").withStyle(ChatFormatting.BLUE)
                                : Component.translatable("tooltip." + CreateDelightCore.MODID + ".Cooled").withStyle(ChatFormatting.AQUA);
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".holdControlCool"));
                        var coolTimeComponent = Component.literal(formatTime(coolTime)).withStyle(ChatFormatting.GOLD);
                        var amountConsumeComponent = Component.literal(amountConsume.toString() + " mB").withStyle(ChatFormatting.GOLD);
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".coolTime").append(coolTimeComponent));
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".amountConsume").append(amountConsumeComponent));
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".coolType").append(coolType));
                    }
                    else{
                        event.getToolTip().add(Component.translatable("tooltip." + CreateDelightCore.MODID + ".holdControlToSeeCool"));
                    }
                }
            }
        }
    }
}

