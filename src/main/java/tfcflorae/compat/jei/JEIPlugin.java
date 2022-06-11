package tfcflorae.compat.jei;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

import tfcflorae.TFCFlorae;
import tfcflorae.common.TFCFHelpers;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.dries007.tfc.util.Helpers;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static <C extends Container, T extends Recipe<C>> List<T> getRecipes(net.minecraft.world.item.crafting.RecipeType<T> type)
    {
        ClientLevel level = Minecraft.getInstance().level;
        assert level != null;
        return level.getRecipeManager().getAllRecipesFor(type);
    }

    private static <C extends Container, T extends Recipe<C>> List<T> getRecipes(net.minecraft.world.item.crafting.RecipeType<T> type, Predicate<T> filter)
    {
        return getRecipes(type).stream().filter(filter).collect(Collectors.toList());
    }

    private static void addCatalystTag(IRecipeCatalystRegistration r, TagKey<Item> tag, RecipeType<?> recipeType)
    {
        Helpers.getAllTagValues(tag, ForgeRegistries.ITEMS).forEach(item -> r.addRecipeCatalyst(new ItemStack(item), recipeType));
    }

    private static <T> RecipeType<T> type(String name, Class<T> tClass)
    {
        return RecipeType.create(TFCFlorae.MOD_ID, name, tClass);
    }

    @Override
    public ResourceLocation getPluginUid()
    {
        return TFCFHelpers.identifier("jei");
    }
    @Override
    public void registerCategories(IRecipeCategoryRegistration r)
    {
        //IGuiHelper gui = r.getJeiHelpers().getGuiHelper();
    }

    @Override
    public void registerRecipes(IRecipeRegistration r)
    {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r)
    {
    }
}
