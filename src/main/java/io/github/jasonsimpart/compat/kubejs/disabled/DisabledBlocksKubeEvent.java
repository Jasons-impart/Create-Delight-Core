package io.github.jasonsimpart.compat.kubejs.disabled;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.RegExpKJS;
import io.github.jasonsimpart.disabled.DisabledRule;
import io.github.jasonsimpart.disabled.DisabledRuleTarget;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.regex.Pattern;

public final class DisabledBlocksKubeEvent implements KubeEvent {
    public DisabledRuleBuilder block(Object id) {
        return DisabledRuleBuilder.block(new DisabledRule(DisabledRuleTarget.BLOCK, ID.mc(id), null, null, null, null, null));
    }

    public DisabledRuleBuilder blocks(Object ids) {
        DisabledRuleBuilder.Group group = new DisabledRuleBuilder.Group();
        for (Object id : ListJS.orSelf(ids)) {
            group.add(block(id));
        }
        return group;
    }

    public DisabledRuleBuilder tag(Object id) {
        ResourceLocation tagId = ID.mc(id);
        return DisabledRuleBuilder.block(new DisabledRule(DisabledRuleTarget.BLOCK, null, null, TagKey.create(Registries.BLOCK, tagId), null, null, null));
    }

    public DisabledRuleBuilder regex(Object regex) {
        Pattern pattern = RegExpKJS.wrap(regex);
        if (pattern == null) {
            pattern = Pattern.compile(String.valueOf(regex));
        }
        return DisabledRuleBuilder.block(new DisabledRule(DisabledRuleTarget.BLOCK, null, null, null, pattern, null, null));
    }

    public DisabledRuleBuilder state(Object id, Map<?, ?> properties) {
        return DisabledRuleBuilder.block(new DisabledRule(DisabledRuleTarget.BLOCK, ID.mc(id), null, null, null, StatePropertyConverter.toStringMap(properties), null));
    }

    public DisabledRuleBuilder blockEntity(Object id, Map<?, ?> partialNbt) {
        CompoundTag tag = NbtMapConverter.toCompound(partialNbt);
        return DisabledRuleBuilder.block(new DisabledRule(DisabledRuleTarget.BLOCK, ID.mc(id), null, null, null, null, tag));
    }
}
