package io.github.jasonsimpart.compat.kubejs.disabled;

import dev.latvian.mods.kubejs.util.ID;
import io.github.jasonsimpart.disabled.DisablePolicy;
import io.github.jasonsimpart.disabled.DisabledContentManager;
import io.github.jasonsimpart.disabled.DisabledRule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class DisabledRuleBuilder {
    private final DisabledRule rule;

    private DisabledRuleBuilder(DisabledRule rule) {
        this.rule = rule;
        DisabledContentManager.add(rule);
    }

    private DisabledRuleBuilder() {
        this.rule = null;
    }

    static DisabledRuleBuilder item(DisabledRule rule) {
        return new DisabledRuleBuilder(rule);
    }

    static DisabledRuleBuilder block(DisabledRule rule) {
        return new DisabledRuleBuilder(rule);
    }

    public DisabledRuleBuilder removeRecipes() {
        return policy(DisablePolicy.REMOVE_RECIPES);
    }

    public DisabledRuleBuilder hideFromCreativeTabs() {
        return policy(DisablePolicy.HIDE_CREATIVE);
    }

    public DisabledRuleBuilder blockUse() {
        return policy(DisablePolicy.BLOCK_USE);
    }

    public DisabledRuleBuilder blockPlace() {
        return policy(DisablePolicy.BLOCK_PLACE);
    }

    public DisabledRuleBuilder blockAttack() {
        return policy(DisablePolicy.BLOCK_ATTACK);
    }

    public DisabledRuleBuilder blockEquip() {
        return policy(DisablePolicy.BLOCK_EQUIP);
    }

    public DisabledRuleBuilder replaceGeneratedWith(Object id) {
        ResourceLocation blockId = ID.mc(id);
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        BlockState replacement = block == Blocks.AIR ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
        rule.replacement(replacement);
        return this;
    }

    public DisabledRuleBuilder replaceWith(Object id) {
        return replaceGeneratedWith(id);
    }

    protected DisabledRuleBuilder policy(DisablePolicy policy) {
        rule.policy(policy);
        return this;
    }

    public static final class Group extends DisabledRuleBuilder {
        private final List<DisabledRuleBuilder> children = new ArrayList<>();

        Group() {
            super();
        }

        void add(DisabledRuleBuilder child) {
            children.add(child);
        }

        @Override
        protected DisabledRuleBuilder policy(DisablePolicy policy) {
            children.forEach(child -> child.policy(policy));
            return this;
        }

        @Override
        public DisabledRuleBuilder replaceGeneratedWith(Object id) {
            children.forEach(child -> child.replaceGeneratedWith(id));
            return this;
        }
    }
}
