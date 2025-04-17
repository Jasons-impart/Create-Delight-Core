package io.github.jasonsimpart.createdelightcore.mixin.ae2;

import appeng.menu.AEBaseMenu;
import appeng.menu.me.items.CraftingTermMenu;
import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CraftingTermMenu.class)
public abstract class CraftingTermMenuMixin extends AEBaseMenu {
    @Final
    @Shadow(remap = false)
    private CraftingContainer recipeTestContainer;
    @Shadow(remap = false)
    private Recipe<CraftingContainer> currentRecipe;
    public CraftingTermMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @ModifyArg(method = "updateCurrentRecipeAndOutput", at = @At(value = "INVOKE",ordinal = 1, target = "Lappeng/menu/slot/CraftingTermSlot;set(Lnet/minecraft/world/item/ItemStack;)V"))
    public ItemStack updateCurrentRecipeAndOutput(ItemStack stack) {
        if (!stack.isEmpty()) {
            Level level = ((AEBaseMenuAccessor) this).getPlayerInventory().player.level();
            QualityUtils.handleConversion(stack, recipeTestContainer, currentRecipe, level.registryAccess());
        }
        return stack;
    }
}
