package io.github.jasonsimpart.compat.qualityfood;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.registry.QFComponents;
import net.minecraft.world.item.ItemStack;

public final class QualityFoodCompat {
    private static boolean apiRuntimeFailureLogged;
    private static boolean apiUnavailableFailureLogged;
    private static boolean apiUnavailable;

    private QualityFoodCompat() {
    }

    public static QualityResult clearQuality(ItemStack stack) {
        if (apiUnavailable) {
            return QualityResult.failure();
        }

        try {
            Quality quality = stack.get(QFComponents.QUALITY_DATA_COMPONENT.get());
            if (quality == null) {
                return QualityResult.success(0);
            }
            int level = quality.level();
            stack.remove(QFComponents.QUALITY_DATA_COMPONENT.get());
            return QualityResult.success(Math.max(level, 0));
        } catch (LinkageError exception) {
            logApiFailure(exception);
            return QualityResult.failure();
        } catch (RuntimeException exception) {
            logApiFailure(exception);
            return QualityResult.failure();
        }
    }

    public static QualityResult getQualityLevel(ItemStack stack) {
        if (apiUnavailable) {
            return QualityResult.failure();
        }

        try {
            Quality quality = stack.get(QFComponents.QUALITY_DATA_COMPONENT.get());
            return QualityResult.success(quality == null ? 0 : Math.max(quality.level(), 0));
        } catch (LinkageError | RuntimeException exception) {
            logApiFailure(exception);
            return QualityResult.failure();
        }
    }

    public static Quality getQualityData(ItemStack stack) {
        if (apiUnavailable) {
            return null;
        }

        try {
            return stack.get(QFComponents.QUALITY_DATA_COMPONENT.get());
        } catch (LinkageError | RuntimeException exception) {
            logApiFailure(exception);
            return null;
        }
    }

    public enum QualityRestoreResult {
        SUCCESS,
        FAILED,
        UNKNOWN
    }

    public static QualityRestoreResult restoreQuality(ItemStack stack, Quality quality) {
        if (apiUnavailable) {
            return QualityRestoreResult.UNKNOWN;
        }

        try {
            var component = QFComponents.QUALITY_DATA_COMPONENT.get();
            if (quality == null) {
                stack.remove(component);
                return stack.get(component) == null
                        ? QualityRestoreResult.SUCCESS
                        : QualityRestoreResult.FAILED;
            }
            Quality before = stack.get(component);
            stack.set(component, quality);
            if (quality.equals(stack.get(component))) {
                return QualityRestoreResult.SUCCESS;
            }
            if (before == null) {
                stack.remove(component);
            } else {
                stack.set(component, before);
            }
            return QualityRestoreResult.FAILED;
        } catch (LinkageError | RuntimeException exception) {
            logApiFailure(exception);
            return QualityRestoreResult.UNKNOWN;
        }
    }

    public record QualityResult(boolean success, int level) {
        private static QualityResult success(int level) {
            return new QualityResult(true, level);
        }

        private static QualityResult failure() {
            return new QualityResult(false, 0);
        }
    }

    private static void logApiFailure(Throwable exception) {
        if (exception instanceof LinkageError) {
            apiUnavailable = true;
            if (!apiUnavailableFailureLogged) {
                apiUnavailableFailureLogged = true;
                io.github.jasonsimpart.CreateDelightCore.LOGGER.warn("Quality Food API is unavailable; skipping quality operations", exception);
            }
            return;
        }
        if (!apiRuntimeFailureLogged) {
            apiRuntimeFailureLogged = true;
            io.github.jasonsimpart.CreateDelightCore.LOGGER.warn("Quality Food API operation failed", exception);
        }
    }

}
