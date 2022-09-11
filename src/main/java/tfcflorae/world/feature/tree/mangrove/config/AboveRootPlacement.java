package tfcflorae.world.feature.tree.mangrove.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance)
{
    public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockStateProvider.CODEC.fieldOf("above_root_provider").forGetter(decorator -> {
            return decorator.aboveRootProvider;
        }), Codec.floatRange(0.0F, 1.0F).fieldOf("above_root_placement_chance").forGetter(decorator -> {
            return decorator.aboveRootPlacementChance;
        })).apply(instance, AboveRootPlacement::new);
    });
}