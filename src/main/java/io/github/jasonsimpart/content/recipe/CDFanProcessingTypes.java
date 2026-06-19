package io.github.jasonsimpart.content.recipe;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.registry.ModRecipeTypes;
import io.github.jasonsimpart.registry.ModTags;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public final class CDFanProcessingTypes {
    private static final ResourceLocation FREEZING_ID =
            ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "freezing");

    @Nullable
    public static FreezingType FREEZING;

    private CDFanProcessingTypes() {
    }

    public static void register(RegisterEvent event) {
        event.register(CreateRegistries.FAN_PROCESSING_TYPE, FREEZING_ID, () -> FREEZING = new FreezingType());
    }

    public static class FreezingType implements FanProcessingType {
        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(ModTags.Fluids.FAN_PROCESSING_CATALYSTS_FREEZING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            return blockState.is(ModTags.Blocks.FAN_PROCESSING_CATALYSTS_FREEZING);
        }

        @Override
        public int getPriority() {
            return 691100;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            return findRecipe(stack, level).isPresent();
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level) {
            return findRecipe(stack, level)
                    .map(RecipeHolder::value)
                    .map(recipe -> RecipeApplier.applyRecipeOn(level, stack, recipe, false))
                    .orElse(null);
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {
            if (level.random.nextInt(8) != 0) {
                return;
            }
            Vector3f color = new Color(0xDDE8FF).asVectorF();
            level.addParticle(new DustParticleOptions(color, 1), pos.x + (level.random.nextFloat() - .5f) * .5f,
                    pos.y + .5f, pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
            level.addParticle(ParticleTypes.SNOWFLAKE, pos.x + (level.random.nextFloat() - .5f) * .5f, pos.y + .5f,
                    pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
            particleAccess.setColor(Color.mixColors(0xEEEEFF, 0xDDE8FF, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 128f) {
                particleAccess.spawnExtraParticle(ParticleTypes.SNOWFLAKE, .125f);
            }
            if (random.nextFloat() < 1 / 32f) {
                particleAccess.spawnExtraParticle(ParticleTypes.POOF, .125f);
            }
        }

        @Override
        public void affectEntity(Entity entity, Level level) {
            if (level.isClientSide) {
                spawnEntityParticles(entity, level);
                return;
            }

            if (entity instanceof EnderMan || entity.getType() == EntityType.BLAZE) {
                entity.hurt(level.damageSources().freeze(), 8);
            }

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 7, false, false));
                livingEntity.setIsInPowderSnow(true);
                livingEntity.setTicksFrozen(Math.min(livingEntity.getTicksRequiredToFreeze() + 5, livingEntity.getTicksFrozen() + 5));
            }

            if (entity instanceof SnowGolem snowGolem) {
                snowGolem.heal(4);
            }

            if (entity instanceof Stray stray) {
                stray.heal(2);
            }

            if (entity.isOnFire()) {
                entity.clearFire();
                level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE,
                        SoundSource.NEUTRAL, 0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
            }

            if (entity instanceof Skeleton skeleton) {
                progressSkeletonConversion(skeleton, level);
            }
        }

        private static Optional<RecipeHolder<FanFreezingRecipe>> findRecipe(ItemStack stack, Level level) {
            return ModRecipeTypes.FAN_FREEZING.find(new SingleRecipeInput(stack), level);
        }

        private static void spawnEntityParticles(Entity entity, Level level) {
            if (!(entity instanceof Skeleton)) {
                return;
            }
            Vec3 pos = entity.getPosition(0);
            Vec3 particlePos = pos.add(0, 0.5f, 0)
                    .add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1)
                            .multiply(1, 0.2f, 1)
                            .normalize()
                            .scale(1f));
            level.addParticle(ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z, 0, 0.1f, 0);
            if (level.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SNOWFLAKE, pos.x, pos.y + .5f, pos.z,
                        (level.random.nextFloat() - .5f) * .5f, 0.1f, (level.random.nextFloat() - .5f) * .5f);
            }
        }

        private static void progressSkeletonConversion(Skeleton skeleton, Level level) {
            int progress = skeleton.getPersistentData().getInt("CreateFreezing");
            if (progress < 50) {
                if (progress % 10 == 0) {
                    level.playSound(null, skeleton.blockPosition(), SoundEvents.STRAY_AMBIENT, SoundSource.NEUTRAL,
                            1f, 1.5f * progress / 50f);
                }
                skeleton.getPersistentData().putInt("CreateFreezing", progress + 1);
                return;
            }

            level.playSound(null, skeleton.blockPosition(), SoundEvents.SKELETON_CONVERTED_TO_STRAY,
                    SoundSource.NEUTRAL, 1.25f, 0.65f);

            Stray stray = EntityType.STRAY.create(level);
            if (stray == null) {
                return;
            }
            CompoundTag data = skeleton.saveWithoutId(new CompoundTag());
            data.remove("UUID");
            stray.deserializeNBT(skeleton.registryAccess(), data);
            stray.setPos(skeleton.getPosition(0));
            level.addFreshEntity(stray);
            skeleton.discard();
        }
    }
}
