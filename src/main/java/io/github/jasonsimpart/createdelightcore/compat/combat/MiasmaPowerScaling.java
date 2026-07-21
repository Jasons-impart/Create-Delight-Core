package io.github.jasonsimpart.createdelightcore.compat.combat;

public final class MiasmaPowerScaling {
    private MiasmaPowerScaling() {
    }

    public static float getMiasmaPower(int spellLevel, float rawMultiplier) {
        float base = 2.0F + spellLevel - 1.0F;
        float effectiveMultiplier;
        if (rawMultiplier <= 1.0F) {
            effectiveMultiplier = rawMultiplier;
        } else {
            effectiveMultiplier = 1.0F + 4.0F * (rawMultiplier - 1.0F) / (rawMultiplier + 3.0F);
        }
        return base * effectiveMultiplier;
    }
}
