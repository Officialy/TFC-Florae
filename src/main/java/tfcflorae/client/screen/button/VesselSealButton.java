package tfcflorae.client.screen.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;

import tfcflorae.client.screen.ceramics.LargeVesselScreen;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;

public class VesselSealButton extends Button
{
    private final LargeVesselBlockEntity vessel;

    public VesselSealButton(LargeVesselBlockEntity barrel, int guiLeft, int guiTop, CreateNarration onTooltip)
    {
        super(guiLeft + 123, guiTop + 35, 20, 20, Component.empty(), b -> {}, onTooltip);
        this.vessel = barrel;
    }

    @Override
    public void onPress()
    {
        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

//    @Override
    public void renderButton(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, LargeVesselScreen.BACKGROUND);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        final int v = vessel.getBlockState().getValue(LargeVesselBlock.SEALED) ? 0 : 20;
//todo        blit(poseStack, x, y, 236, v, 20, 20, 256, 256);

        if (isHoveredOrFocused())
        {
// todo           renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
