package tfcflorae.objects.blocks.FruitWood;

import java.util.HashMap;
import java.util.Map;

import net.dries007.tfc.api.types.IFruitTree;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;

import tfcflorae.util.OreDictionaryHelper;

public class BlockFruitTrapDoor extends BlockTrapDoor
{
    public BlockFruitTrapDoor()
    {
        super(Material.WOOD);
        setHardness(0.5F);
        OreDictionaryHelper.register(this, "trapdoor");
    }
}