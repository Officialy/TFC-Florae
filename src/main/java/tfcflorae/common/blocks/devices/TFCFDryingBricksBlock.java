package tfcflorae.common.blocks.devices;


import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DryingBricksBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blockentities.TFCFTickCounterBlockEntity;

public class TFCFDryingBricksBlock extends DryingBricksBlock
{
    public static final IntegerProperty COUNT = TFCBlockStateProperties.COUNT_1_4;
    public static final BooleanProperty DRIED = TFCBlockStateProperties.DRIED;

    public static final VoxelShape SHAPE = box(0, 0, 0, 16, 1, 16);

    private final Supplier<? extends Item> dryItem;

    public TFCFDryingBricksBlock(ExtendedProperties properties, Supplier<? extends Item> dryItem)
    {
        super(properties, dryItem);
        this.dryItem = dryItem;
        registerDefaultState(getStateDefinition().any().setValue(COUNT, 1).setValue(DRIED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        final ItemStack held = player.getItemInHand(hand);
        if (Helpers.isItem(held, asItem()) && !player.isShiftKeyDown() && !state.getValue(DRIED))
        {
            final int count = state.getValue(COUNT);
            if (count < 4)
            {
                level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
                final SoundType soundType = getSoundType(state, level, pos, player);
                level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1f) / 2f, soundType.getPitch() * 0.8f);
                TFCFTickCounterBlockEntity.reset(level, pos);
                if (!player.isCreative())
                {
                    held.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        else if (held.isEmpty() && player.isShiftKeyDown())
        {
            int count = state.getValue(COUNT);
            ItemStack drop = new ItemStack(state.getValue(DRIED) ? dryItem.get() : asItem());
            ItemHandlerHelper.giveItemToPlayer(player, drop);
            if (count > 1)
            {
                level.setBlockAndUpdate(pos, state.setValue(COUNT, count - 1));
            }
            else
            {
                level.destroyBlock(pos, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        TFCFTickCounterBlockEntity.reset(level, pos);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand)
    {
        level.getBlockEntity(pos, TFCFBlockEntities.TICK_COUNTER.get()).ifPresent(counter -> {
            if (level.isRainingAt(pos.above()))
            {
                counter.resetCounter();
            }
            else
            {
                final int ticks = TFCConfig.SERVER.mudBricksTicks.get();
                if (ticks > -1 && counter.getTicksSinceUpdate() > ticks)
                {
                    level.setBlockAndUpdate(pos, state.setValue(DRIED, true));
                }
            }
        });
    }
}
