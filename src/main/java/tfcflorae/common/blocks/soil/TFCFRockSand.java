package tfcflorae.common.blocks.soil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;


import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.registry.RegistryRock;

import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.util.TriFunction;
import tfcflorae.util.registry.TFCFRegistryRock;

public enum TFCFRockSand
{
    PEBBLE((self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.8F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(), TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), null, false),
                        (self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.8F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), null, false)),
    ROCKY((self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.85F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(), TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock), TFCFBlocks.ROCKY_SAND_TFC.get(PEBBLE).get(sandColor).get(rock), null, false),
                        (self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.85F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock), TFCFBlocks.ROCKY_SAND_TFCF.get(PEBBLE).get(sandColor).get(rock), null, false)),
    ROCKIER((self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock), TFCFBlocks.ROCKY_SAND_TFC.get(ROCKY).get(sandColor).get(rock), null, false),
                        (self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock), TFCFBlocks.ROCKY_SAND_TFCF.get(ROCKY).get(sandColor).get(rock), null, false)),
    ROCKIEST((self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(1F).sound(SoundType.BASALT).requiresCorrectToolForDrops(), TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCBlocks.ROCK_BLOCKS.get(rock).get(Rock.BlockType.COBBLE), TFCFBlocks.ROCKY_SAND_TFC.get(ROCKIER).get(sandColor).get(rock), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), true),
                        (self, sandColor, rock) -> new RockySoilBlock(Block.Properties.copy(Blocks.SAND).mapColor( sandColor.getMapColor()).strength(1F).sound(SoundType.BASALT).requiresCorrectToolForDrops(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(Rock.BlockType.LOOSE), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(Rock.BlockType.COBBLE), TFCFBlocks.ROCKY_SAND_TFCF.get(ROCKIER).get(sandColor).get(rock), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), true)),

    SANDIEST_TILES((self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.25f, 8).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), null, TFCFBlocks.ROCK_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES), true),
                        (self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.25f, 8).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCBlocks.SAND.get(sandColor.toSandTFC(true)), null, TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES), true)),
    SANDIER_TILES((self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.35f, 9).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock), null, null, false),
                        (self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.35f, 9).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock), null, null, false)),
    SANDY_TILES((self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.5f, 10).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock), null, null, false),
                        (self, sandColor, rock) -> new TFCFRockySoilBlock(Block.Properties.copy(Blocks.STONE).mapColor( sandColor.getMapColor()).strength(1.5f, 10).sound(SoundType.TUFF).requiresCorrectToolForDrops(), TFCFBlocks.SAND_LAYERS.get(sandColor), TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock), null, null, false));

    public static final TFCFRockSand[] VALUES = values();

    public static TFCFRockSand valueOf(int i)
    {
        return i >= 0 && i < VALUES.length ? VALUES[i] : PEBBLE;
    }

    private final TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory;
    private final TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> TFCFFactory;

    TFCFRockSand(TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory, TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> TFCFFactory)
    {
        this.TFCFactory = TFCFactory;
        this.TFCFFactory = TFCFFactory;
    }

    TFCFRockSand(TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory)
    {
        this.TFCFactory = null;
        this.TFCFFactory = null;
    }

    public Block TFCCreate(Colors sandColor, RegistryRock rock)
    {
        return TFCFactory.apply(this, sandColor, rock);
    }

    public Block TFCFCreate(Colors sandColor, TFCFRegistryRock rock)
    {
        return TFCFFactory.apply(this, sandColor, rock);
    }

    public TriFunction<TFCFRockSand, Colors, RegistryRock, Block> getTFCFactory()
    {
        return TFCFactory;
    }

    public TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> getTFCFFactory()
    {
        return TFCFFactory;
    }

    public TFCFRockSand transform()
    {
        switch (this)
        {
            case SANDY_TILES:
                return SANDIER_TILES;
            case SANDIER_TILES:
                return SANDIEST_TILES;
            case SANDIEST_TILES:
                return ROCKY;
            case PEBBLE:
                return ROCKY;
            case ROCKY:
                return ROCKIER;
            case ROCKIER:
                return ROCKIEST;
            default:
                return this;
        }
    }
}
