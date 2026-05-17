package io.github.jasonsimpart.content.effect;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class DaredevilFormEffect extends MobEffect {
    public DaredevilFormEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B0000);
        addAttributeModifier(Attributes.MAX_HEALTH, id("daredevil_form_health_decrease"), -0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ARMOR, id("daredevil_form_armor_decrease"), -0.01D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, id("daredevil_form_attack_increase"), 0.02D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, id("daredevil_form_attack_speed_increase"), 0.02D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path);
    }
}
