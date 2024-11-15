package tfcflorae.world.feature.tree;

import com.mojang.serialization.Codec;
import net.dries007.tfc.mixin.accessor.StructureTemplateAccessor;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.dries007.tfc.world.feature.tree.RandomTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TFCFRandomTreeFeature extends RandomTreeFeature
{
    public TFCFRandomTreeFeature(Codec<RandomTreeConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomTreeConfig> context)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource random = context.random();
        final RandomTreeConfig config = context.config();

        final ChunkPos chunkPos = new ChunkPos(pos);
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos().set(pos);
        final StructureTemplateManager manager = TFCFTreeHelpers.getStructureManager(level);
        final StructurePlaceSettings settings = TFCFTreeHelpers.getPlacementSettings(level, chunkPos, random);
        final ResourceLocation structureId = config.structureNames().get(random.nextInt(config.structureNames().size()));
        final StructureTemplate structure = manager.getOrCreate(structureId);
        if (((StructureTemplateAccessor) structure).accessor$getPalettes().isEmpty())
        {
            throw new IllegalStateException("Empty structure: " + structureId);
        }

        if (TFCFTreeHelpers.isValidLocation(level, pos, settings, config.placement()))
        {
            config.trunk().ifPresent(trunk -> {
                final int height = TFCFTreeHelpers.placeTrunk(level, mutablePos, random, settings, trunk);
                mutablePos.move(0, height - config.placement().height(), 0);
            });

            TFCFTreeHelpers.placeTemplate(structure, settings, level, mutablePos.subtract(TFCFTreeHelpers.transformCenter(structure.getSize(), settings)));
            return true;
        }
        return false;
    }
}