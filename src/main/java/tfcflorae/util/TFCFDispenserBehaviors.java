package tfcflorae.util;

import net.dries007.tfc.common.items.TFCMinecartItem;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.DispenserBehaviors;

import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.items.TFCFItems;

public final class TFCFDispenserBehaviors
{
    public static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

    public static final DispenseItemBehavior CHEST_BEHAVIOR = new OptionalDispenseItemBehavior()
    {
        public ItemStack execute(BlockSource level, ItemStack stack)
        {
            final BlockPos blockpos = level.getPos().relative(level.getBlockState().getValue(DispenserBlock.FACING));
            for (AbstractChestedHorse horse : level.getLevel().getEntitiesOfClass(AbstractChestedHorse.class, new AABB(blockpos), horse -> horse.isAlive() && !horse.hasChest()))
            {
                if (horse.isTamed() && horse.getSlot(499).set(stack))
                {
                    stack.shrink(1);
                    this.setSuccess(true);
                    return stack;
                }
            }
            return super.execute(level, stack);
        }
    };

    public static DispenseItemBehavior MINECART_BEHAVIOR = new DefaultDispenseItemBehavior()
    {
        private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack)
        {
            if (stack.getItem() instanceof TFCMinecartItem cartItem)
            {
                final Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                final Level level = source.getLevel();

                final double x = source.x() + (double) direction.getStepX() * 1.125D;
                final double y = Math.floor(source.y()) + (double) direction.getStepY();
                final double z = source.z() + (double) direction.getStepZ() * 1.125D;

                final BlockPos offsetPos = source.getPos().relative(direction);
                final BlockState state = level.getBlockState(offsetPos);
                final RailShape railshape = state.getBlock() instanceof BaseRailBlock ? ((BaseRailBlock) state.getBlock()).getRailDirection(state, level, offsetPos, null) : RailShape.NORTH_SOUTH;

                double offset;
                if (Helpers.isBlock(state, BlockTags.RAILS))
                {
                    offset = railshape.isAscending() ? 0.6 : 0.1;
                }
                else
                {
                    if (!state.isAir() || !Helpers.isBlock(level.getBlockState(offsetPos.below()), BlockTags.RAILS))
                    {
                        return this.defaultBehavior.dispense(source, stack);
                    }

                    BlockState offsetState = level.getBlockState(offsetPos.below());
                    // noinspection deprecation
                    RailShape offsetShape = offsetState.getBlock() instanceof BaseRailBlock ? offsetState.getValue(((BaseRailBlock) offsetState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                    offset = direction != Direction.DOWN && offsetShape.isAscending() ? -0.4 : -0.9;
                }
                cartItem.createMinecartEntity(level, stack, x, y + offset, z);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        protected void playSound(BlockSource source)
        {
            source.getLevel().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, source.getPos(), 0);
        }
    };

    /**
     * {@link DispenserBlock#registerBehavior(ItemLike, DispenseItemBehavior)} is not thread safe
     */
    public static void registerDispenserBehaviors()
    {
        // Chest
        TFCFBlocks.WOODS.values().stream().map(map -> map.get(Wood.BlockType.CHEST).get()).forEach(chest -> DispenserBlock.registerBehavior(chest, DispenserBehaviors.CHEST_BEHAVIOR));

        // Minecart Chest
        TFCFItems.CHEST_MINECARTS.values().forEach(reg -> DispenserBlock.registerBehavior(reg.get(), DispenserBehaviors.MINECART_BEHAVIOR));

        // Fish
        TFCFItems.FRESHWATER_FISH_BUCKETS.values().forEach(reg -> DispenserBlock.registerBehavior(reg.get(), DispenserBehaviors.VANILLA_BUCKET_BEHAVIOR));
    }
}
