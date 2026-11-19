package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** CocktailBlockItem inherits plain BlockItem placement and does not populate Mojito's drink inventory. */
@Mixin(BlockItem.class)
public abstract class CocktailPlacementQualityMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void createdelightcore$storePlacedCocktail(BlockPlaceContext context, BlockState state,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || context.getLevel().isClientSide
                || !(context.getItemInHand().getItem() instanceof CocktailBlockItem)) return;
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DrinkBlockEntity drink) {
            drink.addItem(context.getItemInHand().copyWithCount(1));
            drink.refresh();
        }
    }
}
