package io.github.jasonsimpart.compat.createmetallurgy.backport;

public interface CrucibleGaugeCacheAccess {
    boolean createdelightcore$isGaugeCacheValid();

    boolean createdelightcore$hasGaugeAttachmentCached();

    void createdelightcore$setGaugeAttachmentCache(boolean hasGauge);

    void createdelightcore$invalidateGaugeAttachmentCache();
}
