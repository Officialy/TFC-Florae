package tfcflorae.client.screen.ceramics;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.util.Helpers;

import tfcflorae.client.screen.button.VesselSealButton;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;
import tfcflorae.common.container.ceramics.LargeVesselContainer;

public class LargeVesselScreen extends BlockEntityScreen<LargeVesselBlockEntity, LargeVesselContainer>
{
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");
    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/large_vessel.png");

    public LargeVesselScreen(LargeVesselContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);
    }

    @Override
    public void init()
    {
        super.init();
/*        addRenderableWidget(new VesselSealButton(blockEntity, getGuiLeft() + 9, getGuiTop()));/*new Button.OnTooltip()
        {
            @Override
            public void onTooltip(Button button, PoseStack poseStack, int x, int y)
            {
                renderTooltip(poseStack, isSealed() ? UNSEAL : SEAL, x, y);
            }

            @Override
            public void narrateTooltip(Consumer<Component> consumer)
            {
                consumer.accept(isSealed() ? UNSEAL : SEAL);
            }
        }));*/
    }

    @Override
    protected void renderLabels(GuiGraphics poseStack, int mouseX, int mouseY)
    {
        super.renderLabels(poseStack, mouseX, mouseY);
        if (isSealed())
        {
            drawDisabled(poseStack, 0, LargeVesselBlockEntity.SLOTS - 1);
        }
    }

    private boolean isSealed()
    {
        return blockEntity.getBlockState().getValue(LargeVesselBlock.SEALED);
    }
}
