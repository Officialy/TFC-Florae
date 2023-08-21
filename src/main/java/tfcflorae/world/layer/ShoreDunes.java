package tfcflorae.world.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.dries007.tfc.world.FastConcurrentCache;
import net.dries007.tfc.world.layer.Plate;
import net.dries007.tfc.world.layer.framework.TypedArea;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.river.*;

import org.jetbrains.annotations.VisibleForTesting;

public abstract class ShoreDunes
{
    public static final float SHORE_DUNES_WIDTH = 0.4f;

    private static final int[] DIRECTIONS = new int[] {-1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1};

    public static ShoreDunes create(TypedArea<Plate> area, int sampleX, int sampleZ, long seed, float sourceChance, float length, int depth, float feather)
    {
        final Plate root = area.get(sampleX, sampleZ);

        if (root.oceanic())
        {
            return new Empty(root);
        }

        // Flood fill the plate region, to find the extent of the watershed
        final LongSet interior = new LongOpenHashSet();
        final LongSet sources = new LongOpenHashSet();
        final LongList queue = new LongArrayList();

        long first = RiverHelpers.pack(sampleX, sampleZ);
        queue.add(first);
        interior.add(first);
        while (!queue.isEmpty())
        {
            long key = queue.removeLong(queue.size() - 1); // Functions as a stack but it's a flood fill, so we don't care
            int x0 = RiverHelpers.unpackX(key), z0 = RiverHelpers.unpackZ(key);
            for (int i = 0; i < DIRECTIONS.length; i += 2)
            {
                int x1 = x0 + DIRECTIONS[i], z1 = z0 + DIRECTIONS[i | 1];
                long next = RiverHelpers.pack(x1, z1);
                Plate plate = area.get(x1, z1);
                if (plate.equals(root))
                {
                    if (interior.add(next))
                    {
                        queue.add(next);
                    }
                }
                else if (plate.oceanic())
                {
                    sources.add(next);
                }
            }
        }

        if (sources.isEmpty())
        {
            return new Empty(root); // No sources, so no riverBanks.
        }

        // Seed and generate a new riverBanks instance
        RandomSource random = new XoroshiroRandomSource((seed + 1) ^ root.hashCode());
        return new Lines(root, interior, sources, random, sourceChance, length, depth, feather);
    }

    private final Plate plate;

    protected ShoreDunes(Plate plate)
    {
        this.plate = plate;
    }

    public abstract List<RiverFractal> getRivers();

    public Plate getPlate()
    {
        return plate;
    }

    @Override
    public int hashCode()
    {
        return plate.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoreDunes watershed = (ShoreDunes) o;
        return plate.equals(watershed.plate);
    }

    static class Empty extends ShoreDunes
    {
        Empty(Plate plate)
        {
            super(plate);
        }

        @Override
        public List<RiverFractal> getRivers()
        {
            return Collections.emptyList();
        }
    }

    public static class Lines extends ShoreDunes
    {
        public final LongSet interior, sources;
        private final List<RiverFractal> riverBanks;

        public Lines(Plate plate, LongSet interior, LongSet sources, RandomSource random, float sourceChance, float length, int depth, float feather)
        {
            super(plate);

            this.interior = interior;
            this.sources = sources;

            // We need to have a consistent iteration order across sources, in order to deterministically generate a watershed from any given sample position
            // The easiest way to guarantee this, is to sort the sources.
            final RiverFractal.MultiParallelBuilder context = new Builder();
            sources.longStream().sorted().forEach(key -> {
                final float x0 = RiverHelpers.unpackX(key) + 0.5f, z0 = RiverHelpers.unpackZ(key) + 0.5f;
                if (random.nextFloat() < sourceChance)
                {
                    // Iterate in a circle until we find an angle that works
                    float angle = random.nextFloat() * 2 * Mth.PI;
                    for (int i = 0; i < 8; i++)
                    {
                        // Project in this direction, and see if we still target interior
                        float dx = Mth.cos(angle) * 1.4f, dz = Mth.sin(angle) * 1.4f;
                        long adj = RiverHelpers.pack(x0 + dx, z0 + dz);
                        if (this.interior.contains(adj))
                        {
                            context.add(new RiverFractal.Builder(random, x0, z0, angle, length, depth, feather));
                            break;
                        }
                        angle += 0.25f * Mth.PI;
                    }
                }
            });

            this.riverBanks = context.buildFractals();
        }

        @Override
        public List<RiverFractal> getRivers()
        {
            return riverBanks;
        }

        @VisibleForTesting
        public LongSet getSources()
        {
            return sources;
        }

        class Builder extends RiverFractal.MultiParallelBuilder
        {
            @Override
            protected boolean isLegal(RiverFractal.Vertex prev, RiverFractal.Vertex vertex)
            {
                final int x = RiverHelpers.floor(vertex.x()), z = RiverHelpers.floor(vertex.y());
                final long key = RiverHelpers.pack(x, z);
                return interior.contains(key);
            }
        }
    }

