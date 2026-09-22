package io.github.jasonsimpart;

import io.github.jasonsimpart.compat.alexscaves.AlexCavesDimensionSpawnGuardEvents;
import io.github.jasonsimpart.content.event.DropReportEvents;
import io.github.jasonsimpart.compat.tetra.TetraCombatCompat;
import io.github.jasonsimpart.compat.iceandfire.DragonBloodCollectionCompat;
import io.github.jasonsimpart.compat.tacz.TaczEnergyReloadCompat;
import io.github.jasonsimpart.content.disabled.DisabledContentEvents;
import io.github.jasonsimpart.util.ModIds;
import io.github.jasonsimpart.util.OptionalMods;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;
import java.util.Set;

public final class ModCommonEvents {
    private static final ResourceLocation CREATE_BLAZE_CAKE = ResourceLocation.fromNamespaceAndPath("create", "blaze_cake");
    private static final ResourceLocation SUPPLEMENTARIES_FLAMMABLE = ResourceLocation.fromNamespaceAndPath("supplementaries", "flammable");
    private static final String BUDDING_QUARTZ_MESSAGE = "message.createdelightcore.budding_quartz";
    private static final Set<ResourceLocation> PROTECTED_BUDDING_QUARTZ = Set.of(
            ResourceLocation.fromNamespaceAndPath("ae2", "flawless_budding_quartz"),
            ResourceLocation.fromNamespaceAndPath("ae2", "flawed_budding_quartz"),
            ResourceLocation.fromNamespaceAndPath("ae2", "chipped_budding_quartz"),
            ResourceLocation.fromNamespaceAndPath("ae2", "damaged_budding_quartz")
    );

    private ModCommonEvents() {
    }

    public static void register(IEventBus modEventBus) {
        io.github.jasonsimpart.content.event.PackVillagerTrades.register();
        io.github.jasonsimpart.content.event.FluidInteractions.register(modEventBus);
        modEventBus.addListener(ModCommonEvents::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(ModCommonEvents::igniteAfterEatingBlazeCake);
        NeoForge.EVENT_BUS.addListener(ModCommonEvents::protectBuddingQuartz);
        NeoForge.EVENT_BUS.addListener(AlexCavesDimensionSpawnGuardEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(AlexCavesDimensionSpawnGuardEvents::onMobPositionCheck);
        NeoForge.EVENT_BUS.addListener(DropReportEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(io.github.jasonsimpart.content.event.LegacyStructureLoot::onLoad);
        DisabledContentEvents.register(modEventBus);
        io.github.jasonsimpart.compat.create.BasicInteractions.register();
        if (OptionalMods.isLoaded(ModIds.ALEXSMOBS)) {
            io.github.jasonsimpart.compat.alexsmobs.PackBlockInteractions.register();
        }
        if (OptionalMods.isLoaded(ModIds.FRUITS_DELIGHT)) {
            io.github.jasonsimpart.compat.fruitsdelight.CauldronFeedback.register();
        }
        if (OptionalMods.isLoaded(ModIds.IMPROVED_MOBS)) {
            io.github.jasonsimpart.compat.improvedmobs.ImprovedMobsCompat.register();
        }
        if (OptionalMods.isLoaded(ModIds.LIGHTMANS_CURRENCY)) {
            io.github.jasonsimpart.compat.lightmanscurrency.MobCurrencyDrops.register();
            io.github.jasonsimpart.compat.lightmanscurrency.TraderWhitelist.register();
            io.github.jasonsimpart.compat.lightmanscurrency.WalletUpgradeGuard.register();
        }
        if (OptionalMods.allLoaded(ModIds.WAYSTONES, ModIds.LIGHTMANS_CURRENCY)) {
            OptionalMods.invokeRegister("io.github.jasonsimpart.compat.waystones.WaystonesCurrencyCompat");
        }
        if (OptionalMods.allLoaded(ModIds.QUALITY_FOOD, ModIds.LIGHTMANS_CURRENCY)) {
            OptionalMods.invokeRegisterSoft("io.github.jasonsimpart.compat.qualityfood.QualityFoodCurrencyCompat");
        }
        if (OptionalMods.isLoaded(ModIds.TETRA)) {
            TetraCombatCompat.register();
        }
        if (OptionalMods.isLoaded(ModIds.ICE_AND_FIRE)) {
            DragonBloodCollectionCompat.register();
        }
        if (OptionalMods.allLoaded(ModIds.TACZ, ModIds.AE2)) {
            TaczEnergyReloadCompat.register();
        }
        modEventBus.addListener(ModCommonEvents::registerHarvesterBehaviours);
        modEventBus.addListener(ModCommonEvents::registerCapabilities);
    }

    private static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                io.github.jasonsimpart.registry.ModBlockEntities.LIFE_MATTER_INJECTOR.get(),
                (blockEntity, side) -> blockEntity.getInventory());
    }

    private static void registerHarvesterBehaviours(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var clusterBehaviour = new io.github.jasonsimpart.compat.create.FlowerClusterHarvesterBehaviour();
            com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.register(
                    io.github.jasonsimpart.registry.ModBlocks.FIRE_LILY_CLUSTER.get(), clusterBehaviour);
            com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.register(
                    io.github.jasonsimpart.registry.ModBlocks.FROST_LILY_CLUSTER.get(), clusterBehaviour);
            com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.register(
                    io.github.jasonsimpart.registry.ModBlocks.LIGHTNING_LILY_CLUSTER.get(), clusterBehaviour);
            if (OptionalMods.isLoaded(ModIds.VINERY)) {
                io.github.jasonsimpart.compat.create.VineryHarvesterBehaviour.registerBlocks();
            }
        });
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        if (!Config.ENABLE_BLAZE_CAKE_FOOD_PATCH.get()) {
            return;
        }

        BuiltInRegistries.ITEM.getOptional(CREATE_BLAZE_CAKE).ifPresent(item -> event.modify(item, builder -> builder.set(DataComponents.FOOD, blazeCakeFood())));
    }

    private static void igniteAfterEatingBlazeCake(LivingEntityUseItemEvent.Finish event) {
        if (!Config.ENABLE_BLAZE_CAKE_FOOD_PATCH.get() || event.getEntity().level().isClientSide()) {
            return;
        }

        Optional<Item> blazeCake = BuiltInRegistries.ITEM.getOptional(CREATE_BLAZE_CAKE);
        if (blazeCake.isPresent() && event.getItem().is(blazeCake.get())) {
            event.getEntity().igniteForSeconds(160.0F);
        }
    }

    private static void protectBuddingQuartz(BlockEvent.BreakEvent event) {
        if (!Config.ENABLE_AE2_BUDDING_QUARTZ_PROTECTION.get()) {
            return;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        if (!PROTECTED_BUDDING_QUARTZ.contains(blockId) || event.getPlayer().isShiftKeyDown()) {
            return;
        }

        event.getPlayer().sendSystemMessage(Component.translatable(BUDDING_QUARTZ_MESSAGE));
        event.setCanceled(true);
    }

    private static FoodProperties blazeCakeFood() {
        FoodProperties.Builder food = new FoodProperties.Builder()
                .nutrition(10)
                .saturationModifier(0.7F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 2), 1.0F);

        optionalEffect(SUPPLEMENTARIES_FLAMMABLE)
                .ifPresent(effect -> food.effect(() -> new MobEffectInstance(effect, 60 * 20, 2), 1.0F));

        return food.build();
    }

    private static Optional<Holder.Reference<MobEffect>> optionalEffect(ResourceLocation effectId) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
    }
}
