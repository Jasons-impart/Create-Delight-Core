package io.github.jasonsimpart.mixin;

import io.github.jasonsimpart.util.ModIds;
import io.github.jasonsimpart.util.OptionalMods;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class CDCMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_ROOT = "io.github.jasonsimpart.mixin.";
    private static final String ECLIPTICSEASONS_PACKAGE = MIXIN_ROOT + "eclipticseasons.";
    private static final String CREATE_METALLURGY_PACKAGE = MIXIN_ROOT + "createmetallurgy.";
    private static final String CREATE_PACKAGE = MIXIN_ROOT + "create.";
    private static final String XAERO_PACKAGE = MIXIN_ROOT + "xaero.";

    private static final List<Rule> RULES = List.of(
            Rule.forPackage(MIXIN_ROOT + "lightmanscurrency.", ModIds.LIGHTMANS_CURRENCY),
            Rule.forPackage(MIXIN_ROOT + "createconnected.", ModIds.CREATE_CONNECTED),
            Rule.forPackage(MIXIN_ROOT + "alexscaves.", ModIds.ALEXSCAVES),
            Rule.forPackage(MIXIN_ROOT + "cmr.", ModIds.CMR),
            Rule.forPackage(ECLIPTICSEASONS_PACKAGE + "Quality", ModIds.ECLIPTIC_SEASONS, ModIds.QUALITY_FOOD),
            Rule.forPackage(ECLIPTICSEASONS_PACKAGE, ModIds.ECLIPTIC_SEASONS),
            Rule.forPackage(MIXIN_ROOT + "iceandfire.", ModIds.ICE_AND_FIRE),
            Rule.forClass(CREATE_METALLURGY_PACKAGE + "CreateMetallurgyJEIMixin", ModIds.CREATE_METALLURGY, ModIds.JEI),
            Rule.forPackage(CREATE_METALLURGY_PACKAGE, ModIds.CREATE_METALLURGY),
            Rule.custom(CREATE_PACKAGE,
                    name -> name.endsWith("CategoryMixin") ? ModIds.JEI : null,
                    ModIds.CREATE),
            Rule.forPackage(MIXIN_ROOT + "kubejs.", ModIds.KUBEJS),
            Rule.forPackage(MIXIN_ROOT + "northstar.", ModIds.NORTHSTAR),
            Rule.forPackage(MIXIN_ROOT + "quark.", ModIds.QUARK),
            Rule.forPackage(MIXIN_ROOT + "vintageimprovements.", ModIds.VINTAGE_IMPROVEMENTS),
            Rule.forPackage(MIXIN_ROOT + "waystones.", ModIds.WAYSTONES, ModIds.LIGHTMANS_CURRENCY),
            Rule.forClass(XAERO_PACKAGE + "PatreonMixin", ModIds.XAEROLIB),
            Rule.forClass(XAERO_PACKAGE + "MinimapInternetMixin", ModIds.XAERO_MINIMAP),
            Rule.forClass(XAERO_PACKAGE + "WorldMapInternetMixin", ModIds.XAERO_WORLDMAP)
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (Rule rule : RULES) {
            Boolean decision = rule.decide(mixinClassName);
            if (decision != null) {
                return decision;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private record Rule(Predicate<String> matches, java.util.function.Function<String, String[]> requiredMods) {
        Boolean decide(String mixinClassName) {
            if (!matches.test(mixinClassName)) {
                return null;
            }
            for (String modId : requiredMods.apply(mixinClassName)) {
                if (!OptionalMods.isLoaded(modId)) {
                    return false;
                }
            }
            return true;
        }

        static Rule forPackage(String prefix, String... modIds) {
            return new Rule(name -> name.startsWith(prefix), name -> modIds);
        }

        static Rule forClass(String className, String... modIds) {
            return new Rule(className::equals, name -> modIds);
        }

        /**
         * Package rule with a per-class extra requirement: {@code extra} returns an additional
         * required mod id for this class, or null when the package-level mods suffice.
         */
        static Rule custom(String prefix, java.util.function.Function<String, String> extra, String... modIds) {
            return new Rule(name -> name.startsWith(prefix), name -> {
                String extraMod = extra.apply(name);
                if (extraMod == null) {
                    return modIds;
                }
                String[] all = java.util.Arrays.copyOf(modIds, modIds.length + 1);
                all[modIds.length] = extraMod;
                return all;
            });
        }
    }
}
