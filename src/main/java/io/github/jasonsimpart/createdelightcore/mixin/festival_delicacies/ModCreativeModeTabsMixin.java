package io.github.jasonsimpart.createdelightcore.mixin.festival_delicacies;

import cn.foggyhillside.festival_delicacies.registry.ModCreativeModeTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(value = ModCreativeModeTabs.class, remap = false)
public class ModCreativeModeTabsMixin {
    private static final Set<String> CREATEDELIGHTCORE_HIDDEN_ITEMS = Set.of(
            "apricot",
            "apricot_sapling",
            "apricot_leaves",
            "apricot_log",
            "hawthorn",
            "hawthorn_sapling",
            "hawthorn_leaves",
            "hawthorn_log",
            "jujube_sapling",
            "jujube_leaves",
            "jujube_log",
            "olive_sapling",
            "olive_leaves",
            "olive_log",
            "peach",
            "peach_sapling",
            "peach_leaves",
            "peach_log",
            "persimmon",
            "persimmon_sapling",
            "persimmon_leaves",
            "persimmon_log",
            "tangerine",
            "tangerine_sapling",
            "tangerine_leaves",
            "tangerine_log",
            "walnut_sapling",
            "walnut_leaves",
            "walnut_log"
    );

    @Redirect(method = "lambda$static$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/level/ItemLike;)V", remap = true))
    private static void createdelightcore$hideTreeItems(CreativeModeTab.Output output, ItemLike item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.asItem());
        if (!"festival_delicacies".equals(id.getNamespace()) || !CREATEDELIGHTCORE_HIDDEN_ITEMS.contains(id.getPath())) {
            output.accept(item);
        }
    }
}
