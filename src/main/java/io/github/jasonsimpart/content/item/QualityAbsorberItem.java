package io.github.jasonsimpart.content.item;

import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QualityAbsorberItem extends Item {
    private static final String QUALITY_FOOD_MODID = "quality_food";
    private static final String LIGHTMANS_CURRENCY_MODID = "lightmanscurrency";
    private static final String CURRENCY_COMPAT_CLASS = "io.github.jasonsimpart.compat.qualityfood.QualityFoodCurrencyCompat";
    private static final String FAILURE_MESSAGE = "message.createdelightcore.quality_absorber.failed";
    private static final String PENDING_RECOVERY_MESSAGE = "message.createdelightcore.quality_absorber.pending_recovery";
    private static final int COOLDOWN_TICKS = 20;

    public QualityAbsorberItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!ModList.get().isLoaded(QUALITY_FOOD_MODID) || !ModList.get().isLoaded(LIGHTMANS_CURRENCY_MODID)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide() && hasPendingRecovery(player)) {
            player.displayClientMessage(Component.translatable(PENDING_RECOVERY_MESSAGE), true);
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && timeLeft > 0 && entity instanceof Player player) {
            player.getCooldowns().removeCooldown(this);
        }
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            AbsorptionResult result = tryAbsorbQuality(player);
            if (result == AbsorptionResult.ROLLED_BACK_FAILURE) {
                player.getCooldowns().removeCooldown(this);
                player.displayClientMessage(Component.translatable(FAILURE_MESSAGE), true);
            } else if (result == AbsorptionResult.PENDING_RECOVERY) {
                player.displayClientMessage(Component.translatable(PENDING_RECOVERY_MESSAGE), true);
            }
        }
        return stack;
    }

    private static AbsorptionResult tryAbsorbQuality(Player player) {
        if (!ModList.get().isLoaded(QUALITY_FOOD_MODID) || !ModList.get().isLoaded(LIGHTMANS_CURRENCY_MODID)) {
            return AbsorptionResult.ROLLED_BACK_FAILURE;
        }

        try {
            Class<?> compatClass = Class.forName(CURRENCY_COMPAT_CLASS);
            Method method = compatClass.getMethod("absorbInventoryQuality", Player.class);
            Object result = method.invoke(null, player);
            if (result instanceof Enum<?> enumResult) {
                return switch (enumResult.name()) {
                    case "SUCCESS" -> AbsorptionResult.SUCCESS;
                    case "PENDING_RECOVERY" -> AbsorptionResult.PENDING_RECOVERY;
                    default -> AbsorptionResult.ROLLED_BACK_FAILURE;
                };
            }
            if (result instanceof Boolean success) {
                return success ? AbsorptionResult.SUCCESS : AbsorptionResult.ROLLED_BACK_FAILURE;
            }
            return AbsorptionResult.ROLLED_BACK_FAILURE;
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocationException
                    ? invocationException.getCause()
                    : exception;
            CreateDelightCore.LOGGER.warn("Failed to use createdelightcore Quality Food currency compat", cause);
            return AbsorptionResult.ROLLED_BACK_FAILURE;
        } catch (LinkageError exception) {
            CreateDelightCore.LOGGER.warn("Quality Food currency compat is unavailable", exception);
            return AbsorptionResult.ROLLED_BACK_FAILURE;
        }
    }

    private static boolean hasPendingRecovery(Player player) {
        try {
            Class<?> compatClass = Class.forName(CURRENCY_COMPAT_CLASS);
            Method method = compatClass.getMethod("hasPendingRecovery", Player.class);
            Object result = method.invoke(null, player);
            return result instanceof Boolean pending && pending;
        } catch (ReflectiveOperationException exception) {
            return false;
        } catch (LinkageError exception) {
            return false;
        }
    }

    private enum AbsorptionResult {
        SUCCESS,
        ROLLED_BACK_FAILURE,
        PENDING_RECOVERY
    }
}
