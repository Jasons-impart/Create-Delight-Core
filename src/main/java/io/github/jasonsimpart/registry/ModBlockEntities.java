package io.github.jasonsimpart.registry;

import io.github.jasonsimpart.CreateDelightCore;
import io.github.jasonsimpart.content.quality.harvest.LifeMatterInjectorBlockEntity;
import io.github.jasonsimpart.content.quality.harvest.QualityHarvestControllerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateDelightCore.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QualityHarvestControllerBlockEntity>> QUALITY_HARVEST_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("quality_harvest_controller", () -> BlockEntityType.Builder
                    .of(QualityHarvestControllerBlockEntity::new, ModBlocks.QUALITY_HARVEST_CONTROLLER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LifeMatterInjectorBlockEntity>> LIFE_MATTER_INJECTOR =
            BLOCK_ENTITY_TYPES.register("life_matter_injector", () -> BlockEntityType.Builder
                    .of(LifeMatterInjectorBlockEntity::new, ModBlocks.LIFE_MATTER_INJECTOR.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
