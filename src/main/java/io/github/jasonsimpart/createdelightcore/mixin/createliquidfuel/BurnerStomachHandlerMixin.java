package io.github.jasonsimpart.createdelightcore.mixin.createliquidfuel;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.forsteri.createliquidfuel.core.BurnerStomachHandler", remap = false)
public abstract class BurnerStomachHandlerMixin {
    @Unique
    private static final ResourceLocation createdelightcore$BLAZE_COOLER =
            ResourceLocation.fromNamespaceAndPath("fluidlogistics", "blaze_cooler");

    @Inject(method = "tick(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;)Z",
            at = @At("HEAD"), cancellable = true, require = 1)
    private static void createdelightcore$skipCoolerTick(SmartBlockEntity entity,
                                                       CallbackInfoReturnable<Boolean> callback) {
        if (createdelightcore$isBlazeCooler(entity)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    private static void createdelightcore$skipCoolerFuel(SmartBlockEntity entity, ItemStack stack,
                                                       boolean forceOverflow, boolean simulate,
                                                       CallbackInfoReturnable<Boolean> fuelCallback,
                                                       CallbackInfo callback) {
        if (createdelightcore$isBlazeCooler(entity)) {
            callback.cancel();
        }
    }

    @Unique
    private static boolean createdelightcore$isBlazeCooler(SmartBlockEntity entity) {
        return createdelightcore$BLAZE_COOLER.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()));
    }
}
