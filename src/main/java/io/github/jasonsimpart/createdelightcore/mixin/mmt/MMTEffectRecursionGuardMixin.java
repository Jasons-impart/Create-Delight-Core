package io.github.jasonsimpart.createdelightcore.mixin.mmt;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MMT 的 EffectLevelEvent 仅在 MMTDamageCalculate.hurt 中广播，而其护甲荆棘效果会在
 * 监听器内直接对攻击者 hurt()，反弹伤害的来源实体又是 LivingEntity，双方都有荆棘时
 * 会互相嵌套反弹直到一方死亡或栈溢出。这里跳过嵌套触发的广播，只保留一层效果处理。
 */
@Pseudo
@Mixin(targets = "com.inolia_zaicek.more_mod_tetra.Event.MMTDamageCalculate", remap = false)
public class MMTEffectRecursionGuardMixin {
    private static int createdelightcore$depth = 0;

    @Redirect(
        method = "hurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
        ),
        require = 0
    )
    private static boolean createdelightcore$suppressNestedEffectLevelPost(IEventBus bus, Event event) {
        if (createdelightcore$depth > 0) {
            return !event.isCanceled();
        }
        createdelightcore$depth++;
        try {
            return bus.post(event);
        } finally {
            createdelightcore$depth--;
        }
    }
}
