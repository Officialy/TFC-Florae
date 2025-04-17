package tfcflorae.client.screen.button;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.LargeVesselScreen;
import net.dries007.tfc.common.blocks.LargeVesselBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;

import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;

public class VesselSealButton extends Button
{
    private final LargeVesselBlockEntity vessel;

    public VesselSealButton(LargeVesselBlockEntity barrel, int guiLeft, int guiTop, Component tooltip) {
        super(guiLeft + 123, guiTop + 35, 20, 20, tooltip, (b) -> {
        }, RenderHelpers.NARRATION);
        this.setTooltip(Tooltip.create(tooltip));
        this.vessel = barrel;
    }

    @Override
    public void onPress()
    {
        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int v = (Boolean)this.vessel.getBlockState().getValue(LargeVesselBlock.SEALED) ? 0 : 20;
        graphics.blit(LargeVesselScreen.BACKGROUND, this.getX(), this.getY(), 236.0F, (float)v, 20, 20, 256, 256);
    }
}
