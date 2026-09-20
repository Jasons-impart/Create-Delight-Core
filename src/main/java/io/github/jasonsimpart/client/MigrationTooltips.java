package io.github.jasonsimpart.client;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import io.github.jasonsimpart.compat.improvedmobs.DifficultyLootRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class MigrationTooltips {
    private MigrationTooltips() {}

    public static void append(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (!(stack.getItem() instanceof SequencedAssemblyItem) && stack.has(AllDataComponents.SEQUENCED_ASSEMBLY)) {
            event.getToolTip().add(Component.translatable("tooltip.createdelightcore.sequenced_assembly_explanation"));
        }
        if (!ModList.get().isLoaded("improvedmobs")) return;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("iceandfire")) return;
        for (var rule : DifficultyLootRules.RULES) {
            if (!rule.item().equals(id.getPath())) continue;
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath("iceandfire", rule.entity()))
                    .ifPresent(entity -> event.getToolTip().add(Component.translatable(
                            "tooltip.createdelightcore.difficulty_loot", entity.getDescription(), rule.difficulty())));
        }
    }
}
