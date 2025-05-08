package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class CDCDamageTypes {
    public static final ResourceKey<DamageType> MOLTEN_METAL =key("molten_metal");
    public static final ResourceKey<DamageType> ICE_CREAM =key("ice_cream");
    public static final ResourceKey<DamageType> RADIATION =key("radiation");

    public CDCDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CreateDelightCore.id(name));
    }

    public static void bootstrap(BootstapContext<DamageType> ctx) {
        (new DamageTypeBuilder(ICE_CREAM)).scaling(DamageScaling.ALWAYS).effects(DamageEffects.FREEZING).register(ctx);
        (new DamageTypeBuilder(MOLTEN_METAL)).scaling(DamageScaling.ALWAYS).effects(DamageEffects.BURNING).register(ctx);
        (new DamageTypeBuilder(RADIATION)).scaling(DamageScaling.ALWAYS).effects(DamageEffects.HURT).register(ctx);
    }

    private static DamageSource source(ResourceKey<DamageType> key, LevelReader level) {
        Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(registry.getHolderOrThrow(key));
    }

    public static DamageSource moltenMetal(Level level) {
        return source(MOLTEN_METAL, level);
    }

    public static DamageSource iceCream(Level level) {
        return source(ICE_CREAM, level);
    }

    public static DamageSource radiation(Level level) {
        return source(RADIATION, level);
    }

}
