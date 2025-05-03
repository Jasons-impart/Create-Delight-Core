package io.github.jasonsimpart.createdelightcore.content.filter;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemQualityAttribute implements ItemAttribute {
    Quality quality;
    public ItemQualityAttribute(Quality quality) {
        this.quality = quality;
    }
    @Override
    public boolean appliesTo(ItemStack stack) {
        return QualityUtils.getQuality(stack).equals(quality);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack stack) {
        return List.of(new ItemQualityAttribute(QualityUtils.getQuality(stack)));
    }

    @Override
    public String getTranslationKey() {
        return "food_quality";
    }


    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { quality.getTranslation()};
    }
    @Override
    public void writeNBT(CompoundTag nbt) {

        nbt.putString("foodQualityName", quality.getName());
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new ItemQualityAttribute(Quality.byName(nbt.getString("foodQualityName")));
    }
}
