package io.github.jasonsimpart.createdelightcore.mixin.mmt;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "com.inolia_zaicek.more_mod_tetra.Event.Post.EffectLevelEvent", remap = false)
public class EffectLevelEventMixin {
    @Shadow
    private float fixedDamage;

    @Shadow
    private float normalMulti;

    @Shadow
    private List<Float> independentMulti;

    @Shadow
    @Final
    private LivingEntity attacker;

    @Shadow
    @Final
    private LivingEntity target;

    @Shadow
    @Final
    public LivingHurtEvent hurtEvent;

    @Inject(method = "addIndependentMulti", at = @At("RETURN"), require = 0)
    private void createdelightcore$logIndependentDamageMultipliers(float multiplier, CallbackInfo ci) {
        if (!CDConfig.logMoreModTetraIndependentDamageMultipliers) {
            return;
        }

        List<Float> independentMultipliers = List.copyOf(independentMulti);
        if (independentMultipliers.isEmpty()) {
            return;
        }

        float originalDamage = hurtEvent.getAmount();
        float independentProduct = 1.0F;

        for (float value : independentMultipliers) {
            independentProduct *= value;
        }

        float beforeIndependent = (originalDamage + fixedDamage) * (1.0F + normalMulti);
        float projectedDamage = Math.max(beforeIndependent * independentProduct, 0.0F);

        CreateDelightCore.LOGGER.info(
                "[CDCore][MMT Damage] attacker={} target={} source={} base={} fixed={} normalMulti={} addedIndependentMultiplier={} independentMultipliers={} independentProduct={} projectedDamageSoFar={}",
                describe(attacker),
                describe(target),
                hurtEvent.getSource().getMsgId(),
                originalDamage,
                fixedDamage,
                normalMulti,
                multiplier,
                independentMultipliers,
                independentProduct,
                projectedDamage
        );
    }

    private static String describe(LivingEntity entity) {
        if (entity == null) {
            return "<none>";
        }

        return entity.getScoreboardName() + "[" + ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()) + "]";
    }
}
