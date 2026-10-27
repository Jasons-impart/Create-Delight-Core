package com.ueperae.scrollrollfix;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import plus.dragons.createenchantmentindustry.api.registry.CEIRegistries;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviourProvider;

@Mod(ScrollRollFix.MOD_ID)
public final class ScrollRollFix {
    public static final String MOD_ID = "scrollrollfix";

    private static final DeferredRegister<PrintingBehaviourProvider> PRINTING_BEHAVIOURS =
            DeferredRegister.create(CEIRegistries.PRINTING_BEHAVIOUR_PROVIDER, MOD_ID);
    private static final ResourceLocation SCROLL_ID = ResourceLocation.fromNamespaceAndPath("tetra", "scroll_rolled");
    private static final String DYE_NAMESPACE = "create_dragons_plus";
    private static final String DYE_SUFFIX = "_dye";
    private static final int FLUID_COST = 250;
    private static final int PRIORITY = PrintingBehaviourProvider.DEFAULT_PRIORITY + 1;

    public ScrollRollFix() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PRINTING_BEHAVIOURS.register("tetra_scroll_dye", () -> new PrintingBehaviourProvider(
                PRIORITY,
                TetraScrollPrintingBehaviour::create));
        PRINTING_BEHAVIOURS.register(modEventBus);
    }

    private static boolean isDye(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return false;
        }
        final Fluid fluid = fluidStack.getFluid();
        final ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) {
            return false;
        }
        return DYE_NAMESPACE.equals(id.getNamespace()) && id.getPath().endsWith(DYE_SUFFIX);
    }

    private static final class TetraScrollPrintingBehaviour implements PrintingBehaviour {
        private final ItemStack template;

        private TetraScrollPrintingBehaviour(ItemStack template) {
            this.template = template.copy();
        }

        private static Optional<DataResult<PrintingBehaviour>> create(
                Level level, SmartFluidTankBehaviour tank, ItemStack template) {
            if (!isValidScroll(template)) {
                return Optional.empty();
            }
            return Optional.of(DataResult.success(new TetraScrollPrintingBehaviour(template)));
        }

        private static boolean isValidScroll(ItemStack stack) {
            final Item scroll = ForgeRegistries.ITEMS.getValue(SCROLL_ID);
            if (scroll == null || !stack.is(scroll)) {
                return false;
            }

            final CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
            return blockEntityTag != null
                    && blockEntityTag.contains("data", Tag.TAG_LIST)
                    && !blockEntityTag.getList("data", Tag.TAG_COMPOUND).isEmpty();
        }

        @Override
        public int getRequiredItemCount(Level level, ItemStack stack) {
            return stack.is(Items.PAPER) ? 1 : 0;
        }

        @Override
        public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
            return stack.is(Items.PAPER) && isDye(fluidStack) ? FLUID_COST : 0;
        }

        @Override
        public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
            if (!stack.is(Items.PAPER)) {
                return ItemStack.EMPTY;
            }
            final ItemStack result = template.copy();
            result.setCount(1);
            return result;
        }

        @Override
        public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            tooltip.add(Component.translatable("create_enchantment_industry.gui.goggles.printing")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable(
                            "create_enchantment_industry.gui.goggles.printing.cost",
                            Component.literal(FLUID_COST + " mB"))
                    .withStyle(ChatFormatting.GREEN));
            return true;
        }
    }
}
