package io.github.jasonsimpart.createdelightcore.mixin.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.cadentem.quality_food.capability.LevelData;
import io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern.TavernQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/** QF's general loot handling does not cover all reconstructed, non-food cocktail drops. */
@Mixin(Block.class)
public abstract class CocktailDropsQualityMixin {
    @ModifyReturnValue(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;",
            at = @At("RETURN"))
    private static List<ItemStack> createdelightcore$qualityOnPickup(List<ItemStack> drops, BlockState state,
                                                                   ServerLevel level, BlockPos pos, BlockEntity entity) {
        return createdelightcore$qualifyDrops(drops, state, level, pos);
    }

    @ModifyReturnValue(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"))
    private static List<ItemStack> createdelightcore$qualityOnBreak(List<ItemStack> drops, BlockState state,
                                                                  ServerLevel level, BlockPos pos, BlockEntity blockEntity,
                                                                  Entity entity, ItemStack tool) {
        return createdelightcore$qualifyDrops(drops, state, level, pos);
    }

    @Unique
    private static List<ItemStack> createdelightcore$qualifyDrops(List<ItemStack> drops, BlockState state,
                                                                ServerLevel level, BlockPos pos) {
        if (!(state.getBlock().asItem() instanceof CocktailBlockItem)) return drops;
        var quality = LevelData.get(level, pos, true);
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (var drop : drops) result.add(TavernQuality.supportsQuality(drop) ? TavernQuality.qualify(drop, quality) : drop);
        return result;
    }
}
