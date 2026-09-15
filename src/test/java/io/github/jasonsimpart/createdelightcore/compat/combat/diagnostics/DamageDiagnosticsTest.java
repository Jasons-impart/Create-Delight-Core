package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

/** Executable regression suite; no game world or additional test framework is required. */
public final class DamageDiagnosticsTest {
    private static final List<String> OBSERVATIONS = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        arithmeticIsUnchanged();
        intermediateOverflowIsNotFinalDamage();
        firstFailureSurvivesBoundedHistory();
        if (args.length > 0) checkInstalledTargets(Path.of(args[0]));
        System.out.println("Damage diagnostics regression tests passed");
    }

    public static float arithmetic(float left, float right, int operation, String site) {
        float result = FloatArithmetic.apply(left, right, operation);
        if (!Float.isFinite(result)) OBSERVATIONS.add(site + " " + FloatArithmetic.symbol(operation));
        return result;
    }

    private static Class<?> instrumentedFixture(String method, int expectedCount) throws Exception {
        String resource = "/" + Fixture.class.getName().replace('.', '/') + ".class";
        ClassNode node = new ClassNode();
        try (InputStream input = DamageDiagnosticsTest.class.getResourceAsStream(resource)) {
            new ClassReader(input.readAllBytes()).accept(node, 0);
        }
        int count = DamageArithmeticTransformer.instrument(node, method,
                DamageDiagnosticsTest.class.getName().replace('.', '/'));
        check(count == expectedCount, "Unexpected fixture operation count " + count);
        check(DamageArithmeticTransformer.instrument(node, method) == 0, "Observer was applied twice");
        ClassWriter writer = new ClassWriter(0); // Do not mask maxStack/frame bugs by recomputing them.
        node.accept(writer);
        byte[] transformed = writer.toByteArray();
        return new ClassLoader(DamageDiagnosticsTest.class.getClassLoader()) {
            Class<?> loadFixture() { return defineClass(Fixture.class.getName(), transformed, 0, transformed.length); }
        }.loadFixture();
    }

    private static void arithmeticIsUnchanged() throws Exception {
        Method transformed = instrumentedFixture("calculate", 5).getMethod("calculate", float.class, float.class, int.class);
        float[] edges = {0.0F, -0.0F, Float.MIN_VALUE, -Float.MIN_VALUE, Float.MIN_NORMAL,
                1.0F, -1.0F, Float.MAX_VALUE, -Float.MAX_VALUE,
                Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN};
        for (float left : edges) for (float right : edges) for (int operation = 0; operation < 5; operation++) {
            same(Fixture.calculate(left, right, operation), (float) transformed.invoke(null, left, right, operation));
        }
        Random random = new Random(9137);
        for (int i = 0; i < 500; i++) {
            float left = Float.intBitsToFloat(random.nextInt());
            float right = Float.intBitsToFloat(random.nextInt());
            int operation = i % 5;
            same(Fixture.calculate(left, right, operation), (float) transformed.invoke(null, left, right, operation));
        }
        OBSERVATIONS.clear();
    }

    private static void intermediateOverflowIsNotFinalDamage() throws Exception {
        Method transformed = instrumentedFixture("guarded", 1).getMethod("guarded", float.class, float.class, boolean.class);
        same(7.0F, (float) transformed.invoke(null, Float.MAX_VALUE, 1.01F, true));
        check(OBSERVATIONS.size() == 1 && OBSERVATIONS.get(0).contains("#guarded")
                && OBSERVATIONS.get(0).endsWith("*"), "Missed actual multiplication before finite cap");
        OBSERVATIONS.clear();
        same(Float.POSITIVE_INFINITY, (float) transformed.invoke(null, Float.MAX_VALUE, 1.01F, false));
        check(OBSERVATIONS.size() == 1, "Changed or missed uncapped overflow");
        OBSERVATIONS.clear();
    }

    private static void firstFailureSurvivesBoundedHistory() {
        DamageTrace trace = new DamageTrace("test", () -> "fixture", null);
        check(!trace.observe("finite MAX", Float.MAX_VALUE), "Finite MAX incorrectly classified");
        check(trace.observe("first multiplication", Float.POSITIVE_INFINITY), "First failure not recorded");
        for (int i = 0; i < 200; i++) trace.observe("later-" + i, 1.0F);
        check(!trace.observe("later NaN", Float.NaN), "First failure was overwritten");
        check(trace.firstNonFinite().equals("first multiplication"), "Lost first failure");
        check(trace.snapshot().contains("earlierEntriesOmitted="), "History did not remain bounded");
    }

    private static void checkInstalledTargets(Path mods) throws Exception {
        checkTarget(mods.resolve("more_mod_tetra-2.4.15-all.jar"),
                "com/inolia_zaicek/more_mod_tetra/Event/MMTDamageCalculate.class",
                new String[]{"hurt", "onLivingDamage"}, 10);
        checkTarget(mods.resolve("tetrawear-1.20.1-1.0.0.jar"),
                "se/mickelus/tetrawear/effects/ArmorHoning.class", new String[]{"onLivingHurt"}, 1);
    }

    private static void checkTarget(Path jarPath, String entry, String[] methods, int expected) throws Exception {
        ClassNode node = new ClassNode();
        try (JarFile jar = new JarFile(jarPath.toFile()); InputStream input = jar.getInputStream(jar.getJarEntry(entry))) {
            new ClassReader(input).accept(node, 0);
        }
        int count = 0;
        for (String method : methods) count += DamageArithmeticTransformer.instrument(node, method);
        check(count == expected, "Target bytecode changed: " + jarPath + " observed=" + count);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        new ClassReader(writer.toByteArray());
        System.out.println("Verified original target: " + jarPath.getFileName() + " operations=" + count);
    }

    private static void same(float expected, float actual) {
        check(Float.isNaN(expected) ? Float.isNaN(actual)
                        : Float.floatToRawIntBits(expected) == Float.floatToRawIntBits(actual),
                "Changed arithmetic: " + FloatArithmetic.format(expected) + " -> " + FloatArithmetic.format(actual));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static final class Fixture {
        public static float calculate(float left, float right, int operation) {
            return switch (operation) {
                case 0 -> left + right;
                case 1 -> left - right;
                case 2 -> left * right;
                case 3 -> left / right;
                case 4 -> left % right;
                default -> throw new IllegalArgumentException();
            };
        }

        public static float guarded(float base, float multiplier, boolean guard) {
            float intermediate = base * multiplier;
            return guard ? 7.0F : intermediate;
        }
    }
}
