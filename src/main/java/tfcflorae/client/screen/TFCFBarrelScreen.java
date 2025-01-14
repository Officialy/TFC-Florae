package tfcflorae.client.screen;

import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;

import org.jetbrains.annotations.Nullable;
import tfcflorae.client.screen.button.TFCFBarrelSealButton;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;
import tfcflorae.common.container.TFCFBarrelContainer;

public class TFCFBarrelScreen extends BlockEntityScreen<TFCFBarrelBlockEntity, TFCFBarrelContainer>
{
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");
    private static final int MAX_RECIPE_NAME_LENGTH = 100;

    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/barrel.png");

    public TFCFBarrelScreen(TFCFBarrelContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);
        inventoryLabelY += 12;
        imageHeight += 12;
    }

    @Override
    public void init()
    {
        super.init();
        addRenderableWidget(new TFCFBarrelSealButton(blockEntity, getGuiLeft(), getGuiTop(), this.isSealed() ? UNSEAL : SEAL){
           /* @Override
            public void setTooltip(@Nullable Tooltip pTooltip) {
                renderTooltip(poseStack, *//*isSealed() ? UNSEAL : SEAL,*//* x, y);
            }*/

            @Override
            public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
                pNarrationElementOutput.add(NarratedElementType.USAGE, isSealed() ? UNSEAL : SEAL); //todo test
            }

        });
    }

    @Override
    protected void renderLabels(GuiGraphics poseStack, int mouseX, int mouseY)
    {
        super.renderLabels(poseStack, mouseX, mouseY);
        if (isSealed())
        {
            drawDisabled(poseStack, TFCFBarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, TFCFBarrelBlockEntity.SLOT_ITEM);

            // Draw the text displaying both the seal date, and the recipe name
            BarrelRecipe recipe = blockEntity.getRecipe();
            if (recipe != null)
            {
                FormattedText resultText = recipe.getTranslationComponent();
                if (font.width(resultText) > MAX_RECIPE_NAME_LENGTH)
                {
                    int line = 0;
                    for (FormattedCharSequence text : font.split(resultText, MAX_RECIPE_NAME_LENGTH))
                    {
                        poseStack.drawString(font, text, 70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - font.width(text), 2), titleLabelY + (line * font.lineHeight), 0x404040);
                        line++;
                    }
                }
                else
                {
                    poseStack.drawString(font, resultText.getString(), 70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - font.width(resultText), 2), 61, 0x404040);
                }
            }
            String date = ICalendar.getTimeAndDate(Calendars.CLIENT.ticksToCalendarTicks(blockEntity.getSealedTick()), Calendars.CLIENT.getCalendarDaysInMonth()).getString();
            poseStack.drawString(font, date, (int) (imageWidth / 2f - font.width(date) / 2f), 74, 0x404040);
        }
    }

    @Override
    protected void renderBg(GuiGraphics poseStack, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);
        blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty())
            {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil((float) 50 * fluidStack.getAmount() / (float) TFCConfig.SERVER.barrelCapacity.get());

                RenderHelpers.fillAreaWithSprite(poseStack, sprite, leftPos + 8, topPos + 70 - fillHeight, 16, fillHeight, 16, 16);

                resetToBackgroundSprite();
            }
        });

        poseStack.blit(texture,getGuiLeft() + 7, getGuiTop() + 19, 176, 0, 18, 52);
    }

    @Override
    protected void renderTooltip(GuiGraphics poseStack, int mouseX, int mouseY)
    {
        super.renderTooltip(poseStack, mouseX, mouseY);
        final int relX = mouseX - getGuiLeft();
        final int relY = mouseY - getGuiTop();

        if (relX >= 7 && relY >= 19 && relX < 25 && relY < 71)
        {
            blockEntity.getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty())
                {
                    renderTooltip(poseStack,/* Tooltips.fluidUnitsOf(fluid),*/ mouseX, mouseY);
                }
            });
        }
    }

    private boolean isSealed()
    {
        return blockEntity.getBlockState().getValue(TFCFBarrelBlock.SEALED);
    }

}
