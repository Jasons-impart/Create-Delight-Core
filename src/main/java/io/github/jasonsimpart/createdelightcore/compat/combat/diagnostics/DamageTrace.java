package io.github.jasonsimpart.createdelightcore.compat.combat.diagnostics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** Event-local, bounded history. No entity or event is retained in a global collection. */
public final class DamageTrace {
    private static final AtomicLong IDS = new AtomicLong();
    public static final int HISTORY_LIMIT = 48;
    public final long id = IDS.incrementAndGet();
    final String phase;
    final DamageTrace parent;
    final Supplier<String> subject;
    private final Deque<String> history = new ArrayDeque<>();
    private String firstNonFinite;
    private int omitted;
    boolean boundToEvent;
    boolean reported;
    boolean reportDenied;
    boolean suspicious;

    DamageTrace(String phase, Supplier<String> subject, DamageTrace parent) {
        this.phase = phase;
        this.subject = subject;
        this.parent = parent;
    }

    public void add(String entry) {
        if (history.size() == HISTORY_LIMIT) {
            history.removeFirst();
            omitted++;
        }
        history.addLast(entry);
    }

    public boolean observe(String entry, float result) {
        return observe(entry, (double) result);
    }

    public boolean observe(String entry, double result) {
        add(entry);
        suspicious |= !Double.isFinite(result) || Math.abs(result) >= 1.0E30;
        if (!Double.isFinite(result) && firstNonFinite == null) {
            firstNonFinite = entry;
            return true;
        }
        return false;
    }

    public String snapshot() {
        StringBuilder text = new StringBuilder(1024);
        text.append("phase=").append(phase).append(", parentTrace=")
                .append(parent == null ? "none" : parent.id).append(", firstNonFinite=")
                .append(firstNonFinite == null ? "none observed" : firstNonFinite).append('\n');
        if (omitted > 0) text.append("  earlierEntriesOmitted=").append(omitted).append('\n');
        history.forEach(line -> text.append("  ").append(line).append('\n'));
        return text.toString();
    }

    public String firstNonFinite() { return firstNonFinite; }
}
