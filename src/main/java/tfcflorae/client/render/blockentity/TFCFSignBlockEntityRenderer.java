package tfcflorae.client.render.blockentity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dries007.tfc.common.blocks.wood.TFCStandingSignBlock;
import net.dries007.tfc.common.blocks.wood.Wood;

import org.joml.Vector3f;
import tfcflorae.TFCFlorae;
import tfcflorae.common.blocks.TFCFBlocks;

import static net.minecraft.client.renderer.Sheets.SIGN_SHEET;

// todo: custom SignEditScreen
public class TFCFSignBlockEntityRenderer extends SignRenderer
{
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);

    private static Material createSignMaterial(String domain, String name)
    {
        return new Material(SIGN_SHEET, new ResourceLocation(domain, "entity/signs/" + name));
    }

    private static int getDarkColor(SignBlockEntity sign)
    {
        int i = sign.getFrontText().getColor().getTextColor();
        int j = 100;//todo (int) ((double) NativeImage.getR(i) * 0.4D);
        int k = 100;//(int) ((double) NativeImage.getG(i) * 0.4D);
        int l = 100;//(int) ((double) NativeImage.getB(i) * 0.4D);
        return i == DyeColor.BLACK.getTextColor() && sign.getFrontText().hasGlowingText() ? -988212 : 33456;//todo NativeImage.combine(0, l, k, j);
    }

    private static boolean isOutlineVisible(SignBlockEntity sing, int dyeIndex)
    {
        if (dyeIndex == DyeColor.BLACK.getTextColor())
        {
            return true;
        }
        else
        {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping())
            {
                return true;
            }
            else
            {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(sing.getBlockPos())) < (double) OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    private final Font font;
    private final Map<Block, Material> materials;
    private final Map<Block, SignModel> models;

    public TFCFSignBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this(context, TFCFBlocks.WOODS.entrySet()
            .stream()
            .map(entry -> new SignModelData(
                TFCFlorae.MOD_ID,
                entry.getKey().getSerializedName(),
                entry.getValue().get(Wood.BlockType.SIGN).get(),
                entry.getValue().get(Wood.BlockType.WALL_SIGN).get()
            )));
    }

    public TFCFSignBlockEntityRenderer(BlockEntityRendererProvider.Context context, Stream<SignModelData> blocks)
    {
        super(context);

        this.font = context.getFont();

        ImmutableMap.Builder<Block, Material> materialBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Block, SignModel> modelBuilder = ImmutableMap.builder();

        blocks.forEach(data -> {
            final Material material = createSignMaterial(data.domain, data.name);
            final SignModel model = new SignModel(context.bakeLayer(new ModelLayerLocation(new ResourceLocation(data.domain, "sign/" + data.name), "main")));

            materialBuilder.put(data.sign, material);
            materialBuilder.put(data.wallSign, material);
            modelBuilder.put(data.sign, model);
            modelBuilder.put(data.wallSign, model);
        });

        this.materials = materialBuilder.build();
        this.models = modelBuilder.build();
    }

    public void render(SignBlockEntity sign, float partialTicks, PoseStack poseStack, MultiBufferSource source, int packedLight, int overlay)
    {
        BlockState state = sign.getBlockState();
        poseStack.pushPose();
        float scale = 0.6666667F;
        SignModel model = models.get(state.getBlock());
        if (state.getBlock() instanceof TFCStandingSignBlock)
        {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            float yRot = -((float) (state.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            model.stick.visible = true;
        }
        else
        {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            float yRot = -state.getValue(WallSignBlock.FACING).toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.translate(0.0D, -0.3125D, -0.4375D);
            model.stick.visible = false;
        }

        poseStack.pushPose();
        poseStack.scale(scale, -scale, -scale);
        Material material = materials.get(state.getBlock());
        VertexConsumer vertexconsumer = material.buffer(source, model::renderType);
        model.root.render(poseStack, vertexconsumer, packedLight, overlay);
        poseStack.popPose();

        float rescale = 0.010416667F;

        poseStack.translate(0.0D, 0.33333334F, 0.046666667F);
        poseStack.scale(rescale, -rescale, rescale);

        int darkColor = getDarkColor(sign);
        FormattedCharSequence[] lines = sign.getFrontText().getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {  //todo theres now getBackText??
            List<FormattedCharSequence> list = this.font.split(component, 90);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int textColor;
        boolean outline;
        int totalLight;
        if (sign.getFrontText().hasGlowingText())
        {
            textColor = sign.getFrontText().getColor().getTextColor();
            outline = isOutlineVisible(sign, textColor);
            totalLight = 15728880;
        }
        else
        {
            textColor = darkColor;
            outline = false;
            totalLight = packedLight;
        }

        for (int i1 = 0; i1 < 4; ++i1)
        {
            FormattedCharSequence formattedcharsequence = lines[i1];
            float f3 = (float) (-this.font.width(formattedcharsequence) / 2);
            if (outline)
            {
                this.font.drawInBatch8xOutline(formattedcharsequence, f3, (float) (i1 * 10 - 20), textColor, darkColor, poseStack.last().pose(), source, totalLight);
            }
            else
            {
                this.font.drawInBatch(formattedcharsequence, f3, (float) (i1 * 10 - 20), textColor, false, poseStack.last().pose(), source, Font.DisplayMode.NORMAL, 0, totalLight);
            }
        }

        poseStack.popPose();
    }

    public record SignModelData(String domain, String name, Block sign, Block wallSign) {}
}
