package tfcflorae.client;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.client.render.blockentity.AnvilBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BarrelBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.SluiceBlockEntityRenderer;
import net.dries007.tfc.client.screen.KnappingScreen;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfcflorae.client.model.entity.*;
import tfcflorae.client.particle.FallingLeafParticle;
import tfcflorae.client.particle.SporeParticle;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.client.particle.WaterFlowParticle;
import tfcflorae.client.render.blockentity.MineralSheetBlockEntityRenderer;
import tfcflorae.client.render.blockentity.TFCFChestBlockEntityRenderer;
import tfcflorae.client.render.blockentity.TFCFSignBlockEntityRenderer;
import tfcflorae.client.render.blockentity.TFCFToolRackBlockEntityRenderer;
import tfcflorae.client.render.entity.*;
import tfcflorae.client.screen.TFCFAnvilPlanScreen;
import tfcflorae.client.screen.TFCFAnvilScreen;
import tfcflorae.client.screen.TFCFBarrelScreen;
import tfcflorae.client.screen.ceramics.LargeVesselScreen;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.common.container.TFCFContainerTypes;
import tfcflorae.common.entities.Fish;
import tfcflorae.common.entities.Silkmoth;
import tfcflorae.common.entities.TFCFEntities;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TFCFHelpers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import static net.dries007.tfc.common.blocks.wood.Wood.BlockType.*;

public class ClientEventHandler
{
    public static void init()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ClientEventHandler::clientSetup);
        bus.addListener(ClientEventHandler::onConfigReload);
        bus.addListener(ClientEventHandler::registerModelLoaders);
        bus.addListener(ClientEventHandler::registerColorHandlerBlocks);
        bus.addListener(ClientEventHandler::registerColorHandlerItems);
        bus.addListener(ClientEventHandler::registerParticleFactories);
        bus.addListener(ClientEventHandler::registerClientReloadListeners);
        bus.addListener(ClientEventHandler::registerEntityRenderers);
        bus.addListener(ClientEventHandler::registerLayerDefinitions);
        bus.addListener(ClientEventHandler::onTextureStitch);
    }

    public static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {

            // Screens
            MenuScreens.register(TFCFContainerTypes.EARTHENWARE_CLAY_KNAPPING.get(), KnappingScreen::new);
            MenuScreens.register(TFCFContainerTypes.KAOLINITE_CLAY_KNAPPING.get(), KnappingScreen::new);
            MenuScreens.register(TFCFContainerTypes.STONEWARE_CLAY_KNAPPING.get(), KnappingScreen::new);
            MenuScreens.register(TFCFContainerTypes.FLINT_KNAPPING.get(), KnappingScreen::new);
            MenuScreens.register(TFCFContainerTypes.LARGE_VESSEL.get(), LargeVesselScreen::new);
            MenuScreens.register(TFCFContainerTypes.BARREL.get(), TFCFBarrelScreen::new);
            MenuScreens.register(TFCFContainerTypes.ANVIL.get(), TFCFAnvilScreen::new);
            MenuScreens.register(TFCFContainerTypes.ANVIL_PLAN.get(), TFCFAnvilPlanScreen::new);

            TFCFBlocks.WOODS.values().forEach(map -> ItemProperties.register(map.get(BARREL).get().asItem(), Helpers.identifier("sealed"), (stack, level, entity, unused) -> stack.hasTag() ? 1.0f : 0f));
        });

        // Render Types
        final RenderType solid = RenderType.solid();
        final RenderType cutout = RenderType.cutout();
        final RenderType cutoutMipped = RenderType.cutoutMipped();
        final RenderType translucent = RenderType.translucent();

        TFCFItems.FLINT_TOOLS.values().forEach(tool -> {
            Item javelin = tool.get(RockCategory.ItemType.JAVELIN).get();
            ItemProperties.register(javelin, Helpers.identifier("throwing"), (stack, level, entity, unused) ->
                entity != null && ((entity.isUsingItem() && entity.getUseItem() == stack) || (entity instanceof Monster monster && monster.isAggressive())) ? 1.0F : 0.0F
            );
        });

        TFCFBlocks.WOODS.values().forEach(map -> {
            Stream.of(SAPLING, DOOR, TRAPDOOR, FENCE, FENCE_GATE, BUTTON, PRESSURE_PLATE, SLAB, STAIRS, TWIG, BARREL, SCRIBING_TABLE).forEach(type -> ItemBlockRenderTypes.setRenderLayer(map.get(type).get(), cutout));
            Stream.of(FALLEN_LEAVES).forEach(type -> ItemBlockRenderTypes.setRenderLayer(map.get(type).get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == solid));
        });

        TFCFBlocks.WOODS.forEach((key, value) -> {
            if (!key.isFruitTree() && !key.isMangrove())
            Stream.of(LEAVES).forEach(type -> ItemBlockRenderTypes.setRenderLayer(value.get(type).get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == cutoutMipped));
        });

        TFCFBlocks.LEAVES_ONLY.values().forEach(map -> {
            Stream.of(LEAVES).forEach(type -> ItemBlockRenderTypes.setRenderLayer(map.get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == cutoutMipped));
        });
        TFCFBlocks.WOODS_SEASONAL_LEAVES.values().forEach(map -> {
            Stream.of(LEAVES).forEach(type -> ItemBlockRenderTypes.setRenderLayer(map.get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == cutoutMipped));
        });
