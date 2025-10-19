package io.github.jasonsimpart.createdelightcore.mixin.alexscaves;

import com.github.alexmodguy.alexscaves.server.block.fluid.AcidFluidType;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AcidFluidType.class)
public class AcidFluidTypeMixin {
    @Inject(method = "onVaporize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), cancellable = true)
    public void onVaporizeMixin(Player player, Level level, BlockPos pos, FluidStack stack, CallbackInfo ci) {
        if (level.random.nextFloat() <= 0.05)
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), ACItemRegistry.URANIUM_SHARD.get().getDefaultInstance().copyWithCount(1)));
        ci.cancel();
    }

}
