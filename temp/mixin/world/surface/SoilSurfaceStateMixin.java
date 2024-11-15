package tfcflorae.mixin.world.surface;

import org.spongepowered.asm.mixin.*;

import java.util.List;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;

import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.util.TFCFHelpers;

@Mixin(SoilSurfaceState.class)
public class SoilSurfaceStateMixin implements SurfaceState
{
    @Shadow public static final Noise2D PATCH_NOISE = new OpenSimplex2D(18273952837592L).octaves(2).spread(0.04f);

    @Overwrite(remap = false)
    public static SurfaceState buildType(SoilBlockType type)
    {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
            sand(),
            transitionSoil(sand(), soil(type, SoilBlockType.Variant.SANDY_LOAM), SoilBlockType.Variant.SANDY_LOAM),
            soil(type, SoilBlockType.Variant.SANDY_LOAM),
            transitionSoil(soil(type, SoilBlockType.Variant.SANDY_LOAM), soil(type, SoilBlockType.Variant.LOAM), SoilBlockType.Variant.LOAM),
            soil(type, SoilBlockType.Variant.LOAM),
            transitionSoil(soil(type, SoilBlockType.Variant.LOAM), soil(type, SoilBlockType.Variant.SILTY_LOAM), SoilBlockType.Variant.SILTY_LOAM),
            soil(type, SoilBlockType.Variant.SILTY_LOAM),
            transitionSoil(soil(type, SoilBlockType.Variant.SILTY_LOAM), soil(type, SoilBlockType.Variant.SILT), SoilBlockType.Variant.SILT),
            soil(type, SoilBlockType.Variant.SILT)
        );
        return type == SoilBlockType.GRASS ? new SoilSurfaceStateMixin.NeedsPostProcessing(regions) : new SoilSurfaceStateMixin(regions);
    }

    @Unique
    private static SurfaceState transitionSoil(SurfaceState first, SurfaceState second, SoilBlockType.Variant variant)
    {
        return context -> {
            final BlockPos pos = context.pos();
            final RandomSource random = context.random();
            float rainfall = context.rainfall();
            float noise = PATCH_NOISE.noise(pos.getX(), pos.getZ());
            float noiseGauss = noise + (2 * (float) random.nextGaussian());
            float noiseRainfall = rainfall + (10 * (float) random.nextGaussian());

            if (first != buildType(SoilBlockType.DIRT))
            {
                if (noiseRainfall <= 80f)
                {
                    if (noiseRainfall <= 50f)
                    {
                        return context.getRock().sand().defaultBlockState();
                    }
                    else
                    {
                        if (noiseGauss > 0.2F)
                        {
                            return context.getRock().sand().defaultBlockState();
                        }
                        else
                        {
                            SandBlockType sandColor = context.level() instanceof WorldGenLevel level ? TFCFHelpers.getSandColor(level, pos, Config.COMMON.toggleCheapSandColourCalculations.get()) : SandBlockType.YELLOW;
                            float randomRainfall = random.nextInt(Math.round(rainfall)) * 0.01F;
                            if (randomRainfall <= Mth.abs(noiseGauss))
                            {
                                return TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor).get().defaultBlockState();
                            }
                            else
                            {
                                return TFCFBlocks.DENSE_SAND_GRASS.get(sandColor).get().defaultBlockState();
                            }
                        }
                    }
                }
                else if (noiseRainfall <= 110f)
                {
                    if (noiseGauss > 0)
                    {
                        return TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).get(variant).get().defaultBlockState();
                    }
                    else
                    {
                        return TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).get(variant).get().defaultBlockState();
                    }
                }
            }
            return noiseGauss > 0 ? first.getState(context) : second.getState(context);
        };
    }

    @Shadow
    private static SurfaceState sand()
    {
        return context -> context.getRock().sand().defaultBlockState();
    }

    @Shadow
    private static SurfaceState sandstone()
    {
        return context -> context.getRock().sandstone().defaultBlockState();
    }

    @Shadow
    private static SurfaceState gravel()
    {
        return context -> context.getRock().gravel().defaultBlockState();
    }

    @Shadow
    private static SurfaceState soil(SoilBlockType type, SoilBlockType.Variant variant)
    {
        final Supplier<Block> block = TFCBlocks.SOIL.get(type).get(variant);
        return context -> block.get().defaultBlockState();
    }

    @Shadow
    private final List<SurfaceState> regions;

    private SoilSurfaceStateMixin(List<SurfaceState> regions)
    {
        this.regions = regions;
    }

    @Shadow
    @Override
    public BlockState getState(SurfaceBuilderContext context)
    {
        // Adjust rainfall to bias a little bit towards sand regions
        // Without: pure sand < 55mm, mixed sand < 105mm. With: pure sand < 75mm, mixed sand < 136mm
        final float rainfall = context.rainfall() + 20f;
        final int index = Mth.clamp((int) Mth.clampedMap(rainfall, 0, 500, 0, regions.size() - 0.01f), 0, regions.size() - 1);

        return regions.get(index).getState(context);
    }

    static class NeedsPostProcessing extends SoilSurfaceStateMixin
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
}