//        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.CHARRED_TREE_TWIG.get(), cutout);
        TFCFBlocks.MANGROVE_ROOTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        TFCFBlocks.CLAY_LARGE_VESSELS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        // Grasses and such
        TFCFBlocks.TFCSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        TFCFBlocks.TFCFSOIL.get(TFCFSoil.GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.SPARSE_BOG_IRON_GRASS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.DENSE_BOG_IRON_GRASS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.BOG_IRON_GRASS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.ROOTED_BOG_IRON.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.MYCELIUM_BOG_IRON.get(), cutout);

        TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.ROOTED_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.MYCELIUM_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));

        TFCFBlocks.JOSHUA_TRUNK.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.JOSHUA_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.WOODS_SEASONAL_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.ROOT_SPIKES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.PALM_TRUNKS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.PALM_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.PALM_FRUITS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFC_PALM_TRUNKS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFC_PALM_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.TFC_PALM_SAPLINGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        // Plants
        TFCFBlocks.PLANTS.forEach((key, reg) -> {
            if (key.isVine())
                ItemBlockRenderTypes.setRenderLayer(reg.get(), translucent);
            else
                ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout);
        });
        TFCFBlocks.FRUITING_PLANTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.POTTED_PLANTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));

        // Rock blocks
        TFCFBlocks.TFCF_ROCK_BLOCKS.values().forEach(map -> {
            ItemBlockRenderTypes.setRenderLayer(map.get(Rock.BlockType.SPIKE).get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(map.get(Rock.BlockType.AQUEDUCT).get(), cutout);
        });
        TFCFBlocks.ROCK_BLOCKS.values().forEach(map -> {
            ItemBlockRenderTypes.setRenderLayer(map.get(TFCFRock.TFCFBlockType.ROCK_PILE).get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(map.get(TFCFRock.TFCFBlockType.MOSSY_ROCK_PILE).get(), cutout);
        });
        TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.values().forEach(map -> {
            ItemBlockRenderTypes.setRenderLayer(map.get(TFCFRock.TFCFBlockType.ROCK_PILE).get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(map.get(TFCFRock.TFCFBlockType.MOSSY_ROCK_PILE).get(), cutout);
        });
        TFCFBlocks.DRIPSTONE_BLOCKS.values().forEach(map -> {
            ItemBlockRenderTypes.setRenderLayer(map.get(Rock.BlockType.SPIKE).get(), cutout);
        });
        TFCFBlocks.GEYSER_TFC.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.GEYSER_TFCF.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));

        TFCFBlocks.ORES.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));
        TFCFBlocks.GRADED_ORES.values().forEach(map -> map.values().forEach(inner -> inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout))));
        TFCFBlocks.ORE_DEPOSITS.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));

        TFCFBlocks.ORES_TFC_ROCK.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));
        TFCFBlocks.GRADED_ORES_TFC_ROCK.values().forEach(map -> map.values().forEach(inner -> inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout))));
        TFCFBlocks.ORES_TFCF_ROCK.values().forEach(map -> map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout)));
        TFCFBlocks.GRADED_ORES_TFCF_ROCK.values().forEach(map -> map.values().forEach(inner -> inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout))));

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.GLOWSTONE.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.GLOWSTONE_BUDDING.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.GLOWSTONE_CLUSTER.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.LARGE_GLOWSTONE_BUD.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.MEDIUM_GLOWSTONE_BUD.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.SMALL_GLOWSTONE_BUD.get(), cutout);

        TFCFBlocks.CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.BUDDING_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.CLUSTER_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.LARGE_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.MEDIUM_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.SMALL_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.SPIDER_EGG.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.SPIDER_EGGS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.LARGE_SPIDER_EGG.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.WEBBED_GLOW_BLOCK.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.WEBBED_TORCH_BLOCK.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.WEBBED_CHEST.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.CREEPING_WEBS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.HANGING_SPIDER_WEB_SLENDER.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.HANGING_SPIDER_WEB_THICK.get(), cutout);

        TFCFBlocks.SPARSE_SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.DENSE_SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        TFCFBlocks.BAMBOO_LOGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.STRIPPED_BAMBOO_LOGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
        TFCFBlocks.BAMBOO_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));
        TFCFBlocks.BAMBOO_SAPLINGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutoutMipped));

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.VANILLA_BAMBOO_LOGS.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.VANILLA_STRIPPED_BAMBOO_LOGS.get(), cutout);

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.MINERAL_SHEET.get(), cutout);

        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.SILKMOTH_NEST.get(), cutout);
    
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.OCHRE_FROGLIGHT.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.VERDANT_FROGLIGHT.get(), cutout);
        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.PEARLESCENT_FROGLIGHT.get(), cutout);
