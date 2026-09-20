package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeType;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.event.MBDRegistryEvent;
import com.lowdragmc.mbd2.common.machine.definition.MBDMachineDefinition;
import com.lowdragmc.mbd2.common.machine.definition.MultiblockMachineDefinition;
import com.lowdragmc.mbd2.common.machine.definition.config.*;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineStructureFormedEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.integration.create.machine.CreateKineticMachineDefinition;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Core owns machine identities and capabilities; pack data owns processing recipes. */
public final class MbdCompat {
    private static final List<Runnable> SETUP = new ArrayList<>();
    private static final String[] MACHINES = {
            "alloy_electric_furnace", "copper_coil", "forged_steel_import_bus", "forged_steel_export_bus",
            "hydropower_station", "hydropower_amplifier", "wooden_fan", "steel_fan", "forge_steel_fan", "dragon_steel_fan",
            "butchery_room", "create_in", "andesite_import_bus", "andesite_export_bus",
            "assembly_line", "assembly_import_hatch", "assemble_import_bus",
            "big_centrifuge", "centrifuge_rotor", "steel_import_bus", "steel_export_bus",
            "fission_reactor", "fission_fuel_assembly", "fission_reactor_controller",
            "mechanic_grinding_wheel", "contract_executor", "electrolyzer", "greenhouse_builder", "mechanical_craft_encoder", "mortar", "quality_destroyer", "small_centrifugation"
    };

    private MbdCompat() {}

    public static void register(IEventBus bus) {
        var tabs = net.neoforged.neoforge.registries.DeferredRegister.create(
                net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, CreateDelightCore.MODID);
        tabs.register("machines", () -> net.minecraft.world.item.CreativeModeTab.builder()
                .title(net.minecraft.network.chat.Component.translatable("itemGroup.createdelightcore.machines"))
                .icon(() -> MBDRegistries.MACHINE_DEFINITIONS.get(id("alloy_electric_furnace")).item().getDefaultInstance())
                .build());
        tabs.register(bus);
        bus.addListener(MbdCompat::registerRecipeTypes);
        bus.addListener(MbdCompat::registerMachines);
        bus.addListener(MbdCompat::setup);
        bus.addListener(MbdMachineTests::register);
        if (net.neoforged.fml.ModList.get().isLoaded("butchercraft")) MbdButchery.register(bus);
        NeoForge.EVENT_BUS.addListener(MbdCompat::structureFormed);
        MbdAssembly.register();
        MbdCraftEncoder.register();
        MbdSmallProcessing.register();
        MbdGrinding.register();
        MbdGreenhouse.register();
        MbdCentrifuge.register();
        MbdReactor.register();
        NeoForge.EVENT_BUS.addListener(MbdHydropower::formed);
        NeoForge.EVENT_BUS.addListener(MbdHydropower::invalid);
        if (Boolean.getBoolean("createdelightcore.verifyMbdRecipes")) {
            NeoForge.EVENT_BUS.addListener(MbdRecipeValidation::started);
        }
    }

    private static void structureFormed(MachineStructureFormedEvent event) {
        if (!event.machine.getDefinition().id().equals(id("alloy_electric_furnace"))) return;
        if (event.machine.getTraitByName("forged_steel_import_item_slot") instanceof ItemSlotCapabilityTrait input) {
            var storage = input.storage;
            // Preserve the old filter: a full matching stack blocks further insertion.
            storage.setFilter(stack -> {
                for (int slot = 0; slot < storage.getSlots(); slot++) {
                    var existing = storage.getStackInSlot(slot);
                    if (ItemStack.isSameItemSameComponents(stack, existing) && existing.getCount() >= 64) return false;
                }
                return true;
            });
        }
    }

