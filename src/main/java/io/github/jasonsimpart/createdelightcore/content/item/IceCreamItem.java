package io.github.jasonsimpart.createdelightcore.content.item;

import com.gumillea.cosmopolitan.common.item.FrozenDessertItem;
import com.gumillea.cosmopolitan.core.util.CosmoCompat;
import com.teamabnormals.neapolitan.core.registry.NeapolitanSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class IceCreamItem extends FrozenDessertItem {

    public IceCreamItem(Properties properties, boolean bowl, int tFrozen) {
        super(properties, bowl, tFrozen);
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return CosmoCompat.nea ? (SoundEvent) NeapolitanSoundEvents.ICE_CREAM_EAT.get() : SoundEvents.GENERIC_EAT;
    }

    @Override
    public SoundEvent getEatingSound() {
        return CosmoCompat.nea ? (SoundEvent)NeapolitanSoundEvents.ICE_CREAM_EAT.get() : SoundEvents.GENERIC_EAT;
    }

}