//        ItemBlockRenderTypes.setRenderLayer(TFCFBlocks.FROGSPAWN.get(), cutout);

        TFCFBlocks.GROUNDCOVER.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout));
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        // Entities
        for (TFCFWood wood : TFCFWood.VALUES)
        {
            event.registerEntityRenderer(TFCFEntities.BOATS.get(wood).get(), ctx -> new TFCFBoatRenderer(ctx, wood.getSerializedName(), wood == TFCFWood.BAMBOO ? true : false));
        }

        event.registerEntityRenderer(TFCFEntities.SILKMOTH.get(), ctx -> new TFCFSimpleMobRenderer.Builder<>(ctx, SilkmothModel::new, "silk_moth").texture(Silkmoth::getTextureLocation).build());
        event.registerEntityRenderer(TFCFEntities.FROG.get(), FrogRenderer::new);
        event.registerEntityRenderer(TFCFEntities.TADPOLE.get(), TadpoleRenderer::new);
        event.registerEntityRenderer(TFCFEntities.PARROT.get(), TFCFParrotRenderer::new);
        event.registerEntityRenderer(TFCFEntities.FRESHWATER_FISH.get(Fish.LARGEMOUTH_BASS).get(), ctx -> new SalmonLikeRenderer(ctx, "largemouth_bass"));
        event.registerEntityRenderer(TFCFEntities.FRESHWATER_FISH.get(Fish.SMALLMOUTH_BASS).get(), ctx -> new SalmonLikeRenderer(ctx, "smallmouth_bass"));
        event.registerEntityRenderer(TFCFEntities.FRESHWATER_FISH.get(Fish.LAKE_TROUT).get(), ctx -> new SalmonLikeRenderer(ctx, "lake_trout"));
        event.registerEntityRenderer(TFCFEntities.FRESHWATER_FISH.get(Fish.RAINBOW_TROUT).get(), ctx -> new SalmonLikeRenderer(ctx, "rainbow_trout"));
        event.registerEntityRenderer(TFCFEntities.FRESHWATER_COD_FISH.get(Fish.CRAPPIE).get(), ctx -> new CodLikeRenderer(ctx, "crappie"));
        //event.registerEntityRenderer(TFCFEntities.FRESHWATER_FISH.get(Fish.CRAPPIE).get(), ctx -> new TFCFSimpleMobRenderer.Builder<>(ctx, CodModel::new, "crappie").flops().build());

        // BEs
        event.registerBlockEntityRenderer(TFCFBlockEntities.CHEST.get(), TFCFChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCFBlockEntities.TRAPPED_CHEST.get(), TFCFChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCFBlockEntities.SIGN.get(), TFCFSignBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCFBlockEntities.LECTERN.get(), LecternRenderer::new);
        event.registerBlockEntityRenderer(TFCFBlockEntities.ANVIL.get(), ctx -> new AnvilBlockEntityRenderer());
        event.registerBlockEntityRenderer(TFCFBlockEntities.BARREL.get(), ctx -> new BarrelBlockEntityRenderer());
        event.registerBlockEntityRenderer(TFCFBlockEntities.SLUICE.get(), ctx -> new SluiceBlockEntityRenderer());
        event.registerBlockEntityRenderer(TFCFBlockEntities.TOOL_RACK.get(), ctx -> new TFCFToolRackBlockEntityRenderer());
        event.registerBlockEntityRenderer(TFCFBlockEntities.MINERAL_SHEET.get(), ctx -> new MineralSheetBlockEntityRenderer());
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        LayerDefinition boatLayer = BoatModel.createBodyModel();
        LayerDefinition raftLayer = RaftModel.createBodyModel();
        LayerDefinition signLayer = SignRenderer.createSignLayer();
        for (TFCFWood wood : TFCFWood.VALUES)
        {
            event.registerLayerDefinition(TFCFBoatRenderer.boatName(wood.getSerializedName()), wood == TFCFWood.BAMBOO ? () -> raftLayer : () -> boatLayer);
            event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("sign/" + wood.name().toLowerCase(Locale.ROOT)), () -> signLayer);
        }
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("silk_moth"), SilkmothModel::createBodyLayer);
        event.registerLayerDefinition(FrogRenderer.MODEL_LAYER, FrogModel::createBodyLayer);
        event.registerLayerDefinition(TadpoleRenderer.MODEL_LAYER, TadpoleModel::createBodyLayer);
        event.registerLayerDefinition(TFCFParrotRenderer.MODEL_LAYER, TFCFParrotModel::createBodyLayer);
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("largemouth_bass"), SalmonModel::createBodyLayer);
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("smallmouth_bass"), SalmonModel::createBodyLayer);
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("lake_trout"), SalmonModel::createBodyLayer);
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("rainbow_trout"), SalmonModel::createBodyLayer);
        event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("crappie"), CodModel::createBodyLayer);
    }

    public static void onConfigReload(ModConfigEvent.Reloading event)
    {
    }

    public static void registerModelLoaders(ModelEvent.RegisterAdditional event)
    {
        for (TFCFWood wood : TFCFWood.VALUES)
        {
            if (wood.isPalmTree())
            {
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_1"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_2"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_corner_1"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_corner_2"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_top_1"));
            }
        }
        for (Wood wood : Wood.VALUES)
        {
            if (wood == Wood.PALM)
            {
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_1"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_2"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_corner_1"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_corner_2"));
                event.register(TFCFHelpers.identifier("block/wood/leaves/" + wood.getSerializedName() + "_leaves_top_1"));
            }
        }
    }

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.Block event)
    {
        final BlockColors registry = event.getBlockColors();
        final BlockColor grassColor = (state, worldIn, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final BlockColor tallGrassColor = (state, worldIn, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final BlockColor foliageColor = (state, worldIn, pos, tintIndex) -> TFCColors.getFoliageColor(pos, tintIndex);
        final BlockColor seasonalFoliageColor = (state, worldIn, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, 0);

        //TFCFBlocks.TFCSOIL.get(TFCFSoil.GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> registry.register(tallGrassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        //TFCFBlocks.TFCSOIL.get(TFCFSoil.CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));

        TFCFBlocks.TFCFSOIL.get(TFCFSoil.GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> registry.register(tallGrassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_KAOLINITE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));
        TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, reg.get()));

        registry.register(grassColor, TFCFBlocks.SPARSE_BOG_IRON_GRASS.get());
        registry.register(grassColor, TFCFBlocks.DENSE_BOG_IRON_GRASS.get());
        registry.register(grassColor, TFCFBlocks.BOG_IRON_GRASS.get());

        TFCFBlocks.WOODS.forEach((wood, reg) -> {
            if (!wood.isFruitTree() && !wood.isMangrove())
                registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(Wood.BlockType.LEAVES).get(), reg.get(Wood.BlockType.FALLEN_LEAVES).get());
            else
                registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(Wood.BlockType.FALLEN_LEAVES).get());
        });
        TFCFBlocks.LEAVES_ONLY.forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(), reg.get()));
        TFCFBlocks.JOSHUA_LEAVES.forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(), reg.get()));
        TFCFBlocks.WOODS_SEASONAL_LEAVES.forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(), reg.get()));
        TFCFBlocks.PALM_LEAVES.forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(), reg.get()));
        TFCFBlocks.TFC_PALM_LEAVES.forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, reg.get(), reg.get()));

        // Plants
        TFCFBlocks.PLANTS.forEach((plant, reg) -> registry.register(plant.isConifer() ? foliageColor : plant.isTallGrass() ? tallGrassColor : plant.isSeasonal() ? seasonalFoliageColor : plant.isFoliage() ? foliageColor : grassColor, reg.get()));
        TFCFBlocks.FRUITING_PLANTS.forEach((plant, reg) -> registry.register(plant.isConifer() ? foliageColor : plant.isTallGrass() ? tallGrassColor : plant.isSeasonal() ? seasonalFoliageColor : plant.isFoliage() ? foliageColor : grassColor, reg.get()));
        TFCFBlocks.POTTED_PLANTS.forEach((plant, reg) -> registry.register(grassColor, reg.get()));

        TFCFBlocks.SPARSE_SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, reg.get()));
        TFCFBlocks.DENSE_SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, reg.get()));
        TFCFBlocks.SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, reg.get()));

        TFCFBlocks.BAMBOO_LEAVES.forEach((plant, reg) -> registry.register(seasonalFoliageColor, reg.get()));
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event)
    {
        final ItemColors registry = event.getItemColors();
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);
        final ItemColor foliageColor = (stack, tintIndex) -> TFCColors.getFoliageColor(null, tintIndex);
        final ItemColor seasonalFoliageColor = (stack, tintIndex) -> TFCColors.getSeasonalFoliageColor(null, tintIndex, 0);

        TFCFBlocks.WOODS.forEach((wood, value) -> {
            if (value.get(FALLEN_LEAVES).get() != null)
            {
                registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, value.get(FALLEN_LEAVES).get());
            }
            if (!wood.isFruitTree() && value.get(LEAVES).get() != null)
            {
                registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, value.get(LEAVES).get());
            }
        });

        TFCFBlocks.LEAVES_ONLY.forEach((key, value) -> registry.register(seasonalFoliageColor, value.get(), value.get()));
        TFCFBlocks.JOSHUA_LEAVES.forEach((key, value) -> registry.register(seasonalFoliageColor, value.get(), value.get()));
        TFCFBlocks.WOODS_SEASONAL_LEAVES.forEach((key, value) -> registry.register(seasonalFoliageColor, value.get(), value.get()));

        // Plants
        TFCFBlocks.PLANTS.forEach((plant, reg) -> {
            if (plant.isItemTinted())
                registry.register(plant.isConifer() ? seasonalFoliageColor : plant.isSeasonal() ? seasonalFoliageColor : grassColor, reg.get());
        });
        TFCFBlocks.FRUITING_PLANTS.forEach((plant, reg) -> {
            if (plant.isItemTinted())
                registry.register(plant.isConifer() ? seasonalFoliageColor : plant.isSeasonal() ? seasonalFoliageColor : grassColor, reg.get());
        });

        TFCFBlocks.BAMBOO_LEAVES.forEach((plant, reg) -> registry.register(seasonalFoliageColor, reg.get()));
    }

    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event)
    {
        // Colormaps
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(TFCFParticles.WATER_FLOW.get(), WaterFlowParticle.Provider::new);
        particleEngine.register(TFCFParticles.FALLING_LEAF.get(), set -> new FallingLeafParticle.Provider(set, true));
        particleEngine.register(TFCFParticles.FALLING_SPORE.get(), set -> new SporeParticle.Provider(set, true));
    }

    public static void onTextureStitch(TextureStitchEvent.Post event)
    {
        final ResourceLocation texture = event.getAtlas().location();
      /*  if (texture.equals(TFCFRenderHelpers.BLOCKS_ATLAS))
        {
            for (Mineral mineral : Mineral.values())
            {
                event.addSprite(TFCFHelpers.identifier("block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_0"));
                event.addSprite(TFCFHelpers.identifier("block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_1"));
                event.addSprite(TFCFHelpers.identifier("block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_2"));
                event.addSprite(TFCFHelpers.identifier("block/mineral/" + mineral.name().toLowerCase(Locale.ROOT) + "_3"));
            }
        }
        else if (texture.equals(Sheets.CHEST_SHEET)*//* && hasLeavesOnly()*//*)
        {
            Arrays.stream(TFCFWood.VALUES).map(TFCFWood::getSerializedName).forEach(name -> {
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_right/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_right/" + name));
            });
            Arrays.stream(Rock.VALUES).map(Rock::getSerializedName).forEach(name -> {
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_right/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_right/" + name));
            });
            Arrays.stream(TFCFRock.VALUES).map(TFCFRock::getSerializedName).forEach(name -> {
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/normal_right/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_left/" + name));
                event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_right/" + name));
            });
            event.addSprite(TFCFHelpers.identifier("entity/chest/normal/rock"));
            event.addSprite(TFCFHelpers.identifier("entity/chest/normal_left/rock"));
            event.addSprite(TFCFHelpers.identifier("entity/chest/normal_right/rock"));
            event.addSprite(TFCFHelpers.identifier("entity/chest/trapped/rock"));
            event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_left/rock"));
            event.addSprite(TFCFHelpers.identifier("entity/chest/trapped_right/rock"));
        }
        else if (texture.equals(Sheets.SIGN_SHEET)/* && hasLeavesOnly()*//*)
        {
            Arrays.stream(TFCFWood.VALUES).map(TFCFWood::getSerializedName).forEach(name -> event.addSprite(TFCFHelpers.identifier("entity/signs/" + name)));
        }*/
    }
}
