package io.github.jasonsimpart.compat.improvedmobs;

/** Pack difficulty rules, independent of the optional mod's storage implementation. */
public final class DifficultyRules {
    private DifficultyRules() {}

    public static double deathPenalty(double difficulty) {
        if (!Double.isFinite(difficulty) || difficulty <= 0) return 0;
        double scaled = Math.min(difficulty, 250) / 250;
        return Math.floor((scaled * .1 + (1 - scaled) * .4) * difficulty / 5) * 5;
    }
}
