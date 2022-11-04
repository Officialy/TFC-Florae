package tfcflorae.common.blocks.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.ExtendedProperties;

import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFTrappedChestBlock extends TFCFChestBlock
{
    public TFCFTrappedChestBlock(ExtendedProperties properties, String textureLocation)
    {
        super(properties, textureLocation, TFCFBlockEntities.TRAPPED_CHEST::get);
    }

    @Override
    protected Stat<ResourceLocation> getOpenChestStat()
    {
        return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return Mth.clamp(ChestBlockEntity.getOpenCount(level, pos), 0, 15);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return direction == Direction.UP ? state.getSignal(level, pos, direction) : 0;
    }
}
