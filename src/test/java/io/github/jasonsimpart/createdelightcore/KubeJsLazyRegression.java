package io.github.jasonsimpart.createdelightcore;

import io.github.jasonsimpart.createdelightcore.mixin.CombatMixinPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.jar.JarFile;

/** Standalone regression against an explicitly supplied upstream JAR; no Minecraft launch required. */
public final class KubeJsLazyRegression {
    private static final String NAME = "dev.latvian.mods.kubejs.util.Lazy";
    private static final String MIXIN = "io.github.jasonsimpart.createdelightcore.mixin.kubejs.LazyMixin";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply the KubeJS JAR path");
        byte[] original;
        try (JarFile jar = new JarFile(args[0])) {
            original = jar.getInputStream(jar.getJarEntry(NAME.replace('.', '/') + ".class")).readAllBytes();
        }
        ClassNode node = read(original);
        byte[] normalized = write(node);
        var plugin = new CombatMixinPlugin();
        plugin.postApply(NAME, node, MIXIN, null);
        byte[] patched = write(node);
        plugin.postApply(NAME, node, MIXIN, null);
        check(Arrays.equals(patched, write(node)), "Patch must be idempotent");
        ClassNode baseline = read(original);
        for (int i = 0; i < node.methods.size(); i++) {
            var method = node.methods.get(i);
            if (method.name.equals("get") || method.name.equals("forget")) {
                check((method.access & Opcodes.ACC_SYNCHRONIZED) == 0, "Factory would run under a method monitor");
                for (var instruction : method.instructions) {
                    check(instruction.getOpcode() != Opcodes.MONITORENTER, "Unexpected monitor in cache bridge");
                }
            }
            if (method.name.equals("get") || method.name.equals("forget") || method.name.equals("<init>")) {
                node.methods.set(i, baseline.methods.get(i));
            }
        }
        check(node.fields.removeIf(field -> field.name.equals("createdelightcore$lazyCache")), "Missing cache field");
        check(Arrays.equals(normalized, write(node)), "Patch changed unrelated class contents");
        var incompatible = read(original);
        incompatible.methods.removeIf(method -> method.name.equals("forget"));
        byte[] beforeFailure = write(incompatible);
        try {
            plugin.postApply(NAME, incompatible, MIXIN, null);
            throw new AssertionError("Changed method shape was accepted");
        } catch (IllegalStateException expected) {
            check(Arrays.equals(beforeFailure, write(incompatible)), "Partial patch on failure");
        }
        Class<?> patchedClass = define(patched);
        semantics(patchedClass);
        invalidationDuringComputation(patchedClass, false);
        invalidationDuringComputation(patchedClass, true);
        concurrentMisses(patchedClass);
        callbackCanWaitForForget(patchedClass);
        long originalNulls = race(define(original));
        long patchedNulls = race(patchedClass);
        check(patchedNulls == 0, "Patched get returned null " + patchedNulls + " times");
        System.out.println("PASS: bridge scope, idempotency, shape guard, cache, null, expiration, supplier retry,");
        System.out.println("      invalidation during computation, concurrent misses, callback waiting for forget, concurrency");
        System.out.println("8 threads x 250000 iterations: original nulls=" + originalNulls + ", patched nulls=" + patchedNulls);
    }

    private static void semantics(Class<?> type) throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Supplier<Object> supplier = () -> { calls.incrementAndGet(); return new Object(); };
        Object lazy = type.getMethod("of", Supplier.class).invoke(null, supplier);
        var get = type.getMethod("get");
        Object first = get.invoke(lazy);
        check(first == get.invoke(lazy) && calls.get() == 1, "Cache miss on repeated get");
        type.getMethod("forget").invoke(lazy);
        check(first != get.invoke(lazy) && calls.get() == 2, "forget did not invalidate cache");
        Object expired = type.getMethod("of", Supplier.class, long.class).invoke(null, supplier, -1000L);
        check(get.invoke(expired) != get.invoke(expired), "Expired cache was retained");
        AtomicInteger nullCalls = new AtomicInteger();
        Supplier<Object> nullable = () -> { nullCalls.incrementAndGet(); return null; };
        Object nullLazy = type.getMethod("of", Supplier.class).invoke(null, nullable);
        check(get.invoke(nullLazy) == null && get.invoke(nullLazy) == null && nullCalls.get() == 1,
                "A legitimate null must still be cached");
        AtomicInteger attempts = new AtomicInteger();
        Supplier<Object> throwing = () -> {
            if (attempts.getAndIncrement() == 0) throw new IllegalStateException("expected supplier failure");
            return new Object();
        };
        Object retry = type.getMethod("of", Supplier.class).invoke(null, throwing);
        try {
            get.invoke(retry);
            throw new AssertionError("Supplier exception swallowed");
        } catch (InvocationTargetException expected) {
            check(expected.getCause() instanceof IllegalStateException, "Wrong supplier exception");
        }
        // Retry on another thread must remain possible after a callback fails.
        var executor = Executors.newSingleThreadExecutor();
        try {
            check(executor.submit(() -> get.invoke(retry)).get(10, TimeUnit.SECONDS) != null, "Retry failed");
        } finally {
            executor.shutdownNow();
        }
    }

    private static void invalidationDuringComputation(Class<?> type, boolean refillBeforeCompletion) throws Exception {
        var started = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        Object oldValue = new Object();
        Object newValue = new Object();
        Supplier<Object> factory = () -> {
            if (calls.incrementAndGet() == 1) {
                started.countDown();
                await(resume);
                return oldValue;
            }
            return newValue;
        };
        Object lazy = type.getMethod("of", Supplier.class).invoke(null, factory);
        var get = type.getMethod("get");
        var forget = type.getMethod("forget");
        var executor = Executors.newSingleThreadExecutor();
        try {
            var computing = executor.submit(() -> get.invoke(lazy));
            await(started);
            forget.invoke(lazy);
            forget.invoke(lazy); // Repeated invalidations must not reuse an earlier empty state identity.
            if (refillBeforeCompletion) check(get.invoke(lazy) == newValue, "New generation failed to publish");
            resume.countDown();
            check(computing.get(5, TimeUnit.SECONDS) == oldValue, "In-flight caller lost its own result");
            check(get.invoke(lazy) == newValue && calls.get() == 2, "Old computation repopulated the cache");
        } finally {
            resume.countDown();
            executor.shutdownNow();
        }
    }

    private static void concurrentMisses(Class<?> type) throws Exception {
        var started = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        Object slowValue = new Object();
        Object fastValue = new Object();
        Supplier<Object> factory = () -> {
            if (calls.incrementAndGet() == 1) {
                started.countDown();
                await(resume);
                return slowValue;
            }
            return fastValue;
        };
        Object lazy = type.getMethod("of", Supplier.class).invoke(null, factory);
        var get = type.getMethod("get");
        var executor = Executors.newSingleThreadExecutor();
        try {
            var slow = executor.submit(() -> get.invoke(lazy));
            await(started);
            check(get.invoke(lazy) == fastValue, "Concurrent miss was blocked");
            resume.countDown();
            check(slow.get(5, TimeUnit.SECONDS) == slowValue, "Slow caller lost its own result");
            check(get.invoke(lazy) == fastValue && calls.get() == 2, "CAS winner was overwritten");
        } finally {
            resume.countDown();
            executor.shutdownNow();
        }
    }

    private static void callbackCanWaitForForget(Class<?> type) throws Exception {
        var executor = Executors.newSingleThreadExecutor();
        var holder = new Object[1];
        var forget = type.getMethod("forget");
        Supplier<Object> factory = () -> {
            try {
                executor.submit(() -> forget.invoke(holder[0])).get(5, TimeUnit.SECONDS);
                return new Object();
            } catch (Exception error) {
                throw new AssertionError("Callback cannot wait for another thread to invalidate the same Lazy", error);
            }
        };
        try {
            holder[0] = type.getMethod("of", Supplier.class).invoke(null, factory);
            check(type.getMethod("get").invoke(holder[0]) != null, "Cross-thread callback returned null");
        } finally {
            executor.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            check(latch.await(5, TimeUnit.SECONDS), "Fixture coordination timed out");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }

    private static long race(Class<?> type) throws Exception {
        Supplier<Object> supplier = Object::new;
        Object lazy = type.getMethod("of", Supplier.class).invoke(null, supplier);
        var get = type.getMethod("get");
        var forget = type.getMethod("forget");
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(8);
        var futures = new ArrayList<java.util.concurrent.Future<Long>>();
        try {
            for (int thread = 0; thread < 8; thread++) {
                futures.add(executor.submit((Callable<Long>) () -> {
                    start.await();
                    long nulls = 0;
                    for (int i = 0; i < 250_000; i++) {
                        if (get.invoke(lazy) == null) nulls++;
                        forget.invoke(lazy);
                    }
                    return nulls;
                }));
            }
            start.countDown();
            long nulls = 0;
            for (var future : futures) nulls += future.get(60, TimeUnit.SECONDS);
            return nulls;
        } finally {
            executor.shutdownNow();
        }
    }

    private static Class<?> define(byte[] bytes) {
        return new ClassLoader(KubeJsLazyRegression.class.getClassLoader()) {
            Class<?> loadLazy() { return defineClass(NAME, bytes, 0, bytes.length); }
        }.loadLazy();
    }

    private static ClassNode read(byte[] bytes) {
        var node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static byte[] write(ClassNode node) {
        var writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
