package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import io.github.jasonsimpart.createdelightcore.compat.combat.ResistanceDamageTransformer;
import io.github.jasonsimpart.createdelightcore.compat.combat.DamagePipelinePreparation;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Random;

public final class ResistanceDamageTest {
    private static final String DAMAGE_SOURCE = "net/minecraft/world/damagesource/DamageSource";

    public static void run() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = Fixture.class.getResourceAsStream("/" + Type.getInternalName(Fixture.class) + ".class")) {
            new ClassReader(input).accept(node, 0);
        }
        for (MethodNode method : node.methods) {
            if (method.name.equals("getDamageAfterMagicAbsorb")) method.desc = "(L" + DAMAGE_SOURCE + ";F)F";
        }
        // Reproduce the failed runtime order on a copy: observers destroy the raw match.
        ClassNode wrongOrder = new ClassNode();
        node.accept(wrongOrder);
        DamagePipelineTransformer.instrument(wrongOrder, true);
        rejects(wrongOrder);
        // Exercise the exact production composition, then JVM-execute its observed bytecode.
        String fixtureName = node.name;
        node.name = "net/minecraft/world/entity/LivingEntity";
        check(DamagePipelinePreparation.apply(node, true,
                Type.getInternalName(DamagePipelineTest.class)).probes() > 0, "Missing composed probes");
        node.name = fixtureName;
        verify(node);
        rejects(node); // A second application must fail rather than double-divide the damage.
        ClassNode noMatch = new ClassNode();
        noMatch.name = "MissingMethod";
        rejects(noMatch);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        byte[] code = writer.toByteArray();
        Class<?> type = new ClassLoader(ResistanceDamageTest.class.getClassLoader()) {
            Class<?> fixture() {
                ClassWriter source = new ClassWriter(0);
                source.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, DAMAGE_SOURCE, null, "java/lang/Object", null);
                source.visitEnd();
                byte[] sourceBytes = source.toByteArray();
                defineClass(DAMAGE_SOURCE.replace('/', '.'), sourceBytes, 0, sourceBytes.length);
                return defineClass(Fixture.class.getName(), code, 0, code.length);
            }
        }.fixture();
        Method calculate = type.getDeclaredMethods()[0];
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals("getDamageAfterMagicAbsorb")) calculate = method;
        }
        float[] edges = {0F, -0F, Float.MIN_VALUE, Float.MIN_NORMAL, 1F, 13.7F, Float.MAX_VALUE,
                -Float.MAX_VALUE, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN};
        int[] amplifiers = {-2, -1, 0, 1, 2, 3, 4, 5, 10};
        for (int amplifier : amplifiers) for (int sundering : new int[]{0, 1, 2}) for (boolean bypass : new boolean[]{false, true}) {
            type.getField("amplifier").setInt(null, amplifier);
            type.getField("sundering").setInt(null, sundering);
            type.getField("bypass").setBoolean(null, bypass);
            for (float damage : edges) {
                same(expected(damage, amplifier, sundering, bypass),
                        (float) calculate.invoke(null, null, damage));
            }
        }
        type.getField("sundering").setInt(null, 0);
        type.getField("bypass").setBoolean(null, false);
        for (int amplifier = -1; amplifier <= 4; amplifier++) {
            type.getField("amplifier").setInt(null, amplifier);
            float result = (float) calculate.invoke(null, null, Float.MAX_VALUE);
            check(Float.isFinite(result), "Finite MAX still overflowed at resistance amplifier=" + amplifier);
            if (amplifier == -1) same(Float.MAX_VALUE, result);
            float absorption = 0F - (result - Math.max(result - 0F, 0F));
            check(Float.isFinite(absorption), "No-absorption path became nonfinite");
        }
        Random random = new Random(2571);
        double maxRelativeDelta = 0;
        for (int i = 0; i < 5000; i++) {
            float damage = random.nextFloat() * 10000F;
            int amplifier = i % 6 - 1;
            type.getField("amplifier").setInt(null, amplifier);
            float actual = (float) calculate.invoke(null, null, damage);
            same(expected(damage, amplifier, 0, false), actual);
            int factor = 25 - 5 * (amplifier + 1);
            float old = Math.max(damage * factor / 25F, 0F);
            if (old > 0) maxRelativeDelta = Math.max(maxRelativeDelta, Math.abs((double) actual - old) / old);
        }
        check(maxRelativeDelta < 3E-7, "Unexpected ordinary damage drift: " + maxRelativeDelta);
        System.out.println("Resistance ratio-first JVM tests passed; max ordinary relative rounding delta=" + maxRelativeDelta);
    }

    /** The exported class already has probes; strip only our numeric observers in the test copy. */
    public static void restoreRawTestCopy(ClassNode node) throws Exception {
        if (!node.name.equals("net/minecraft/world/entity/LivingEntity")) return;
        for (MethodNode method : node.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call)
                    || !call.owner.equals(DamageArithmeticTransformer.OBSERVER)
                    || !call.name.startsWith("pipeline")) continue;
            AbstractInsnNode site = previousOpcode(call);
            check(site instanceof LdcInsnNode, "Unexpected observer site shape");
            if (call.name.equals("pipelineArithmetic")) {
                AbstractInsnNode operation = previousOpcode(site);
                int op = (Integer) ((LdcInsnNode) operation).cst;
                int[] codes = call.desc.startsWith("(DD")
                        ? new int[]{Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM}
                        : new int[]{Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM};
                method.instructions.insertBefore(call, new InsnNode(codes[op]));
                method.instructions.remove(operation);
            } else if (call.name.equals("pipelineNarrow")) {
                method.instructions.insertBefore(call, new InsnNode(Opcodes.D2F));
            } else check(call.name.equals("pipelineValue"), "Unknown observer " + call.name);
            method.instructions.remove(site);
            method.instructions.remove(call);
        }
        verify(node);
    }

    private static AbstractInsnNode previousOpcode(AbstractInsnNode instruction) {
        AbstractInsnNode previous = instruction.getPrevious();
        while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
        return previous;
    }

    private static void rejects(ClassNode node) {
        try {
            ResistanceDamageTransformer.apply(node);
            throw new AssertionError("Unmatched/repeated patch did not fail");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().contains("found 0"), "Unexpected mismatch error");
        }
    }

    private static void verify(ClassNode node) throws Exception {
        for (MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            }
        }
    }

    private static float expected(float damage, int amplifier, int sundering, boolean bypass) {
        if (bypass) return damage;
        int factor = 25 - 5 * (amplifier + 1);
        float result = damage * (factor / 25F);
        if (sundering > 0) result += damage * sundering * 0.2F;
        return Math.max(result, 0F);
    }

    private static void same(float expected, float actual) {
        check(Float.isNaN(expected) ? Float.isNaN(actual)
                : Float.floatToRawIntBits(expected) == Float.floatToRawIntBits(actual), "Changed ratio-first semantics");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    public static class Fixture {
        public static int amplifier = -1, sundering;
        public static boolean bypass;

        public static float getDamageAfterMagicAbsorb(Object source, float damage) {
            if (bypass) return damage;
            int reduction = 5 * (amplifier + 1);
            int factor = 25 - reduction;
            float intermediate = damage * factor;
            float original = damage;
            damage = intermediate / 25F;
            if (sundering > 0) damage += original * sundering * 0.2F;
            return Math.max(damage, 0F);
        }
    }
}
