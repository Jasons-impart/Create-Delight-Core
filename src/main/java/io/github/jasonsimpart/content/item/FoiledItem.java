package io.github.jasonsimpart.content.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FoiledItem extends Item {
    public FoiledItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
