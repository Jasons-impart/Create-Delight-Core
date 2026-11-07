// Adapted from Create More Recipes by iglee42 (MIT). See META-INF/licenses/create-more-recipes.txt.
package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.content.cooler.SnowmanCoolerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Internal;

public class CDArmInteractionPointTypes {
	static {
		register("snowman_cooler", new SnowmanCoolerType());
	}

	private static <T extends ArmInteractionPointType> void register(String name, T type) {
		Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, CreateDelightCore.id(name), type);
	}

	@Internal
	public static void init() {
	}

	//
	public static class SnowmanCoolerType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return CDBlocks.SNOWMAN_COOLER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new SnowmanCoolerPoint(this, level, pos, state);
		}
	}


	public static class SnowmanCoolerPoint extends AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint {
		public SnowmanCoolerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ItemStack stack, boolean simulate) {
			ItemStack input = stack.copy();
			InteractionResultHolder<ItemStack> res =
				SnowmanCoolerBlock.tryInsert(cachedState, level, pos, input, false, false, simulate);
			ItemStack remainder = res.getObject();
			if (input.isEmpty()) {
				return remainder;
			} else {
				if (!simulate)
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
				return input;
			}
		}
	}

}
