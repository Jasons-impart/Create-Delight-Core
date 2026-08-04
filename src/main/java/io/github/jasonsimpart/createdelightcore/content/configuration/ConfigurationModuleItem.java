package io.github.jasonsimpart.createdelightcore.content.configuration;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ConfigurationModuleItem extends Item {
    private final ResourceLocation defaultMode;
    private final ResourceLocation defaultTarget;
    private final int defaultInitialCharge;
    private final int defaultChargeCost;
    private final int defaultMaxCharge;

    public ConfigurationModuleItem(Properties properties, ResourceLocation defaultMode, ResourceLocation defaultTarget,
                                   int defaultInitialCharge, int defaultChargeCost, int defaultMaxCharge) {
        super(properties);
        this.defaultMode = defaultMode;
        this.defaultTarget = defaultTarget;
        this.defaultInitialCharge = defaultInitialCharge;
        this.defaultChargeCost = defaultChargeCost;
        this.defaultMaxCharge = defaultMaxCharge;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putString(ConfigurationModuleManager.TAG_MODE, defaultMode.toString());
        stack.getOrCreateTag().putString(ConfigurationModuleManager.TAG_TARGET, defaultTarget.toString());
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_CHARGE, defaultInitialCharge);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_CHARGE_COST, defaultChargeCost);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_MAX_CHARGE, defaultMaxCharge);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_TIER, 0);
        stack.getOrCreateTag().putInt(ConfigurationModuleManager.TAG_DATA_VERSION, ConfigurationModuleManager.DATA_VERSION);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide) {
            ConfigurationModuleManager.ensureSelectedMode(stack);
        }
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
                player.displayClientMessage(Component.translatable("item.createdelightcore.configuration_module.error.no_modes"), true);
                return InteractionResult.FAIL;
            }
            Optional<BlockItem> targetItem = ConfigurationModuleManager.getTargetBlockItem(selected.get());
            if (targetItem.isEmpty()) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.configuration_module.error.invalid_target"), true);
                return InteractionResult.FAIL;
            }
            target = targetItem.get();
            chargeCost = selected.get().chargeCost();
            requirements = selected.get().extraIngredients();
        }

        boolean creative = player.getAbilities().instabuild;
        int[] removalPlan = null;
        AutoRefillPlan autoRefill = null;
        if (!creative && !context.getLevel().isClientSide) {
            removalPlan = new int[player.getInventory().getContainerSize()];
            if (!reserveRequirements(player.getInventory(), moduleStack, requirements, removalPlan)) {
                player.displayClientMessage(Component.translatable("item.createdelightcore.configuration_module.error.ingredients"), true);
                return InteractionResult.FAIL;
            }
            int currentCharge = ConfigurationModuleManager.getCharge(moduleStack);
            if (currentCharge < chargeCost) {
                autoRefill = findAutoRefillPlan(context.getLevel(), player.getInventory(), moduleStack,
                        chargeCost, currentCharge, removalPlan);
                if (autoRefill == null) {
                    player.displayClientMessage(Component.translatable("item.createdelightcore.configuration_module.error.no_charge"), true);
                    return InteractionResult.FAIL;
                }
                removalPlan = autoRefill.removals();
            }
        }

        ItemStack targetStack = new ItemStack(target);
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), context.isInside());
        UseOnContext targetContext = new UseOnContext(context.getLevel(), player, context.getHand(), targetStack, hit);
        PlacementOffset offset = ConfigurationModulePlacementHelper.findOffset(player, context.getLevel(),
                context.getLevel().getBlockState(context.getClickedPos()), context.getClickedPos(), hit, moduleStack);
        InteractionResult result = offset.isSuccessful()
                ? ConfigurationModulePlacementHelper.placeWithOffset(context.getLevel(), target, player,
                context.getHand(), targetStack, hit, offset)
                : target.place(new BlockPlaceContext(targetContext));

        if (!context.getLevel().isClientSide && result.consumesAction() && !creative) {
            int charge = ConfigurationModuleManager.getCharge(moduleStack);
            if (autoRefill != null) {
                charge = Math.min(ConfigurationModuleManager.getMaxCharge(moduleStack),
                        charge + autoRefill.chargeAdded());
            }
            ConfigurationModuleManager.setCharge(moduleStack, charge - chargeCost);
            consumeRemovalPlan(player.getInventory(), removalPlan);
            if (autoRefill != null) {
                player.displayClientMessage(Component.translatable(
                        "item.createdelightcore.configuration_module.message.auto_refill",
                        autoRefill.refillCount(), autoRefill.chargeAdded()), true);
            }
            player.getInventory().setChanged();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
        return result;
    }

    private static boolean reserveRequirements(Inventory inventory, ItemStack moduleStack,
                                               List<ConfigurationRequirement> requirements, int[] removals) {
        for (ConfigurationRequirement requirement : requirements) {
            if (!reserveIngredient(inventory, moduleStack, requirement.ingredient(), requirement.count(), removals)) {
                return false;
            }
        }
        return true;
    }

    private static AutoRefillPlan findAutoRefillPlan(Level level, Inventory inventory, ItemStack moduleStack,
                                                     int chargeCost, int currentCharge, int[] baseRemovals) {
        int maxCharge = ConfigurationModuleManager.getMaxCharge(moduleStack);
        if (chargeCost > maxCharge) {
            return null;
        }
        List<ConfigurationModuleRefillRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .filter(ConfigurationModuleRefillRecipe.class::isInstance)
                .map(ConfigurationModuleRefillRecipe.class::cast)
                .filter(recipe -> recipe.supportsModule(moduleStack))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();

        AutoRefillPlan best = null;
        int missingCharge = chargeCost - currentCharge;
        for (ConfigurationModuleRefillRecipe recipe : recipes) {
            int refillCount = (missingCharge + recipe.refillCharge() - 1) / recipe.refillCharge();
            int[] trialRemovals = baseRemovals.clone();
            boolean ingredientsAvailable = true;
            for (ConfigurationRequirement requirement : recipe.refillRequirements()) {
                if (!reserveIngredient(inventory, moduleStack, requirement.ingredient(),
                        requirement.count() * refillCount, trialRemovals)) {
                    ingredientsAvailable = false;
                    break;
                }
            }
            if (!ingredientsAvailable) {
                continue;
            }
            int added = refillCount * recipe.refillCharge();
            if (Math.min(maxCharge, currentCharge + added) < chargeCost) {
                continue;
            }
            if (best == null || refillCount < best.refillCount()) {
                best = new AutoRefillPlan(trialRemovals, refillCount, added);
            }
        }
        return best;
    }

    private static boolean reserveIngredient(Inventory inventory, ItemStack moduleStack, Ingredient ingredient,
                                             int count, int[] removals) {
        int remaining = count;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate == moduleStack || candidate.isEmpty() || !ingredient.test(candidate)) {
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
        return remaining == 0;
    }

    private static void consumeRemovalPlan(Inventory inventory, int[] removals) {
        for (int slot = 0; slot < removals.length; slot++) {
            if (removals[slot] > 0) {
                inventory.getItem(slot).shrink(removals[slot]);
            }
        }
    }

    private record AutoRefillPlan(int[] removals, int refillCount, int chargeAdded) {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ConfigurationModuleManager.getSnapshotTarget(stack).ifPresentOrElse(
                target -> tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.mode",
                        target.getDescription()).withStyle(ChatFormatting.GOLD)),
                () -> tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.unselected")
                        .withStyle(ChatFormatting.GRAY))
        );
        ConfigurationModuleManager.getSnapshotTargetId(stack).ifPresent(target -> tooltip.add(Component.translatable(
                "item.createdelightcore.configuration_module.tooltip.target", target.toString())
                .withStyle(ChatFormatting.DARK_GRAY)));
        tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.charge",
                ConfigurationModuleManager.getCharge(stack), ConfigurationModuleManager.getMaxCharge(stack))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.cost",
                ConfigurationModuleManager.getSnapshotChargeCost(stack)).withStyle(ChatFormatting.BLUE));
        for (ItemStack ingredient : ConfigurationModuleManager.getSnapshotExtraIngredients(stack)) {
            tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.ingredient",
                    ingredient.getHoverName(), ingredient.getCount()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.control",
                        ConfigurationModuleKeys.MODIFIER.getTranslatedKeyMessage())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.auto_refill")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxCharge = ConfigurationModuleManager.getMaxCharge(stack);
        return maxCharge <= 0 ? 0 : Math.round(13.0F * ConfigurationModuleManager.getCharge(stack) / maxCharge);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int maxCharge = ConfigurationModuleManager.getMaxCharge(stack);
        float ratio = maxCharge <= 0 ? 0.0F : (float) ConfigurationModuleManager.getCharge(stack) / maxCharge;
        return Mth.hsvToRgb(Math.max(0.0F, ratio) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final ConfigurationModuleItemRenderer renderer = new ConfigurationModuleItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}
