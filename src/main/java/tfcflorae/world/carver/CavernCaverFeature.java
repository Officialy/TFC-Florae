package tfcflorae.world.carver;

import com.mojang.serialization.Codec;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

import net.dries007.tfc.world.carver.CarverHelpers;

public class CavernCaverFeature extends CaveWorldCarver
{
   public CavernCaverFeature(Codec<CaveCarverConfiguration> codec)
   {
      super(codec);
   }

   @Override
   protected int getCaveBound()
   {
      return 10;
   }

   @Override
   protected float getThickness(RandomSource random)
   {
      return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
   }

   @Override
   protected double getYScale()
   {
      return 5.0D;
   }

   /**
    * Carves a single block, replacing it with the appropiate state if possible, and handles replacing exposed dirt with
    * grass.
    * @param biome The position to carve at. The method does not mutate this position.
    * @param bool An additional mutable block position object to be used and modified by the method
    * @param mutablePos Set to true if the block carved was the surface, which is checked as being either grass or
    * mycelium
    */
   @Override
   protected boolean carveBlock(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biome, CarvingMask mask, BlockPos.MutableBlockPos mutablePos, BlockPos.MutableBlockPos mutablePos2, Aquifer aquifer, MutableBoolean bool)
   {
//      if (CarverHelpers.canReplaceBlock(chunk.getBlockState(mutablePos)))
//      {
         BlockState blockstate;
         if (mutablePos.getY() <= context.getMinGenY() + 15)
         {
            blockstate = LAVA.createLegacyBlock();
         }
         else
         {
            blockstate = CAVE_AIR;
         }
         chunk.setBlockState(mutablePos, blockstate, false);
         return true;
//      }
//      else
//      {
//         return false;
//      }
   }
}