package io.github.jasonsimpart.createdelightcore.compat.createenchantmentindustry;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.api.PrintEntryRegisterEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;

import java.util.List;

/** Adds Tetra's data-bearing rolled scrolls to Create: Enchantment Industry's printer. */
public final class TetraScrollPrinterCompat {
    private static final ResourceLocation ENTRY_ID = CreateDelightCore.id("tetra_scroll");
    private static final ResourceLocation SCROLL_ID = ResourceLocation.fromNamespaceAndPath("tetra", "scroll_rolled");
    private static final ResourceLocation INK_ID = ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "ink");
    private static final int INK_COST = 250;
    private static final PrintEntry PRINT_ENTRY = new TetraScrollPrintEntry();

    private TetraScrollPrinterCompat() {
    }

    public static void registerPrintEntry(PrintEntryRegisterEvent event) {
        event.register(PRINT_ENTRY);
    }

    private static final class TetraScrollPrintEntry implements PrintEntry {
        @Override
        public ResourceLocation id() {
            return ENTRY_ID;
        }

        @Override
        public boolean match(ItemStack target) {
            Item scroll = ForgeRegistries.ITEMS.getValue(SCROLL_ID);
            if (scroll == null || !target.is(scroll)) {
                return false;
            }

            CompoundTag blockEntityTag = target.getTagElement("BlockEntityTag");
            return blockEntityTag != null
                    && blockEntityTag.contains("data", Tag.TAG_LIST)
                    && !blockEntityTag.getList("data", Tag.TAG_COMPOUND).isEmpty();
        }

        @Override
        public boolean valid(ItemStack target, ItemStack input) {
            return input.is(Items.PAPER);
        }

        @Override
        public int requiredInkAmount(ItemStack target) {
            return INK_COST;
        }

        @Override
        public Fluid requiredInkType(ItemStack target) {
            Fluid ink = ForgeRegistries.FLUIDS.getValue(INK_ID);
            if (ink == null) {
                throw new IllegalStateException("Create: Enchantment Industry ink fluid is not registered");
            }
            return ink;
        }

        @Override
        public ItemStack print(ItemStack target, ItemStack input) {
            ItemStack result = target.copy();
            result.setCount(1);
            return result;
        }

        @Override
        public boolean isTooExpensive(ItemStack target, int capacity) {
            return INK_COST > capacity;
        }

        @Override
        public void addToGoggleTooltip(List<Component> tooltip, boolean sneaking, ItemStack target) {
            tooltip.add(target.getHoverName().copy().withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable(
                    "create_enchantment_industry.gui.goggles.ink_consumption",
                    INK_COST
            ).withStyle(ChatFormatting.GREEN));
        }

        @Override
        public MutableComponent getDisplaySourceContent(ItemStack target) {
            return target.getHoverName().copy();
        }
    }
}
