package io.github.jasonsimpart.createdelightcore.mixin.quality_food;

import com.teamabnormals.neapolitan.common.block.MintBlock;
import com.teamabnormals.neapolitan.common.block.StrawberryBushBlock;
import de.cadentem.quality_food.capability.LevelData;
import de.cadentem.quality_food.config.QualityConfig;
import de.cadentem.quality_food.core.Modification;
import de.cadentem.quality_food.core.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;
import dev.xkmc.fruitsdelight.content.block.DoubleFruitBushBlock;
import dev.xkmc.fruitsdelight.content.block.FruitBushBlock;
import io.github.jasonsimpart.createdelightcore.content.util.EclipticSeasonsUtil;
import io.github.jasonsimpart.createdelightcore.content.util.QualityFoodHarvestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.vinery.core.block.GrapeBush;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

import java.util.Collection;

import static de.cadentem.quality_food.util.QualityUtils.QUALITY_TAG;
import static de.cadentem.quality_food.util.QualityUtils.applyQuality;
import static de.cadentem.quality_food.util.QualityUtils.getQuality;
import static de.cadentem.quality_food.util.QualityUtils.isRelevantCrop;
import static de.cadentem.quality_food.util.QualityUtils.isValidQuality;

@Mixin(QualityUtils.class)
public abstract class QualityFoodMixin {
    @Unique
    private static final RandomSource create_Delight_Core$RANDOM = RandomSource.create();
    @Unique
    private static final ResourceLocation create_Delight_Core$POWDERY_CANE = new ResourceLocation("mynethersdelight", "powdery_cane");
    @Unique
    private static final ResourceLocation create_Delight_Core$POWDERY_CANNON = new ResourceLocation("mynethersdelight", "powdery_cannon");

    @Inject(method = "applyQuality(Lnet/minecraft/world/item/ItemStack;Ljava/util/Collection;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void applyQualityFromIngredientsMixin(ItemStack stack, Collection<ItemStack> ingredients, Player player, CallbackInfo ci) {
        boolean hasValidIngredient = false;
        for (ItemStack ingredient : ingredients) {
            if (!Utils.isValidItem(ingredient)) {
                continue;
            }
            hasValidIngredient = true;
            if (!isValidQuality(getQuality(ingredient))) {
                create_Delight_Core$clearQuality(stack);
                ci.cancel();
                return;
            }
        }

        if (!hasValidIngredient) {
            create_Delight_Core$clearQuality(stack);
            ci.cancel();
        }
    }

