package io.github.jasonsimpart.createdelightcore.mixin.youkaishomecoming;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jasonsimpart.createdelightcore.compat.alcohol.YoukaiBottleQuality;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

/** FluidItemTile is a static interface method; scope its native block interaction instead. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class FluidItemInteractionQualityMixin {
    @WrapMethod(method = "use")
    private InteractionResult createdelightcore$nativeBottleQuality(Level level, Player player, InteractionHand hand,
                                                                     BlockHitResult hit, Operation<InteractionResult> original) {
        if (level.isClientSide) return original.call(level, player, hand, hit);
        try (var scope = YoukaiBottleQuality.begin(level.getBlockEntity(hit.getBlockPos()))) {
            return original.call(level, player, hand, hit);
        }
    }
}
