package io.github.jasonsimpart.compat.improvedmobs;

import java.util.List;

/** Shared by the server drop handler and client acquisition hints; no IM class loading. */
public final class DifficultyLootRules {
    public record Rule(String entity, String item, int difficulty, double chance) {}

    public static final List<Rule> RULES = List.of(
            new Rule("cockatrice", "cockatrice_eye", 100, .5),
            new Rule("dread_lich", "dragonsteel_ice_ingot", 150, .1),
            new Rule("dread_knight", "dragonsteel_ice_ingot", 150, .1),
            new Rule("dread_thrall", "dragonsteel_ice_ingot", 150, .05),
            new Rule("dread_ghoul", "dragonsteel_ice_ingot", 150, .05));

    private DifficultyLootRules() {}
}
