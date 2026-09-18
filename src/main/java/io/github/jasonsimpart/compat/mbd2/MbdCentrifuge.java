package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.recipe.event.TransferProxyRecipeEvent;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineRecipeModifyEvent;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;

final class MbdCentrifuge {
    private MbdCentrifuge() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdCentrifuge::modified);
        NeoForge.EVENT_BUS.addListener(MbdCentrifuge::convert);
    }

    static void modified(MachineRecipeModifyEvent.After event) {
        if (!(event.machine instanceof MBDMultiblockMachine machine)
                || !machine.getDefinition().id().equals(MbdCompat.id("big_centrifuge")) || event.recipe == null) return;
        float speed = 32;
        for (var part : machine.getParts()) {
            if (machine.getLevel().getBlockEntity(part.getPos()) instanceof MBDKineticMachineBlockEntity kinetic
                    && ((com.lowdragmc.mbd2.common.machine.MBDMachine) kinetic.getMetaMachine()).getDefinition().id().equals(MbdCompat.id("create_in"))) {
                speed = Math.max(speed, Math.abs(kinetic.getSpeed()));
            }
        }
        var recipe = event.recipe.copy();
        recipe.duration = (int) (recipe.duration * (4 - (speed - 32) * 3 / 224));
        event.setRecipe(recipe);
    }

    static void convert(TransferProxyRecipeEvent event) {
        if (!event.recipeType.getRegistryName().equals(MbdCompat.id("big_centrifugation"))
                || !event.proxyTypeId.equals(ResourceLocation.parse("vintageimprovements:centrifugation"))) return;
        event.mbdRecipe = null;
        if (!(event.proxyRecipe instanceof ProcessingRecipe<?, ?> source)) return;
        var builder = MBDRecipeBuilder.of(MbdCompat.id("big_centrifugation/proxy/" + event.proxyRecipeId.getNamespace()
                + "/" + event.proxyRecipeId.getPath()), event.recipeType);
        // Old proxy deliberately used MBD's default 100 ticks, rather than Vintage's processing time.
        source.getIngredients().forEach(builder::inputItems);
        source.getFluidIngredients().forEach(builder::inputFluids);
        source.getRollableResults().forEach(output -> {
            builder.chance = output.getChance();
            builder.outputItems(output.getStack());
        });
        builder.chance = 1;
        source.getFluidResults().forEach(builder::outputFluids);
        builder.isXEIHidden = !event.recipeType.isProxyRecipeXEIVisible();
        event.mbdRecipe = builder.buildRawRecipe();
    }
}
