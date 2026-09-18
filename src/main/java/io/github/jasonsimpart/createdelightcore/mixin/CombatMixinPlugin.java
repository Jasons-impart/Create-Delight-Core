package io.github.jasonsimpart.createdelightcore.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CombatMixinPlugin implements IMixinConfigPlugin {
    private static final String KUBEJS_LAZY_MIXIN = "io.github.jasonsimpart.createdelightcore.mixin.kubejs.LazyMixin";
    private static final String KUBEJS_LAZY = "dev/latvian/mods/kubejs/util/Lazy";
    private static final String LAZY_CACHE = "io/github/jasonsimpart/createdelightcore/util/KubeJsLazyCache";
    private static final String LAZY_CACHE_FIELD = "createdelightcore$lazyCache";
    private static final String LAZY_CACHE_DESC = "L" + LAZY_CACHE + ";";
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
            patchKubeJsLazy(targetClass);
            LOGGER.info("[CDCore][KubeJSLazy] Applied atomic cache; factory runs without cache locks in {}", targetClassName);
        }
        if (mixinClassName.endsWith(".alexsmobs.WarpedToadPredationDamageMixin")) {
            LOGGER.info("[CDCore][PredationDamage] Applied crimson-mosquito predation damage 10000 in {}", targetClassName);
        }
    }

    private static void patchKubeJsLazy(ClassNode targetClass) {
        if (!KUBEJS_LAZY.equals(targetClass.name)) {
            throw new IllegalStateException("Unexpected KubeJS Lazy target: " + targetClass.name);
        }
        MethodNode get = null;
        MethodNode forget = null;
        MethodNode constructor = null;
        for (MethodNode method : targetClass.methods) {
            if ("get".equals(method.name) && "()Ljava/lang/Object;".equals(method.desc)) {
                get = method;
            } else if ("forget".equals(method.name) && "()V".equals(method.desc)) {
                forget = method;
            } else if ("<init>".equals(method.name) && "(Ljava/util/function/Supplier;J)V".equals(method.desc)) {
                constructor = method;
            }
        }
        int unsupported = Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE;
        if (get == null || forget == null || constructor == null
                || (get.access & unsupported) != 0 || (forget.access & unsupported) != 0
                || !hasInstanceField(targetClass, "factory", "Ljava/util/function/Supplier;")
                || !hasInstanceField(targetClass, "expires", "J")
                || !hasInstanceField(targetClass, "value", "Ljava/lang/Object;")
                || !hasInstanceField(targetClass, "cached", "Z")) {
            throw new IllegalStateException("KubeJS Lazy shape changed; review the atomic cache patch");
        }
        if (targetClass.fields.stream().anyMatch(field -> field.name.equals(LAZY_CACHE_FIELD))) {
            if (hasInstanceField(targetClass, LAZY_CACHE_FIELD, LAZY_CACHE_DESC)
                    && callsCache(get, "get") && callsCache(forget, "forget")) {
                return;
            }
            throw new IllegalStateException("KubeJS Lazy cache field collision");
        }
        var returns = new java.util.ArrayList<org.objectweb.asm.tree.AbstractInsnNode>();
        for (var instruction : constructor.instructions) {
            if (instruction.getOpcode() == Opcodes.RETURN) returns.add(instruction);
        }
        if (returns.size() != 1 || targetClass.methods.stream().filter(method -> method.name.equals("<init>")).count() != 1) {
            throw new IllegalStateException("KubeJS Lazy constructor changed; review cache initialization");
        }

        // Validate the complete shape before mutating anything. The tiny bridges leave factory/expires intact.
        targetClass.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                LAZY_CACHE_FIELD, LAZY_CACHE_DESC, null, null));
        var initialize = new InsnList();
        initialize.add(new VarInsnNode(Opcodes.ALOAD, 0));
        initialize.add(new TypeInsnNode(Opcodes.NEW, LAZY_CACHE));
        initialize.add(new InsnNode(Opcodes.DUP));
        initialize.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, LAZY_CACHE, "<init>", "()V", false));
        initialize.add(new FieldInsnNode(Opcodes.PUTFIELD, KUBEJS_LAZY, LAZY_CACHE_FIELD, LAZY_CACHE_DESC));
        constructor.instructions.insertBefore(returns.get(0), initialize);
        constructor.maxStack = Math.max(constructor.maxStack, 3);

        var read = cacheReceiver();
        read.add(new VarInsnNode(Opcodes.ALOAD, 0));
        read.add(new FieldInsnNode(Opcodes.GETFIELD, KUBEJS_LAZY, "factory", "Ljava/util/function/Supplier;"));
        read.add(new VarInsnNode(Opcodes.ALOAD, 0));
        read.add(new FieldInsnNode(Opcodes.GETFIELD, KUBEJS_LAZY, "expires", "J"));
        read.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, LAZY_CACHE, "get", "(Ljava/util/function/Supplier;J)Ljava/lang/Object;", false));
        read.add(new InsnNode(Opcodes.ARETURN));
        replaceBody(get, read, 4);

        var clear = cacheReceiver();
        clear.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, LAZY_CACHE, "forget", "()V", false));
        clear.add(new InsnNode(Opcodes.RETURN));
        replaceBody(forget, clear, 1);
    }

    private static boolean hasInstanceField(ClassNode type, String name, String descriptor) {
        return type.fields.stream().anyMatch(field -> field.name.equals(name) && field.desc.equals(descriptor)
                && (field.access & Opcodes.ACC_STATIC) == 0);
    }

    private static boolean callsCache(MethodNode method, String name) {
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(LAZY_CACHE) && call.name.equals(name)) return true;
        }
        return false;
    }

    private static InsnList cacheReceiver() {
        var instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, KUBEJS_LAZY, LAZY_CACHE_FIELD, LAZY_CACHE_DESC));
        return instructions;
    }

    private static void replaceBody(MethodNode method, InsnList instructions, int maxStack) {
        method.access &= ~Opcodes.ACC_SYNCHRONIZED;
        method.instructions = instructions;
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        method.maxStack = maxStack;
        method.maxLocals = 1;
    }
}
