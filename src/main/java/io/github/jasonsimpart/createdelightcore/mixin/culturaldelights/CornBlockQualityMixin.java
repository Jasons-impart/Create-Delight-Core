package io.github.jasonsimpart.createdelightcore.mixin.culturaldelights;

import com.baisylia.culturaldelights.block.custom.CornBlock;
import de.cadentem.quality_food.capability.LevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CornBlock.class)
public class CornBlockQualityMixin {
    @Unique
    private static final ResourceLocation create_Delight_Core$CORN_UPPER =
            new ResourceLocation("culturaldelights", "corn_upper");

    @Inject(
            method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At("RETURN"))
    private void create_Delight_Core$syncQualityAfterNaturalGrowth(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci) {
        create_Delight_Core$syncUpperQuality(level, pos);
    }

    @Inject(
            method = "performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("RETURN"))
    private void create_Delight_Core$syncQualityAfterBonemeal(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci) {
        create_Delight_Core$syncUpperQuality(level, pos);
    }

    @Unique
    private static void create_Delight_Core$syncUpperQuality(ServerLevel level, BlockPos lowerPos) {
        BlockPos upperPos = lowerPos.above();
        ResourceLocation upperId = ForgeRegistries.BLOCKS.getKey(level.getBlockState(upperPos).getBlock());
        if (create_Delight_Core$CORN_UPPER.equals(upperId)) {
            LevelData.set(level, upperPos, LevelData.get(level, lowerPos));
        }
    }
}
