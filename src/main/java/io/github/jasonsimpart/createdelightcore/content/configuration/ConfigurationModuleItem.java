package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ConfigurationModuleItem extends Item {
    public ConfigurationModuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putString(ConfigurationModuleManager.TAG_MODE, "createdelightcore:shaft");
        stack.getOrCreateTag().putString(ConfigurationModuleManager.TAG_TARGET, "create:shaft");
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_CHARGE, 4);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_CHARGE_COST, 1);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_MAX_CHARGE, 64);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_TIER, 0);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_DATA_VERSION, ConfigurationModuleManager.DATA_VERSION);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack moduleStack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockItem target;
        int chargeCost;
        List<ConfigurationRequirement> requirements;
        if (context.getLevel().isClientSide) {
            Optional<BlockItem> snapshotTarget = ConfigurationModuleManager.getSnapshotTarget(moduleStack);
            if (snapshotTarget.isEmpty()) {
                return InteractionResult.PASS;
            }
            target = snapshotTarget.get();
            chargeCost = ConfigurationModuleManager.getSnapshotChargeCost(moduleStack);
            requirements = List.of();
        } else {
            Optional<ConfigurationMode> selected = ConfigurationModuleManager.ensureSelectedMode(moduleStack);
            if (selected.isEmpty()) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.kinetic_configuration_module.error.no_modes"), true);
                return InteractionResult.FAIL;
            }
            Optional<BlockItem> targetItem = ConfigurationModuleManager.getTargetBlockItem(selected.get());
            if (targetItem.isEmpty()) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.kinetic_configuration_module.error.invalid_target"), true);
                return InteractionResult.FAIL;
            }
            target = targetItem.get();
            chargeCost = selected.get().chargeCost();
            requirements = selected.get().extraIngredients();
        }

        boolean creative = player.getAbilities().instabuild;
        if (!creative && ConfigurationModuleManager.getCharge(moduleStack) < chargeCost) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.kinetic_configuration_module.error.no_charge"), true);
            }
            return InteractionResult.FAIL;
        }

        int[] removalPlan = null;
        if (!creative && !context.getLevel().isClientSide) {
            removalPlan = createRemovalPlan(player.getInventory(), moduleStack, requirements);
            if (removalPlan == null) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.kinetic_configuration_module.error.ingredients"), true);
                return InteractionResult.FAIL;
            }
        }

        ItemStack targetStack = new ItemStack(target);
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), context.isInside());
        UseOnContext targetContext = new UseOnContext(context.getLevel(), player, context.getHand(), targetStack, hit);
        InteractionResult result = target.useOn(targetContext);

        if (!context.getLevel().isClientSide && result.consumesAction() && !creative) {
            ConfigurationModuleManager.setCharge(moduleStack,
                    ConfigurationModuleManager.getCharge(moduleStack) - chargeCost);
            consumeRemovalPlan(player.getInventory(), removalPlan);
            player.getInventory().setChanged();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
        return result;
    }

    private static int[] createRemovalPlan(Inventory inventory, ItemStack moduleStack,
                                           List<ConfigurationRequirement> requirements) {
        int[] removals = new int[inventory.getContainerSize()];
        for (ConfigurationRequirement requirement : requirements) {
            int remaining = requirement.count();
            for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
                ItemStack candidate = inventory.getItem(slot);
                if (candidate == moduleStack || candidate.isEmpty() || !requirement.ingredient().test(candidate)) {
                    continue;
                }
                int available = candidate.getCount() - removals[slot];
                if (available <= 0) {
                    continue;
                }
                int taken = Math.min(available, remaining);
                removals[slot] += taken;
                remaining -= taken;
            }
            if (remaining > 0) {
                return null;
            }
        }
        return removals;
    }

    private static void consumeRemovalPlan(Inventory inventory, int[] removals) {
        for (int slot = 0; slot < removals.length; slot++) {
            if (removals[slot] > 0) {
                inventory.getItem(slot).shrink(removals[slot]);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ConfigurationModuleManager.getSnapshotTarget(stack).ifPresentOrElse(
                target -> tooltip.add(Component.translatable("item.createdelightcore.kinetic_configuration_module.tooltip.mode",
                        target.getDescription()).withStyle(ChatFormatting.GOLD)),
                () -> tooltip.add(Component.translatable("item.createdelightcore.kinetic_configuration_module.tooltip.unselected")
                        .withStyle(ChatFormatting.GRAY))
        );
        tooltip.add(Component.translatable("item.createdelightcore.kinetic_configuration_module.tooltip.charge",
                ConfigurationModuleManager.getCharge(stack), ConfigurationModuleManager.getMaxCharge(stack))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.createdelightcore.kinetic_configuration_module.tooltip.control")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
