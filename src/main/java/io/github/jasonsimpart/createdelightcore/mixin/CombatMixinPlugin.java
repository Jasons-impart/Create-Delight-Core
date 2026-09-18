package io.github.jasonsimpart.createdelightcore.mixin;

import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamageArithmeticTransformer;
import io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics.DamagePipelineTransformer;
import io.github.jasonsimpart.createdelightcore.compat.combat.DamagePipelinePreparation;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CombatMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("CreateDelightCore/CombatMixinPlugin");
    private static final String APOTHIC_ATTRIBUTES_MIXIN = ".combat.apothicattributes.";
    private static final String BETTER_COMBAT_MIXIN = ".combat.bettercombat.";
    private static final String IRONS_SPELLBOOKS_MIXIN = ".combat.ironsspellbooks.";
    private static final String TRAVELOPTICS_MIXIN = ".combat.traveloptics.";
    private static final String JEI_TETRA_MIXIN = ".jeitetra.";
    private static final String MMT_MIXIN = ".mmt.";
    private static final String TETRA_MIXIN = ".tetra.";
    private static final String SOPHISTICATED_BACKPACKS_MIXIN = ".sophisticatedbackpacks.";
    private static final String CREATE_INTEGRATED_FARMING_MIXIN = ".createintegratedfarming.";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".alexsmobs.")) {
            return decide(mixinClassName, "alexsmobs");
        }
        if (mixinClassName.contains(".tetrawear.")) {
            return decide(mixinClassName, "tetrawear");
        }
        if (mixinClassName.contains(".cmr.")) {
            return decide(mixinClassName, "cmr");
        }
        if (mixinClassName.contains(APOTHIC_ATTRIBUTES_MIXIN)) {
            return decide(mixinClassName, "attributeslib");
        }
        if (mixinClassName.contains(BETTER_COMBAT_MIXIN)) {
            return decide(mixinClassName, "bettercombat");
        }
        if (mixinClassName.contains(IRONS_SPELLBOOKS_MIXIN)) {
            return decide(mixinClassName, "irons_spellbooks");
        }
        if (mixinClassName.contains(TRAVELOPTICS_MIXIN)) {
            return decide(mixinClassName, "traveloptics");
        }
        if (mixinClassName.contains(JEI_TETRA_MIXIN)) {
            return decide(mixinClassName, "jeitetra");
        }
        if (mixinClassName.contains(MMT_MIXIN)) {
            return decide(mixinClassName, "more_mod_tetra");
        }
        if (mixinClassName.contains(TETRA_MIXIN)) {
            return decide(mixinClassName, "tetra");
        }
        if (mixinClassName.contains(SOPHISTICATED_BACKPACKS_MIXIN)) {
            return decide(mixinClassName, "sophisticatedbackpacks");
        }
        if (mixinClassName.contains(CREATE_INTEGRATED_FARMING_MIXIN)) {
            return decide(mixinClassName, "create_integrated_farming");
        }
        return true;
    }

    private static boolean decide(String mixinClassName, String modId) {
        boolean loaded = isLoaded(modId);
        LOGGER.info("{} combat mixin {} because mod {} is {}", loaded ? "Applying" : "Skipping", mixinClassName, modId, loaded ? "loaded" : "absent");
        return loaded;
    }

    private static boolean isLoaded(String modId) {
        LoadingModList modList = FMLLoader.getLoadingModList();
        if (modList == null) {
            modList = LoadingModList.get();
        }
        return modList != null && modList.getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith(".alexsmobs.WarpedToadPredationDamageMixin")) {
            LOGGER.info("[CDCore][PredationDamage] Applied crimson-mosquito predation damage 10000 in {}", targetClassName);
        }
        if (mixinClassName.endsWith(".combat.LivingDamagePipelineArithmeticMixin")
                || mixinClassName.endsWith(".combat.CombatRulesDiagnosticsMixin")
                || mixinClassName.endsWith(".combat.apothicattributes.ArmorFormulaDiagnosticsMixin")) {
            boolean rules = !mixinClassName.endsWith(".combat.LivingDamagePipelineArithmeticMixin");
            DamagePipelineTransformer.Result result = DamagePipelinePreparation.apply(targetClass, rules);
            if (targetClass.name.equals("net/minecraft/world/entity/LivingEntity")) {
                LOGGER.info("[CDCore][ResistanceDamage] Applied ratio-first damage * (factor / 25.0F) before probes in {}", targetClassName);
            }
            LOGGER.info("[DamageDiagnostics] Pipeline {} roots={} methods={} probes={} sites={}",
                    targetClassName, result.roots(), result.methods(), result.probes(), result.sites());
            if (result.roots() == 0 || result.probes() == 0) {
                throw new IllegalStateException("Damage pipeline probes did not match " + targetClassName);
            }
        }
        if (mixinClassName.endsWith(".mmt.MMTDamageCalculateMixin")) {
            int count = DamageArithmeticTransformer.instrument(targetClass, "hurt")
                    + DamageArithmeticTransformer.instrument(targetClass, "onLivingDamage");
            LOGGER.info("[DamageDiagnostics] Instrumented {} actual float operations in {}", count, targetClassName);
            if (count != 10) LOGGER.warn("[DamageDiagnostics] Expected 10 MMT 2.4.15 float operations; recheck target version/other mixins");
        }
        if (mixinClassName.endsWith(".tetrawear.ArmorHoningDiagnosticsMixin")) {
            int count = DamageArithmeticTransformer.instrument(targetClass, "onLivingHurt");
            LOGGER.info("[DamageDiagnostics] Instrumented {} actual float operations in {}", count, targetClassName);
            if (count != 1) LOGGER.warn("[DamageDiagnostics] Expected 1 TetraWear 1.0.0 float operation; recheck target version/other mixins");
        }
    }
}
