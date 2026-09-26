package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.createliquidfuel.BlazeBurnerFuels;
import io.github.jasonsimpart.createdelightcore.util.GoggleBurnTimeTicker;
import io.github.jasonsimpart.createdelightcore.util.GoggleTooltipStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * CreateCyberGoggles 的燃烧室 goggle 行把总时长写死成 500，并只读 remainingBurnTime，
 * 液体燃料燃烧时会显示成 0/500。这里在方法入口接管，按燃料周期 + 胃囊流体重算剩余时间。
 *
 * 冷却室子类有自己的 addToGoggleTooltip，虚拟分发不会走到这里。
 */
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false, priority = 500)
public abstract class BlazeBurnerGoggleTooltipMixin {
    @Shadow(remap = false)
    public boolean isCreative;

    @Shadow(remap = false)
    protected int remainingBurnTime;

    @Shadow(remap = false)
    protected FuelType activeFuel;

    @Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void createdelightcore$replaceBurnerTooltip(List<Component> tooltip, boolean isPlayerSneaking,
                                                       CallbackInfoReturnable<Boolean> cir) {
        BlazeBurnerBlockEntity self = (BlazeBurnerBlockEntity) (Object) this;
        int remaining = remainingBurnTime;
        if (ModList.get().isLoaded("createliquidfuel")) {
            try {
                FluidStack liquid = readStomachFluid(self);
                remaining = (int) Math.min(Integer.MAX_VALUE, BlazeBurnerFuels.totalBurnTicks(remainingBurnTime, liquid));
            } catch (Throwable t) {
                CreateDelightCore.LOGGER.warn("Burner liquid fuel tooltip fallback", t);
            }
        }

        if (!isCreative && remaining <= 0) {
            cir.setReturnValue(false);
            return;
        }

        long displayTicks = isCreative
                ? Long.MAX_VALUE
                : GoggleBurnTimeTicker.displayTicks(self.getLevel(), self.getBlockPos(), remaining);
        if (!isCreative && displayTicks <= 0) {
            cir.setReturnValue(false);
            return;
        }

        GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.burnerState")
                .withStyle(stateColor()));
        GoggleTooltipStyle.addLine(tooltip, Component.translatable("createdelightcore.tooltip.leftTime")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(isCreative ? "∞" : String.valueOf(displayTicks / 20))
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("createdelightcore.tooltip.seconds")
                        .withStyle(ChatFormatting.DARK_GRAY)));
        cir.setReturnValue(true);
    }

    private ChatFormatting stateColor() {
        if (isCreative) {
            return ChatFormatting.AQUA;
        }
        return switch (activeFuel) {
            case NONE -> ChatFormatting.AQUA;
            case NORMAL -> ChatFormatting.GOLD;
            case SPECIAL -> ChatFormatting.DARK_PURPLE;
        };
    }

    private static FluidStack readStomachFluid(BlazeBurnerBlockEntity self) {
        return self.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .map(h -> h.getTanks() > 0 ? h.getFluidInTank(0) : FluidStack.EMPTY)
                .orElse(FluidStack.EMPTY);
    }
}
