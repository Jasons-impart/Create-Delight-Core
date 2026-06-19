package io.github.jasonsimpart.mixin.createmetallurgy;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(targets = "fr.lucreeper74.createmetallurgy.compat.jei.CreateMetallurgyJEI", remap = false)
public class CreateMetallurgyJEIMixin {
    private static final ResourceLocation ICE_AND_FIRE_DRAGON_EGG =
            ResourceLocation.fromNamespaceAndPath("iceandfire", "dragon_egg");

    @Redirect(
            method = "registerIngredients",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;stream()Ljava/util/stream/Stream;"),
            remap = true
    )
    private Stream<EntityType<?>> createdelightcore$skipUnsafeEntityIngredient(DefaultedRegistry<EntityType<?>> registry) {
        return registry.stream()
                .filter(entityType -> !ICE_AND_FIRE_DRAGON_EGG.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)));
    }
}
