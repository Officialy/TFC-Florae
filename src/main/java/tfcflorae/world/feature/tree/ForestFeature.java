package tfcflorae.world.feature.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.feature.tree.ForestConfig;

import org.jetbrains.annotations.Nullable;

import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.interfaces.TFCBiomesMixinInterface;
import tfcflorae.util.TFCFHelpers;

public class ForestFeature extends Feature<ForestConfig>
{
    private static TFCBiomes staticBiomes = new TFCBiomes();

    static final BiomeExtension STEPPES = ((TFCBiomesMixinInterface) (Object) staticBiomes).getStaticSteppes();
    static final BiomeExtension SHRUBLANDS = ((TFCBiomesMixinInterface) (Object) staticBiomes).getStaticShrublands();
    static final BiomeExtension MOORLANDS = ((TFCBiomesMixinInterface) (Object) staticBiomes).getStaticMoorlands();

    public ForestFeature(Codec<ForestConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ForestConfig> context)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource rand = context.random();
        final ForestConfig config = context.config();

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData data = provider.get(level, pos);
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final ForestType forestType = data.getForestType();
        final ForestConfig.Type typeConfig = config.typeMap().get(forestType);
        final Biome biome = level.getBiome(pos).value();
        final BiomeExtension variants = TFCBiomes.getExtensionOrThrow(context.level(), biome);
        final float density = data.getForestDensity();

        if (rand.nextFloat() > typeConfig.perChunkChance()) return false;

        int treeCount = typeConfig.treeCount().sample(rand);
        final int groundCount = typeConfig.groundcoverCount().sample(rand);
        final int bushCount = typeConfig.sampleBushCount(rand, typeConfig.bushCount(), treeCount, density);

        boolean placedTrees = false;
        boolean placedBushes = false;

