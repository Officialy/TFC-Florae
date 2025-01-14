package tfcflorae.world.surface;

import java.util.List;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.util.registry.RegistrySoilVariant;

import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.*;

public class TFCFSoilSurfaceState implements SurfaceState
{
    public static SurfaceState buildType(TFCFSoil type)
    {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
            sand(),
            transition(sand(), soil(type, SoilBlockType.Variant.SANDY_LOAM)),
            soil(type, SoilBlockType.Variant.SANDY_LOAM),
            transition(soil(type, SoilBlockType.Variant.SANDY_LOAM), soil(type, SoilBlockType.Variant.LOAM)),
            soil(type, SoilBlockType.Variant.LOAM),
            transition(soil(type, SoilBlockType.Variant.LOAM), soil(type, SoilBlockType.Variant.SILTY_LOAM)),
            soil(type, SoilBlockType.Variant.SILTY_LOAM),
            transition(soil(type, SoilBlockType.Variant.SILTY_LOAM), soil(type, SoilBlockType.Variant.SILT)),
            soil(type, SoilBlockType.Variant.SILT)
        );
        return new TFCFSoilSurfaceState(regions);
    }

    public static SurfaceState buildTypeGrass(TFCFSoil type)
    {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
            sand(),
            transition(sand(), soil(type, SoilBlockType.Variant.SANDY_LOAM)),
            soil(type, SoilBlockType.Variant.SANDY_LOAM),
            transition(soil(type, SoilBlockType.Variant.SANDY_LOAM), soil(type, SoilBlockType.Variant.LOAM)),
            soil(type, SoilBlockType.Variant.LOAM),
            transition(soil(type, SoilBlockType.Variant.LOAM), soil(type, SoilBlockType.Variant.SILTY_LOAM)),
            soil(type, SoilBlockType.Variant.SILTY_LOAM),
            transition(soil(type, SoilBlockType.Variant.SILTY_LOAM), soil(type, SoilBlockType.Variant.SILT)),
            soil(type, SoilBlockType.Variant.SILT)
        );
        return new TFCFSoilSurfaceState.NeedsPostProcessing(regions);
    }

    public static SurfaceState buildTypeRock(TFCFRockSoil type)
    {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
            sand(),
            transition(sand(), rockSoil(type, SoilBlockType.Variant.SANDY_LOAM)),
            rockSoil(type, SoilBlockType.Variant.SANDY_LOAM),
            transition(rockSoil(type, SoilBlockType.Variant.SANDY_LOAM), rockSoil(type, SoilBlockType.Variant.LOAM)),
            rockSoil(type, SoilBlockType.Variant.LOAM),
            transition(rockSoil(type, SoilBlockType.Variant.LOAM), rockSoil(type, SoilBlockType.Variant.SILTY_LOAM)),
            rockSoil(type, SoilBlockType.Variant.SILTY_LOAM),
            transition(rockSoil(type, SoilBlockType.Variant.SILTY_LOAM), rockSoil(type, SoilBlockType.Variant.SILT)),
            rockSoil(type, SoilBlockType.Variant.SILT)
        );
        return new TFCFSoilSurfaceState(regions);
    }

    public static SurfaceState buildTypeRockSand(TFCFRockSand type)
    {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
            sand(),
            transition(sand(), rockSand(type)),
            rockSand(type),
            transition(rockSand(type), rockSand(type)),
            rockSand(type),
            transition(rockSand(type), rockSand(type)),
            rockSand(type),
            transition(rockSand(type), rockSand(type)),
            rockSand(type)
        );
        return new TFCFSoilSurfaceState(regions);
    }

    public static SurfaceState buildSandOrGravel(boolean sandIsSandstone)
    {
        final SurfaceState sand = sandIsSandstone ? sandstone() : sand();
        final SurfaceState gravel = gravel();
        return new TFCFSoilSurfaceState(ImmutableList.of(
            sand,
            transition(sand, gravel),
            gravel,
            gravel,
            gravel,
            gravel,
            gravel,
            gravel,
            gravel
        ));
    }

    public static SurfaceState transition(SurfaceState first, SurfaceState second)
    {
        return context -> {
            final BlockPos pos = context.pos();
            double noise = SoilSurfaceState.PATCH_NOISE.noise(pos.getX(), pos.getZ());
            return noise > 0 ? first.getState(context) : second.getState(context);
        };
    }

    /*public static SurfaceState rockTFC(Rock rock, Rock.BlockType type)
    {
        final Supplier<Block> block = TFCBlocks.ROCK_BLOCKS.get(rock).get(type);
        return context -> block.get().defaultBlockState();
    }*/

    public static SurfaceState rock(Rock.BlockType type)
    {
        return context -> {
            if (isTFCFRock(context))
            {
                return TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockType(context)).get(type).get().defaultBlockState();
            }
            else
            {
                return TFCBlocks.ROCK_BLOCKS.get(rockType(context)).get(type).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState rockCustom(RegistryRock rock, Rock.BlockType type)
    {
        return context -> {
            if (isCurrentRock(context, rock))
            {
                return TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockType(context)).get(type).get().defaultBlockState();
            }
            else
            {
                return TFCBlocks.ROCK_BLOCKS.get(rockType(context)).get(type).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState sand()
    {
        return context -> context.getRock().sand().defaultBlockState();
    }

    public static SurfaceState sandstone()
    {
        return context -> context.getRock().sandstone().defaultBlockState();
    }

    public static SurfaceState gravel()
    {
        return context -> context.getRock().gravel().defaultBlockState();
    }

    public static SurfaceState soil(TFCFSoil type, SoilBlockType.Variant variant)
    {
        final Supplier<Block> block = TFCFBlocks.TFCSOIL.get(type).get(variant);
        return context -> block.get().defaultBlockState();
    }

    public static SurfaceState TFCFsoil(TFCFSoil type, TFCFSoil.TFCFVariant variant)
    {
        final Supplier<Block> block = TFCFBlocks.TFCFSOIL.get(type).get(variant);
        return context -> block.get().defaultBlockState();
    }

    /*public static SurfaceState rockSoilTFC(TFCFRockSoil type, SoilBlockType.Variant variant, Rock rock)
    {
        final Supplier<Block> block = TFCFBlocks.TFCROCKSOIL.get(type).get(variant).get(rock);
        return context -> block.get().defaultBlockState();
    }*/

    public static SurfaceState rockSoil(TFCFRockSoil type, SoilBlockType.Variant variant)
    {
        return context -> {
            if (isTFCFRock(context))
            {
                return TFCFBlocks.TFCROCKSOIL2.get(type).get(variant).get(rockType(context)).get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.TFCROCKSOIL.get(type).get(variant).get(rockType(context)).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState rockSandGrass()
    {
        return context -> TFCFBlocks.SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
    }

    public static SurfaceState rockSandSparseGrass()
    {
        return context -> TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
    }

    public static SurfaceState rockSandDenseGrass()
    {
        return context -> TFCFBlocks.DENSE_SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
    }

    /*public static SurfaceState rockSandTFC(TFCFRockSand type, Rock rock)
    {
        return context -> TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rock).get().defaultBlockState();
    }*/

    public static SurfaceState rockSand(TFCFRockSand type)
    {
        return context -> {
            if (isTFCFRock(context))
            {
                return TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(sandColor(context)).get(rockType(context)).get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rockType(context)).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState rockRareSandGrass()
    {
        final Supplier<Block> pinkSand = TFCFBlocks.SAND_GRASS.get(Colors.PINK);
        final Supplier<Block> blackSand =  TFCFBlocks.SAND_GRASS.get(Colors.BLACK);

        return context -> {
            if (context.rainfall() > 300f && context.averageTemperature() > 15f)
            {
                return pinkSand.get().defaultBlockState();
            }
            else if (context.rainfall() > 300f)
            {
                return blackSand.get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState rockRareSandSparseGrass()
    {
        final Supplier<Block> pinkSand = TFCFBlocks.SPARSE_SAND_GRASS.get(Colors.PINK);
        final Supplier<Block> blackSand =  TFCFBlocks.SPARSE_SAND_GRASS.get(Colors.BLACK);

        return context -> {
            if (context.rainfall() > 300f && context.averageTemperature() > 15f)
            {
                return pinkSand.get().defaultBlockState();
            }
            else if (context.rainfall() > 300f)
            {
                return blackSand.get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
            }
        };
    }

    public static SurfaceState rockRareSandDenseGrass()
    {
        final Supplier<Block> pinkSand = TFCFBlocks.DENSE_SAND_GRASS.get(Colors.PINK);
        final Supplier<Block> blackSand =  TFCFBlocks.DENSE_SAND_GRASS.get(Colors.BLACK);

        return context -> {
            if (context.rainfall() > 300f && context.averageTemperature() > 15f)
            {
                return pinkSand.get().defaultBlockState();
            }
            else if (context.rainfall() > 300f)
            {
                return blackSand.get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.DENSE_SAND_GRASS.get(sandColor(context)).get().defaultBlockState();
            }
        };
    }

    /*public static SurfaceState rockRareSandTFC(TFCFRockSand type)
    {
        return context -> {
            final Supplier<Block> pinkSand = TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.PINK).get(rockType(context));
            final Supplier<Block> blackSand =  TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.BLACK).get(rockType(context));

            if (context.rainfall() > 300f && context.averageTemperature() > 15f)
            {
                return pinkSand.get().defaultBlockState();
            }
            else if (context.rainfall() > 300f)
            {
                return blackSand.get().defaultBlockState();
            }
            else
            {
                return TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rockType(context)).get().defaultBlockState();
            }
        };
    }*/

    public static SurfaceState rockRareSand(TFCFRockSand type)
    {
        return context -> {
            if (isTFCFRock(context))
            {
                final Supplier<Block> pinkSand = TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(Colors.PINK).get(rockType(context));
                final Supplier<Block> blackSand =  TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(Colors.BLACK).get(rockType(context));

                if (context.rainfall() > 300f && context.averageTemperature() > 15f)
                {
                    return pinkSand.get().defaultBlockState();
                }
                else if (context.rainfall() > 300f)
                {
                    return blackSand.get().defaultBlockState();
                }
                else
                {
                    return TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(sandColor(context)).get(rockType(context)).get().defaultBlockState();
                }
            }
            else
            {
                final Supplier<Block> pinkSand = TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.PINK).get(rockType(context));
                final Supplier<Block> blackSand =  TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.BLACK).get(rockType(context));

                if (context.rainfall() > 300f && context.averageTemperature() > 15f)
                {
                    return pinkSand.get().defaultBlockState();
                }
                else if (context.rainfall() > 300f)
                {
                    return blackSand.get().defaultBlockState();
                }
                else
                {
                    return TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rockType(context)).get().defaultBlockState();
                }
            }
        };
    }

    public static Colors sandColor(SurfaceBuilderContext context)
    {
        if (context.getBottomRock().sand() != null)
        {
            for (Colors sandColors : Colors.values())
            {
                if (context.getBottomRock().sand() == TFCBlocks.SAND.get(sandColors.toSandTFC(true)).get())
                {
                    return sandColors;
                }
                else if (context.getBottomRock().sand() == TFCFBlocks.SAND.get(sandColors.nonTFC()).get())
                {
                    return sandColors;
                }
            }
        }
        else if (Colors.fromMaterialColour(context.getBottomRock().sand().defaultBlockState().getBlock().defaultMapColor()) != null)
        {
            return Colors.fromMaterialColour(context.getBottomRock().sand().defaultBlockState().getBlock().defaultMapColor());
        }
        return Colors.YELLOW;
    }

    public static RegistryRock rockType(SurfaceBuilderContext context)
    {
        if (surfaceRock(context) != null)
        {
            for (Rock rockTFC : Rock.values())
            {
                if (surfaceRock(context).raw() == TFCBlocks.ROCK_BLOCKS.get(rockTFC).get(Rock.BlockType.RAW).get())
                {
                    return rockTFC;
                }
                else
                {
                    for (TFCFRock rockTFCF : TFCFRock.values())
                    {
                        if (surfaceRock(context).raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(Rock.BlockType.RAW).get())
                        {
                            return rockTFCF;
                        }
                    }
                }
            }
        }
        return Rock.GRANITE;
    }

    public static boolean isTFCFRock(SurfaceBuilderContext context)
    {
        if (surfaceRock(context) != null)
        {
            for (TFCFRock r2 : TFCFRock.values())
            {
                if (surfaceRock(context).raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(r2).get(Rock.BlockType.RAW).get())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCurrentRock(SurfaceBuilderContext context, RegistryRock rock)
    {
        return rockType(context) == rock;
    }

    public static RegistrySoilVariant currentSoilVariant(SurfaceBuilderContext context)
    {
        for (SoilBlockType type : SoilBlockType.values())
        {
            for (SoilBlockType.Variant variantTFC : SoilBlockType.Variant.values())
            {
                if (SoilSurfaceState.buildType(type).getState(context).getBlock() == TFCBlocks.SOIL.get(type).get(variantTFC).get())
                {
                    return variantTFC;
                }
            }
        }
        return SoilBlockType.Variant.LOAM;
    }

    public static RockSettings surfaceRock(SurfaceBuilderContext context)
    {
        return context.getRock();
    }

    public final List<SurfaceState> regions;

    public TFCFSoilSurfaceState(List<SurfaceState> regions)
    {
        this.regions = regions;
    }

    @Override
    public BlockState getState(SurfaceBuilderContext context)
    {
        // Adjust rainfall to bias a little bit towards sand regions
        // Without: pure sand < 55mm, mixed sand < 105mm. With: pure sand < 75mm, mixed sand < 136mm
        final float rainfall = context.rainfall() + 20f;
        final int index = Mth.clamp((int) Mth.clampedMap(rainfall, 0, 500, 0, regions.size() - 0.01f), 0, regions.size() - 1);

        return regions.get(index).getState(context);
    }

    static class NeedsPostProcessing extends TFCFSoilSurfaceState
    {
        private NeedsPostProcessing(List<SurfaceState> regions)
        {
            super(regions);
        }

        @Override
        public void setState(SurfaceBuilderContext context)
        {
            context.chunk().setBlockState(context.pos(), getState(context), false);
            context.chunk().markPosForPostprocessing(context.pos());
        }
    }

    public static RegistrySoilVariant getSoilVariant(WorldGenLevel level, BlockPos pos)
    {
        final RandomSource random = level.getRandom();
        final ChunkDataProvider provider = ChunkDataProvider.get(level);
        final ChunkData data = provider.get(level, pos);

        // Adjust rainfall to bias a little bit towards sand regions
        // Without: pure sand < 55mm, mixed sand < 105mm. With: pure sand < 75mm, mixed sand < 136mm
        final float rainfall = data.getRainfall(pos) + 20f;
        final int index = Mth.clamp((int) Mth.clampedMap(rainfall, 0, 500, 0, 9 - 0.01f), 0, 9 - 1);

        switch (index)
        {
            case 1:
                return transitionSoil(SoilBlockType.Variant.SANDY_LOAM, SoilBlockType.Variant.SANDY_LOAM, pos, random);
            case 2:
                return SoilBlockType.Variant.SANDY_LOAM;
            case 3:
                return transitionSoil(SoilBlockType.Variant.SANDY_LOAM, SoilBlockType.Variant.LOAM, pos, random);
            case 4:
                return SoilBlockType.Variant.LOAM;
            case 5:
                return transitionSoil(SoilBlockType.Variant.LOAM, SoilBlockType.Variant.SILTY_LOAM, pos, random);
            case 6:
                return SoilBlockType.Variant.SILTY_LOAM;
            case 7:
                return transitionSoil(SoilBlockType.Variant.SILTY_LOAM, SoilBlockType.Variant.SILT, pos, random);
            case 8:
                return SoilBlockType.Variant.SILT;
            default:
                return SoilBlockType.Variant.SANDY_LOAM;
        }
    }

    public static RegistrySoilVariant transitionSoil(RegistrySoilVariant first, RegistrySoilVariant second, BlockPos pos, RandomSource random)
    {
        double noise = SoilSurfaceState.PATCH_NOISE.noise(pos.getX(), pos.getZ());
        double noiseGauss = noise + (2 * (float) random.nextGaussian());
        return noiseGauss > 0 ? first : second;
    }
}