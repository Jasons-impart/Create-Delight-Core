package io.github.jasonsimpart.createdelightcore.compat.combat;

import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageArithmeticTransformer;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamagePipelineTransformer;
import org.objectweb.asm.tree.ClassNode;

/** One postApply entry: rewrite raw instructions before observers consume them. */
public final class DamagePipelinePreparation {
    private DamagePipelinePreparation() {}

    public static DamagePipelineTransformer.Result apply(ClassNode target, boolean rules) {
        return apply(target, rules, DamageArithmeticTransformer.OBSERVER);
    }

    public static DamagePipelineTransformer.Result apply(ClassNode target, boolean rules, String observer) {
        if (target.name.equals("net/minecraft/world/entity/LivingEntity")) {
            ResistanceDamageTransformer.apply(target);
        }
        return DamagePipelineTransformer.instrument(target, rules, observer);
    }
}