        treeCount = (int) (treeCount * (0.6f + 0.9f * density));
        if (variants != STEPPES && variants != SHRUBLANDS && variants != MOORLANDS)
        {
            for (int i = 0; i < treeCount; i++)
            {
                placedTrees |= placeTree(level, context.chunkGenerator(), rand, pos, config, data, mutablePos, typeConfig);
            }
        }
        else
        {
            placedTrees = true;
        }
        for (int j = 0; j < bushCount; j++)
        {
            placedBushes |= placeBush(level, rand, pos, config, data, mutablePos);
        }
        if (placedTrees)
        {
            placeGroundcover(level, rand, pos, config, data, mutablePos, groundCount);
            placeFallenTree(level, rand, pos, config, data, mutablePos);
        }
        return placedTrees || placedBushes;
    }

    private boolean placeTree(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, BlockPos.MutableBlockPos mutablePos, ForestConfig.Type typeConfig)
    {
        final int chunkX = chunkBlockPos.getX();
        final int chunkZ = chunkBlockPos.getZ();

        mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
        mutablePos.setY(level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, mutablePos.getX(), mutablePos.getZ()));

        final ForestConfig.Entry entry = getTree(data, random, config, mutablePos);
        if (entry != null)
        {
            ConfiguredFeature<?, ?> feature;
            final int oldChance = entry.oldGrowthChance();
            final int deadChance = entry.deadChance();
            if (typeConfig.allowOldGrowth() && oldChance > 0 && random.nextInt(oldChance) == 0)
            {
                feature = entry.getOldGrowthFeature();
            }
            else if (deadChance > 0 && random.nextInt(deadChance) == 0)
            {
                feature = entry.getDeadFeature();
            }
            else
            {
                final int spoilerChance = entry.spoilerOldGrowthChance();
                if (typeConfig.hasSpoilers() && spoilerChance > 0 && random.nextInt(spoilerChance) == 0)
                {
                    feature = entry.getOldGrowthFeature();
                }
                else
                {
                    feature = entry.getFeature();
                }
            }
            return feature.place(level, generator, random, mutablePos);
        }
        return false;
    }

    private boolean placeBush(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, BlockPos.MutableBlockPos mutablePos)
    {
        final int chunkX = chunkBlockPos.getX();
        final int chunkZ = chunkBlockPos.getZ();

        mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
        mutablePos.setY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mutablePos.getX(), mutablePos.getZ()));

        final ForestConfig.Entry entry = getTree(data, random, config, mutablePos);
        if (entry != null && EnvironmentHelpers.canPlaceBushOn(level, mutablePos))
        {
            entry.bushLog().ifPresent(log -> entry.bushLeaves().ifPresent(leaves -> {
                placeBushPart(level, mutablePos, log, leaves, 1.0F, random, true);
                for (int i = 0; i < 5; i++)
                {
                    if (random.nextInt(4) == 0)
                    {
                        mutablePos.move(Direction.Plane.HORIZONTAL.getRandomDirection(random));
                        placeBushPart(level, mutablePos, leaves, leaves, 0.7F, random, false);
                        if (random.nextInt(6) == 0)
                        {
                            mutablePos.move(Direction.UP);
                            placeBushPart(level, mutablePos, leaves, leaves, 0.6F, random, false);
                            break;
                        }
                    }
                }
            }));
            return true;
        }
        return false;
    }

    private void placeBushPart(WorldGenLevel level, BlockPos.MutableBlockPos mutablePos, BlockState log, BlockState leaves, float decay, RandomSource rand, boolean needsEmptyCenter)
    {
        if (EnvironmentHelpers.isWorldgenReplaceable(level, mutablePos))
        {
            setBlock(level, mutablePos, log);
        }
        else if (needsEmptyCenter)
        {
            return;
        }
        for (Direction facing : Helpers.DIRECTIONS)
        {
            if (facing != Direction.DOWN)
            {
                BlockPos offsetPos = mutablePos.offset(facing.getStepX(), facing.getStepY(), facing.getStepZ());
                if (EnvironmentHelpers.isWorldgenReplaceable(level, offsetPos) && rand.nextFloat() < decay)
                {
                    setBlock(level, offsetPos, leaves);
                }
            }
        }
    }

    private void placeGroundcover(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, BlockPos.MutableBlockPos mutablePos, int tries)
    {
        final int chunkX = chunkBlockPos.getX();
        final int chunkZ = chunkBlockPos.getZ();

        mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
        mutablePos.setY(level.getHeight(Heightmap.Types.OCEAN_FLOOR, mutablePos.getX(), mutablePos.getZ()));

        final ForestConfig.Entry entry = getTree(data, random, config, mutablePos);
        if (entry != null)
        {
            entry.groundcover().ifPresent(groundcover -> {
                for (int j = 0; j < tries; ++j)
                {
                    BlockState placementState = groundcover.get(random);

                    mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
                    mutablePos.setY(level.getHeight(Heightmap.Types.OCEAN_FLOOR, mutablePos.getX(), mutablePos.getZ()));

                    placementState = FluidHelpers.fillWithFluid(placementState, level.getFluidState(mutablePos).getType());
                    if (placementState != null && EnvironmentHelpers.isWorldgenReplaceable(level.getBlockState(mutablePos)) && EnvironmentHelpers.isOnSturdyFace(level, mutablePos))
                    {
                        setBlock(level, mutablePos, placementState);
                    }
                }
            });
        }
    }

    private void placeFallenTree(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, BlockPos.MutableBlockPos mutablePos)
    {
        final int chunkX = chunkBlockPos.getX();
        final int chunkZ = chunkBlockPos.getZ();

        mutablePos.set(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));
        mutablePos.setY(level.getHeight(Heightmap.Types.OCEAN_FLOOR, mutablePos.getX(), mutablePos.getZ()));

        mutablePos.move(Direction.DOWN);
        BlockState downState = level.getBlockState(mutablePos);
        mutablePos.move(Direction.UP);
        if (Helpers.isBlock(downState, TFCTags.Blocks.BUSH_PLANTABLE_ON) || Helpers.isBlock(downState, TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON))
        {
            final ForestConfig.Entry entry = getTree(data, random, config, mutablePos);
            if (entry != null)
            {
                final int fallChance = entry.fallenChance();
                if (fallChance > 0 && level.getRandom().nextInt(fallChance) == 0)
                {
                    BlockState log = entry.fallenLog().orElse(null);
                    if (log != null)
                    {
                        final Direction axis = Direction.Plane.HORIZONTAL.getRandomDirection(random);

                        log = Helpers.setProperty(log, TFCBlockStateProperties.NATURAL, false);
                        log = Helpers.setProperty(log, BlockStateProperties.AXIS, axis.getAxis());

                        final int length = 4 + random.nextInt(10);
                        final BlockPos start = mutablePos.immutable();
                        final boolean[] moment = new boolean[length];

                        mutablePos.set(start);
                        int valid = 0;
                        for (; valid < length; valid++)
                        {
                            final BlockState replaceState = level.getBlockState(mutablePos);
                            if (EnvironmentHelpers.isWorldgenReplaceable(replaceState) || replaceState.getBlock() instanceof ILeavesBlock)
                            {
                                mutablePos.move(Direction.DOWN);
                                moment[valid] = level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, Direction.UP);
                            }
                            else
                            {
                                break;
                            }

                            mutablePos.move(Direction.UP);
                            mutablePos.move(axis);
                        }

                        int left = 0, right = valid - 1;
                        for (; left < moment.length; left++)
                        {
                            if (moment[left]) break;
                        }
                        for (; right >= 0; right--)
                        {
                            if (moment[right]) break;
                        }

                        if (left <= valid / 2 && right >= valid / 2 && valid >= 3)
                        {
                            // Balanced
                            mutablePos.set(start);
                            for (int i = 0; i < length; i++)
                            {
                                level.setBlock(mutablePos, log, Block.UPDATE_ALL);

                                BlockPos.MutableBlockPos mutablePosMycelium = new BlockPos.MutableBlockPos(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ());

                                final ChunkDataProvider provider = ChunkDataProvider.get(level);
                                final ChunkData dataNew = provider.get(level, mutablePosMycelium);

                                final float rainfall = dataNew.getRainfall(mutablePosMycelium);
                                final float rainfallInverted = ((ClimateModel.MAXIMUM_RAINFALL - rainfall) + 10F) * 0.1F;

                                final float actualForestDensity = dataNew.getForestDensity();
                                final float forestDensity = actualForestDensity == 0 ? 0.001F : actualForestDensity; // Cannot divide by 0.

                                final ForestType forestType = data.getForestType();
                                final BlockState fungiState = Helpers.getRandomElement(ForgeRegistries.BLOCKS, TFCFTags.Blocks.FUNGI_ON_FALLEN_LOGS, random).map(Block::defaultBlockState).orElse(TFCFBlocks.PLANTS.get(TFCFPlant.PORCINI).get().defaultBlockState());

                                if (Mth.clamp(random.nextFloat(), 0, 1F - actualForestDensity) <= forestType.ordinal())
                                {
                                    if (fungiState != null && fungiState.canSurvive(level, mutablePosMycelium.above()))
                                    {
                                        level.setBlock(mutablePosMycelium.above(), fungiState, Block.UPDATE_ALL);
                                    }
                                }

                                final int radius = Math.round((forestDensity * 1.5F) * forestType.ordinal());
                                if (radius > 0 && random.nextInt(Math.round(((rainfallInverted * 1.2F) / forestDensity) * (5 - forestType.ordinal()) * 0.75F)) <= forestType.ordinal())
                                {
                                    final int radiusSquared = radius * radius;

                                    final int posX = mutablePosMycelium.getX();
                                    final int posZ = mutablePosMycelium.getZ();
                                    final int posY = mutablePosMycelium.getY();

                                    for (int x = posX - radius; x <= posX + radius; ++x)
                                    {
                                        for (int z = posZ - radius; z <= posZ + radius; ++z)
                                        {
                                            final int relX = x - posX;
                                            final int relZ = z - posZ;

                                            if (relX * relX + relZ * relZ <= radiusSquared)
                                            {
                                                for (int y = posY - radius; y <= posY; ++y)
                                                {
                                                    mutablePosMycelium.set(x, y, z);

                                                    if (level.getBlockState(mutablePosMycelium.above()).isAir())
                                                    {
                                                        final BlockState stateAt = level.getBlockState(mutablePosMycelium);

                                                        if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_BOG_IRON))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.MYCELIUM_BOG_IRON.get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }
                                                        else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_LOAM))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(SoilBlockType.Variant.LOAM).get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }
                                                        else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SANDY_LOAM))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(SoilBlockType.Variant.SANDY_LOAM).get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }
                                                        else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SILT))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(SoilBlockType.Variant.SILT).get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }
                                                        else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_SILTY_LOAM))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(SoilBlockType.Variant.SILTY_LOAM).get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }
                                                        else if (Helpers.isBlock(stateAt, TFCFTags.Blocks.IS_HUMUS))
                                                        {
                                                            level.setBlock(mutablePosMycelium, TFCFBlocks.TFCFSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(TFCFSoil.TFCFVariant.HUMUS).get().defaultBlockState(), Block.UPDATE_ALL);
                                                        }

                                                        if (Mth.clamp(random.nextFloat(), 0, 1F - actualForestDensity) <= forestType.ordinal())
                                                        {
                                                            if (fungiState != null && fungiState.canSurvive(level, mutablePosMycelium.above()))
                                                            {
                                                                level.setBlock(mutablePosMycelium.above(), fungiState, Block.UPDATE_ALL);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } 
                                }
                                mutablePos.move(axis);
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private ForestConfig.Entry getTree(ChunkData chunkData, RandomSource random, ForestConfig config, BlockPos pos)
    {
        List<ForestConfig.Entry> entries = new ArrayList<>(4);
        float rainfall = chunkData.getRainfall(pos);
        float averageTemperature = chunkData.getAverageTemp(pos);
        config.entries().stream().map(configuredFeature -> configuredFeature.value().config()).map(cfg -> (ForestConfig.Entry) cfg).forEach(entry -> {
            // silly way to halfway guarantee that stuff is in general order of dominance
            float lastRain = entry.getAverageRain();
            float lastTemp = entry.getAverageTemp();
            if (entry.isValid(averageTemperature, rainfall))
            {
                if (entry.distanceFromMean(lastTemp, lastRain) < entry.distanceFromMean(averageTemperature, rainfall))
                {
                    entries.add(entry); // if the last one was closer to it's target, just add it normally
                }
                else
                {
                    entries.add(0, entry); // if the new one is closer, stick it in front
                }
            }
        });

        if (entries.isEmpty()) return null;
        if (config.useWeirdness())
        {
            // remove up to 3 entries from the config based on weirdness, less likely to happen each time
            float weirdness = chunkData.getForestWeirdness();
            Collections.rotate(entries, -(int) (weirdness * (entries.size() - 1f)));
            for (int i = 1; i >= -1; i--)
            {
                if (entries.size() <= 1)
                    break;
                if (random.nextFloat() > weirdness - (0.15f * i) + 0.1f)
                {
                    entries.remove(entries.size() - 1);
                }
            }
        }


        int index = 0;
        while (index < entries.size() - 1 && random.nextFloat() < 0.6f)
        {
            index++;
        }
        return entries.get(index);
    }

    /**
     * Holder for {@link ForestConfig.Entry} in order to hold all tree instances in a tag.
     */
    public static class Entry extends Feature<ForestConfig.Entry>
    {
        public Entry(Codec<ForestConfig.Entry> codec)
        {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<ForestConfig.Entry> context)
        {
            throw new IllegalArgumentException("This is not a real feature and should never be placed!");
        }
    }
}