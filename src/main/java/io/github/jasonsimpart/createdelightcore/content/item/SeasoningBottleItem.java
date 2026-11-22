package io.github.jasonsimpart.createdelightcore.content.item;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeasoningBottleItem extends Item {
    public static final int CAPACITY = 250;
    public static final TagKey<Fluid> SEASONINGS = TagKey.create(Registries.FLUID,
            CreateDelightCore.id("seasoning_bottle_fluids"));
    private static final String VAT_INPUT = "VatInput";
    private static final ResourceLocation VAT = ResourceLocation.fromNamespaceAndPath("northeast_china_delight", "vat");

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()
                || getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getType() != HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            if (isVatInput(stack)) {
                stack.removeTagKey(VAT_INPUT);
            } else {
                stack.getOrCreateTag().putBoolean(VAT_INPUT, true);
            }
            player.displayClientMessage(Component.translatable(
                    "tooltip.createdelightcore.seasoning_bottle.vat_mode_changed", vatModeName(stack)), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return isVat(level, pos);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        Level level = context.getLevel();
        // The vat resolves the direction in its recipe interaction.
        if (player == null || isVat(level, context.getClickedPos())
                || FluidUtil.getFluidHandler(level, context.getClickedPos(),
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
        tooltip.add(Component.translatable("tooltip.createdelightcore.seasoning_bottle.vat_mode", vatModeName(stack)));
    }

    public static boolean isVatInput(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(VAT_INPUT);
    }

    private static boolean isVat(LevelReader level, BlockPos pos) {
        return VAT.equals(ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock()));
    }

    private static Component vatModeName(ItemStack stack) {
        return Component.translatable(isVatInput(stack)
                ? "tooltip.createdelightcore.seasoning_bottle.vat_input"
                : "tooltip.createdelightcore.seasoning_bottle.vat_output");
    }

    public static FluidStack contents(ItemStack stack) {
        return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    }
}
