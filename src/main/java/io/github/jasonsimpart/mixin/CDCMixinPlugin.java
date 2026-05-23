package io.github.jasonsimpart.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CDCMixinPlugin implements IMixinConfigPlugin {
    private static final String ALEXSCAVES_MIXIN_PACKAGE = "io.github.jasonsimpart.mixin.alexscaves.";
    private static final String QUARK_MIXIN_PACKAGE = "io.github.jasonsimpart.mixin.quark.";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(ALEXSCAVES_MIXIN_PACKAGE)) {
            return isModLoaded("alexscaves");
        }
        if (mixinClassName.startsWith(QUARK_MIXIN_PACKAGE)) {
            return isModLoaded("quark");
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

    private static boolean isModLoaded(String modId) {
        LoadingModList loadingModList = LoadingModList.get();
        return loadingModList != null && loadingModList.getModFileById(modId) != null;
    }
}
