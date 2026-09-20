package io.github.jasonsimpart.util;

import net.minecraft.world.item.ItemStack;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

/** Capacitated matching: overlapping requirements can reassign earlier choices without consuming items. */
public final class ItemAllocation {
    private ItemAllocation() {}

    /** Returns counts indexed by requirement then inventory slot, or null if no complete allocation exists. */
    public static int[][] allocate(List<ItemStack> inventory, int[] required, BiPredicate<Integer, ItemStack> accepts) {
        int slots = inventory.size(), source = slots + required.length, sink = source + 1;
        int[][] residual = new int[sink + 1][sink + 1];
        for (int slot = 0; slot < slots; slot++) {
            var stack = inventory.get(slot);
            residual[source][slot] = stack.getCount();
            for (int entry = 0; entry < required.length; entry++) {
                if (!stack.isEmpty() && accepts.test(entry, stack)) residual[slot][slots + entry] = stack.getCount();
            }
        }
        for (int entry = 0; entry < required.length; entry++) {
            if (required[entry] < 0) return null;
            residual[slots + entry][sink] = required[entry];
        }
        int[] parent = new int[sink + 1];
        while (true) {
            Arrays.fill(parent, -1);
            parent[source] = source;
            var queue = new ArrayDeque<Integer>();
            queue.add(source);
            while (!queue.isEmpty() && parent[sink] == -1) {
                int from = queue.removeFirst();
                for (int to = 0; to <= sink; to++) if (parent[to] == -1 && residual[from][to] > 0) {
                    parent[to] = from;
                    queue.addLast(to);
                }
            }
            if (parent[sink] == -1) break;
            int count = Integer.MAX_VALUE;
            for (int to = sink; to != source; to = parent[to]) count = Math.min(count, residual[parent[to]][to]);
            for (int to = sink; to != source; to = parent[to]) {
                residual[parent[to]][to] -= count;
                residual[to][parent[to]] += count;
            }
        }
        int[][] result = new int[required.length][slots];
        for (int entry = 0; entry < required.length; entry++) {
            if (residual[slots + entry][sink] != 0) return null;
            for (int slot = 0; slot < slots; slot++) result[entry][slot] = residual[slots + entry][slot];
        }
        return result;
    }
}
