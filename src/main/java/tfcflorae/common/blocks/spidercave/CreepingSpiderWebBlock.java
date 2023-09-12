package tfcflorae.common.blocks.spidercave;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

import tfcflorae.util.TFCFHelpers;

import org.jetbrains.annotations.NotNull;

public class CreepingSpiderWebBlock extends TFCBushBlock implements DirectionPropertyBlock
{
    protected static final VoxelShape UP_SHAPE = box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape DOWN_SHAPE = box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    protected static final VoxelShape EAST_SHAPE = box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);

    protected static final Map<BooleanProperty, VoxelShape> SHAPES = ImmutableMap.<BooleanProperty, VoxelShape>builder()
        .put(UP, UP_SHAPE)
        .put(DOWN, DOWN_SHAPE)
        .put(NORTH, NORTH_SHAPE)
        .put(SOUTH, SOUTH_SHAPE)
        .put(EAST, EAST_SHAPE)
        .put(WEST, WEST_SHAPE)
        .build();

    protected final Map<BlockState, VoxelShape> shapeCache;
    public boolean canWalkThroughEffortlessly;

    public CreepingSpiderWebBlock(ExtendedProperties properties)
    {
        super(properties);

        registerDefaultState(DirectionPropertyBlock.setAllDirections(getStateDefinition().any(), false));
        shapeCache = DirectionPropertyBlock.makeShapeCache(getStateDefinition(), SHAPES::get);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), Helpers.isBlock(facingState, TFCTags.Blocks.CREEPING_PLANTABLE_ON));
        return isEmpty(state) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_SHAPE_ORDER)
        {
            if (Helpers.isBlock(level.getBlockState(mutablePos.setWithOffset(pos, direction)), TFCTags.Blocks.CREEPING_PLANTABLE_ON))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        if (!canSurvive(state, level, pos))
        {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapeCache.get(state);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return updateStateFromSides(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST));
    }

    private BlockState updateStateFromSides(LevelAccessor level, BlockPos pos, BlockState state)
    {
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean hasEarth = false;
        for (Direction direction : UPDATE_SHAPE_ORDER)
        {
            mutablePos.setWithOffset(pos, direction);
            boolean ground = Helpers.isBlock(level.getBlockState(mutablePos), TFCTags.Blocks.CREEPING_PLANTABLE_ON);

            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), ground);
            hasEarth |= ground;
        }
        return hasEarth ? state : Blocks.AIR.defaultBlockState();
    }

    private boolean isEmpty(BlockState state)
    {
        for (BooleanProperty property : SHAPES.keySet())
        {
            if (state.getValue(property))
            {
                return false;
            }
        }
        return true;
    }

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        if (entity != null)
        {
            if (TFCFHelpers.canWalkThroughEffortlessly(entity))
            {
                canWalkThroughEffortlessly = true;
            }
            else
            {
                canWalkThroughEffortlessly = false;
            }
        }
	}

    @Override
    public float getSpeedFactor()
    {
        final float modifier = TFCConfig.SERVER.plantsMovementModifier.get().floatValue(); // 0.0 = full speed factor, 1.0 = no modifier
        return canWalkThroughEffortlessly ? 1.0F : Helpers.lerp(modifier, speedFactor, 1.0f);
    }
}
