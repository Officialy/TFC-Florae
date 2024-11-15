package tfcflorae.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.client.screen.button.AnvilPlanSelectButton;
import net.dries007.tfc.client.screen.button.NextPageButton;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import org.jetbrains.annotations.Nullable;

import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.container.TFCFAnvilPlanContainer;

public class TFCFAnvilPlanScreen extends BlockEntityScreen<TFCFAnvilBlockEntity, TFCFAnvilPlanContainer>
{
    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/anvil_plan.png");

    @Nullable private Button leftButton, rightButton;
    @Nullable private List<AnvilPlanSelectButton> recipeButtons;
    private int maxPageInclusive;
    private int currentPage;

    public TFCFAnvilPlanScreen(TFCFAnvilPlanContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);

        this.currentPage = 0;
        this.maxPageInclusive = 0;
    }

    @Override
    protected void init()
    {
        super.init();

        final int recipesPerPage = 18;
        final int guiLeft = getGuiLeft(), guiTop = getGuiTop();

        final ItemStack inputStack = blockEntity
            .getCapability(Capabilities.ITEM, null)
            .map(t -> t.getStackInSlot(TFCFAnvilBlockEntity.SLOT_INPUT_MAIN))
            .orElse(ItemStack.EMPTY);
        final List<AnvilRecipe> recipes = AnvilRecipe.getAll(playerInventory.player.level(), inputStack, blockEntity.getTier());

        recipeButtons = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++)
        {
            final int page = i / recipesPerPage;
            final int index = i % recipesPerPage;
            final int posX = 7 + (index % 9) * 18;
            final int posY = 17 + ((index % 18) / 9) * 18;

            final AnvilRecipe recipe = recipes.get(i);
            final AnvilPlanSelectButton button = new AnvilPlanSelectButton(guiLeft + posX, guiTop + posY, page, recipe, Component.literal("TESTING"));// RenderHelpers.makeButtonTooltip(this, recipe.getResultItem().getHoverName()));

            button.setCurrentPage(0);
            recipeButtons.add(button);
            addRenderableWidget(button);
        }

        maxPageInclusive = (recipes.size() - 1) / recipesPerPage;

        addRenderableWidget(leftButton = NextPageButton.left(guiLeft + 7, guiTop + 56, button -> {
            if (currentPage > 0)
            {
                currentPage--;
                updateCurrentPage();
            }
        }));
        addRenderableWidget(rightButton = NextPageButton.right(guiLeft + 160, guiTop + 56, button -> {
            if (currentPage < maxPageInclusive)
            {
                currentPage++;
                updateCurrentPage();
            }
        }));

        updateCurrentPage();
    }

    @Override
    protected void renderBg(GuiGraphics poseStack, float partialTicks, int mouseX, int mouseY)
    {
        drawDefaultBackground(poseStack);
    }

    @Override
    protected void renderTooltip(GuiGraphics poseStack, int mouseX, int mouseY)
    {
        super.renderTooltip(poseStack, mouseX, mouseY);

        for (Renderable widget : renderables)
        {
            if (widget instanceof AnvilPlanSelectButton button && button.isHoveredOrFocused())
            {
                button.render(poseStack, mouseX, mouseY, 0);
                return;
            }
        }
    }

    private void updateCurrentPage()
    {
        assert recipeButtons != null && leftButton != null && rightButton != null;

        for (AnvilPlanSelectButton button : recipeButtons)
        {
            button.setCurrentPage(currentPage);
        }

        leftButton.active = leftButton.visible = currentPage > 0;
        rightButton.active = rightButton.visible = currentPage < maxPageInclusive;
    }
}
