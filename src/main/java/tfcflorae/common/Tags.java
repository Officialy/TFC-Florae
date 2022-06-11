package tfcflorae.common;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("unused")
public class Tags
{
    public static class Blocks
    {
        /*public static final TagKey<Block> OVEN_BLOCKS = create("oven_blocks");
        public static final TagKey<Block> OVEN_INSULATION = create("oven_insulation");
        public static final TagKey<Block> CHIMNEYS = create("chimneys");
        public static final TagKey<Block> GREENHOUSE = create("greenhouse");*/

        private static TagKey<Block> create(String id)
        {
            return TagKey.create(Registry.BLOCK_REGISTRY, TFCFHelpers.identifier(id));
        }
    }

    public static class Items
    {
        public static final TagKey<Item> USABLE_ON_OVEN = create("usable_on_oven");

        private static TagKey<Item> create(String id)
        {
            return TagKey.create(Registry.ITEM_REGISTRY, TFCFHelpers.identifier(id));
        }

    }

    public static class Biomes
    {

        private static TagKey<Biome> create(String id)
        {
            return TagKey.create(Registry.BIOME_REGISTRY, TFCFHelpers.identifier(id));
        }
    }
}
