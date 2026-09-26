package io.github.jasonsimpart.createdelightcore.content.util;

import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 在 MBD2 配方的 IN 阶段（扣料前）抓取输入品质样本，供 OUT 阶段写入产物。
 * 扣料可能发生在开跑或收尾，两阶段之间会跨 tick，因此按机器缓存而不是 ThreadLocal。
 */
public final class MBD2RecipeQualityContext {
    private static final Map<Object, List<ItemStack>> SOURCES = new WeakHashMap<>();

    private MBD2RecipeQualityContext() {
    }

    public static void capture(Object machine, List<ItemStack> sources) {
        if (machine == null) {
            return;
        }
        if (sources == null || sources.isEmpty()) {
            SOURCES.remove(machine);
            return;
        }
        List<ItemStack> copies = new ArrayList<>(sources.size());
        for (ItemStack source : sources) {
            if (!source.isEmpty()) {
                copies.add(source.copy());
            }
        }
        if (copies.isEmpty()) {
            SOURCES.remove(machine);
        } else {
            SOURCES.put(machine, Collections.unmodifiableList(copies));
        }
    }

    public static List<ItemStack> take(Object machine) {
        if (machine == null) {
            return List.of();
        }
        List<ItemStack> sources = SOURCES.remove(machine);
        return sources == null ? List.of() : sources;
    }

    public static void apply(ItemStack output, List<ItemStack> sources) {
        if (output.isEmpty() || sources == null || sources.isEmpty()) {
            return;
        }
        QualityUtils.applyQuality(output, sources, null);
    }
}