    private static void registerRecipeTypes(MBDRegistryEvent.MBDRecipeType event) {
        for (var name : List.of("alloy_electric_furnace", "hydropower_station", "butchery", "assembly_line", "big_centrifugation", "big_centrifugation_fuel", "fission_react", "fission_react_fuel",
                "contract_executor", "electrolyzer", "mortar", "small_centrifugation")) {
            var project = read("recipe/" + name);
            var proxies = project.getList("proxies", Tag.TAG_STRING).stream()
                    .map(raw -> ResourceLocation.parse(raw.getAsString())).toArray(ResourceLocation[]::new);
            var type = new MBDRecipeType(id(name), proxies) {
                @Override
                public com.lowdragmc.mbd2.api.recipe.MBDRecipe toMBDrecipe(
                        net.minecraft.world.item.crafting.RecipeType<?> sourceType, ResourceLocation sourceId,
                        net.minecraft.world.item.crafting.Recipe<?> source) {
                    if (!java.util.Set.of("big_centrifugation", "assembly_line", "small_centrifugation", "mortar").contains(name)) {
                        return super.toMBDrecipe(sourceType, sourceId, source);
                    }
                    // Upstream's generic item conversion throws for fluid-only Create outputs
                    // before posting the proxy event. Our converters understand these recipes.
                    var sourceTypeId = net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.getKey(sourceType);
                    if (sourceTypeId == null) return null;
                    var converted = new com.lowdragmc.mbd2.api.recipe.event.TransferProxyRecipeEvent(
                            this, sourceTypeId, sourceType, sourceId, source, null);
                    NeoForge.EVENT_BUS.post(converted.postCustomEvent());
                    return converted.isCanceled() ? null : converted.mbdRecipe;
                }

                @Override
                public List<net.minecraft.world.item.crafting.RecipeHolder<com.lowdragmc.mbd2.api.recipe.MBDRecipe>> searchFuelRecipe(
                        net.minecraft.world.item.crafting.RecipeManager manager,
                        com.lowdragmc.mbd2.api.capability.recipe.IRecipeCapabilityHolder holder) {
                    if (!getFuelRecipeConfig().isEnable() || !holder.hasProxies()) return List.of();
                    // MBD2 21.0.6 mistakenly queries this processing type for every fuel type.
                    return getFuelRecipeConfig().getFuelRecipeTypes().stream()
                            .filter(fuel -> !fuel.equals(id(name)))
                            .map(MBDRegistries.RECIPE_TYPES::get)
                            .filter(java.util.Objects::nonNull)
                            .flatMap(fuel -> manager.getAllRecipesFor(fuel).stream())
                            .filter(recipe -> recipe.value().matchRecipe(holder).isSuccess()
                                    && recipe.value().matchTickRecipe(holder).isSuccess())
                            .sorted(java.util.Comparator.comparingInt(recipe -> recipe.value().priority))
                            .toList();
                }

                @Override
                public com.lowdragmc.lowdraglib2.gui.ui.UI createRecipeUI(com.lowdragmc.mbd2.api.recipe.MBDRecipe recipe) {
                    var ui = super.createRecipeUI(recipe);
                    // The legacy 41-pixel duration field must not overprint output slots.
                    ui.selectId("@duration", com.lowdragmc.lowdraglib2.gui.ui.elements.Label.class).forEach(label ->
                            label.setText(net.minecraft.network.chat.Component.literal(recipe.duration + " t")));
                    return ui;
                }
            };
            event.register(type);
            SETUP.add(() -> {
                if (project.contains("fuelType")) {
                    type.getFuelRecipeConfig().setEnable(true);
                    type.getFuelRecipeConfig().getFuelRecipeTypes().add(ResourceLocation.parse(project.getString("fuelType")));
                }
                type.setUiTemplate(MbdMachineUI.create(project.getCompound("ui")));
                type.setXEIVisible(project.getBoolean("visible"));
                type.setProxyRecipeXEIVisible(project.getBoolean("proxyVisible"));
                if (project.contains("icon")) type.setIcon(MbdMachineUI.texture(project.getCompound("icon")));
            });
        }
    }

