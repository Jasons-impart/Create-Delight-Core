package io.github.jasonsimpart.createdelightcore.mixin.bakeries;

import com.renyigesai.bakeries.block.pizza.PizzaBlock;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

@Mixin(PizzaBlock.class)
public class PizzaBlockMixin {
    @Unique
    private static final ResourceLocation create_Delight_Core$bakeriesPizza =
            ResourceLocation.fromNamespaceAndPath("bakeries", "pizza");
    @Unique
    private static final ResourceLocation create_Delight_Core$vegetablePizza =
            CreateDelightCore.id("vegetable_pizza");
    @Unique
    private static final ResourceLocation create_Delight_Core$meatloversPizza =
            CreateDelightCore.id("meatlovers_pizza");
    @Unique
    private static final ResourceLocation create_Delight_Core$netherPizza =
            CreateDelightCore.id("nether_pizza");

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void create_Delight_Core$cutPizzaWithKnifeOnly(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        Item sliceItem = create_Delight_Core$getSliceItem(state);
        if (sliceItem == null) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(ModTags.KNIVES)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (!level.isClientSide) {
            int slice = state.getValue(PizzaBlock.SLICE);
            Quality quality = LevelData.get(level, pos);
            PizzaBlock pizzaBlock = (PizzaBlock) (Object) this;
            if (slice < pizzaBlock.getSlice() - 1) {
                level.setBlock(pos, state.setValue(PizzaBlock.SLICE, slice + 1), 3);
                if (quality != Quality.NONE) {
                    LevelData.set(level, pos, quality);
                }
            } else {
                level.removeBlock(pos, false);
            }

            Direction direction = player.getDirection().getOpposite();
            ItemStack sliceStack = new ItemStack(sliceItem);
            if (QualityUtils.isValidQuality(quality)) {
                QualityUtils.applyQuality(sliceStack, quality);
            }
            ItemUtils.spawnItemEntity(
                    level,
                    sliceStack,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.3D,
                    pos.getZ() + 0.5D,
                    direction.getStepX() * 0.15D,
                    0.05D,
                    direction.getStepZ() * 0.15D
            );
            level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Unique
    private static Item create_Delight_Core$getSliceItem(BlockState state) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        ResourceLocation sliceId = null;

        if (create_Delight_Core$bakeriesPizza.equals(blockId)) {
            sliceId = CreateDelightCore.id("pizza_slice");
        } else if (create_Delight_Core$vegetablePizza.equals(blockId)) {
            sliceId = CreateDelightCore.id("vegetable_pizza_slice");
        } else if (create_Delight_Core$meatloversPizza.equals(blockId)) {
            sliceId = CreateDelightCore.id("meatlovers_pizza_slice");
        } else if (create_Delight_Core$netherPizza.equals(blockId)) {
            sliceId = CreateDelightCore.id("nether_pizza_slice");
        }

        return sliceId == null ? null : ForgeRegistries.ITEMS.getValue(sliceId);
    }
}
