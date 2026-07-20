package io.github.jasonsimpart.createdelightcore.content.fluid;

import com.google.gson.JsonParseException;
import com.simibubi.create.AllFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * A virtual fluid type whose visual identity is carried by each FluidStack.
 *
 * <p>Supported NBT keys:</p>
 * <ul>
 *     <li>{@code Color}: RGB or ARGB integer.</li>
 *     <li>{@code Name}: translation key used as the display name.</li>
 *     <li>{@code CustomName}: literal text or a serialized JSON component.</li>
 *     <li>{@code Variant}: stable subtype identifier for recipe and JEI use.</li>
 * </ul>
 */
public class GeneticCultureFluidType extends AllFluids.TintedFluidType {
    public static final String COLOR_KEY = "Color";
    public static final String NAME_KEY = "Name";
    public static final String CUSTOM_NAME_KEY = "CustomName";
    public static final String VARIANT_KEY = "Variant";
    public static final int DEFAULT_COLOR = 0xFF7D8C6C;

    public GeneticCultureFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties, stillTexture, flowingTexture);
    }

    @Override
    public int getTintColor(FluidStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(COLOR_KEY, Tag.TAG_ANY_NUMERIC)) {
            return DEFAULT_COLOR;
        }

        int color = tag.getInt(COLOR_KEY);
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    @Override
    public Component getDescription(FluidStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return super.getDescription(stack);
        }

        if (tag.contains(CUSTOM_NAME_KEY, Tag.TAG_STRING)) {
            Component customName = parseCustomName(tag.getString(CUSTOM_NAME_KEY));
            if (customName != null) {
                return customName;
            }
        }

        if (tag.contains(NAME_KEY, Tag.TAG_STRING)) {
            String translationKey = tag.getString(NAME_KEY);
            if (!translationKey.isBlank()) {
                return Component.translatable(translationKey);
            }
        }

        return super.getDescription(stack);
    }

    @Nullable
    private static Component parseCustomName(String customName) {
        if (customName.isBlank()) {
            return null;
        }

        if (customName.startsWith("{") || customName.startsWith("[")) {
            try {
                Component parsed = Component.Serializer.fromJson(customName);
                if (parsed != null) {
                    return parsed;
                }
            } catch (JsonParseException ignored) {
                // Fall through to literal text so malformed data remains readable.
            }
        }

        return Component.literal(customName);
    }

    @Override
    protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return NO_TINT;
    }
}
