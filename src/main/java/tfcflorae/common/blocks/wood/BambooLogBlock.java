package tfcflorae.common.blocks.wood;


import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;

import org.jetbrains.annotations.Nullable;

import static tfcflorae.common.blocks.wood.TFCPalmTrunkBlock.NATURAL;

public class BambooLogBlock extends LogBlock implements IFluidLoggable
{
    public static final VoxelShape SHAPE = box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    public static final VoxelShape AXIS_X_SHAPE = box(0.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    public static final VoxelShape AXIS_Y_SHAPE = box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    public static final VoxelShape AXIS_Z_SHAPE = box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 16.0D);

    public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;

    @Nullable private final Supplier<? extends Block> stripped;

    public BambooLogBlock(ExtendedProperties properties, @Nullable Supplier<? extends Block> stripped)
    {
        super(properties, stripped);
        this.stripped = stripped;
        this.registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(getFluidProperty()));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        switch (state.getValue(AXIS))
        {
            case X:
                return AXIS_X_SHAPE;
            case Y:
                return AXIS_Y_SHAPE;
            case Z:
                return AXIS_Z_SHAPE;
            default:
                return SHAPE;
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        super.getStateForPlacement(context);

        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState state = defaultBlockState();
        if (getFluidProperty().canContain(fluidState.getType()))
        {
            state = state.setValue(getFluidProperty(), getFluidProperty().keyFor(fluidState.getType()));
        }
        return state.setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90)
        {
            if (state.getValue(AXIS) == Direction.Axis.X)
                return state.setValue(AXIS, Direction.Axis.Z); 
            if (state.getValue(AXIS) == Direction.Axis.Z)
                return state.setValue(AXIS, Direction.Axis.X); 
        }
        return state;
    }

    @Override
    public FluidProperty getFluidProperty()
    {
        return FLUID;
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return IFluidLoggable.super.getFluidLoggedState(state);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos)
    {
        final float baseSpeed = (state.getValue(NATURAL) ? 2 : 1) * state.getDestroySpeed(level, pos);
        if (baseSpeed == -1.0F)
        {
            return 0.0F;
        }
        else
        {
            final int toolModifier = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
            return player.getDigSpeed(state, pos) / baseSpeed / (float) toolModifier;
        }
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate)
    {
        if (context.getItemInHand().canPerformAction(action) && action == ToolActions.AXE_STRIP && stripped != null)
        {
            return Helpers.copyProperties(stripped.get().defaultBlockState(), state);
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        final BlockPos posBelow = pos.below();
        final BlockState stateBelow = level.getBlockState(posBelow);
        return !FluidHelpers.isAirOrEmptyFluid(stateBelow) && (stateBelow.isSolid() || stateBelow.isFaceSturdy(level, posBelow, Direction.UP));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        FluidHelpers.tickFluid(level, currentPos, state);
        if (!state.canSurvive(level, currentPos))
        {
            level.scheduleTick(currentPos, this, 1);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (!state.canSurvive(level, pos))
        {
            level.destroyBlock(pos, true);
        }
    }
}
