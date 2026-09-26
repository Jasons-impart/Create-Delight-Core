package io.github.jasonsimpart.createdelightcore.mixin.mbd2;

import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.common.trait.RecipeHandlerTrait;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import io.github.jasonsimpart.createdelightcore.content.util.MBD2RecipeQualityContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * MBD2 物品产出按输入品质传递到产物。
 * 扣料可能在开跑或收尾发生，OUT 之前输入已不在槽内，因此在 IN 扣料前抓取样本。
 */
@Mixin(value = ItemSlotCapabilityTrait.ItemRecipeHandler.class, remap = false)
public abstract class ItemRecipeHandlerQualityMixin {
    @Unique
    private IO create_delight_core$io;

    @Unique
    private List<ItemStack> create_delight_core$sources = List.of();

    @Unique
    private ItemSlotCapabilityTrait create_delight_core$itemTrait() {
        return (ItemSlotCapabilityTrait) ((RecipeHandlerTrait<?>) (Object) this).trait;
    }

    @Inject(
            method = "handleRecipeInner(Lcom/lowdragmc/mbd2/api/capability/recipe/IO;Lcom/lowdragmc/mbd2/api/recipe/MBDRecipe;Ljava/util/List;Ljava/lang/String;Z)Ljava/util/List;",
            at = @At("HEAD")
    )
    private void create_delight_core$beginHandle(
            IO io,
            MBDRecipe recipe,
            List<Ingredient> ingredients,
            String chanceCaches,
            boolean simulate,
            CallbackInfoReturnable<List<Ingredient>> cir
    ) {
        this.create_delight_core$io = io;
        this.create_delight_core$sources = List.of();
        ItemSlotCapabilityTrait itemTrait = create_delight_core$itemTrait();
        if (simulate || itemTrait == null || itemTrait.storage == null || itemTrait.getMachine() == null || ingredients == null) {
            return;
        }
        Object machine = itemTrait.getMachine();
        if (io == IO.IN) {
            List<ItemStack> sources = new ArrayList<>();
            int slots = itemTrait.storage.getSlots();
            for (int index = 0; index < slots; index++) {
                ItemStack stack = itemTrait.storage.getStackInSlot(index);
                if (stack.isEmpty()) {
                    continue;
                }
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.test(stack)) {
                        sources.add(stack.copy());
                        break;
                    }
                }
            }
            MBD2RecipeQualityContext.capture(machine, sources);
        } else if (io == IO.OUT) {
            this.create_delight_core$sources = MBD2RecipeQualityContext.take(machine);
        }
    }

    @Redirect(
            method = "handleRecipeInner(Lcom/lowdragmc/mbd2/api/capability/recipe/IO;Lcom/lowdragmc/mbd2/api/recipe/MBDRecipe;Ljava/util/List;Ljava/lang/String;Z)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/lowdragmc/lowdraglib/misc/ItemStackTransfer;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack create_delight_core$applyQualityBeforeInsert(
            com.lowdragmc.lowdraglib.misc.ItemStackTransfer storage,
            int slot,
            ItemStack stack,
            boolean simulate
    ) {
        if (this.create_delight_core$io == IO.OUT
                && !simulate
                && !this.create_delight_core$sources.isEmpty()) {
            MBD2RecipeQualityContext.apply(stack, this.create_delight_core$sources);
        }
        return storage.insertItem(slot, stack, simulate);
    }
}
