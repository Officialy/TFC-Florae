package tfcflorae.common.container;

import java.util.function.Supplier;

import net.dries007.tfc.util.KnappingType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ItemStackContainer;
import net.dries007.tfc.common.container.KnappingContainer;
import net.dries007.tfc.util.registry.RegistrationHelpers;

import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.container.ceramics.LargeVesselContainer;
import tfcflorae.common.recipes.TFCFRecipeTypes;

import static tfcflorae.TFCFlorae.MOD_ID;

@SuppressWarnings("RedundantTypeArguments")
public class TFCFContainerTypes
{
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MOD_ID);

    /*public static final RegistryObject<MenuType<LargeEarthenwareVesselContainer>> LARGE_EARTHENWARE_VESSEL = TFCFContainerTypes.<LargeEarthenwareVesselBlockEntity, LargeEarthenwareVesselContainer>registerBlock("large_earthenware_vessel", TFCFBlockEntities.LARGE_EARTHENWARE_VESSEL, LargeEarthenwareVesselContainer::create);
    public static final RegistryObject<MenuType<LargeKaoliniteVesselContainer>> LARGE_KAOLINITE_VESSEL = TFCFContainerTypes.<LargeKaoliniteVesselBlockEntity, LargeKaoliniteVesselContainer>registerBlock("large_kaolinite_vessel", TFCFBlockEntities.LARGE_KAOLINITE_VESSEL, LargeKaoliniteVesselContainer::create);
    public static final RegistryObject<MenuType<LargeStonewareVesselContainer>> LARGE_STONEWARE_VESSEL = TFCFContainerTypes.<LargeStonewareVesselBlockEntity, LargeStonewareVesselContainer>registerBlock("large_stoneware_vessel", TFCFBlockEntities.LARGE_STONEWARE_VESSEL, LargeStonewareVesselContainer::create);*/

    public static final RegistryObject<MenuType<LargeVesselContainer>> LARGE_VESSEL = TFCFContainerTypes.<LargeVesselBlockEntity, LargeVesselContainer>registerBlock("large_vessel", TFCFBlockEntities.LARGE_VESSEL, LargeVesselContainer::create);
    public static final RegistryObject<MenuType<TFCFBarrelContainer>> BARREL = TFCFContainerTypes.<TFCFBarrelBlockEntity, TFCFBarrelContainer>registerBlock("barrel", TFCFBlockEntities.BARREL, TFCFBarrelContainer::create);
    public static final RegistryObject<MenuType<TFCFAnvilContainer>> ANVIL = TFCFContainerTypes.<TFCFAnvilBlockEntity, TFCFAnvilContainer>registerBlock("anvil", TFCFBlockEntities.ANVIL, TFCFAnvilContainer::create);
    public static final RegistryObject<MenuType<TFCFAnvilPlanContainer>> ANVIL_PLAN = TFCFContainerTypes.<TFCFAnvilBlockEntity, TFCFAnvilPlanContainer>registerBlock("anvil_plan", TFCFBlockEntities.ANVIL, TFCFAnvilPlanContainer::create);

    public static final RegistryObject<MenuType<KnappingContainer>> EARTHENWARE_CLAY_KNAPPING = TFCFContainerTypes.registerItem("earthenware_clay_knapping", TFCFContainerTypes::createEarthenwareClay);
    public static final RegistryObject<MenuType<KnappingContainer>> KAOLINITE_CLAY_KNAPPING = TFCFContainerTypes.registerItem("kaolinite_clay_knapping", TFCFContainerTypes::createKaoliniteClay);
    public static final RegistryObject<MenuType<KnappingContainer>> STONEWARE_CLAY_KNAPPING = TFCFContainerTypes.registerItem("stoneware_clay_knapping", TFCFContainerTypes::createStonewareClay);
    public static final RegistryObject<MenuType<KnappingContainer>> FLINT_KNAPPING = TFCFContainerTypes.registerItem("flint_knapping", TFCFContainerTypes::createFlint);

    public static KnappingContainer createEarthenwareClay(ItemStack stack, InteractionHand hand, int slot, Inventory playerInventory, int windowId)
    {
        return null;// new KnappingContainer(EARTHENWARE_CLAY_KNAPPING.get(), TFCFRecipeTypes.EARTHENWARE_CLAY_KNAPPING.get(), windowId, playerInventory, stack, hand, slot);//, 5, true, true, TFCSounds.KNAP_CLAY.get()).init(playerInventory, 20);
    }

    public static KnappingContainer createKaoliniteClay(ItemStack stack, InteractionHand hand, int slot, Inventory playerInventory, int windowId)
    {
        return null;// new KnappingContainer(KAOLINITE_CLAY_KNAPPING.get(), TFCFRecipeTypes.KAOLINITE_CLAY_KNAPPING.get(), windowId, playerInventory, stack, hand, slot);//, 5, true, true, TFCSounds.KNAP_CLAY.get()).init(playerInventory, 20);
    }

    public static KnappingContainer createStonewareClay(ItemStack stack, InteractionHand hand, int slot, Inventory playerInventory, int windowId)
    {
        return null;// new KnappingContainer(STONEWARE_CLAY_KNAPPING.get(), TFCFRecipeTypes.STONEWARE_CLAY_KNAPPING.get(), windowId, playerInventory, stack, hand, slot);//, 5, true, true, TFCSounds.KNAP_CLAY.get()).init(playerInventory, 20);
    }

    public static KnappingContainer createFlint(ItemStack stack, InteractionHand hand, int slot, Inventory playerInventory, int windowId)
    {
        return null;// new KnappingContainer(FLINT_KNAPPING.get(), TFCFRecipeTypes.FLINT_KNAPPING.get(), windowId, playerInventory, stack, hand, slot);//, 1, false, false, TFCSounds.KNAP_STONE.get()).init(playerInventory, 20);
    }

    private static <T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> RegistryObject<MenuType<C>> registerBlock(String name, Supplier<BlockEntityType<T>> type, BlockEntityContainer.Factory<T, C> factory)
    {
        return RegistrationHelpers.registerBlockEntityContainer(CONTAINERS, name, type, factory);
    }

    private static <C extends ItemStackContainer> RegistryObject<MenuType<C>> registerItem(String name, ItemStackContainer.Factory<C> factory)
    {
        return RegistrationHelpers.registerItemStackContainer(CONTAINERS, name, factory);
    }

    private static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> register(String name, IContainerFactory<C> factory)
    {
        return RegistrationHelpers.registerContainer(CONTAINERS, name, factory);
    }
}