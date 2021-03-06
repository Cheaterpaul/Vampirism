package de.teamlapen.vampirism.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;


public class TotemBaseBlock extends VampirismBlock {
    private static final VoxelShape shape = makeShape();
    private final static String regName = "totem_base";

    private static VoxelShape makeShape() {
        VoxelShape a = Block.makeCuboidShape(1, 0, 1, 15, 1, 15);
        VoxelShape b = Block.makeCuboidShape(2, 1, 2, 14, 2, 14);
        VoxelShape c = Block.makeCuboidShape(3, 2, 3, 13, 3, 13);

        VoxelShape d1 = Block.makeCuboidShape(4, 3, 4, 7, 16, 7);
        VoxelShape d2 = Block.makeCuboidShape(9, 3, 4, 12, 16, 7);
        VoxelShape d3 = Block.makeCuboidShape(4, 3, 9, 7, 16, 12);
        VoxelShape d4 = Block.makeCuboidShape(9, 3, 9, 12, 16, 12);

        VoxelShape e = Block.makeCuboidShape(5, 3, 5, 11, 16, 11);

        return VoxelShapes.or(a, b, c, d1, d2, d3, d4, e);
    }

    public TotemBaseBlock() {
        super(regName, Properties.create(Material.ROCK).hardnessAndResistance(40, 2000).sound(SoundType.STONE).notSolid());

    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }


    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        BlockPos up = pos.up();
        BlockState upState = world.getBlockState(pos.up());
        if (upState.getBlock() instanceof TotemTopBlock) {
            TileEntity upTE = world.getTileEntity(pos.up());
            if (!upState.getBlock().removedByPlayer(upState, world, pos.up(), player, willHarvest, fluid)) {
                return false;
            }
            if (willHarvest) {
                Block.spawnDrops(upState.getBlockState(), world, up, upTE);
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }
}
