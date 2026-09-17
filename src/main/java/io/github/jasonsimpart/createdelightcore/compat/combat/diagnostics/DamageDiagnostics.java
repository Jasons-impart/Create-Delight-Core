package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Observation only: no setters, cancellation, clamping or replacement of thrown game exceptions. */
public final class DamageDiagnostics {
    private static final Logger LOGGER = LogManager.getLogger("CreateDelightCore/DamageDiagnostics");
    private static final ThreadLocal<DamageTrace> CURRENT = new ThreadLocal<>();
    private static final boolean JVM_ENABLED = Boolean.getBoolean("createdelightcore.damageDiagnostics");
    private static long windowStart;
    private static int reports;
    private static int suppressed;

    private DamageDiagnostics() {}

    public static boolean enabled() { return JVM_ENABLED || CDConfig.logNonFiniteDamage; }

    public static DamageTrace begin(String phase, LivingEntity entity, DamageSource source, float amount) {
        if (!enabled()) return null;
        DamageTrace trace = new DamageTrace(phase, () -> describe(entity, source), CURRENT.get());
        CURRENT.set(trace);
        observe(trace, phase.endsWith(".actuallyHurt") ? "pipeline input=" + FloatArithmetic.format(amount)
                : "ForgeHooks input=" + FloatArithmetic.format(amount), amount);
        return trace;
    }

    public static void end(DamageTrace trace) {
        if (trace == null) return;
        try {
            if (trace.suspicious) report(trace, "FINAL_TRACE", null);
        } finally {
            if (trace.parent == null) CURRENT.remove();
            else CURRENT.set(trace.parent);
        }
    }

    public static void returned(DamageTrace trace, float result) {
        if (trace != null) observe(trace, "ForgeHooks returned=" + FloatArithmetic.format(result), result);
    }

    public static void failed(DamageTrace trace, Throwable failure) {
        if (trace == null) return;
        trace.add("originalException=" + failure.getClass().getName() + ": " + failure.getMessage());
        trace.suspicious = true;
        report(trace, "ORIGINAL_EXCEPTION", failure);
    }

    public static void created(DamageTraceAccess event, String phase, LivingEntity entity,
                               DamageSource source, float amount) {
        if (!enabled()) return;
        DamageTrace trace = CURRENT.get();
        if (trace == null || trace.boundToEvent || !trace.phase.equals(phase)) {
            trace = new DamageTrace(phase, () -> describe(entity, source), null);
        }
        trace.boundToEvent = true;
        event.createdelightcore$setDamageTrace(trace);
        observe(trace, "event constructed=" + FloatArithmetic.format(amount), amount);
    }

    public static void setAmount(DamageTraceAccess event, float before, float after) {
        if (!enabled()) return;
        DamageTrace trace = event.createdelightcore$getDamageTrace();
        if (trace == null) return; // This event was constructed while diagnostics were disabled.
        if (Float.floatToRawIntBits(before) == Float.floatToRawIntBits(after)) return;
        String kind = Float.isFinite(before) && !Float.isFinite(after)
                ? "EVENT_FINITE_TO_NONFINITE" : "event.setAmount";
        observe(trace, kind + " " + FloatArithmetic.format(before) + " -> "
                + FloatArithmetic.format(after) + " caller=" + caller(), after);
    }

    /** Called in place of a watched FADD/FSUB/FMUL/FDIV/FREM, with the original operands. */
    public static float arithmetic(float left, float right, int operation, String site) {
        float result = FloatArithmetic.apply(left, right, operation);
        if (!enabled()) return result;
        DamageTrace trace = CURRENT.get();
        if (trace == null && !Float.isFinite(result)) {
            trace = new DamageTrace("outside ForgeHooks scope", () -> "source/target unavailable", null);
        }
        if (trace != null) {
            observe(trace, "ACTUAL_FLOAT_OP " + site + ": " + FloatArithmetic.format(left) + " "
                    + FloatArithmetic.symbol(operation) + " " + FloatArithmetic.format(right)
                    + " = " + FloatArithmetic.format(result), result);
        }
        return result;
    }

    public static void checkpoint(String site, float value) {
        if (!enabled()) return;
        DamageTrace trace = CURRENT.get();
        if (trace == null && !Float.isFinite(value)) {
            trace = new DamageTrace("outside ForgeHooks scope", () -> "source/target unavailable", null);
        }
        if (trace != null) observe(trace, site + "=" + FloatArithmetic.format(value), value);
    }

    /** Pipeline probes are silent outside an active damage scope, even for nonfinite results. */
    public static float pipelineArithmetic(float left, float right, int operation, String site) {
        float result = FloatArithmetic.apply(left, right, operation);
        if (!enabled() || CURRENT.get() == null) return result;
        pipelineObserve("ACTUAL_FLOAT_OP " + site + ": " + FloatArithmetic.format(left) + " "
                + FloatArithmetic.symbol(operation) + " " + FloatArithmetic.format(right)
                + " = " + FloatArithmetic.format(result), result);
        return result;
    }

