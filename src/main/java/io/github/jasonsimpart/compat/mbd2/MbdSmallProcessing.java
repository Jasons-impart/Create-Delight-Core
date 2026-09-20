package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.api.recipe.event.TransferProxyRecipeEvent;
import com.lowdragmc.mbd2.common.capability.recipe.ForgeEnergyRecipeCapability;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;

final class MbdSmallProcessing {
    private MbdSmallProcessing() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::convert);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::working);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::use);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::useItem);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::contract);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::ui);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::before);
        NeoForge.EVENT_BUS.addListener(MbdSmallProcessing::after);
    }

    static boolean is(MBDMachine machine, String id) { return machine.getDefinition().id().equals(MbdCompat.id(id)); }

    static void convert(TransferProxyRecipeEvent event) {
        var name = event.recipeType.getRegistryName();
        boolean mortar = name.equals(MbdCompat.id("mortar"));
        if (!mortar && !name.equals(MbdCompat.id("small_centrifugation"))) return;
        event.mbdRecipe = null;
        if (!event.proxyTypeId.toString().equals(mortar ? "create:milling" : "vintageimprovements:centrifugation")
                || !(event.proxyRecipe instanceof ProcessingRecipe<?, ?> source)) return;
        // The small machine has only item slots: fluid recipes belong to the large centrifuge.
        if (!source.getFluidIngredients().isEmpty() || !source.getFluidResults().isEmpty()) return;
        var builder = MBDRecipeBuilder.of(MbdCompat.id(name.getPath() + "/proxy/" + event.proxyRecipeId.getNamespace()
                + "/" + event.proxyRecipeId.getPath()), event.recipeType);
        builder.duration(mortar ? source.getProcessingDuration() : 100);
        source.getIngredients().forEach(builder::inputItems);
        source.getRollableResults().forEach(output -> {
            builder.chance = output.getChance();
            builder.outputItems(output.getStack());
        });
        if (!mortar) {
            builder.chance = 1;
            builder.perTick = true;
            builder.input(ForgeEnergyRecipeCapability.CAP, 100);
        }
        builder.isXEIHidden = !event.recipeType.isProxyRecipeXEIVisible();
        event.mbdRecipe = builder.buildRawRecipe();
    }

    private static void working(MachineOnRecipeWorkingEvent event) {
        if (is(event.machine, "mortar")) event.machine.getRecipeLogic().setProgress(event.machine.getRecipeLogic().getProgress() - 1);
    }

    static boolean grind(MBDMachine machine, Player player, InteractionHand hand) {
        if (!is(machine, "mortar")) return false;
        if (!player.isShiftKeyDown() && !machine.getMachineStateName().equals("working")) return false;
        if (machine.getLevel().isClientSide) return true;
        if (player.isShiftKeyDown()) {
            var output = machine.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, machine.getPos(), Direction.DOWN);
            if (output != null && output.getSlots() > 0) {
                var stack = output.extractItem(0, output.getStackInSlot(0).getCount(), false);
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
        } else if (machine.getMachineStateName().equals("working")) {
            var logic = machine.getRecipeLogic();
            logic.setProgress(Math.min(logic.getDuration(), logic.getProgress() + Math.max(1, logic.getDuration() / 10)));
            machine.getLevel().playSound(null, machine.getPos(), SoundEvents.STONE_HIT, SoundSource.BLOCKS, 1, 1);
        }
        player.swing(hand, true);
        machine.getHolder().setChanged();
        return true;
    }

    private static void use(MachineUseWithoutItemEvent event) {
        if (grind(event.machine, event.player, InteractionHand.MAIN_HAND)) event.setInteractionResult(InteractionResult.SUCCESS);
    }

    private static void useItem(MachineUseItemOnEvent event) {
        if (grind(event.machine, event.player, event.hand)) event.setItemInteractionResult(ItemInteractionResult.SUCCESS);
    }

    static void contract(MachineRecipeModifyEvent.After event) {
        if (!is(event.machine, "contract_executor") || event.recipe == null) return;
        int count = 0, heat = 0;
        for (var direction : Direction.Plane.HORIZONTAL) {
            var state = event.machine.getLevel().getBlockState(event.machine.getPos().relative(direction));
            if (!(state.getBlock() instanceof BlazeBurnerBlock)) continue;
            count++;
            heat += switch (state.getValue(BlazeBurnerBlock.HEAT_LEVEL)) {
                case KINDLED -> 2;
                case SEETHING -> 3;
                default -> 1;
            };
        }
        event.machine.getCustomData().putInt("blazeBurnerCount", count);
        if (count == 0) { event.setRecipe(null); return; }
        var recipe = event.recipe.copy();
        recipe.duration = Math.max(1, (int) (recipe.duration / (heat / Math.sqrt(count))));
        event.setRecipe(recipe);
    }

    private static void ui(MachineUIEvent event) {
        if (!is(event.machine, "contract_executor") || event.ui == null) return;
        event.ui.selectId("blaze_burner_count", com.lowdragmc.lowdraglib2.gui.ui.elements.Label.class).forEach(label ->
                label.bind(com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder.componentS2C(() ->
                        Component.translatable("message.createdelight.blaze_burner_count", event.machine.getCustomData().getInt("blazeBurnerCount"))).build()));
    }

    private static void before(MachineBeforeRecipeWorkingEvent event) {
        if (is(event.machine, "small_centrifugation")) event.machine.triggerGeckolibAnim("", "working", 1);
    }

    private static void after(MachineAfterRecipeWorkingEvent event) {
        if (is(event.machine, "small_centrifugation")) event.machine.triggerGeckolibAnim("", "idle", 1);
    }
}
