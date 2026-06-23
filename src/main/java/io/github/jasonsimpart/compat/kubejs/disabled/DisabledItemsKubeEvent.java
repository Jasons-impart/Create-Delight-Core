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
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.regex.Pattern;

public final class DisabledItemsKubeEvent implements KubeEvent {
    public DisabledRuleBuilder item(Object id) {
        return DisabledRuleBuilder.item(new DisabledRule(DisabledRuleTarget.ITEM, ID.mc(id), null, null, null, null, null));
    }

    public DisabledRuleBuilder items(Object ids) {
        DisabledRuleBuilder.Group group = new DisabledRuleBuilder.Group();
        for (Object id : ListJS.orSelf(ids)) {
            group.add(item(id));
        }
        return group;
    }

    public DisabledRuleBuilder tag(Object id) {
        ResourceLocation tagId = ID.mc(id);
        return DisabledRuleBuilder.item(new DisabledRule(DisabledRuleTarget.ITEM, null, TagKey.create(Registries.ITEM, tagId), null, null, null, null));
    }

    public DisabledRuleBuilder regex(Object regex) {
        Pattern pattern = RegExpKJS.wrap(regex);
        if (pattern == null) {
            pattern = Pattern.compile(String.valueOf(regex));
        }
        return DisabledRuleBuilder.item(new DisabledRule(DisabledRuleTarget.ITEM, null, null, null, pattern, null, null));
    }

    public DisabledRuleBuilder stack(Object id, Map<?, ?> partialNbt) {
        CompoundTag tag = NbtMapConverter.toCompound(partialNbt);
        return DisabledRuleBuilder.item(new DisabledRule(DisabledRuleTarget.ITEM, ID.mc(id), null, null, null, null, tag));
    }
}