    public static double pipelineArithmetic(double left, double right, int operation, String site) {
        double result = FloatArithmetic.apply(left, right, operation);
        if (!enabled() || CURRENT.get() == null) return result;
        pipelineObserve("ACTUAL_DOUBLE_OP " + site + ": " + FloatArithmetic.format(left) + " "
                + FloatArithmetic.symbol(operation) + " " + FloatArithmetic.format(right)
                + " = " + FloatArithmetic.format(result), result);
        return result;
    }

    public static float pipelineNarrow(double value, String site) {
        float result = (float) value;
        if (!enabled() || CURRENT.get() == null) return result;
        pipelineObserve("ACTUAL_D2F " + site + ": " + FloatArithmetic.format(value)
                + " -> " + FloatArithmetic.format(result), result);
        return result;
    }

    public static float pipelineValue(float value, String site) {
        if (!enabled() || CURRENT.get() == null) return value;
        pipelineObserve("PIPELINE_VALUE " + site + "=" + FloatArithmetic.format(value), value);
        return value;
    }

    public static double pipelineValue(double value, String site) {
        if (!enabled() || CURRENT.get() == null) return value;
        pipelineObserve("PIPELINE_VALUE " + site + "=" + FloatArithmetic.format(value), value);
        return value;
    }

    private static void pipelineObserve(String entry, double value) {
        if (!enabled()) return;
        DamageTrace trace = CURRENT.get();
        if (trace != null && trace.observe(entry, value)) {
            report(trace, "FIRST_NONFINITE_OBSERVED", new Throwable("observation stack"));
        }
    }

    public static void contribution(String operation, float argument, float total) {
        if (!enabled()) return;
        DamageTrace trace = CURRENT.get();
        if (trace == null) return;
        observe(trace, "MMT_EFFECT " + operation + " argument=" + FloatArithmetic.format(argument)
                + " total=" + FloatArithmetic.format(total) + " caller=" + caller(), total);
    }

    private static void observe(DamageTrace trace, String entry, float value) {
        if (trace.observe(entry, value)) report(trace, "FIRST_NONFINITE_OBSERVED", new Throwable("observation stack"));
    }

    private static synchronized boolean admit() {
        long now = System.nanoTime();
        if (windowStart == 0 || now - windowStart >= 60_000_000_000L) {
            if (suppressed > 0) LOGGER.warn("[CDCore][DamageDiagnostics] Suppressed {} additional traces in previous window", suppressed);
            windowStart = now;
            reports = 0;
            suppressed = 0;
        }
        if (reports >= 20) { suppressed++; return false; }
        reports++;
        return true;
    }

    private static void report(DamageTrace trace, String reason, Throwable stack) {
        if (trace.reportDenied) return;
        try {
            if (!trace.reported) {
                if (!admit()) { trace.reportDenied = true; return; }
                trace.reported = true;
            }
            String message = "[CDCore][DamageTrace #" + trace.id + "] " + reason + "\n"
                    + trace.subject.get() + "\n" + trace.snapshot();
            if (stack == null) LOGGER.warn(message);
            else LOGGER.warn(message, stack);
        } catch (RuntimeException ignored) {
            // Failure to format a diagnostic must not replace the original combat outcome.
        }
    }

    private static String caller() {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String name = frame.getClassName();
            if (name.equals(Thread.class.getName()) || name.startsWith(DamageDiagnostics.class.getPackageName())
                    || name.startsWith("io.github.jasonsimpart.createdelightcore.mixin.")
                    || name.equals("net.minecraftforge.event.entity.living.LivingHurtEvent")
                    || name.equals("net.minecraftforge.event.entity.living.LivingDamageEvent")
                    || name.equals("com.inolia_zaicek.more_mod_tetra.Event.Post.EffectLevelEvent")) continue;
            return frame.toString();
        }
        return "unknown";
    }

    private static String describe(LivingEntity target, DamageSource source) {
        return "thread=" + Thread.currentThread().getName() + ", dimension=" + target.level().dimension().location()
                + ", gameTime=" + target.level().getGameTime() + ", target=" + describeEntity(target)
                + ", health=" + target.getHealth() + ", armor=" + target.getArmorValue()
                + ", source=" + source.getMsgId() + ", attacker=" + describeEntity(source.getEntity())
                + ", direct=" + describeEntity(source.getDirectEntity());
    }

    private static String describeEntity(Entity entity) {
        return entity == null ? "none" : ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()) + "/"
                + entity.getUUID() + "@" + entity.blockPosition();
    }
}
