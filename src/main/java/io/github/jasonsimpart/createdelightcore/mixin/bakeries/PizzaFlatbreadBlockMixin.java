package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.block.pizza.PizzaFlatbreadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PizzaFlatbreadBlock.class)
public class PizzaFlatbreadBlockMixin {
    @Unique
    private static final ResourceLocation create_Delight_Core$ketchupBottle =
            ResourceLocation.parse("create_bic_bit:ketchup_bottle");

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 0
            )
    )
    private boolean create_Delight_Core$onlyKetchupBottleAsPizzaSauce(ItemStack stack, TagKey<Item> ignoredTag) {
        return create_Delight_Core$isKetchupBottle(stack);
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
                    ordinal = 0
            )
    )
    private void create_Delight_Core$returnBottleAfterUsingPizzaSauce(
            ItemStack stack,
            int amount,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (player.getAbilities().instabuild) {
            return;
        }

        boolean isKetchupBottle = create_Delight_Core$isKetchupBottle(stack);
        stack.shrink(amount);

        if (level.isClientSide || !isKetchupBottle) {
            return;
        }

        ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, glassBottle);
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, glassBottle);
        }
    }

    @Unique
    private static boolean create_Delight_Core$isKetchupBottle(ItemStack stack) {
        return create_Delight_Core$ketchupBottle.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }
}
