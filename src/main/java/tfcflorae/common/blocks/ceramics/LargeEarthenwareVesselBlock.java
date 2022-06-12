package tfcflorae.common.blocks.ceramics;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.capabilities.size.IItemSize;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.dries007.tfc.util.Helpers;
import org.jetbrains.annotations.Nullable;

import tfcflorae.common.blockentities.TFCFBlockEntities;

public class LargeEarthenwareVesselBlock extends DeviceBlock implements IItemSize
{
    public static final BooleanProperty SEALED = TFCBlockStateProperties.SEALED;

    public static final VoxelShape OPENED_SHAPE = box(3D, 0D, 3D, 13D, 10D, 13D);
    public static final VoxelShape CLOSED_SHAPE = Shapes.or(
        OPENED_SHAPE,
        box(2.5D, 9.5D, 2.5D, 13.5D, 11D, 13.5D),
        box(7D, 11D, 7D, 9D, 12D, 9D)
    );

    public static void toggleSeal(Level level, BlockPos pos, BlockState state)
    {
        level.getBlockEntity(pos, TFCFBlockEntities.LARGE_EARTHENWARE_VESSEL.get()).ifPresent(barrel -> {
            final boolean previousSealed = state.getValue(SEALED);
            level.setBlockAndUpdate(pos, state.setValue(SEALED, !previousSealed));
            if (previousSealed)
            {
                barrel.onUnseal();
                Helpers.playSound(level, pos, TFCSounds.OPEN_VESSEL.get());
            }
            else
            {
                barrel.onSeal();
                Helpers.playSound(level, pos, TFCSounds.CLOSE_VESSEL.get());
            }
        });
    }

    public LargeEarthenwareVesselBlock(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.SAVE);

        registerDefaultState(getStateDefinition().any().setValue(SEALED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return state.getValue(SEALED) ? CLOSED_SHAPE : OPENED_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(SEALED));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        return canSurvive(state, level, currentPos) ? super.updateShape(state, facing, facingState, level, currentPos, facingPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return !belowState.isAir() && belowState.isFaceSturdy(level, belowPos, Direction.UP) && super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return context.getItemInHand().getTag() != null ? defaultBlockState().setValue(SEALED, true) : defaultBlockState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (player.isShiftKeyDown())
        {
            toggleSeal(level, pos, state);
        }
        else
        {
            level.getBlockEntity(pos, TFCFBlockEntities.LARGE_EARTHENWARE_VESSEL.get()).ifPresent(vessel -> {
                if (player instanceof ServerPlayer serverPlayer)
                {
                    NetworkHooks.openGui(serverPlayer, vessel, pos);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player)
    {
        final ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        if (state.getValue(SEALED))
        {
            final BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof InventoryBlockEntity<?> inv)
            {
                inv.saveToItem(stack);
            }
        }
        return stack;
    }

    @Override
    public Size getSize(ItemStack stack)
    {
        return stack.getTag() == null ? Size.VERY_LARGE : Size.HUGE;
    }

    @Override
    public Weight getWeight(ItemStack stack)
    {
        return Weight.VERY_HEAVY;
    }

    @Override
    protected void beforeRemove(InventoryBlockEntity<?> entity)
    {
        if (!entity.getBlockState().getValue(SEALED))
        {
            entity.ejectInventory();
        }
        entity.invalidateCapabilities();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag)
    {
        final CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (tag != null)
        {
            // Decode the contents of the vessel
            final ItemStackHandler inventory = new ItemStackHandler();

            inventory.deserializeNBT(tag.getCompound("inventory"));

            if (!Helpers.isEmpty(inventory))
            {
                tooltip.add(new TranslatableComponent("tfc.tooltip.contents").withStyle(ChatFormatting.DARK_GREEN));
                Helpers.addInventoryTooltipInfo(inventory, tooltip);
            }
        }
    }
}
