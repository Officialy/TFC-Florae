package tfcflorae.world.feature.tree;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

import com.google.common.collect.ImmutableList;

import java.util.Locale;

import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.world.feature.TFCFFeatures;

@SuppressWarnings("deprecation")
public class TFCFBambooTreeGrower extends AbstractTreeGrower
{
    public TFCFPlant bamboo;

    public TFCFBambooTreeGrower(TFCFPlant bamboo)
    {
        this.bamboo = bamboo;
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean bees)
    {
        ResourceKey<ConfiguredFeature<TreeConfiguration, ?>> feature = TFCFFeatures.config("tree/grower/" + bamboo.name().toLowerCase(Locale.ROOT), TFCFFeatures.BAMBOO_TREE.get(),
            new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(TFCFBlocks.BAMBOO_LOGS.get(bamboo).get().defaultBlockState()),//.setValue(BambooLogBlock.NATURAL, true)),
                //new WeightedStateProvider(SimpleWeightedRandomList.builder().add(TFCFBlocks.BAMBOO_LOGS.get(bamboo).get().defaultBlockState(), 3).add(TFCFBlocks.BAMBOO_LEAVES.get(bamboo).get().defaultBlockState(), 1)), 
                new StraightTrunkPlacer(12, 9, 0),
                BlockStateProvider.simple(TFCFBlocks.BAMBOO_LEAVES.get(bamboo).get().defaultBlockState().setValue(TFCLeavesBlock.PERSISTENT, false)),
                //new (BlockStateProvider) WeightedStateProvider(SimpleWeightedRandomList.builder().add(TFCFBlocks.BAMBOO_LEAVES.get(bamboo).get().defaultBlockState(), 5).add(TFCFBlocks.BAMBOO_LEAVES.get(bamboo).get().defaultBlockState(), 1)),
                new RandomSpreadFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), ConstantInt.of(5), 150),
                new TwoLayersFeatureSize(1, 0, 1)).decorators(ImmutableList.of(BambooLeavesDecorator.INSTANCE)).ignoreVines().dirt(BlockStateProvider.simple(TFCBlocks.SOIL.get(SoilBlockType.ROOTED_DIRT).get(SoilBlockType.Variant.LOAM).get().defaultBlockState())).build());


        return (ResourceKey) feature; //todo fix cast
    }
}