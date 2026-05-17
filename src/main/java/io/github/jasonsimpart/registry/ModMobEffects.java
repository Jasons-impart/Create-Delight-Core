package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.content.effect.DaredevilFormEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CreateDelightCore.MODID);

    public static final DeferredHolder<MobEffect, DaredevilFormEffect> DAREDEVIL_FORM = MOB_EFFECTS.register("daredevil_form", DaredevilFormEffect::new);

    private ModMobEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
