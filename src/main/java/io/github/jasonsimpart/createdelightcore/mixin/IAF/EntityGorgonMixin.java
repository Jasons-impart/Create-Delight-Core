package io.github.jasonsimpart.createdelightcore.mixin.IAF;

import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(EntityGorgon.class)
public class EntityGorgonMixin {

    @Inject(method = "isBlindfolded", at = @At("TAIL"), cancellable = true, remap = false)
    private static void isBlindfolded(LivingEntity attackTarget, CallbackInfoReturnable<Boolean> cir){
        AtomicReference<Boolean> r = new AtomicReference<>(false);
        CuriosApi.getCuriosInventory(attackTarget).ifPresent(curiosInventory -> {
            var slotInventory = curiosInventory.getCurios().get("head");
            for(int i = 0; i < slotInventory.getSlots(); i++){
                var item = slotInventory.getStacks().getStackInSlot(i).getItem();
                if(item == IafItemRegistry.BLINDFOLD.get()){
                    r.set(true);
                    break;
                }
            }
        });
        cir.setReturnValue(cir.getReturnValue() || r.get());
    }
}
