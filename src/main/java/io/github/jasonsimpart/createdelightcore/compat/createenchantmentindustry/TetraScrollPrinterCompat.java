package io.github.jasonsimpart.createdelightcore.compat.createenchantmentindustry;

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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import plus.dragons.createenchantmentindustry.api.registry.CEIRegistries;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviourProvider;

import java.util.List;
import java.util.Optional;

/** Adds Tetra's data-bearing rolled scrolls to Create: Enchantment Industry's printer. */
public final class TetraScrollPrinterCompat {
    private static final DeferredRegister<PrintingBehaviourProvider> PRINTING_BEHAVIOURS =
            DeferredRegister.create(CEIRegistries.PRINTING_BEHAVIOUR_PROVIDER, "create_enchantment_industry");
    private static final ResourceLocation SCROLL_ID = ResourceLocation.fromNamespaceAndPath("tetra", "scroll_rolled");
    private static final ResourceLocation INK_ID = ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "ink");
    private static final int INK_COST = 250;

    private TetraScrollPrinterCompat() {
    }

    public static void register(IEventBus modEventBus) {
        PRINTING_BEHAVIOURS.register("tetra_scroll", () -> new PrintingBehaviourProvider(
                PrintingBehaviourProvider.DEFAULT_PRIORITY,
                TetraScrollPrintingBehaviour::create));
        PRINTING_BEHAVIOURS.register(modEventBus);
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
            Item scroll = ForgeRegistries.ITEMS.getValue(SCROLL_ID);
            if (scroll == null || !stack.is(scroll)) {
                return false;
            }

            CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
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
            Fluid ink = ForgeRegistries.FLUIDS.getValue(INK_ID);
            return stack.is(Items.PAPER) && ink != null && fluidStack.getFluid() == ink ? INK_COST : 0;
        }

        @Override
        public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
            if (!stack.is(Items.PAPER)) {
                return ItemStack.EMPTY;
            }
            ItemStack result = template.copy();
            result.setCount(1);
            return result;
        }

        @Override
        public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
            // Plays the printer completion sound through the normal CEI printer flow.
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            tooltip.add(Component.translatable("create_enchantment_industry.gui.goggles.printing")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable(
                    "create_enchantment_industry.gui.goggles.ink_consumption",
                    INK_COST
            ).withStyle(ChatFormatting.GREEN));
            return true;
        }
    }
}
