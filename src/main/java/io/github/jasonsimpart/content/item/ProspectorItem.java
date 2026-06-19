package io.github.jasonsimpart.content.item;

import com.mojang.datafixers.util.Pair;
import com.tom.createores.CreateOreExcavation;
import com.tom.createores.OreVeinGenerator;
import com.tom.createores.Registration;
import com.tom.createores.components.OreVeinAtlasDataComponent;
import com.tom.createores.recipe.VeinRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ProspectorItem extends Item {
    private static final int SEARCH_RADIUS = 16;
    private static final int COOLDOWN_TICKS = 60;
    private static final int BLOCK_SIZE = 16;

    public ProspectorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            locateNearestVein(serverLevel, player);
            player.swing(hand, true);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.createdelightcore.shift_prospector").withStyle(ChatFormatting.GRAY));
    }

    private static void locateNearestVein(ServerLevel level, Player player) {
        BlockPos origin = player.blockPosition();
        Pair<BlockPos, RecipeHolder<VeinRecipe>> result = OreVeinGenerator.getPicker(level)
                .locate(origin, level, SEARCH_RADIUS, atlasFilter(player));

        if (result == null) {
            player.displayClientMessage(Component.translatable("message.createdelightcore.no_vein"), true);
            return;
        }

        BlockPos veinPos = result.getFirst();
        VeinRecipe vein = result.getSecond().value();
        int distance = (int) Math.floor(Math.sqrt(origin.distToLowCornerSqr(veinPos.getX(), origin.getY(), veinPos.getZ())));
        Component veinName = vein.getName().copy().withStyle(ChatFormatting.YELLOW);

        Component message = Component.translatable("message.createdelightcore.nearest_vein").append(veinName);
        Component direction = directionFrom(
                Math.floorDiv(origin.getX(), BLOCK_SIZE),
                Math.floorDiv(origin.getZ(), BLOCK_SIZE),
                Math.floorDiv(veinPos.getX(), BLOCK_SIZE),
                Math.floorDiv(veinPos.getZ(), BLOCK_SIZE)
        );

        if (direction == null) {
            message = message.copy().append(Component.translatable("message.createdelightcore.underfoot"));
        } else {
            message = message.copy().append(Component.translatable("message.createdelightcore.at", direction, distance));
        }

        player.displayClientMessage(message, true);
    }

    private static Predicate<RecipeHolder<VeinRecipe>> atlasFilter(Player player) {
        Optional<OreVeinAtlasDataComponent> atlasData = findAtlasData(player);
        if (atlasData.isEmpty()) {
            return vein -> true;
        }

        Optional<ResourceLocation> target = atlasData.get().target();
        if (target.isPresent()) {
            return vein -> vein.id().equals(target.get());
        }

        Set<ResourceLocation> excludedVeins = new HashSet<>(atlasData.get().exclude());
        if (excludedVeins.isEmpty()) {
            return vein -> true;
        }
        return vein -> !excludedVeins.contains(vein.id());
    }

    private static Optional<OreVeinAtlasDataComponent> findAtlasData(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Registration.VEIN_ATLAS_ITEM.get())) {
                return Optional.ofNullable(stack.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT));
            }
        }
        return Optional.empty();
    }

    private static Component directionFrom(int x1, int z1, int x2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        if (dx == 0 && dz == 0) {
            return null;
        }

        double threshold = 2.0;
        if (Math.abs(dz) > threshold * Math.abs(dx)) {
            return Component.translatable(dz > 0 ? "message.createdelightcore.south" : "message.createdelightcore.north");
        }
        if (Math.abs(dx) > threshold * Math.abs(dz)) {
            return Component.translatable(dx > 0 ? "message.createdelightcore.east" : "message.createdelightcore.west");
        }
        if (dx > 0 && dz > 0) {
            return Component.translatable("message.createdelightcore.southeast");
        }
        if (dx > 0) {
            return Component.translatable("message.createdelightcore.northeast");
        }
        if (dz > 0) {
            return Component.translatable("message.createdelightcore.southwest");
        }
        return Component.translatable("message.createdelightcore.northwest");
    }
}
