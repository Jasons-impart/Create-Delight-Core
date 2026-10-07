package io.github.jasonsimpart.createdelightcore.util;

import java.util.Objects;

/** Coordinates the two startup events without loading optional ExtendedAE classes. */
public final class ExtendedAePackageInitialization {
    public static final String SUPPORTED_VERSION = "1.20-1.4.18-forge";
    public static final ExtendedAePackageInitialization STARTUP = new ExtendedAePackageInitialization();

    private Runnable initializer;
    private boolean requested;
    private boolean configLoaded;
    private boolean started;
    private boolean completed;
    private Throwable failure;

    public synchronized void requestInitialization(Runnable initializer) {
        Objects.requireNonNull(initializer, "initializer");
        if (!requested) {
            this.initializer = initializer;
            requested = true;
        }
        initializeIfReady();
    }

    public synchronized void onConfigLoaded() {
        configLoaded = true;
        initializeIfReady();
    }

    public synchronized void recoverConfigurationIfMissing(Runnable recovery) {
        if (!configLoaded) {
            Objects.requireNonNull(recovery, "recovery").run();
        }
    }

    public synchronized void verifyComplete() {
        if (!requested) {
            throw new IllegalStateException("ExtendedAE packing tape startup hook did not run");
        }
        if (!configLoaded) {
            throw new IllegalStateException("ExtendedAE configuration did not finish loading before load completion");
        }
        if (!completed) {
            throw new IllegalStateException("ExtendedAE packing tape whitelist initialization failed", failure);
        }
    }

    private void initializeIfReady() {
        if (!requested || !configLoaded || started) {
            return;
        }
        started = true;
        try {
            initializer.run();
            completed = true;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            initializer = null;
        }
    }
}
