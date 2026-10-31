package io.github.jasonsimpart.createdelightcore.util;

import io.github.jasonsimpart.createdelightcore.compat.createcybergoggles.CyberGogglesFluidTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * CreateCyberGoggles 浮层左侧有图标列，其 {@code CCGLangBuilder.forGoggles} 会按
 * {@code getIndents(font, 4)} 预留空格。ModernUI 下字体度量会变，写死空格仍会遮挡，
 * 这里复用同一换算公式。
 */
public final class GoggleTooltipStyle {
    private static final float DEFAULT_SPACE_WIDTH = 4.0f;
    /** CCG 基准为 4，这里 +1 给 ModernUI 图标列留出余量。 */
    private static final int DEFAULT_INDENTS = 5;

    private GoggleTooltipStyle() {
    }

    public static String indent() {
        Font font = Minecraft.getInstance().font;
        int spaceWidth = font.width(" ");
        int indents = DEFAULT_INDENTS;
        if (spaceWidth != (int) DEFAULT_SPACE_WIDTH) {
            indents = Math.round(DEFAULT_SPACE_WIDTH * DEFAULT_INDENTS / spaceWidth);
        }
        return " ".repeat(Math.max(indents, 0));
    }

    public static void addLine(List<Component> tooltip, Component line) {
        tooltip.add(Component.literal(indent()).append(line));
    }

    public static void addFluidLine(List<Component> tooltip, FluidStack fluid, int capacity) {
        if (CyberGogglesFluidTooltip.add(tooltip, fluid, capacity, DEFAULT_INDENTS - 4)) return;
        var numberFormat = java.text.NumberFormat.getNumberInstance();
        addLine(tooltip, fluid.getDisplayName().copy()
                .append(" " + numberFormat.format(fluid.getAmount()) + "mB / "
                        + numberFormat.format(capacity) + "mB"));
    }
}