    private static void registerMachines(MBDRegistryEvent.Machine event) {
        for (var name : MACHINES) {
            var project = read("machine/" + name);
            var data = project.getCompound("definition");
            var blocks = ConfigBlockProperties.builder().build();
            blocks.deserializeNBT(Platform.getFrozenRegistry(), data.getCompound("blockProperties"));
            var items = ConfigItemProperties.builder().build();
            items.deserializeNBT(Platform.getFrozenRegistry(), data.getCompound("itemProperties"));
            MBDMachineDefinition.Builder builder = project.contains("pattern")
                    ? MultiblockMachineDefinition.builder() : data.contains("kineticMachineSettings")
                    ? CreateKineticMachineDefinition.builder() : MBDMachineDefinition.builder();
            builder.id(id(name)).blockProperties(blocks).itemProperties(items);
            var definition = builder.build();
            if (definition instanceof CreateKineticMachineDefinition kinetic) {
                // Create registers stress values when the block is constructed.
                kinetic.kineticMachineSettings().deserializeNBT(Platform.getFrozenRegistry(), data.getCompound("kineticMachineSettings"));
            }
            definition.stateMachine().deserializeNBT(Platform.getFrozenRegistry(), data.getCompound("stateMachine"));
            if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
                MbdClientModels.register(definition);
            }
            event.register(definition);
            SETUP.add(() -> configure(definition, project));
        }
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SETUP.forEach(Runnable::run);
            SETUP.clear();
            CreateDelightCore.LOGGER.info("Configured {} Core-owned MBD2 machines", MACHINES.length);
        });
    }

    private static void configure(MBDMachineDefinition definition, CompoundTag project) {
        definition.loadFactory();
        var provider = Platform.getFrozenRegistry();
        var data = project.getCompound("definition");
        var settings = data.getCompound("machineSettings").copy();
        var traits = settings.getList("traitDefinitions", Tag.TAG_COMPOUND);
        settings.remove("traitDefinitions");
        definition.machineSettings().deserializeNBT(provider, settings);
        for (var raw : traits) {
            var tag = (CompoundTag) raw;
            var type = MBDRegistries.TRAIT_DEFINITION_TYPES.get(tag.getString("type"));
            if (type == null) throw new IllegalStateException("Unknown trait in " + definition.id() + ": " + tag);
            var trait = type.createDefinition();
            trait.deserializeNBT(provider, tag);
            if (!definition.machineSettings().addTraitDefinition(trait)) {
                throw new IllegalStateException("Conflicting trait in " + definition.id() + ": " + tag);
            }
        }
        if (definition.machineSettings().hasUI()) {
            var uiTag = new CompoundTag();
            uiTag.put("uiTemplate", com.lowdragmc.lowdraglib2.gui.ui.UITemplate.CODEC.encodeStart(
                    provider.createSerializationContext(NbtOps.INSTANCE),
                    MbdMachineUI.create(project.getCompound("ui"))).getOrThrow());
            definition.machineSettings().deserializeNBT(provider, uiTag);
        }
        var recipe = data.getCompound("recipeLogicSettings").copy();
        recipe.remove("recipeModifiers");
        definition.recipeLogicSettings().deserializeNBT(provider, recipe);
        loadModifiers(definition.recipeLogicSettings().recipeModifiers(), data.getCompound("recipeLogicSettings"));
        if (definition.partSettings() != null) {
            var part = data.getCompound("partSettings").copy();
            part.remove("recipeModifiers");
            part.remove("proxyControllerCapabilities");
            definition.partSettings().deserializeNBT(provider, part);
            loadModifiers(definition.partSettings().recipeModifiers(), data.getCompound("partSettings"));
            for (var raw : data.getCompound("partSettings").getList("proxyControllerCapabilities", Tag.TAG_COMPOUND)) {
                var proxy = new ConfigPartSettings.ProxyCapability();
                proxy.deserializeNBT(provider, (CompoundTag) raw);
                definition.partSettings().proxyControllerCapabilities().add(proxy);
            }
        }
        if (definition instanceof MultiblockMachineDefinition multiblock) {
            multiblock.multiblockSettings().deserializeNBT(provider, data.getCompound("multiblockSettings"));
            MbdMachinePattern.configure(multiblock, project.getCompound("pattern"));
        }
    }

    private static void loadModifiers(RecipeModifier.RecipeModifiers target, CompoundTag data) {
        target.recipeModifiers.clear();
        for (var raw : data.getList("recipeModifiers", Tag.TAG_COMPOUND)) {
            var modifier = new RecipeModifier();
            modifier.deserializeNBT(Platform.getFrozenRegistry(), (CompoundTag) raw);
            target.recipeModifiers.add(modifier);
        }
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, path);
    }

    static CompoundTag read(String name) {
        var path = "/assets/createdelightcore/mbd2/" + name + ".snbt";
        try (var input = MbdCompat.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing " + path);
            return TagParser.parseTag(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load Core machine resource " + path, exception);
        }
    }
}
