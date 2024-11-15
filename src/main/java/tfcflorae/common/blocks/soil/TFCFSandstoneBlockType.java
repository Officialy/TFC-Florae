package tfcflorae.common.blocks.soil;

import java.util.function.Function;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;


public enum TFCFSandstoneBlockType
{
    LAYERED(color -> BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color.getMapColor()).strength(0.8f).requiresCorrectToolForDrops());

    private final Function<Colors, BlockBehaviour.Properties> factory;

    TFCFSandstoneBlockType(Function<Colors, BlockBehaviour.Properties> factory)
    {
        this.factory = factory;
    }

    public BlockBehaviour.Properties properties(Colors color)
    {
        return factory.apply(color);
    }
}
