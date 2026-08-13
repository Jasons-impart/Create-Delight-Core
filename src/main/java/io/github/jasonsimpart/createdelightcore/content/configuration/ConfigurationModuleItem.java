package io.github.jasonsimpart.createdelightcore.content.configuration;

import com.simibubi.create.AllKeys;
import io.github.jasonsimpart.createdelightcore.compat.sophisticatedbackpacks.SophisticatedBackpacksCompat;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.fml.ModList;
import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        if (context.getLevel().isClientSide) {
            Optional<BlockItem> snapshotTarget = ConfigurationModuleManager.getSnapshotTarget(moduleStack);
            if (snapshotTarget.isEmpty()) {
                return InteractionResult.PASS;
            }
            target = snapshotTarget.get();
            chargeCost = ConfigurationModuleManager.getSnapshotChargeCost(moduleStack);
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
        }

        boolean creative = player.getAbilities().instabuild;
        AutoRefillPlan autoRefill = null;
        SophisticatedBackpacksCompat.RefillReservation backpackRefill = null;
        if (!creative && !context.getLevel().isClientSide) {
            int currentCharge = ConfigurationModuleManager.getCharge(moduleStack);
            if (currentCharge < chargeCost) {
                IItemHandler playerInventory = new InvWrapper(player.getInventory());
                autoRefill = findAutoRefillPlan(context.getLevel(), playerInventory, moduleStack,
                        chargeCost, currentCharge);
                if (autoRefill == null && ModList.get().isLoaded("sophisticatedbackpacks")
                        && ModList.get().isLoaded("sophisticatedcore")) {
                    backpackRefill = SophisticatedBackpacksCompat.reserveRefillForUse(player, moduleStack,
                            context.getHand(), chargeCost, playerInventory).orElse(null);
                }
                if (backpackRefill == null && autoRefill == null) {
                    player.displayClientMessage(Component.translatable("item.createdelightcore.configuration_module.error.no_charge"), true);
                    return InteractionResult.FAIL;
                }
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
            if (backpackRefill != null) {
                backpackRefill.commit(moduleStack);
                charge = ConfigurationModuleManager.getCharge(moduleStack);
                player.displayClientMessage(Component.translatable(
                        "item.createdelightcore.configuration_module.message.auto_refill",
                        backpackRefill.plan().refillCount(), backpackRefill.plan().chargeAdded()), true);
            } else if (autoRefill != null) {
                charge = Math.min(ConfigurationModuleManager.getMaxCharge(moduleStack),
                        charge + autoRefill.chargeAdded());
            }
            ConfigurationModuleManager.setCharge(moduleStack, charge - chargeCost);
            if (autoRefill != null) {
                consumeRemovalPlan(new InvWrapper(player.getInventory()), autoRefill.removals());
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

    /**
     * Consumes as many complete refill batches as fit in the remaining charge capacity.
     * A partial batch is never consumed, so components are not wasted to fill a remainder.
     */
    public static Optional<RefillPlan> planRefillToCapacityWithoutOverflow(Level level, IItemHandler inventory,
                                                                             ItemStack moduleStack) {
        return planRefillToCapacityWithoutOverflow(level, List.of(inventory), moduleStack);
    }

    /**
     * Plans complete refill batches across inventories in priority order, without consuming ingredients.
     * Each ingredient is reserved from an earlier inventory before trying the next one.
     */
    public static Optional<RefillPlan> planRefillToCapacityWithoutOverflow(Level level,
                                                                             List<IItemHandler> inventories,
                                                                             ItemStack moduleStack) {
        if (inventories.isEmpty()) {
            return Optional.empty();
        }
        int currentCharge = ConfigurationModuleManager.getCharge(moduleStack);
        int maxCharge = ConfigurationModuleManager.getMaxCharge(moduleStack);
        if (currentCharge >= maxCharge) {
            return Optional.empty();
        }
        List<ConfigurationModuleRefillRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .filter(ConfigurationModuleRefillRecipe.class::isInstance)
                .map(ConfigurationModuleRefillRecipe.class::cast)
                .filter(recipe -> recipe.supportsModule(moduleStack))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();

        RefillPlan plan = null;
        for (ConfigurationModuleRefillRecipe recipe : recipes) {
            int maxRefillCount = (maxCharge - currentCharge) / recipe.refillCharge();
            for (int refillCount = maxRefillCount; refillCount > 0; refillCount--) {
                List<int[]> removals = inventories.stream()
                        .map(inventory -> new int[inventory.getSlots()])
                        .toList();
                boolean ingredientsAvailable = true;
                for (ConfigurationRequirement requirement : recipe.refillRequirements()) {
                    if (!reserveIngredient(inventories, moduleStack, requirement.ingredient(),
                            requirement.count() * refillCount, removals)) {
                        ingredientsAvailable = false;
                        break;
                    }
                }
                if (!ingredientsAvailable) {
                    continue;
                }
                int addedCharge = refillCount * recipe.refillCharge();
                if (plan == null || addedCharge > plan.chargeAdded()) {
                    plan = new RefillPlan(List.copyOf(inventories), removals, refillCount, addedCharge);
                }
                break;
            }
        }
        if (plan == null) {
            return Optional.empty();
        }
        return Optional.of(plan);
    }

    public static int applyRefillPlan(ItemStack moduleStack, RefillPlan plan) {
        for (int inventoryIndex = 0; inventoryIndex < plan.inventories().size(); inventoryIndex++) {
            consumeRemovalPlan(plan.inventories().get(inventoryIndex), plan.removals().get(inventoryIndex));
        }
        ConfigurationModuleManager.setCharge(moduleStack,
                ConfigurationModuleManager.getCharge(moduleStack) + plan.chargeAdded());
        return plan.chargeAdded();
    }

    public static int refillToCapacityWithoutOverflow(Level level, IItemHandler inventory, ItemStack moduleStack) {
        return planRefillToCapacityWithoutOverflow(level, inventory, moduleStack)
                .map(plan -> applyRefillPlan(moduleStack, plan))
                .orElse(0);
    }

    private static AutoRefillPlan findAutoRefillPlan(Level level, IItemHandler inventory, ItemStack moduleStack,
                                                     int chargeCost, int currentCharge) {
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
            if (refillCount > (maxCharge - currentCharge) / recipe.refillCharge()) {
                continue;
            }
            int[] trialRemovals = new int[inventory.getSlots()];
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
            if (currentCharge + added < chargeCost) {
                continue;
            }
            if (best == null || refillCount < best.refillCount()) {
                best = new AutoRefillPlan(trialRemovals, refillCount, added);
            }
        }
        return best;
    }

    private static boolean reserveIngredient(IItemHandler inventory, ItemStack moduleStack, Ingredient ingredient,
                                             int count, int[] removals) {
        return reserveIngredient(List.of(inventory), moduleStack, ingredient, count, List.of(removals));
    }

    private static boolean reserveIngredient(List<IItemHandler> inventories, ItemStack moduleStack,
                                             Ingredient ingredient, int count, List<int[]> removals) {
        int remaining = count;
        for (int inventoryIndex = 0; inventoryIndex < inventories.size() && remaining > 0; inventoryIndex++) {
            IItemHandler inventory = inventories.get(inventoryIndex);
            int[] inventoryRemovals = removals.get(inventoryIndex);
            for (int slot = 0; slot < inventory.getSlots() && remaining > 0; slot++) {
                ItemStack candidate = inventory.getStackInSlot(slot);
                if (candidate == moduleStack || candidate.isEmpty() || !ingredient.test(candidate)) {
                    continue;
                }
                int available = candidate.getCount() - inventoryRemovals[slot];
                if (available <= 0) {
                    continue;
                }
                int taken = Math.min(available, remaining);
                inventoryRemovals[slot] += taken;
                remaining -= taken;
            }
        }
        return remaining == 0;
    }

    private static void consumeRemovalPlan(IItemHandler inventory, int[] removals) {
        for (int slot = 0; slot < removals.length; slot++) {
            if (removals[slot] > 0) {
                inventory.extractItem(slot, removals[slot], false);
            }
        }
    }

    private record AutoRefillPlan(int[] removals, int refillCount, int chargeAdded) {
    }

    public record RefillPlan(List<IItemHandler> inventories, List<int[]> removals, int refillCount, int chargeAdded) {
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
        tooltip.add(Component.translatable("item.createdelightcore.configuration_module.tooltip.control",
                        AllKeys.TOOLBELT.getKeybind().getTranslatedKeyMessage())
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
}
