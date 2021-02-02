package tfcflorae.objects.blocks.fruitwood;

import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

import tfcflorae.util.OreDictionaryHelper;

public class BlockFruitFence extends BlockFence 
{
    public BlockFruitFence()
    {
        super(Material.WOOD, Material.WOOD.getMaterialMapColor());
        setHarvestLevel("axe", 0);
        setHardness(2.0F);
        setResistance(15.0F);
        setSoundType(SoundType.WOOD);
        OreDictionaryHelper.register(this, "fence");
        Blocks.FIRE.setFireInfo(this, 5, 20);
    }
}