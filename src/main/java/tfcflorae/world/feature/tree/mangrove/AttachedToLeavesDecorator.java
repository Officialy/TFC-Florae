package tfcflorae.world.feature.tree.mangrove;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.HashSet;
import java.util.List;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import tfcflorae.world.feature.TFCFFeatures;

public class AttachedToLeavesDecorator extends TreeDecorator
{
    public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(decorator -> {
            return decorator.probability;
        }), Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter(decorator -> {
            return decorator.exclusionRadiusXZ;
        }), Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter(decorator -> {
            return decorator.exclusionRadiusY;
        }), BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(decorator -> {
            return decorator.blockProvider;
        }), Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter(decorator -> {
            return decorator.requiredEmptyBlocks;
        }), Direction.CODEC.listOf().fieldOf("directions").forGetter(decorator -> {
            return decorator.directions;
        })).apply(instance, AttachedToLeavesDecorator::new);
    });

    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesDecorator(float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List<Direction> directions)
    {
        this.probability = probability;
        this.exclusionRadiusXZ = exclusionRadiusXZ;
        this.exclusionRadiusY = exclusionRadiusY;
        this.blockProvider = blockProvider;
        this.requiredEmptyBlocks = requiredEmptyBlocks;
        this.directions = directions;
    }

    @Override
    public void place(Context pContext) {

        LevelSimulatedReader level = pContext.level();
        RandomSource random = pContext.random();
        List<BlockPos> trunkPositions = pContext.roots();
        List<BlockPos> foliagePositions = pContext.leaves();

        HashSet<BlockPos> positions = new HashSet<>();
        for (BlockPos pos : copyShuffled(new ObjectArrayList<>(foliagePositions), random))
        {
            Direction direction = Util.getRandom(this.directions, random);
            BlockPos offset = pos.relative(direction);
            if (!positions.contains(offset) && random.nextFloat() < this.probability && this.meetsRequiredEmptyBlocks(level, pos, direction))
            {
                BlockPos minRadius = offset.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
                BlockPos maxRadius = offset.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
                for (BlockPos position : BlockPos.betweenClosed(minRadius, maxRadius))
                {
                    positions.add(position.immutable());
                }
                pContext.setBlock(offset, this.blockProvider.getState(random, offset));
            }
        }
    }

    private boolean meetsRequiredEmptyBlocks(LevelSimulatedReader level, BlockPos pos, Direction direction)
    {
        for (int i = 1; i <= this.requiredEmptyBlocks; i++)
        {
//            if (!Feature.isAir(level, pos.relative(direction, i))) return false;
        }

        return true;
    }

    @Override
    protected TreeDecoratorType<?> type()
    {
        return TFCFFeatures.ATTACHED_TO_LEAVES.get();
    }

    public static <T> void shuffle(List<T> entries, RandomSource random)
    {
        int size = entries.size();
        for (int i = size; i > 1; --i) {
            entries.set(i - 1, entries.set(random.nextInt(i), entries.get(i - 1)));
        }
    }

    public static <T> List<T> copyShuffled(T[] entries, RandomSource random)
    {
        ObjectArrayList<T> objects = new ObjectArrayList<>(entries);
        shuffle(objects, random);
        return objects;
    }

    public static <T> List<T> copyShuffled(ObjectArrayList<T> entries, RandomSource random)
    {
        ObjectArrayList<T> objects = new ObjectArrayList<>(entries);
        shuffle(objects, random);
        return objects;
    }
}