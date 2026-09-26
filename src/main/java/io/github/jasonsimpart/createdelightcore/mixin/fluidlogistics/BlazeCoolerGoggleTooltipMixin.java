package io.github.jasonsimpart.createdelightcore.mixin.fluidlogistics;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.yision.fluidlogistics.content.processing.blazeCooler.BlazeCoolerBlockEntity;
import com.yision.fluidlogistics.content.processing.blazeCooler.BlazeCoolerFuelManager;
import io.github.jasonsimpart.createdelightcore.util.GoggleBurnTimeTicker;
import io.github.jasonsimpart.createdelightcore.util.GoggleTooltipStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * CreateCyberGoggles 给所有 BlazeBurnerBlockEntity 挂了「燃烧室状态」tooltip，
 * 且把 500 tick 写死成 500 秒。冷却室继承燃烧室，会显示错误的加热读数。
 *
 * 冷却室的 remainingBurnTime 只是一次投料的周期（细雪 cool_time 仅 10 tick），
 * 不能直接当剩余冷却时间。应按燃料罐中流体换算总冷却时长。
 *
 * 缩进用 GoggleTooltipStyle（与 CCG getIndents 同公式），避免 ModernUI 下图标遮挡。
 */
@Mixin(value = BlazeCoolerBlockEntity.class, remap = false)
public abstract class BlazeCoolerGoggleTooltipMixin {
    @Shadow(remap = false)
    private SmartFluidTank fuelTank;

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BlazeCoolerBlockEntity self = (BlazeCoolerBlockEntity) (Object) this;
        int remainingBurnTime = self.getRemainingBurnTime();
        boolean isCreative = self.isCreative();
        if (isCreative) {
            GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.coolerState")
                    .withStyle(ChatFormatting.GOLD));
            GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.leftTime")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("∞").withStyle(ChatFormatting.AQUA))
                    .append(Component.translatable("createdelightcore.tooltip.seconds")
                            .withStyle(ChatFormatting.DARK_GRAY)));
            return true;
        }

        long totalTicks = Math.max(remainingBurnTime, 0);
        if (fuelTank != null && !fuelTank.isEmpty()) {
            FluidStack fluid = fuelTank.getFluid();
            BlazeCoolerFuelManager.Fuel fuel = BlazeCoolerFuelManager.find(fluid);
            if (fuel != null && fuel.amount() > 0) {
                totalTicks += (long) (fluid.getAmount() / fuel.amount()) * fuel.coolTime();
            }
        }
        if (totalTicks <= 0) {
            return false;
        }

        long displayTicks = GoggleBurnTimeTicker.displayTicks(self.getLevel(), self.getBlockPos(), totalTicks);
        if (displayTicks <= 0) {
            return false;
        }

        GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.coolerState")
                .withStyle(ChatFormatting.GOLD));
        GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.leftTime")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(displayTicks / 20))
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("createdelightcore.tooltip.seconds")
                        .withStyle(ChatFormatting.DARK_GRAY)));
        return true;
    }
}
