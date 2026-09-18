package io.github.jasonsimpart.createdelightcore.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Cache state is atomic; user callbacks are never executed under a cache monitor or future wait. */
public final class KubeJsLazyCache<T> {
    private record Snapshot<T>(boolean cached, T value) {
    }

    private final AtomicReference<Snapshot<T>> state = new AtomicReference<>(new Snapshot<>(false, null));

    public T get(Supplier<T> factory, long expires) {
        Snapshot<T> observed = state.get();
        if (observed.cached() && (expires <= 0 || System.currentTimeMillis() <= expires)) {
            return observed.value();
        }

        // Like the unsynchronized upstream implementation, concurrent misses may call the factory twice.
        // Never serialize arbitrary callbacks: they may acquire other locks or wait for another thread.
        T computed = factory.get();
        state.compareAndSet(observed, new Snapshot<>(true, computed));
        return computed;
    }

    public void forget() {
        // A fresh identity invalidates in-flight computations, including repeated forget calls (no ABA).
        state.set(new Snapshot<>(false, null));
    }
}
