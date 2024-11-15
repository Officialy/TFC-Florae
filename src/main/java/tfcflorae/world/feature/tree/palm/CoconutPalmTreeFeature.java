package tfcflorae.world.feature.tree.palm;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.BlockPos;

import net.dries007.tfc.util.EnvironmentHelpers;

import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.wood.PalmTrunkBlock;
import tfcflorae.common.blocks.wood.TFCFWood;

public class CoconutPalmTreeFeature extends Feature<NoneFeatureConfiguration>
{
    public CoconutPalmTreeFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final BlockState state = level.getBlockState(pos);
        final BlockState stateBelow = level.getBlockState(pos.below());

        if (EnvironmentHelpers.isWorldgenReplaceable(state) && !state.liquid() && !stateBelow.liquid())
        {
            if (TFCFBlocks.PALM_TRUNKS.get(TFCFWood.COCONUT_PALM).isPresent())
            {
                Block block = TFCFBlocks.PALM_TRUNKS.get(TFCFWood.COCONUT_PALM).get();
                if (block instanceof PalmTrunkBlock && block.defaultBlockState().canSurvive(level, pos))
                {
                    return ((PalmTrunkBlock) block).generatePlant(level, pos, context.random(), level.getRandom().nextInt(3) + 1);
                }
            }
        }
        return false;
    }
}
