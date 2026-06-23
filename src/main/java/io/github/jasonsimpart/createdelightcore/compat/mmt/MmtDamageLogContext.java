package io.github.jasonsimpart.createdelightcore.compat.mmt;

import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MmtDamageLogContext {
    private static final ThreadLocal<MmtDamageLogContext> CURRENT = new ThreadLocal<>();
    private static int nextId;

    private final int id;
    private final LivingHurtEvent hurtEvent;
    private final float baseDamage;
    private final List<Contribution> fixedDamage = new ArrayList<>();
    private final List<Contribution> normalMultipliers = new ArrayList<>();
    private final List<Contribution> independentMultipliers = new ArrayList<>();

    private MmtDamageLogContext(LivingHurtEvent hurtEvent) {
        this.id = ++nextId;
        this.hurtEvent = hurtEvent;
        this.baseDamage = hurtEvent.getAmount();
    }

    public static void begin(LivingHurtEvent hurtEvent) {
        if (!CDConfig.logMoreModTetraIndependentDamageMultipliers) {
            return;
        }

        CURRENT.set(new MmtDamageLogContext(hurtEvent));
    }

    public static void recordFixedDamage(float amount, float total, String operation) {
        record(fixedDamageList(), amount, total, operation);
    }

    public static void recordNormalMultiplier(float amount, float total, String operation) {
        record(normalMultiplierList(), amount, total, operation);
    }

    public static void recordIndependentMultiplier(float amount, float total) {
        record(independentMultiplierList(), amount, total, "multiply");
    }

    public static void finish(LivingHurtEvent hurtEvent) {
        MmtDamageLogContext context = CURRENT.get();
        CURRENT.remove();

        if (context == null || context.independentMultipliers.isEmpty()) {
            return;
        }

        context.log(hurtEvent);
    }

    public static void clear() {
        CURRENT.remove();
    }

    private static List<Contribution> fixedDamageList() {
        MmtDamageLogContext context = CURRENT.get();
        return context == null ? null : context.fixedDamage;
    }

    private static List<Contribution> normalMultiplierList() {
        MmtDamageLogContext context = CURRENT.get();
        return context == null ? null : context.normalMultipliers;
    }

    private static List<Contribution> independentMultiplierList() {
        MmtDamageLogContext context = CURRENT.get();
        return context == null ? null : context.independentMultipliers;
    }

    private static void record(List<Contribution> contributions, float amount, float total, String operation) {
        if (contributions == null) {
            return;
        }

        contributions.add(new Contribution(operation, amount, total, findMmtCaller()));
    }

    private void log(LivingHurtEvent finalHurtEvent) {
        float fixedTotal = lastTotal(fixedDamage);
        float normalTotal = lastTotal(normalMultipliers);
        float independentProduct = product(independentMultipliers);
        float beforeIndependent = (baseDamage + fixedTotal) * (1.0F + normalTotal);
        float projected = Math.max(beforeIndependent * independentProduct, 0.0F);

        StringBuilder builder = new StringBuilder(640);
        builder.append("[CDCore][MMT Damage #").append(id).append("]\n");
        builder.append("  attacker: ").append(describe(hurtEvent.getSource().getEntity())).append('\n');
        builder.append("  direct:   ").append(describe(hurtEvent.getSource().getDirectEntity())).append('\n');
        builder.append("  target:   ").append(describe(hurtEvent.getEntity())).append('\n');
        builder.append("  source:   ").append(hurtEvent.getSource().getMsgId()).append('\n');
        builder.append("  formula:  max(((base + fixed) * (1 + normalMulti)) * independentProduct, 0)\n");
        builder.append("  totals:   base=").append(format(baseDamage))
                .append(", fixed=").append(format(fixedTotal))
                .append(", normalMulti=").append(format(normalTotal))
                .append(", independentProduct=").append(format(independentProduct))
                .append(", projectedBeforeMMTExtraCaps=").append(format(projected))
                .append(", eventAmountAfterMMT=").append(format(finalHurtEvent.getAmount()))
                .append('\n');

        appendContributions(builder, "fixed damage", fixedDamage);
        appendContributions(builder, "normal multiplier", normalMultipliers);
        appendContributions(builder, "independent multiplier", independentMultipliers);

        CreateDelightCore.LOGGER.info(builder.toString());
    }

    private static void appendContributions(StringBuilder builder, String title, List<Contribution> contributions) {
        builder.append("  ").append(title).append(":\n");
        if (contributions.isEmpty()) {
            builder.append("    none\n");
            return;
        }

        for (int i = 0; i < contributions.size(); i++) {
            Contribution contribution = contributions.get(i);
            builder.append("    ").append(i + 1).append(". ")
                    .append(contribution.operation()).append(' ').append(format(contribution.amount()))
                    .append(" -> total ").append(format(contribution.total()))
                    .append(" from ").append(contribution.source().displayName())
                    .append(" (").append(contribution.source().className()).append('#').append(contribution.source().methodName()).append(")\n");
        }
    }

    private static float lastTotal(List<Contribution> contributions) {
        if (contributions.isEmpty()) {
            return 0.0F;
        }

        return contributions.get(contributions.size() - 1).total();
    }

    private static float product(List<Contribution> contributions) {
        float result = 1.0F;
        for (Contribution contribution : contributions) {
            result *= contribution.amount();
        }
        return result;
    }

    private static Source findMmtCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (!className.startsWith("com.inolia_zaicek.more_mod_tetra.")) {
                continue;
            }
            if (className.equals("com.inolia_zaicek.more_mod_tetra.Event.Post.EffectLevelEvent")) {
                continue;
            }

            return new Source(shortClassName(className), element.getMethodName(), humanize(simpleName(className)));
        }

        return new Source("<unknown>", "<unknown>", "<unknown>");
    }

    private static String describe(Object entity) {
        if (!(entity instanceof LivingEntity living)) {
            return entity == null ? "<none>" : entity.toString();
        }

        return living.getScoreboardName() + "[" + ForgeRegistries.ENTITY_TYPES.getKey(living.getType()) + "]";
    }

    private static String shortClassName(String className) {
        return className.substring("com.inolia_zaicek.more_mod_tetra.".length());
    }

    private static String simpleName(String className) {
        int index = className.lastIndexOf('.');
        return index == -1 ? className : className.substring(index + 1);
    }

    private static String humanize(String className) {
        return className
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private record Contribution(String operation, float amount, float total, Source source) {
    }

    private record Source(String className, String methodName, String displayName) {
    }
}
