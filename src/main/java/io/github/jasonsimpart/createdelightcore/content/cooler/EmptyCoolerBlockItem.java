// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.content.cooler;

import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class EmptyCoolerBlockItem extends BlockItem {
    public EmptyCoolerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof SnowGolem)) return InteractionResult.PASS;
        if (player.level().isClientSide) {
            for (int i = 0; i < 20; i++) player.level().addParticle(ParticleTypes.SNOWFLAKE,
                    entity.getX(), entity.getY() + .5, entity.getZ(),
                    (player.getRandom().nextDouble() - .5) * .2, .1, (player.getRandom().nextDouble() - .5) * .2);
            return InteractionResult.FAIL;
        }
        ItemStack filled = CDBlocks.SNOWMAN_COOLER.asStack();
        if (!player.isCreative()) stack.shrink(1);
        if (stack.isEmpty()) player.setItemInHand(hand, filled);
        else player.getInventory().placeItemBackInInventory(filled);
        player.level().playSound(null, entity.blockPosition(), SoundEvents.BUCKET_FILL_POWDER_SNOW, SoundSource.BLOCKS, .5f, .75f);
        entity.discard();
        // Match Create's BlazeBurnerBlockItem: a consuming result lets Player.interactOn
        // clear the hand when the original stack is empty, erasing our filled cooler.
        return InteractionResult.FAIL;
    }
}
