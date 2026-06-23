package io.github.jasonsimpart.compat.waystones;

import io.github.jasonsimpart.Config;
import net.blay09.mods.waystones.api.event.WaystoneTeleportEvent;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.blay09.mods.waystones.requirement.CombinedRequirement;
import net.blay09.mods.waystones.requirement.ExperienceLevelRequirement;
import net.blay09.mods.waystones.requirement.ExperiencePointsRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WaystonesCurrencyCompat {
    private WaystonesCurrencyCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WaystonesCurrencyCompat::replaceTeleportRequirements);
    }

    private static void replaceTeleportRequirements(WaystoneTeleportEvent.Pre event) {
        if (!Config.ENABLE_WAYSTONES_MONEY_TELEPORT.get()) {
            return;
        }

        Entity entity = event.getContext().getEntity();
        if (!(entity instanceof Player player) || player.getAbilities().instabuild) {
            return;
        }

        WarpRequirement replacement = replaceXpRequirements(event.getRequirements());
        if (replacement == event.getRequirements()) {
            return;
        }

        event.setRequirements(replacement);
    }

    public static WarpRequirement replaceXpRequirements(WarpRequirement requirement) {
        if (!Config.ENABLE_WAYSTONES_MONEY_TELEPORT.get()) {
            return requirement;
        }

        Replacement replacement = collectReplacement(requirement);
        if (replacement.costUnits() <= 0) {
            return requirement;
        }

        List<WarpRequirement> requirements = new ArrayList<>(replacement.remaining());
        requirements.add(new WaystoneMoneyRequirement(replacement.costUnits()));
        return new CombinedRequirement(requirements);
    }

    private static Replacement collectReplacement(WarpRequirement requirement) {
        if (requirement instanceof CombinedRequirement combined) {
            int costUnits = 0;
            List<WarpRequirement> remaining = new ArrayList<>();
            for (WarpRequirement child : combined.getRequirements()) {
                Replacement replacement = collectReplacement(child);
                costUnits += replacement.costUnits();
                remaining.addAll(replacement.remaining());
            }
            return new Replacement(costUnits, remaining);
        }

        if (requirement instanceof ExperienceLevelRequirement levelRequirement) {
            return new Replacement(levelRequirement.getLevels(), List.of());
        }

        if (requirement instanceof ExperiencePointsRequirement pointsRequirement) {
            return new Replacement(pointsRequirement.getPoints(), List.of());
        }

        return new Replacement(0, List.of(requirement));
    }

    private record Replacement(int costUnits, Collection<WarpRequirement> remaining) {
    }
}
