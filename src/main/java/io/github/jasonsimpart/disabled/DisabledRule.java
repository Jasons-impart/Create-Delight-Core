package io.github.jasonsimpart.disabled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.component.DataComponents;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class DisabledRule {
    private final DisabledRuleTarget target;
    private final ResourceLocation id;
    private final TagKey<Item> itemTag;
    private final TagKey<Block> blockTag;
    private final Pattern regex;
    private final Map<String, String> stateProperties;
    private final CompoundTag partialNbt;
    private final EnumSet<DisablePolicy> policies;
    private BlockState replacement;

    public DisabledRule(
            DisabledRuleTarget target,
            ResourceLocation id,
            TagKey<Item> itemTag,
            TagKey<Block> blockTag,
            Pattern regex,
            Map<String, String> stateProperties,
            CompoundTag partialNbt
    ) {
        this.target = target;
        this.id = id;
        this.itemTag = itemTag;
        this.blockTag = blockTag;
        this.regex = regex;
        this.stateProperties = stateProperties == null ? Map.of() : Map.copyOf(stateProperties);
        this.partialNbt = partialNbt == null ? null : partialNbt.copy();
        this.policies = EnumSet.noneOf(DisablePolicy.class);
    }

    public DisabledRuleTarget target() {
        return target;
    }

    public EnumSet<DisablePolicy> policies() {
        if (!policies.isEmpty()) {
            return EnumSet.copyOf(policies);
        }

        return switch (target) {
            case ITEM -> EnumSet.of(
                    DisablePolicy.REMOVE_RECIPES,
                    DisablePolicy.HIDE_CREATIVE,
                    DisablePolicy.BLOCK_USE,
                    DisablePolicy.BLOCK_ATTACK,
                    DisablePolicy.BLOCK_EQUIP
            );
            case BLOCK -> EnumSet.of(
                    DisablePolicy.REMOVE_RECIPES,
                    DisablePolicy.HIDE_CREATIVE,
                    DisablePolicy.BLOCK_USE,
                    DisablePolicy.BLOCK_PLACE,
                    DisablePolicy.REPLACE_BLOCK
            );
        };
    }

    public DisabledRule policy(DisablePolicy policy) {
        policies.add(policy);
        return this;
    }

    public DisabledRule replacement(BlockState replacement) {
        this.replacement = replacement;
        policy(DisablePolicy.REPLACE_BLOCK);
        return this;
    }

    public BlockState replacement() {
        return replacement;
    }

    public boolean hasPolicy(DisablePolicy policy) {
        return policies().contains(policy);
    }

    public boolean matchesRecipeString(String value) {
        if (id != null && value.equals(id.toString())) {
            return true;
        }

        if (itemTag != null) {
            String tag = itemTag.location().toString();
            if (value.equals(tag) || value.equals("#" + tag)) {
                return true;
            }
        }

        if (blockTag != null) {
            String tag = blockTag.location().toString();
            if (value.equals(tag) || value.equals("#" + tag)) {
                return true;
            }
        }

        return regex != null && regex.matcher(value).matches();
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        if (target == DisabledRuleTarget.BLOCK && item instanceof BlockItem blockItem) {
            return matches(blockItem.getBlock().defaultBlockState(), null, null);
        }

        if (target != DisabledRuleTarget.ITEM) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (id != null && !id.equals(itemId)) {
            return false;
        }

        if (itemTag != null && !stack.is(itemTag)) {
            return false;
        }

        if (regex != null && !regex.matcher(itemId.toString()).matches()) {
            return false;
        }

        if (partialNbt != null) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null || !containsPartial(customData.copyTag(), partialNbt)) {
                return false;
            }
        }

        return id != null || itemTag != null || regex != null || partialNbt != null;
    }

    public boolean matches(BlockState state, BlockGetter level, BlockPos pos) {
        if (target != DisabledRuleTarget.BLOCK) {
            return false;
        }

        Block block = state.getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null && !id.equals(blockId)) {
            return false;
        }

        if (blockTag != null && !state.is(blockTag)) {
            return false;
        }

        if (regex != null && !regex.matcher(blockId.toString()).matches()) {
            return false;
        }

        if (!matchesStateProperties(state)) {
            return false;
        }

        if (partialNbt != null) {
            if (level == null || pos == null) {
                return false;
            }
            if (!(level instanceof LevelAccessor levelAccessor)) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || !containsPartial(blockEntity.saveWithoutMetadata(levelAccessor.registryAccess()), partialNbt)) {
                return false;
            }
        }

        return id != null || blockTag != null || regex != null || !stateProperties.isEmpty() || partialNbt != null;
    }

    private boolean matchesStateProperties(BlockState state) {
        if (stateProperties.isEmpty()) {
            return true;
        }

        Map<String, Property<?>> properties = new LinkedHashMap<>();
        for (Property<?> property : state.getProperties()) {
            properties.put(property.getName(), property);
        }

        for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
            Property<?> property = properties.get(entry.getKey());
            if (property == null) {
                return false;
            }

            Optional<?> value = property.getValue(entry.getValue());
            if (value.isEmpty() || !state.getValue(property).equals(value.get())) {
                return false;
            }
        }

        return true;
    }

    private static boolean containsPartial(CompoundTag actual, CompoundTag expected) {
        for (String key : expected.getAllKeys()) {
            if (!actual.contains(key)) {
                return false;
            }

            Tag expectedValue = expected.get(key);
            Tag actualValue = actual.get(key);
            if (expectedValue instanceof CompoundTag expectedCompound && actualValue instanceof CompoundTag actualCompound) {
                if (!containsPartial(actualCompound, expectedCompound)) {
                    return false;
                }
            } else if (expectedValue == null || !expectedValue.equals(actualValue)) {
                return false;
            }
        }

        return true;
    }
}