    @Unique
    private static void create_Delight_Core$clearQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(QUALITY_TAG)) {
            return;
        }
        tag.remove(QUALITY_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Inject(method = "applyQuality(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lde/cadentem/quality_food/core/Quality;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void applyQualityMixin(ItemStack stack, BlockState state, Quality blockQuality, Player player, BlockState farmland, CallbackInfo ci) {
        TagKey<Block> crop = TagKey.create(Registries.BLOCK, new ResourceLocation("createdelight", "quality_crops"));
        if (isRelevantCrop(state) || state.is(crop)) {
            Quality selected = Quality.NONE;
            Quality chanceQuality = create_Delight_Core$getChanceQuality(state, blockQuality);
            float growChance = create_Delight_Core$getGrowChance(player, state, chanceQuality);
            BlockState effectiveFarmland = create_Delight_Core$getEffectiveFarmland(player, state, farmland);
            for (Quality quality : Quality.values()) {
                if (quality.level() == 0) {
                    continue;
                }

                double chance;
                if (chanceQuality.level() == 0) {
                    chance = QualityConfig.getChance(quality);
                } else {
                    chance = Mth.clamp(
                            QualityConfig.getChance(quality) * QualityConfig.calculateChance(quality, QualityConfig.getWeight(chanceQuality)) * QualityConfig.getWeight(Quality.DIAMOND),
                            0.0,
                            1.0
                    );
                }
                chance = Modification.harvestOrSeedMultiplier(quality, stack).apply(chance);
                chance = Modification.luck(player).apply(chance);
                chance = Modification.farmland(state, effectiveFarmland).apply(chance);
                chance = Modification.multiplicative(growChance).apply(chance);
                if (chance > 0 && chance >= create_Delight_Core$RANDOM.nextDouble()) {
                    selected = quality;
                }
            }

            applyQuality(stack, selected);
        } else if (isValidQuality(blockQuality)) {
            applyQuality(stack, blockQuality);
        } else if (blockQuality != Quality.NONE_PLAYER_PLACED) {
            applyQuality(stack, player);
        }
        ci.cancel();
    }

    @Unique
    private static Quality create_Delight_Core$getChanceQuality(BlockState state, Quality blockQuality) {
        if (state.is(Blocks.SUGAR_CANE)) {
            return Quality.NONE;
        }

        return blockQuality;
    }

    @Unique
    private static BlockState create_Delight_Core$getEffectiveFarmland(Player player, BlockState state, BlockState farmland) {
        if (player == null) {
            return farmland;
        }

        BlockPos cropPos = QualityFoodHarvestContext.getCropPos();
        if (cropPos == null) {
            return farmland;
        }

        BlockPos basePos = create_Delight_Core$getEffectiveCropPos(player, state, cropPos);
        if (basePos.equals(cropPos)) {
            return farmland;
        }

        return player.level().getBlockState(basePos.below());
    }

    @Unique
    private static BlockPos create_Delight_Core$getEffectiveCropPos(Player player, BlockState state, BlockPos cropPos) {
        if (state.is(Blocks.SUGAR_CANE)) {
            return create_Delight_Core$getBaseCropPos(player, cropPos, Blocks.SUGAR_CANE);
        }

        if (state.getBlock() instanceof TomatoVineBlock) {
            return create_Delight_Core$getBaseCropPos(player, cropPos, state.getBlock());
        }

        return cropPos;
    }

    @Unique
    private static BlockPos create_Delight_Core$getBaseCropPos(Player player, BlockPos cropPos, Block cropBlock) {
        BlockPos basePos = cropPos;
        while (player.level().getBlockState(basePos.below()).is(cropBlock)) {
            basePos = basePos.below();
        }

        return basePos;
    }

    @Unique
    private static float create_Delight_Core$getGrowChance(Player player, BlockState state, Quality blockQuality) {
        if (player == null || player instanceof FakePlayer) {
            return 0.0F;
        }

        if (!ModList.get().isLoaded("eclipticseasons")) {
            return 1.0F;
        }

        BlockPos growPos = QualityFoodHarvestContext.getCropPos();
        if (growPos == null) {
            growPos = player.getOnPos();
        }

        BlockPos effectiveGrowPos = create_Delight_Core$getEffectiveCropPos(player, state, growPos);
        BlockState effectiveGrowState = player.level().getBlockState(effectiveGrowPos);
        int sourceRank = state.is(Blocks.SUGAR_CANE) ? 0 : LevelData.get(player.level(), effectiveGrowPos).level();
        int targetRank = blockQuality.level();
        float growChance = EclipticSeasonsUtil.getGrowChance(player.level(), effectiveGrowPos, effectiveGrowState);
        float baseGrowChance = create_Delight_Core$removeRankBoost(growChance, sourceRank);
        float correctedGrowChance = create_Delight_Core$applyRankBoost(baseGrowChance, targetRank);
        return Mth.clamp(correctedGrowChance * 1.25F, 0.0F, 1.0F);
    }

    @Unique
    private static float create_Delight_Core$applyRankBoost(float chance, int rank) {
        float clamped = Mth.clamp(chance, 0.0F, 1.0F);
        if (rank <= 0) {
            return clamped;
        }

        float boost = create_Delight_Core$getRankBoost(rank);
        return Mth.clamp(boost + (1.0F - boost) * clamped, 0.0F, 1.0F);
    }

    @Unique
    private static float create_Delight_Core$removeRankBoost(float chance, int rank) {
        float clamped = Mth.clamp(chance, 0.0F, 1.0F);
        if (rank <= 0) {
            return clamped;
        }

        float boost = create_Delight_Core$getRankBoost(rank);
        float denominator = 1.0F - boost;
        if (denominator <= 0.0F) {
            return 1.0F;
        }
        return Mth.clamp((clamped - boost) / denominator, 0.0F, 1.0F);
    }

    @Unique
    private static float create_Delight_Core$getRankBoost(int rank) {
        return (float) (Math.pow(2, rank - 1) / 4);
    }

    @Inject(method = "isRelevantCrop", at = @At("HEAD"), cancellable = true, remap = false)
    private static void isRelevantCropMixin(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        if (block instanceof CropBlock cropBlock) {
            if (cropBlock.isMaxAge(state)) {
                cir.setReturnValue(true);
            }
        }
        if (block instanceof StrawberryBushBlock strawberryBushBlock) {
            cir.setReturnValue(strawberryBushBlock.isMaxAge(state));
        } else if (block instanceof MintBlock mintBlock) {
            cir.setReturnValue(mintBlock.isMaxAge(state));
        } else if (block instanceof FruitBushBlock || block instanceof DoubleFruitBushBlock) {
            cir.setReturnValue(state.getValue(BlockStateProperties.AGE_4) == 4);
        } else if (block instanceof GrapeBush || block instanceof SweetBerryBushBlock) {
            cir.setReturnValue(state.getValue(BlockStateProperties.AGE_3) == 3);
        } else {
            Boolean myNethersDelightCropLit = create_Delight_Core$getMyNethersDelightCropLit(state, block);
            if (myNethersDelightCropLit != null) {
                cir.setReturnValue(myNethersDelightCropLit);
            }
        }
    }

    @Unique
    private static Boolean create_Delight_Core$getMyNethersDelightCropLit(BlockState state, Block block) {
        if (!ModList.get().isLoaded("mynethersdelight")) {
            return null;
        }

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (!create_Delight_Core$POWDERY_CANE.equals(blockId)
                && !create_Delight_Core$POWDERY_CANNON.equals(blockId)) {
            return null;
        }

        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty litProperty && "lit".equals(litProperty.getName())) {
                return state.getValue(litProperty);
            }
        }
        return null;
    }
}
