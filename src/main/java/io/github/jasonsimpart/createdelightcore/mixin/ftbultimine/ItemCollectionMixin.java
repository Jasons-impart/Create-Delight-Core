package io.github.jasonsimpart.createdelightcore.mixin.ftbultimine;

import dev.ftb.mods.ftbultimine.ItemCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(ItemCollection.class)
public class ItemCollectionMixin {
    @Inject(method = "drop", at = @At(value = "INVOKE",ordinal = 1, target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, remap = false)
    public void dropMixin(Level world, BlockPos pos, CallbackInfo ci, List<ItemStack> stacks) {
        //使用直接的产生掉落物来代替使用popResource
        stacks.forEach(itemStack -> {
            double d0 = (double) EntityType.ITEM.getHeight() / 2.0;
            double d1 = (double)pos.getX() + 0.5 + Mth.nextDouble(world.random, -0.25, 0.25);
            double d2 = (double)pos.getY() + 0.5 + Mth.nextDouble(world.random, -0.25, 0.25) - d0;
            double d3 = (double)pos.getZ() + 0.5 + Mth.nextDouble(world.random, -0.25, 0.25);
            ItemEntity itemEntity = new ItemEntity(world, d1, d2, d3, itemStack);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        });
        ci.cancel();
    }
}
