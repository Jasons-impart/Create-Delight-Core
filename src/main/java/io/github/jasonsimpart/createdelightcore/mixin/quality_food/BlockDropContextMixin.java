package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodHarvestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockDropContextMixin {
    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private static void create_Delight_Core$setHarvestPos(Level level, BlockPos pos, ItemStack stack, CallbackInfo ci, @Share("create_Delight_Core$prevPos") LocalRef<BlockPos> prevPosRef) {
        prevPosRef.set(QualityFoodHarvestContext.push(pos));
    }

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private static void create_Delight_Core$clearHarvestPos(Level level, BlockPos pos, ItemStack stack, CallbackInfo ci, @Share("create_Delight_Core$prevPos") LocalRef<BlockPos> prevPosRef) {
        QualityFoodHarvestContext.pop(prevPosRef.get());
    }

    @Inject(method = "popResourceFromFace", at = @At("HEAD"))
    private static void create_Delight_Core$setHarvestPosFromFace(Level level, BlockPos pos, Direction direction, ItemStack stack, CallbackInfo ci, @Share("create_Delight_Core$prevPosFromFace") LocalRef<BlockPos> prevPosRef) {
        prevPosRef.set(QualityFoodHarvestContext.push(pos));
    }

    @Inject(method = "popResourceFromFace", at = @At("TAIL"))
    private static void create_Delight_Core$clearHarvestPosFromFace(Level level, BlockPos pos, Direction direction, ItemStack stack, CallbackInfo ci, @Share("create_Delight_Core$prevPosFromFace") LocalRef<BlockPos> prevPosRef) {
        QualityFoodHarvestContext.pop(prevPosRef.get());
    }
}
