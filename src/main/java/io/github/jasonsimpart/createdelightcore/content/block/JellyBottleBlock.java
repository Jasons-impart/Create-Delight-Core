package io.github.jasonsimpart.createdelightcore.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JellyBottleBlock extends Block {
    public JellyBottleBlock(Properties pProperties) {
        super(pProperties);
    }
    public static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15, 13);
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}
