package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.world.item.ItemStack;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SqueezeBasinBlockEntity.class, remap = false)
public abstract class SqueezeBasinBlockEntityMixin {

    public SqueezeBasinBlockEntityMixin() {}

    @Unique
    private static final ThreadLocal<Quality> create_Delight_Core$INPUT = new ThreadLocal<>();
    @Inject(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    public void process$storeInput(CallbackInfo ci, @Local ItemStack itemStack) {
        create_Delight_Core$INPUT.set(QualityUtils.getQuality(itemStack));
    }

    @ModifyArg(method = "lambda$process$5", at = @At(value = "INVOKE", target = "Lorg/forsteri/ratatouille/content/squeeze_basin/SqueezeBasinBlockEntity;acceptOutputs(Ljava/util/List;Z)Z"))
    public List<ItemStack> process$applyQuality(List<ItemStack> outputItems) {
        QualityUtils.applyQuality(outputItems.get(0), create_Delight_Core$INPUT.get());
        create_Delight_Core$INPUT.remove();
        return outputItems;
    }
}
