package io.github.jasonsimpart.disabled;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DisabledContentManager {
    private static final List<DisabledRule> RULES = new ArrayList<>();

    private DisabledContentManager() {
    }

    public static void clear() {
        RULES.clear();
    }

    public static void add(DisabledRule rule) {
        RULES.add(rule);
    }

    public static List<DisabledRule> rules() {
        return List.copyOf(RULES);
    }

    public static boolean shouldHideFromCreative(ItemStack stack) {
        return RULES.stream().anyMatch(rule -> rule.hasPolicy(DisablePolicy.HIDE_CREATIVE) && rule.matches(stack));
    }

    public static boolean shouldBlockUse(ItemStack stack) {
        return RULES.stream().anyMatch(rule -> rule.hasPolicy(DisablePolicy.BLOCK_USE) && rule.matches(stack));
    }

    public static boolean shouldBlockAttack(ItemStack stack) {
        return RULES.stream().anyMatch(rule -> rule.hasPolicy(DisablePolicy.BLOCK_ATTACK) && rule.matches(stack));
    }

    public static boolean shouldBlockEquip(ItemStack stack) {
        return RULES.stream().anyMatch(rule -> rule.hasPolicy(DisablePolicy.BLOCK_EQUIP) && rule.matches(stack));
    }

    public static boolean shouldBlockPlace(BlockState state) {
        return RULES.stream().anyMatch(rule -> rule.hasPolicy(DisablePolicy.BLOCK_PLACE) && rule.matches(state, null, null));
    }

    public static BlockState replacementFor(BlockState state) {
        return replacementFor(state, null, null);
    }

    public static BlockState replacementFor(BlockState state, BlockGetter level, BlockPos pos) {
        Optional<DisabledRule> rule = RULES.stream()
                .filter(candidate -> candidate.hasPolicy(DisablePolicy.REPLACE_BLOCK))
                .filter(candidate -> candidate.matches(state, level, pos))
                .findFirst();

        if (rule.isEmpty()) {
            return state;
        }

        BlockState replacement = rule.get().replacement();
        return replacement == null ? Blocks.AIR.defaultBlockState() : replacement;
    }

    public static boolean shouldRemoveRecipe(JsonElement recipe) {
        return RULES.stream()
                .filter(rule -> rule.hasPolicy(DisablePolicy.REMOVE_RECIPES))
                .anyMatch(rule -> jsonContains(rule, recipe));
    }

    private static boolean jsonContains(DisabledRule rule, JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return false;
        }

        if (element instanceof JsonPrimitive primitive && primitive.isString()) {
            return rule.matchesRecipeString(primitive.getAsString());
        }

        if (element instanceof JsonArray array) {
            for (JsonElement child : array) {
                if (jsonContains(rule, child)) {
                    return true;
                }
            }
            return false;
        }

        if (element instanceof JsonObject object) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (rule.matchesRecipeString(entry.getKey()) || jsonContains(rule, entry.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }
}
