package tfcflorae.world.feature;


import java.util.function.Predicate;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;

import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.soil.SandLayerBlock;

public class TidePoolFeature extends Feature<NoneFeatureConfiguration>
{
    public TidePoolFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        boolean placedAny = false;
        final RandomSource random = context.random();
        final WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        if (level.getBlockState(origin).getBlock() instanceof SandLayerBlock)
        {
            origin = origin.below();
        }

        if (origin.getY() > 67 || origin.getY() < 62)
            return false;

        final boolean hasRim = random.nextDouble() < 0.9D;
        final int rimOffsetX = hasRim ? Mth.nextInt(random, 0, 2) : 0;
        final int rimOffsetZ = hasRim ? Mth.nextInt(random, 0, 2) : 0;
        final boolean willPlaceRim = hasRim && rimOffsetX != 0 && rimOffsetZ != 0;
        final int xSize = Mth.nextInt(random, 3, 7);
        final int zSize = Mth.nextInt(random, 3, 7);
        final int maxLength = Math.max(xSize, zSize);

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final RockSettings rock = provider.get(context.level(), origin).getRockData().getRock(origin);
        final BlockState cobble = rock.cobble().defaultBlockState();
        final BlockState raw = rock.raw().defaultBlockState();
        final BlockState water = TFCBlocks.SALT_WATER.get().defaultBlockState();
        final Predicate<BlockState> test = state -> state.getBlock() == cobble.getBlock() || !state.getFluidState().isEmpty();

        for (BlockPos pos : BlockPos.withinManhattan(origin, xSize, 0, zSize))
        {
            if (pos.distManhattan(origin) > maxLength)
            {
                break;
            }

            if (isClear(level, pos, test))
            {
                if (willPlaceRim || hasSandLayerAround(level, pos))
                {
                    placedAny = true;
                    this.setBlock(level, pos, raw);
                    if (random.nextFloat() < 0.01f && level.getBlockState(pos.above()).isAir())
                    {
                        setBlock(level, pos.above(), TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.GUANO).get().defaultBlockState());
                    }
                }

                BlockPos offsetPos = pos.offset(rimOffsetX, 0, rimOffsetZ);
                if (isClear(level, offsetPos, test))
                {
                    placedAny = true;
                    BlockState toPlace = cobble;
                    if (random.nextBoolean())
                    {
                        toPlace = water;
                        if (random.nextFloat() < 0.15f)
                        {
                            BlockState groundcover = Helpers.getRandomElement(ForgeRegistries.BLOCKS, TFCFTags.Blocks.TIDE_POOL_BLOCKS, random).map(Block::defaultBlockState).orElse(water);
                            groundcover = FluidHelpers.fillWithFluid(groundcover, TFCFluids.SALT_WATER.getSource());
                            if (groundcover != null)
                            {
                                toPlace = groundcover;
                            }
                        }
                    }
                    this.setBlock(level, offsetPos, toPlace);
                    for (Direction direction : Direction.Plane.HORIZONTAL)
                    {
                        if (level.getBlockState(offsetPos.relative(direction)).getBlock() instanceof SandLayerBlock && pos.distManhattan(origin) >= maxLength - random.nextInt(2))
                        {
                            this.setBlock(level, offsetPos.relative(direction), cobble);
                        }
                    }
                }
            }
        }
        return placedAny;
    }

    public static boolean isClear(LevelAccessor level, BlockPos pos, Predicate<BlockState> contentsTest)
    {
        if (contentsTest.test(level.getBlockState(pos)))
        {
            return false;
        }
        else
        {
            for (Direction direction : Helpers.DIRECTIONS)
            {
                final boolean airAbove = level.getBlockState(pos.relative(direction)).isAir();
                if (airAbove && direction != Direction.UP || !airAbove && direction == Direction.UP)
                {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean hasSandLayerAround(LevelAccessor level, BlockPos pos)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            return level.getBlockState(pos.relative(direction)).getBlock() instanceof SandLayerBlock;
        }
        return false;
    }
}