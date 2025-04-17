package tfcflorae;

import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.mojang.logging.LogUtils;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import net.dries007.tfc.util.Helpers;

import tfcflorae.client.*;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.commands.TFCFCommands;
import tfcflorae.common.container.TFCFContainerTypes;
import tfcflorae.common.entities.*;
import tfcflorae.common.entities.ai.TFCFBrain;
import tfcflorae.common.fluid.TFCFFluids;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.common.recipes.TFCFRecipeSerializers;
import tfcflorae.common.recipes.TFCFRecipeTypes;
import tfcflorae.common.recipes.ingredients.TFCFIngredients;
import tfcflorae.util.TFCFDispenserBehaviors;
import tfcflorae.util.TFCFInteractionManager;
import tfcflorae.world.carver.TFCFCarvers;
import tfcflorae.world.feature.TFCFFeatures;
import tfcflorae.world.placement.TFCFPlacements;

import java.util.Map;
import java.util.function.Supplier;

@Mod(TFCFlorae.MOD_ID)
public class TFCFlorae {
    public static final String MOD_ID = "tfcflorae";
    public static final String MOD_NAME = "TFCFlorae";
    public static final String MOD_VERSION = "${version}";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "tfcflorae");
    public static final TFCCreativeTabs.CreativeTabHolder AAAAAAA = register("aaaaaaa", () -> new ItemStack((ItemLike) ((RegistryObject) ((Map) TFCBlocks.ROCK_BLOCKS.get(Rock.QUARTZITE)).get(Rock.BlockType.RAW)).get()), (tab, out) -> {

    });

    public TFCFlorae(FMLJavaModLoadingContext mod) {
        LOGGER.info("Initializing TFC Florae");
        final var bus = mod.getModEventBus();

        bus.addListener(this::setup);
        bus.addListener(TFCFEntities::onEntityAttributeCreation);
        bus.addListener(this::addItemsToTab);

        TFCFItems.ITEMS.register(bus);
        TFCFBlocks.BLOCKS.register(bus);
        TFCFContainerTypes.CONTAINERS.register(bus);
        TFCFEntities.ENTITIES.register(bus);
        TFCFFluids.FLUIDS.register(bus);
        TFCFRecipeTypes.RECIPE_TYPES.register(bus);
        TFCFRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
        TFCFSounds.SOUNDS.register(bus);
        TFCFParticles.PARTICLE_TYPES.register(bus);
        TFCFBlockEntities.BLOCK_ENTITIES.register(bus);
//        ContinentalWorldType.WORLD_TYPES.register(bus);

        TFCFPlacements.PLACEMENT_MODIFIERS.register(bus);
        TFCFFeatures.CONFIGURED_FEATURES.register(bus);
        TFCFFeatures.FEATURES.register(bus);
        TFCFFeatures.TRUNK_DECOR.register(bus);
        TFCFFeatures.LEAF_DECOR.register(bus);
        TFCFCarvers.CARVERS.register(bus);
        TFCFBrain.registerAll(bus);

        TFCFForgeEventHandler.init();
        CREATIVE_TABS.register(bus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandler.init();
        }
    }

    private static TFCCreativeTabs.CreativeTabHolder register(String name, Supplier<ItemStack> icon, CreativeModeTab.DisplayItemsGenerator displayItems) {
        RegistryObject<CreativeModeTab> reg = CREATIVE_TABS.register(name, () -> CreativeModeTab.builder().icon(icon).title(Component.translatable("tfc.creative_tab." + name)).displayItems(displayItems).build());
        return new TFCCreativeTabs.CreativeTabHolder(reg, displayItems);
    }

    public void setup(FMLCommonSetupEvent event) {
        LOGGER.info("TFCFlorae Common Setup");
        TFCFRock.registerDefaultRocks();

        event.enqueueWork(() -> {
            TFCFIngredients.registerIngredientTypes();
            TFCFCommands.registerSuggestionProviders();
            TFCFInteractionManager.init();
            TFCFDispenserBehaviors.registerDispenserBehaviors();
            TFCFBlocks.registerFlowerPotFlowers();
            TFCFFaunas.registerSpawnPlacements();
        });
    }

    public void addItemsToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == TFCFlorae.AAAAAAA.tab().get()) {
            TFCFItems.ITEMS.getEntries().forEach((itemRegistryObject -> {
                if (!(itemRegistryObject.get() instanceof MobBucketItem)) {
                    event.accept(itemRegistryObject.get());
                }
            }));
        }
    }

}
