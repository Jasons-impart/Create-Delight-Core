package io.github.jasonsimpart.createdelightcore.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Plain Java regression tests: no optional mod or Minecraft runtime is needed. */
public final class ExtendedAePackageInitializationTest {
    public static void main(String[] args) throws Exception {
        configFirst();
        startupFirst();
        emptyWhitelist();
        repeatedNotifications();
        concurrentEvents();
        missingEvents();
        callbackFailures();
        missingNotificationRecovery();
        loadedConfigSkipsRecovery();
        recoveryFailures();
        System.out.println("ExtendedAE startup regression: 10 scenarios passed (200 concurrent event pairs)");
    }

    private static void configFirst() {
        Fixture fixture = new Fixture();
        fixture.loadConfig(List.of("ae2:drive", "custom:device"));
        equal(0, fixture.calls, "Config loading alone must not register devices");
        fixture.requestInitialization();
        fixture.verify(List.of("ae2:drive", "custom:device"));
    }

    private static void startupFirst() {
        Fixture fixture = new Fixture();
        fixture.requestInitialization();
        equal(0, fixture.calls, "Startup must not read the uninitialized whitelist");
        fixture.loadConfig(List.of("custom:device"));
        fixture.verify(List.of("custom:device"));
    }

    private static void emptyWhitelist() {
        Fixture fixture = new Fixture();
        fixture.requestInitialization();
        fixture.loadConfig(List.of());
        fixture.verify(List.of());
    }

    private static void repeatedNotifications() {
        Fixture fixture = new Fixture();
        fixture.requestInitialization();
        fixture.requestInitialization();
        fixture.loadConfig(List.of("ae2:drive"));
        fixture.loadConfig(List.of("custom:later_config"));
        fixture.requestInitialization();
        fixture.verify(List.of("ae2:drive"));
    }

    private static void concurrentEvents() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 200; i++) {
                Fixture fixture = new Fixture();
                CountDownLatch ready = new CountDownLatch(2);
                CountDownLatch start = new CountDownLatch(1);
                Future<?> startup = executor.submit(() -> {
                    awaitStart(ready, start);
                    fixture.requestInitialization();
                });
                Future<?> config = executor.submit(() -> {
                    awaitStart(ready, start);
                    fixture.loadConfig(List.of("custom:device"));
                });
                if (!ready.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Event workers did not become ready");
                }
                start.countDown();
                startup.get(5, TimeUnit.SECONDS);
                config.get(5, TimeUnit.SECONDS);
                fixture.verify(List.of("custom:device"));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static void missingEvents() {
        ExtendedAePackageInitialization missingStartup = new ExtendedAePackageInitialization();
        missingStartup.onConfigLoaded();
        expect(IllegalStateException.class, missingStartup::verifyComplete);

        ExtendedAePackageInitialization missingConfig = new ExtendedAePackageInitialization();
        missingConfig.requestInitialization(() -> {
            throw new AssertionError("Unloaded configuration must never be read");
        });
        expect(IllegalStateException.class, missingConfig::verifyComplete);
    }

    private static void callbackFailures() {
        ExtendedAePackageInitialization runtimeFailure = new ExtendedAePackageInitialization();
        RuntimeException original = new IllegalArgumentException("Invalid configured device");
        int[] calls = {0};
        runtimeFailure.requestInitialization(() -> {
            calls[0]++;
            throw original;
        });
        same(original, expect(RuntimeException.class, runtimeFailure::onConfigLoaded),
                "Original initializer exception must propagate");
        runtimeFailure.onConfigLoaded();
        equal(1, calls[0], "A failed initializer must not be repeated");
        same(original, expect(IllegalStateException.class, runtimeFailure::verifyComplete).getCause(),
                "Load completion must retain the original failure");

        ExtendedAePackageInitialization errorFailure = new ExtendedAePackageInitialization();
        AssertionError originalError = new AssertionError("Initializer error");
        errorFailure.onConfigLoaded();
        same(originalError, expect(AssertionError.class, () -> errorFailure.requestInitialization(() -> {
            throw originalError;
        })), "Original initializer error must propagate");
    }

    private static void awaitStart(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Event start timed out");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static void missingNotificationRecovery() {
        Fixture fixture = new Fixture();
        fixture.requestInitialization();
        equal(0, fixture.calls, "Missing configuration notification must defer registration");
        fixture.coordinator.recoverConfigurationIfMissing(() -> fixture.loadConfig(List.of("custom:device")));
        fixture.verify(List.of("custom:device"));
        Fixture empty = new Fixture();
        empty.requestInitialization();
        empty.coordinator.recoverConfigurationIfMissing(() -> empty.loadConfig(List.of()));
        empty.verify(List.of());
    }

    private static void loadedConfigSkipsRecovery() {
        Fixture fixture = new Fixture();
        fixture.loadConfig(List.of("ae2:drive"));
        fixture.requestInitialization();
        fixture.coordinator.recoverConfigurationIfMissing(() -> {
            throw new AssertionError("Already loaded configuration must not be reinitialized");
        });
        fixture.verify(List.of("ae2:drive"));
    }

    private static void recoveryFailures() {
        Fixture fixture = new Fixture();
        fixture.requestInitialization();
        IllegalStateException original = new IllegalStateException("Forge has not loaded the configuration");
        same(original, expect(IllegalStateException.class, () -> fixture.coordinator.recoverConfigurationIfMissing(() -> {
            throw original;
        })), "Recovery must propagate invalid or unavailable configuration errors");
        equal(0, fixture.calls, "Failed configuration recovery must not register an empty default whitelist");
        expect(IllegalStateException.class, fixture.coordinator::verifyComplete);
    }

    private static <T extends Throwable> T expect(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable exception) {
            if (type.isInstance(exception)) {
                return type.cast(exception);
            }
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + exception, exception);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void equal(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void same(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    private static final class Fixture {
        private final ExtendedAePackageInitialization coordinator = new ExtendedAePackageInitialization();
        private final Set<String> devices = new LinkedHashSet<>();
        // Intentionally plain fields, like ExtendedAE: the coordinator must publish config writes.
        private List<String> whitelist;
        private int calls;

        private void loadConfig(List<String> whitelist) {
            this.whitelist = whitelist;
            coordinator.onConfigLoaded();
        }

        private void requestInitialization() {
            coordinator.requestInitialization(() -> {
                calls++;
                whitelist.forEach(devices::add);
            });
        }

        private void verify(List<String> expectedDevices) {
            coordinator.verifyComplete();
            equal(1, calls, "Whitelist initialization must run exactly once");
            equal(new LinkedHashSet<>(expectedDevices), devices, "Configured devices must be preserved");
        }
    }
}
