package io.github.jasonsimpart;

import io.github.jasonsimpart.server.AlexCavesDimensionSpawnGuardEvents;
import io.github.jasonsimpart.server.DropReportEvents;
import io.github.jasonsimpart.disabled.DisabledContentEvents;
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
import net.neoforged.fml.ModList;
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
        modEventBus.addListener(ModCommonEvents::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(ModCommonEvents::igniteAfterEatingBlazeCake);
        NeoForge.EVENT_BUS.addListener(ModCommonEvents::protectBuddingQuartz);
        NeoForge.EVENT_BUS.addListener(AlexCavesDimensionSpawnGuardEvents::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(AlexCavesDimensionSpawnGuardEvents::onMobPositionCheck);
        NeoForge.EVENT_BUS.addListener(DropReportEvents::onServerTick);
        DisabledContentEvents.register(modEventBus);
        registerWaystonesMoneyTeleport();
    }

    private static void registerWaystonesMoneyTeleport() {
        if (!ModList.get().isLoaded("waystones") || !ModList.get().isLoaded("lightmanscurrency")) {
            return;
        }

        try {
            Class.forName("io.github.jasonsimpart.compat.waystones.WaystonesCurrencyCompat")
                    .getMethod("register")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register createdelightcore Waystones currency compat", exception);
        }
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
