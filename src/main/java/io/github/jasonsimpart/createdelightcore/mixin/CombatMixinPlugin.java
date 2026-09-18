package io.github.jasonsimpart.createdelightcore.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CombatMixinPlugin implements IMixinConfigPlugin {
    private static final String KUBEJS_LAZY_MIXIN = "io.github.jasonsimpart.createdelightcore.mixin.kubejs.LazyMixin";
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
        if (KUBEJS_LAZY_MIXIN.equals(mixinClassName)) {
            return decide(mixinClassName, "kubejs");
        }
        if (mixinClassName.contains(".alexsmobs.")) {
            return decide(mixinClassName, "alexsmobs");
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
        if (KUBEJS_LAZY_MIXIN.equals(mixinClassName)) {
            synchronizeKubeJsLazy(targetClass);
            LOGGER.info("[CDCore][KubeJSLazy] Synchronized get() and forget() in {}", targetClassName);
        }
        if (mixinClassName.endsWith(".alexsmobs.WarpedToadPredationDamageMixin")) {
            LOGGER.info("[CDCore][PredationDamage] Applied crimson-mosquito predation damage 10000 in {}", targetClassName);
        }
    }

    private static void synchronizeKubeJsLazy(ClassNode targetClass) {
        if (!"dev/latvian/mods/kubejs/util/Lazy".equals(targetClass.name)) {
            throw new IllegalStateException("Unexpected KubeJS Lazy target: " + targetClass.name);
        }
        MethodNode get = null;
        MethodNode forget = null;
        for (MethodNode method : targetClass.methods) {
            if ("get".equals(method.name) && "()Ljava/lang/Object;".equals(method.desc)) {
                get = method;
            } else if ("forget".equals(method.name) && "()V".equals(method.desc)) {
                forget = method;
            }
        }
        int unsupported = Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE;
        if (get == null || forget == null || (get.access & unsupported) != 0 || (forget.access & unsupported) != 0) {
            throw new IllegalStateException("KubeJS Lazy method shape changed; review the synchronization patch");
        }
        // Both methods lock the same Lazy instance. The JVM also releases the monitor on exceptions.
        // Preserve the upstream bodies, including expiration, supplier failures and forget semantics.
        get.access |= Opcodes.ACC_SYNCHRONIZED;
        forget.access |= Opcodes.ACC_SYNCHRONIZED;
    }
}
