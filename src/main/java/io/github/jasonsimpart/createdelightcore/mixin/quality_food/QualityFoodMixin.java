package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import com.soytutta.mynethersdelight.common.block.PowderyCaneBlock;
import com.soytutta.mynethersdelight.common.block.PowderyCannonBlock;
import com.teamabnormals.neapolitan.common.block.MintBlock;
import com.teamabnormals.neapolitan.common.block.StrawberryBushBlock;
import de.cadentem.quality_food.compat.Compat;
import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.core.Modification;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import dev.xkmc.fruitsdelight.content.block.DoubleFruitBushBlock;
import dev.xkmc.fruitsdelight.content.block.FruitBushBlock;
import io.github.jasonsimpart.createdelightcore.content.util.EclipticSeasonsUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.FakePlayer;
import net.satisfy.vinery.core.block.GrapeBush;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.cadentem.quality_food.util.QualityUtils.*;

@Mixin(QualityUtils.class)
public abstract class QualityFoodMixin {
    @Unique
    private static final RandomSource create_Delight_Core$RANDOM = RandomSource.create();
    @Inject(method = "applyQuality(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lde/cadentem/quality_food/core/Quality;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void applyQualityMixin(ItemStack stack, BlockState state, Quality blockQuality, Player player, BlockState farmland, CallbackInfo ci) {
        TagKey<Block> crop = TagKey.create(Registries.BLOCK, new ResourceLocation("createdelight", "quality_crops"));
        if (isRelevantCrop(state) || state.is(crop)) {
            Quality selected = Quality.NONE;
            for (Quality quality : Quality.values()) {
                if (quality.level() == 0) {
                    continue;
                }

                double chance;
                if (blockQuality.level() == 0) {
                    // Weight would be 0, meaning no quality can be calculated
                    chance = QualityConfig.getChance(quality);
                } else {
                    chance = Mth.clamp(QualityConfig.getChance(quality) * QualityConfig.calculateChance(quality, QualityConfig.getWeight(blockQuality)) * QualityConfig.getWeight(Quality.DIAMOND), 0.0, 1.0);
                }
                chance = Modification.harvestOrSeedMultiplier(quality, stack).apply(chance);
                chance = Modification.luck(player).apply(chance);
                chance = Modification.farmland(state, farmland).apply(chance);
                // 非玩家收割不会拥有品质
                float growChance = 0;
                if (player != null && !(player instanceof FakePlayer)) {
                    growChance = EclipticSeasonsUtil.getGrowChance(player.level(), player.getOnPos(), state) * 1.25f;
//                CreateDelightCore.LOGGER.info("growChance:" + growChance);
                }
                chance = Modification.multiplicative(growChance).apply(chance);
                if (chance > 0 && chance >= create_Delight_Core$RANDOM.nextDouble()) {
                    selected = quality;
                }
            }

            applyQuality(stack, selected);
        } else if (isValidQuality(blockQuality)) {
            // The block itself if it has quality
            applyQuality(stack, blockQuality);
        } else if (blockQuality != Quality.NONE_PLAYER_PLACED) {
            // The block itself or harvested items when the crop has no quality
            applyQuality(stack, player);
        }
        ci.cancel();
    }

    @Inject(method = "isRelevantCrop", at = @At("HEAD"), cancellable = true, remap = false)
    private static void isRelevantCropMixin(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        if (block instanceof CropBlock cropBlock) {
            if (cropBlock.isMaxAge(state)) {
                cir.setReturnValue(true);
            }
        }
        if (block instanceof StrawberryBushBlock strawberryBushBlock)
            cir.setReturnValue(strawberryBushBlock.isMaxAge(state));
        else if (block instanceof MintBlock mintBlock)
            cir.setReturnValue(mintBlock.isMaxAge(state));
        else if ((block instanceof FruitBushBlock || block instanceof DoubleFruitBushBlock))
            cir.setReturnValue(state.getValue(BlockStateProperties.AGE_4) == 4);
        else if (block instanceof GrapeBush || block instanceof SweetBerryBushBlock)
            cir.setReturnValue(state.getValue(BlockStateProperties.AGE_3) == 3);
        else if (block instanceof PowderyCaneBlock)
            cir.setReturnValue(state.getValue(PowderyCaneBlock.LIT));
        else if (block instanceof PowderyCannonBlock)
            cir.setReturnValue(state.getValue(PowderyCannonBlock.LIT));
    }
}
