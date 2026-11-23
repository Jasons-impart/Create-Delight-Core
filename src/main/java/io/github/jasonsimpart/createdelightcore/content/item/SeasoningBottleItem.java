package io.github.jasonsimpart.createdelightcore.content.item;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeasoningBottleItem extends Item {
    public static final int CAPACITY = 250;
    public static final TagKey<Fluid> SEASONINGS = TagKey.create(Registries.FLUID,
            CreateDelightCore.id("seasoning_bottle_fluids"));

    public SeasoningBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY) {
            @Override
            public boolean isFluidValid(int tank, FluidStack fluid) {
                return tank == 0 && !fluid.isEmpty() && fluid.getFluid().is(SEASONINGS);
            }

            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return isFluidValid(0, fluid);
            }
        };
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        var source = FluidUtil.getFluidHandler(level, context.getClickedPos(), context.getClickedFace()).orElse(null);
        if (source == null) {
            return InteractionResult.PASS;
        }
        FluidStack output = source.drain(CAPACITY, FluidAction.SIMULATE);
        if (output.isEmpty()) {
            return InteractionResult.PASS;
        }
        // Leave normal block interaction first on tanks that can take back their output.
        for (int tank = 0; tank < source.getTanks(); tank++) {
            if (source.isFluidValid(tank, output)) {
                return InteractionResult.PASS;
            }
        }
        var result = FluidUtil.tryFillContainer(stack, source, CAPACITY, player, !level.isClientSide);
        if (!result.isSuccess()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.setItemInHand(context.getHand(), result.getResult());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || FluidUtil.getFluidHandler(level, context.getClickedPos(),
                context.getClickedFace()).resolve().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return FluidUtil.interactWithFluidHandler(player, context.getHand(), level, context.getClickedPos(),
                context.getClickedFace()) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !contents(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * contents(stack).getAmount() / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xD49856;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FluidStack fluid = contents(stack);
        tooltip.add(fluid.isEmpty()
                ? Component.translatable("tooltip.createdelightcore.seasoning_bottle.empty", CAPACITY)
                : Component.translatable("tooltip.createdelightcore.seasoning_bottle.contents",
                        fluid.getDisplayName(), fluid.getAmount(), CAPACITY));
    }

    public static FluidStack contents(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    }
}
