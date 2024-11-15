package tfcflorae.world.feature;



import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BeeNestFeature extends Feature<NoneFeatureConfiguration>
{
    public BeeNestFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource random = context.random();

        Direction facingDirection = randomDirection(random);

        setBlock(level, pos, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, facingDirection));
        level.getBlockEntity(pos, BlockEntityType.BEEHIVE).ifPresent((p_202310_) -> {
            int j = 2 + random.nextInt(2);
            for(int k = 0; k < j; ++k)
            {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
                p_202310_.storeBee(compoundTag, random.nextInt(599), false);
            }
        });
        return true;
    }

    public Direction randomDirection(RandomSource random)
    {
        int randomDirection = random.nextInt(4);
        return switch (randomDirection) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.EAST;
            default -> Direction.WEST;
        };
    }
}
