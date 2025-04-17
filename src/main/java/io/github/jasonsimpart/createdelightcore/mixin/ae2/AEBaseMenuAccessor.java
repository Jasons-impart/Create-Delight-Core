package io.github.jasonsimpart.createdelightcore.mixin.ae2;

import appeng.menu.AEBaseMenu;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AEBaseMenu.class)
public interface AEBaseMenuAccessor {
    @Accessor(remap = false)
    public Inventory getPlayerInventory();
}
