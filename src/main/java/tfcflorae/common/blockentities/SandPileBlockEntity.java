package tfcflorae.common.blockentities;

import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;

public class SandPileBlockEntity extends TFCBlockEntity
{
    private BlockState internalState;
    @Nullable private BlockState aboveState;

    public SandPileBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCFBlockEntities.SAND_PILE.get(), pos, state);

        internalState = Blocks.AIR.defaultBlockState();
        aboveState = null;
    }

    public void setHiddenStates(BlockState internalState, @Nullable BlockState aboveState, boolean byPlayer)
    {
        if (Helpers.isBlock(internalState, TFCTags.Blocks.CONVERTS_TO_HUMUS) && !byPlayer)
        {
            this.internalState = TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.HUMUS).get().defaultBlockState();
        }
        else
        {
            this.internalState = internalState;
        }
        this.aboveState = aboveState;
    }


    public BlockState getInternalState()
    {
        return internalState;
    }

    @Nullable
    public BlockState getAboveState()
    {
        return aboveState;
    }

    @Override
    protected void loadAdditional(CompoundTag tag)
    {
        internalState = NbtUtils.readBlockState(getLevel().holderLookup(Registries.BLOCK), tag.getCompound("internalState"));
        aboveState = tag.contains("aboveState", Tag.TAG_COMPOUND) ? NbtUtils.readBlockState(getLevel().holderLookup(Registries.BLOCK), tag.getCompound("aboveState")) : null;
        super.loadAdditional(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        tag.put("internalState", NbtUtils.writeBlockState(internalState));
        if (aboveState != null)
        {
            tag.put("aboveState", NbtUtils.writeBlockState(aboveState));
        }
        super.saveAdditional(tag);
    }
}