    public static class Context
    {
        /**
         * Parameters that are tweaked for best performance.
         */
        private static final int PARTITION_BITS = 5;
        private static final int ZOOM_BITS = 7;

        private static final int WATERSHED_CACHE_BITS = 8;
        private static final int PARTITION_CACHE_BITS = 10;

        /**
         * The diagonal from the center of a unit cell, to the corner.
         * Scale is partition coordinates.
         */
        private static final float PARTITION_RADIUS = (float) Math.sqrt(2) / 2f;
        private static final int PARTITION_TO_ZOOM_BITS = ZOOM_BITS - PARTITION_BITS;

        private final ThreadLocal<TypedArea<Plate>> plates;
        private final FastConcurrentCache<ShoreDunes> watershedCache;
        private final FastConcurrentCache<List<MidpointFractal>> partitionCache;

        private final long seed;
        private final float sourceChance;
        private final float length;
        private final int depth;
        private final float feather;

        public Context(TypedAreaFactory<Plate> plates, long seed, float sourceChance, float length, int depth, float feather)
        {
            this.plates = ThreadLocal.withInitial(plates);
            this.watershedCache = new FastConcurrentCache<>(1 << WATERSHED_CACHE_BITS);
            this.partitionCache = new FastConcurrentCache<>(1 << PARTITION_CACHE_BITS);
            this.seed = seed + 1;
            this.sourceChance = sourceChance;
            this.length = length;
            this.depth = depth;
            this.feather = feather;
        }

        /**
         * Input coordinates are biome quart positions.
         * Partition coordinates are quart positions shifted by {@link #PARTITION_BITS}.
         * Chasm coordinates are quart positions shifted by {@link #ZOOM_BITS}. (Based on the total amount of zoom layers used between plate layers and the final biome area.)
         * In order to compute the partition, we query the four adjacent watersheds, which may overlap the partition area.
         */
        public List<MidpointFractal> getFractalsByPartition(int x, int z)
        {
            final int px = x >> PARTITION_BITS, pz = z >> PARTITION_BITS;
            List<MidpointFractal> partition = partitionCache.getIfPresent(px, pz);
            if (partition == null)
            {
                final Random random = new Random(seed);

                // Locate the four closest adjacent watersheds.
                final Set<ShoreDunes> nearbySheds = new ObjectOpenHashSet<>(2);
                final float watershedScale = 1f / (1 << ZOOM_BITS);
                final float x0 = x * watershedScale, z0 = z * watershedScale;

                nearbySheds.add(create(x0 - 0.5f, z0 - 0.5f));
                nearbySheds.add(create(x0 + 0.5f, z0 - 0.5f));
                nearbySheds.add(create(x0 + 0.5f, z0 + 0.5f));
                nearbySheds.add(create(x0 - 0.5f, z0 + 0.5f));

                // Then, we iterate all sheds, all riverBanks, and all fractals, and partition out only those fractals which come within a minimum distance of the partition area.
                // We define that minimum distance as a circular radius from the center of the partition region, with a radius s.t. the entire region is encompassed (effectively, an inscribed square in the circle).
                final float partitionCenterX = px + 0.5f, partitionCenterZ = pz + 0.5f;
                final float partitionToWatershedScale = 1f / (1 << PARTITION_TO_ZOOM_BITS);

                final float x1 = partitionToWatershedScale * partitionCenterX, z1 = partitionToWatershedScale * partitionCenterZ;
                final float radius = partitionToWatershedScale * (PARTITION_RADIUS + 2 * SHORE_DUNES_WIDTH * Mth.clamp((float) Math.abs(random.nextGaussian() * 3f), 0, 3f));

                partition = new ArrayList<>(32);
                for (ShoreDunes shed : nearbySheds)
                {
                    for (RiverFractal river : shed.getRivers())
                    {
                        for (MidpointFractal fractal : river.getFractals())
                        {
                            if (fractal.maybeIntersect(x1, z1, radius))
                            {
                                partition.add(fractal);
                            }
                        }
                    }
                }

                // Enter the resulting partition in the cache
                partitionCache.set(px, pz, partition);
            }
            return partition;
        }

        public ShoreDunes create(float x, float z)
        {
            return create(RiverHelpers.floor(x), RiverHelpers.floor(z));
        }

        public ShoreDunes create(int x, int z)
        {
            ShoreDunes shed = watershedCache.getIfPresent(x, z);
            if (shed == null)
            {
                shed = ShoreDunes.create(plates.get(), x, z, seed, sourceChance, length, depth, feather);
                watershedCache.set(x, z, shed);
            }
            return shed;
        }
    }
}
