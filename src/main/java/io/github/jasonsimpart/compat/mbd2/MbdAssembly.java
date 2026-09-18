package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.recipe.event.TransferProxyRecipeEvent;
import com.lowdragmc.mbd2.common.capability.recipe.ItemRecipeCapability;
import com.lowdragmc.mbd2.common.capability.recipe.FluidRecipeCapability;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.MBDMultiblockMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineBeforeRecipeWorkingEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineStructureFormedEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.fluid.FluidTankCapabilityTrait;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Comparator;

/** Legacy ordered assembly ports and deterministic batch conversion of Create sequences. */
final class MbdAssembly {
    private MbdAssembly() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdAssembly::formed);
        NeoForge.EVENT_BUS.addListener(MbdAssembly::before);
        NeoForge.EVENT_BUS.addListener(MbdAssembly::convert);
    }

    private static boolean matches(MBDMachine machine) {
        return machine.getDefinition().id().equals(MbdCompat.id("assembly_line"));
    }

    static void formed(MachineStructureFormedEvent event) {
        if (!matches(event.machine) || !(event.machine instanceof MBDMultiblockMachine machine)) return;
        var facing = machine.getFrontFacing().orElse(Direction.NORTH);
        machine.getParts().sort(Comparator
                .comparingInt((com.lowdragmc.mbd2.api.machine.IMultiPart part) ->
                        ((MBDMachine) part).getTraitByName("fluid_tank") != null ? 0 : 1)
                .thenComparingInt(part -> -facing.getAxis().choose(part.getPos().getX(), part.getPos().getY(), part.getPos().getZ())
                        * facing.getAxisDirection().getStep()));
        // MBD2 builds its proxy before emitting the formed event.
        machine.initCapabilitiesProxy();
        route(machine, ItemRecipeCapability.CAP);
        route(machine, FluidRecipeCapability.CAP);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void route(MBDMultiblockMachine machine, com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability<?> capability) {
        var handlers = machine.getRecipeCapabilitiesProxy().get(com.lowdragmc.mbd2.api.capability.recipe.IO.IN, capability);
        if (handlers == null) return;
        for (int i = 0; i < handlers.size(); i++) handlers.set(i,
                new com.lowdragmc.mbd2.api.capability.recipe.RecipeHandlerSlotsProxy(handlers.get(i), java.util.Set.of(capability.name + "_" + i)));
    }

    private static void assignPorts(com.lowdragmc.mbd2.api.recipe.MBDRecipe recipe) {
        for (var capability : java.util.List.of(ItemRecipeCapability.CAP, FluidRecipeCapability.CAP)) {
            var contents = recipe.getInputContents(capability);
            for (int i = 0; i < contents.size(); i++) contents.get(i).slotName = capability.name + "_" + i;
        }
    }

    static void before(MachineBeforeRecipeWorkingEvent event) {
        if (!matches(event.machine) || !(event.machine instanceof MBDMultiblockMachine machine)) return;
        assignPorts(event.recipe);
        var fluidPorts = machine.getParts().stream().map(part -> ((MBDMachine) part).getTraitByName("fluid_tank"))
                .filter(FluidTankCapabilityTrait.class::isInstance).map(FluidTankCapabilityTrait.class::cast).toList();
        var itemPorts = machine.getParts().stream().map(part -> ((MBDMachine) part).getTraitByName("item_slot"))
                .filter(ItemSlotCapabilityTrait.class::isInstance).map(ItemSlotCapabilityTrait.class::cast).toList();
        var fluids = event.recipe.getInputContents(FluidRecipeCapability.CAP);
        var items = event.recipe.getInputContents(ItemRecipeCapability.CAP);
        if (fluids.size() > fluidPorts.size() || items.size() > itemPorts.size()) { event.setCanceled(true); return; }
        for (int i = 0; i < fluids.size(); i++) {
            var expected = FluidRecipeCapability.CAP.of(fluids.get(i).content);
            var actual = fluidPorts.get(i).storages[0].getFluid();
            if (!expected.test(actual) || actual.getAmount() != expected.amount()) { event.setCanceled(true); return; }
        }
        for (int i = 0; i < items.size(); i++) {
            var expected = ItemRecipeCapability.CAP.of(items.get(i).content);
            var actual = itemPorts.get(i).storage.getStackInSlot(0);
            if (!expected.test(actual) || actual.getCount() != expected.count()) { event.setCanceled(true); return; }
        }
    }

    static void convert(TransferProxyRecipeEvent event) {
        if (!event.recipeType.getRegistryName().equals(MbdCompat.id("assembly_line"))
                || !(event.proxyRecipe instanceof SequencedAssemblyRecipe sequence)) return;
        event.mbdRecipe = null;
        int loops = sequence.getLoops();
        if (loops <= 0 || sequence.getSequence().size() <= 1 || 64 / loops == 0) return;
        var builder = MBDRecipeBuilder.of(ResourceLocation.fromNamespaceAndPath(event.proxyRecipeId.getNamespace(),
                event.proxyRecipeId.getPath() + "_mbd2"), event.recipeType);
        builder.inputItems(new SizedIngredient(sequence.getIngredient(), 64 / loops));
        boolean changed = false;
        for (var step : sequence.getSequence()) {
            var recipe = step.getRecipe();
            if (!recipe.getFluidIngredients().isEmpty()) {
                var original = recipe.getFluidIngredients().getFirst();
                int amount = (int) (original.amount() * 64.0 / loops);
                if (loops != 1 && 64 % loops != 0) amount = amount / 50 * 50;
                if (amount <= 0) return;
                builder.inputFluids(new SizedFluidIngredient(original.ingredient(), amount));
                changed = true;
            }
            if (recipe.getIngredients().size() == 2) {
                boolean retained = recipe instanceof ItemApplicationRecipe application && application.shouldKeepHeldItem();
                builder.chance = retained ? 0 : 1;
                builder.inputItems(new SizedIngredient(recipe.getIngredients().get(1), retained ? 1 : 64));
                builder.chance = 1;
                changed = true;
            }
        }
        if (!changed) return;
        var output = sequence.getResultItem(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)).copy();
        output.setCount((int) (output.getCount() * 64.0 / loops));
        builder.duration((int) (Math.sqrt(loops) * 100)).outputItems(output);
        builder.isXEIHidden = !event.recipeType.isProxyRecipeXEIVisible();
        event.mbdRecipe = builder.buildRawRecipe();
        assignPorts(event.mbdRecipe);
    }
}
