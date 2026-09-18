package io.github.jasonsimpart.createdelightcore;

import io.github.jasonsimpart.createdelightcore.mixin.CombatMixinPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.jar.JarFile;

/** An adversarial lock-order probe, not evidence of an actual Minecraft deadlock. */
public final class KubeJsLazyLockOrderProbe {
    private static final String NAME = "dev.latvian.mods.kubejs.util.Lazy";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply the KubeJS JAR path");
        byte[] original;
        try (JarFile jar = new JarFile(args[0])) {
            original = jar.getInputStream(jar.getJarEntry(NAME.replace('.', '/') + ".class")).readAllBytes();
        }
        var node = new ClassNode();
        new ClassReader(original).accept(node, 0);
        for (var method : node.methods) {
            if (method.name.equals("get") || method.name.equals("forget")) method.access |= Opcodes.ACC_SYNCHRONIZED;
        }
        var synchronizedWriter = new ClassWriter(0);
        node.accept(synchronizedWriter);
        node = new ClassNode();
        new ClassReader(original).accept(node, 0);
        new CombatMixinPlugin().postApply(NAME, node,
                "io.github.jasonsimpart.createdelightcore.mixin.kubejs.LazyMixin", null);
        var writer = new ClassWriter(0);
        node.accept(writer);
        if (probe(define(original), "original")) {
            throw new AssertionError("Unexpected deadlock in original Lazy");
        }
        if (probe(define(writer.toByteArray()), "atomic-cache")) {
            throw new AssertionError("Atomic cache introduced a monitor deadlock");
        }
        if (!probe(define(synchronizedWriter.toByteArray()), "synchronized")) {
            throw new AssertionError("Expected lock inversion was not reproduced");
        }
        System.out.println("CONFIRMED: synchronized Lazy adds a monitor deadlock for the controlled lock-order fixture.");
        System.out.println("PASS: original and atomic-cache Lazy both complete the same fixture.");
        System.out.println("This fixture does not establish that Minecraft uses this exact call sequence.");
    }

    private static boolean probe(Class<?> type, String label) throws Exception {
        Object externalLock = new Object();
        Object value = new Object();
        var externalHeld = new CountDownLatch(1);
        var supplierEntered = new CountDownLatch(1);
        var failure = new AtomicReference<Throwable>();
        Supplier<Object> factory = () -> {
            supplierEntered.countDown();
            synchronized (externalLock) {
                return value;
            }
        };
        Object lazy = type.getMethod("of", Supplier.class).invoke(null, factory);
        var get = type.getMethod("get");
        Thread externalFirst = daemon(label + "-external-first", failure, () -> {
            synchronized (externalLock) {
                externalHeld.countDown();
                await(supplierEntered);
                if (get.invoke(lazy) != value) throw new AssertionError("Wrong value");
            }
        });
        Thread lazyFirst = daemon(label + "-lazy-first", failure, () -> {
            await(externalHeld);
            if (get.invoke(lazy) != value) throw new AssertionError("Wrong value");
        });
        externalFirst.start();
        lazyFirst.start();
        var bean = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Throwable error = failure.get();
            if (error != null) throw new AssertionError("Fixture failed", error);
            long[] deadlocked = bean.findMonitorDeadlockedThreads();
            if (deadlocked != null && Arrays.stream(deadlocked).anyMatch(id -> id == externalFirst.getId())
                    && Arrays.stream(deadlocked).anyMatch(id -> id == lazyFirst.getId())) {
                for (var info : bean.getThreadInfo(new long[]{externalFirst.getId(), lazyFirst.getId()}, true, true)) {
                    System.out.println(info);
                }
                return true;
            }
            if (!externalFirst.isAlive() && !lazyFirst.isAlive()) {
                System.out.println(label + ": both threads completed");
                return false;
            }
            Thread.sleep(10);
        }
        throw new AssertionError(label + ": timeout without a verified JVM monitor cycle");
    }

    private static Thread daemon(String name, AtomicReference<Throwable> failure, ThrowingRunnable action) {
        Thread thread = new Thread(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure.compareAndSet(null, error instanceof InvocationTargetException ? error.getCause() : error);
            }
        }, name);
        // A detected intrinsic-monitor deadlock cannot be interrupted; let the diagnostic JVM exit normally.
        thread.setDaemon(true);
        return thread;
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS)) throw new AssertionError("Fixture coordination timed out");
    }

    private static Class<?> define(byte[] bytes) {
        return new ClassLoader(KubeJsLazyLockOrderProbe.class.getClassLoader()) {
            Class<?> loadLazy() { return defineClass(NAME, bytes, 0, bytes.length); }
        }.loadLazy();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
