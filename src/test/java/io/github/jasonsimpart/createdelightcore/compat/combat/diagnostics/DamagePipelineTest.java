package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

/** JVM-loads transformed code, and checks real mapped/SRG/previously merged damage methods. */
public final class DamagePipelineTest {
    private static final List<String> OBSERVATIONS = new ArrayList<>();
    private static final String OBSERVER = DamagePipelineTest.class.getName().replace('.', '/');

    public static void run() throws Exception {
        ClassNode node = readResource(Fixture.class);
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(Type.getInternalName(FakeHooks.class))) {
                    call.owner = "net/minecraftforge/common/ForgeHooks";
                }
            }
            if (method.name.equals("resistance")) {
                method.visibleAnnotations = new ArrayList<>();
                AnnotationNode origin = new AnnotationNode("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
                origin.values = new ArrayList<>(List.of("mixin", "fixture.ResistanceMixin"));
                method.visibleAnnotations.add(origin);
            }
        }
        DamagePipelineTransformer.Result result = DamagePipelineTransformer.instrument(node, false, OBSERVER);
        check(result.roots() == 2 && result.probes() > 10, "Did not find root methods/helpers");
        check(result.sites().stream().anyMatch(site -> site.contains("mixin=fixture.ResistanceMixin")), "Lost Mixin origin");
        check(result.sites().stream().noneMatch(site -> site.startsWith("unrelated")), "Instrumented unrelated method");
        check(result.sites().stream().anyMatch(site -> site.startsWith("lambda$widePipeline")), "Missed lambda helper");
        check(DamagePipelineTransformer.instrument(node, false, OBSERVER).probes() == 0, "Repeated instrumentation");
        verify(node);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        byte[] transformed = writer.toByteArray();
        Class<?> fixture = new ClassLoader(DamagePipelineTest.class.getClassLoader()) {
            Class<?> loadFixture() {
                // Test-only Forge boundary stub, keeping the production target discovery unchanged.
                ClassWriter hooks = new ClassWriter(0);
                hooks.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "net/minecraftforge/common/ForgeHooks", null, "java/lang/Object", null);
                for (String name : List.of("onLivingHurt", "onLivingDamage")) {
                    MethodVisitor method = hooks.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "(F)F", null, null);
                    method.visitCode();
                    method.visitVarInsn(Opcodes.FLOAD, 0);
                    method.visitInsn(Opcodes.FRETURN);
                    method.visitMaxs(1, 1);
                    method.visitEnd();
                }
                hooks.visitEnd();
                byte[] hookBytes = hooks.toByteArray();
                defineClass("net.minecraftforge.common.ForgeHooks", hookBytes, 0, hookBytes.length);
                return defineClass(Fixture.class.getName(), transformed, 0, transformed.length);
            }
        }.loadFixture();
        Method pipeline = fixture.getMethod("pipeline", float.class, boolean.class, boolean.class);
        same(Fixture.pipeline(10, false, false), (float) pipeline.invoke(null, 10F, false, false));
        same(Float.POSITIVE_INFINITY, (float) pipeline.invoke(null, Float.MAX_VALUE, false, false));
        check(OBSERVATIONS.stream().anyMatch(value -> value.contains("#resistance") && value.contains("*25.0")),
                "Missed resistance MAX*25 before /25");
        OBSERVATIONS.clear();
        same(Float.MAX_VALUE, (float) pipeline.invoke(null, Float.MAX_VALUE, true, false));
        check(OBSERVATIONS.isEmpty(), "Early return executed phantom arithmetic");
        try {
            pipeline.invoke(null, 1F, false, true);
            throw new AssertionError("Lost original exception");
        } catch (InvocationTargetException expected) {
            check(expected.getCause() instanceof IllegalStateException
                    && expected.getCause().getMessage().equals("original failure"), "Changed original exception");
        }
        Method wide = fixture.getMethod("widePipeline", double.class, double.class, int.class);
        Method wideResult = fixture.getMethod("wide", double.class, double.class, int.class);
        double[] edges = {0, -0.0, Double.MIN_VALUE, Double.MIN_NORMAL, 1, -1, Float.MAX_VALUE,
                Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
        for (double left : edges) for (double right : edges) for (int op = 0; op < 5; op++) {
            same(Fixture.widePipeline(left, right, op), (float) wide.invoke(null, left, right, op));
            same(Fixture.wide(left, right, op), (double) wideResult.invoke(null, left, right, op));
        }
        Random random = new Random(1917);
        for (int i = 0; i < 500; i++) {
            double left = Double.longBitsToDouble(random.nextLong());
            double right = Double.longBitsToDouble(random.nextLong());
            same(Fixture.widePipeline(left, right, i % 5), (float) wide.invoke(null, left, right, i % 5));
            same(Fixture.wide(left, right, i % 5), (double) wideResult.invoke(null, left, right, i % 5));
        }
        OBSERVATIONS.clear();
        wide.invoke(null, (double) Float.MAX_VALUE, 2.0, 2);
        check(OBSERVATIONS.stream().anyMatch(value -> value.startsWith("D2F")), "Missed finite-double narrowing overflow");
        OBSERVATIONS.clear();
        System.out.println("Damage pipeline JVM regression tests passed: roots=" + result.roots() + " probes=" + result.probes());
    }

    public static void checkMinecraftJar(Path path) throws Exception {
        try (JarFile jar = new JarFile(path.toFile())) {
            for (String entry : List.of("net/minecraft/world/entity/LivingEntity.class",
                    "net/minecraft/world/entity/player/Player.class", "net/minecraft/world/damagesource/CombatRules.class")) {
                try (InputStream input = jar.getInputStream(jar.getJarEntry(entry))) {
                    checkRealNode(read(input), entry.endsWith("CombatRules.class"));
                }
            }
        }
    }

    public static void checkMergedClass(Path path) throws Exception {
        try (InputStream input = Files.newInputStream(path)) { checkRealNode(read(input), false); }
    }

    public static void checkAttributesJar(Path path) throws Exception {
        try (JarFile jar = new JarFile(path.toFile()); InputStream input = jar.getInputStream(
                jar.getJarEntry("dev/shadowsoffire/attributeslib/api/ALCombatRules.class"))) {
            checkRealNode(read(input), true);
        }
    }

    private static void checkRealNode(ClassNode node, boolean all) throws Exception {
        ResistanceDamageTest.checkRealClass(node);
        DamagePipelineTransformer.Result result = DamagePipelineTransformer.instrument(node, all);
        check(result.roots() > 0 && result.probes() > 0, "No real damage roots matched: " + node.name);
        if (node.name.endsWith("/LivingEntity")) {
            check(result.sites().stream().anyMatch(site -> site.startsWith("getDamageAfterMagicAbsorb(")
                    || site.startsWith("m_6515_(")), "Missed resistance method");
        }
        verify(node);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        new ClassReader(writer.toByteArray());
        System.out.println("Verified pipeline " + node.name + " roots=" + result.roots()
                + " methods=" + result.methods() + " probes=" + result.probes());
    }

    private static void verify(ClassNode node) throws Exception {
        for (MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            }
        }
    }

    private static ClassNode readResource(Class<?> type) throws Exception {
        try (InputStream input = type.getResourceAsStream("/" + Type.getInternalName(type) + ".class")) { return read(input); }
    }

    private static ClassNode read(InputStream input) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(input).accept(node, 0);
        return node;
    }

    public static float pipelineArithmetic(float left, float right, int op, String site) {
        float result = FloatArithmetic.apply(left, right, op);
        if (!Float.isFinite(result)) OBSERVATIONS.add(site + " " + left + FloatArithmetic.symbol(op) + right);
        return result;
    }

    public static double pipelineArithmetic(double left, double right, int op, String site) {
        double result = FloatArithmetic.apply(left, right, op);
        if (!Double.isFinite(result)) OBSERVATIONS.add(site + " double");
        return result;
    }

    public static float pipelineNarrow(double value, String site) {
        float result = (float) value;
        if (!Float.isFinite(result)) OBSERVATIONS.add("D2F " + site);
        return result;
    }

    public static float pipelineValue(float value, String site) {
        if (!Float.isFinite(value)) OBSERVATIONS.add("VALUE " + site);
        return value;
    }
    public static double pipelineValue(double value, String site) {
        if (!Double.isFinite(value)) OBSERVATIONS.add("VALUE " + site);
        return value;
    }

    private static void same(float expected, float actual) {
        check(Float.isNaN(expected) ? Float.isNaN(actual)
                : Float.floatToRawIntBits(expected) == Float.floatToRawIntBits(actual), "Changed pipeline result");
    }

    private static void same(double expected, double actual) {
        check(Double.isNaN(expected) ? Double.isNaN(actual)
                : Double.doubleToRawLongBits(expected) == Double.doubleToRawLongBits(actual), "Changed double result");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    public static class FakeHooks {
        public static float onLivingHurt(float amount) { return amount; }
        public static float onLivingDamage(float amount) { return amount; }
    }

    public static class Fixture {
        public static float pipeline(float amount, boolean early, boolean fail) {
            float value = FakeHooks.onLivingHurt(amount);
            if (early) return value;
            value = resistance(value);
            if (fail) throw new IllegalStateException("original failure");
            return FakeHooks.onLivingDamage(Math.max(value, 0F));
        }

        public static float resistance(float value) { return value * 25F / 25F; }
        public static float unrelated(float value) { return value * 2F; }

        public static float widePipeline(double left, double right, int op) {
            FakeHooks.onLivingHurt(1F);
            java.util.function.DoubleSupplier compute = () -> wide(left, right, op);
            return (float) compute.getAsDouble();
        }

        public static double wide(double left, double right, int op) { return switch (op) {
            case 0 -> left + right;
            case 1 -> left - right;
            case 2 -> left * right;
            case 3 -> left / right;
            case 4 -> left % right;
            default -> throw new IllegalArgumentException();
        }; }
    }
}
