package io.github.jasonsimpart.createdelightcore.mixin.northeastchinadelight;

import com.gunmu.northeast_china_delight.item.SeasoningBottleItem;
import io.github.jasonsimpart.createdelightcore.compat.northeastchinadelight.PackVinegar;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(value = SeasoningBottleItem.class, remap = false)
public abstract class VinegarBottleItemMixin extends Item {
    protected VinegarBottleItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (PackVinegar.isBottle(held) && !SeasoningBottleItem.isFull(held)) {
            return InteractionResultHolder.fail(held);
        }
        return super.use(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return PackVinegar.isBottle(stack) ? UseAnim.DRINK : super.getUseAnimation(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!PackVinegar.isBottle(stack)) return super.finishUsingItem(stack, level, consumer);
        if (!SeasoningBottleItem.isFull(stack) || !stack.isEdible()) return stack;

        ItemStack remainder = super.finishUsingItem(stack, level, consumer);
        if (consumer instanceof Player player && player.getAbilities().instabuild) return remainder;

        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (remainder.isEmpty()) return bottle;
        if (!level.isClientSide && consumer instanceof Player player && !player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
        return remainder;
    }
}
