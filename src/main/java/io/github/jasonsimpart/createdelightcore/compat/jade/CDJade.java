package io.github.jasonsimpart.createdelightcore.compat.jade;

import com.google.common.collect.Maps;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import org.apache.commons.compress.utils.Lists;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.util.CommonProxy;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@WailaPlugin
public class CDJade implements IWailaPlugin {
    public static IWailaClientRegistration client;
    private final List<IWailaPlugin> plugins = Lists.newArrayList();

    public CDJade() {
        Map<String, Supplier<Supplier<IWailaPlugin>>> loaders = Maps.newHashMap();
        loaders.put("cmr", () -> CDPlugin::new);
        loaders.forEach((modid, loader) -> {
            if (!CommonProxy.isModLoaded(modid)) {
                return;
            }
            try {
                plugins.add(loader.get().get());
            } catch (Throwable e) {
                CreateDelightCore.LOGGER.error("Failed to load plugin for %s".formatted(modid), e);
            }
        });
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        plugins.removeIf($ -> {
            try {
                $.register(registration);
                return false;
            } catch (Throwable e) {
                CreateDelightCore.LOGGER.error("Failed to register plugin %s".formatted($.getClass().getName()), e);
                return true;
            }
        });
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        client = registration;
        plugins.forEach($ -> $.registerClient(registration));
    }


}
