package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.core.Quality;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodBlockUseContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockUseContextMixin {
    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "use", at = @At("HEAD"))
    private void create_Delight_Core$pushQualityBlockUseContext(
            Level level,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Share("create_Delight_Core$previousQuality") LocalRef<Quality> previousQuality
    ) {
        BlockPos pos = hit.getBlockPos();
        previousQuality.set(QualityFoodBlockUseContext.push(level, pos, asState()));
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void create_Delight_Core$popQualityBlockUseContext(
            Level level,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Share("create_Delight_Core$previousQuality") LocalRef<Quality> previousQuality
    ) {
        QualityFoodBlockUseContext.pop(previousQuality.get());
    }
}
