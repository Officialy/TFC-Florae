package tfcflorae.common.blocks.plant;

import java.util.Random;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.EpiphytePlantBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;

public abstract class FungiEpiphyteBlock extends EpiphytePlantBlock
{
    public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;

    protected static final VoxelShape PLANT_SHAPE = box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public static FungiEpiphyteBlock create(RegistryPlant plant, ExtendedProperties properties)
    {
        return new FungiEpiphyteBlock(properties)
        {
            @Override
            public RegistryPlant getPlant()
            {
                return plant;
            }
        };
    }

    protected FungiEpiphyteBlock(ExtendedProperties properties)
    {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        if (random.nextDouble() < TFCConfig.SERVER.plantGrowthChance.get())
        {
            state = state.setValue(AGE, Math.min(state.getValue(AGE) + 1, 3));

            if (random.nextInt(25) < TFCConfig.SERVER.plantGrowthChance.get())
            {
                int i = 5;
                int j = 4;

                for(BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4)))
                {
                    if (level.getBlockState(blockpos).is(this))
                    {
                        --i;
                        if (i <= 0) {
                            return;
                        }
                    }
                }

                BlockPos blockpos1 = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
                for(int k = 0; k < 4; ++k)
                {
                    if (level.isEmptyBlock(blockpos1) && state.canSurvive(level, blockpos1))
                    {
                        pos = blockpos1;
                    }

                    blockpos1 = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
                }

                if (level.isEmptyBlock(blockpos1) && state.canSurvive(level, blockpos1))
                {
                    level.setBlock(blockpos1, state, 2);
                }
            }
        }
        level.setBlockAndUpdate(pos, updateStateWithCurrentMonth(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockState attachedState = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
        return (Helpers.isBlock(attachedState, BlockTags.LOGS) && level.getRawBrightness(pos, 0) < 13);
    }
}