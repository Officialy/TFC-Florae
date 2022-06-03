package tfcflorae.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import tfcflorae.TFCFlorae;

import static tfcflorae.TFCFlorae.MOD_ID;
import static net.dries007.tfc.util.SelfTests.*;

public final class ClientSelfTests
{
    @SuppressWarnings("deprecation")
    public static boolean validateModels()
    {
        final BlockModelShaper shaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        final BakedModel missingModel = shaper.getModelManager().getMissingModel();
        final TextureAtlasSprite missingParticle = missingModel.getParticleIcon();

        final List<BlockState> missingModelErrors = stream(ForgeRegistries.BLOCKS, MOD_ID)
            .flatMap(states(s -> s.getRenderShape() == RenderShape.MODEL && shaper.getBlockModel(s) == missingModel))
            .toList();
        final List<BlockState> missingParticleErrors = stream(ForgeRegistries.BLOCKS, MOD_ID)
            .flatMap(states(s -> !s.isAir() && shaper.getParticleIcon(s) == missingParticle))
            .toList();

        return logErrors("{} block states with missing models:", missingModelErrors, TFCFlorae.LOGGER)
            | logErrors("{} block states with missing particles:", missingParticleErrors, TFCFlorae.LOGGER);
    }
}
