package tfcflorae.client.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

import net.dries007.tfc.client.screen.button.BarrelSealButton;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;

import tfcflorae.client.screen.TFCFBarrelScreen;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;

public class TFCFBarrelSealButton extends BarrelSealButton
{
    private final TFCFBarrelBlockEntity barrel;

    public TFCFBarrelSealButton(TFCFBarrelBlockEntity barrel, int guiLeft, int guiTop, Component onTooltip)
    {
        super(barrel, guiLeft, guiTop, onTooltip);
        this.barrel = barrel;
    }

    @Override
    public void onPress()
    {
        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void renderWidget(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TFCFBarrelScreen.BACKGROUND);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        final int v = barrel.getBlockState().getValue(TFCFBarrelBlock.SEALED) ? 0 : 20;
        poseStack.blit(new ResourceLocation("minecraft:dirt"), getX(), getY(), 236, v, 20, 20, 256, 256);
        //todo minecraft:dirt
        if (isHoveredOrFocused())
        {
            renderWidget(poseStack, mouseX, mouseY, partialTicks);
        }
    }
}
