package tfcflorae;

import com.mojang.logging.LogUtils;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.LooseRockBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.LooseFlintBlock;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.blocks.wood.TFCFSaplingBlock;
import tfcflorae.common.commands.TFCFCommands;
import tfcflorae.common.items.TFCFItems;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public class TFCFForgeEventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        IEventBus busMod = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(TFCFForgeEventHandler::onItemExpireEvent);
        bus.addListener(TFCFForgeEventHandler::onPlayerRightClickBlock);
        bus.addListener(TFCFForgeEventHandler::registerCommands);
//        busMod.addListener(TFCFForgeEventHandler::onPackFinder);
    }

/* todo   public static void onPackFinder(AddPackFindersEvent event)
    {
        try
        {
            if (event.getPackType() == PackType.CLIENT_RESOURCES)
            {
                var modFile = ModList.get().getModFileById(TFCFlorae.MOD_ID).getFile();
                var resourcePath = modFile.getFilePath();
                var pack = new PathResourcePack(modFile.getFileName() + ":overload", resourcePath)
                {
                    @Nonnull
                    @Override
                    protected Path resolve(@Nonnull String... paths)
                    {
                        return modFile.findResource(paths);
                    }
                };
                var metadata = pack.getMetadataSection(PackMetadataSection.SERIALIZER);
                if (metadata != null)
                {
                    TFCFlorae.LOGGER.info("Injecting Florae override pack");
                    event.addRepositorySource((consumer, constructor) ->
                        consumer.accept(constructor.create("builtin/tfcflorae_data", Component.literal("TFC Florae Resources"), true, () -> pack, metadata, Pack.Position.TOP, PackSource.BUILT_IN, false))
                    );
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }*/

    public static void registerCommands(RegisterCommandsEvent event)
    {
        LOGGER.debug("Registering TFC Florae Commands");
        TFCFCommands.registerCommands(event.getDispatcher());
    }

    public static void onItemExpireEvent(ItemExpireEvent event)
    {
        if (!event.getEntity().isAlive()) return;

        ItemEntity item = event.getEntity();
        ItemStack itemStack = item.getItem();
        Level level = item.level();
        BlockPos pos = item.blockPosition();

        if (level.isEmptyBlock(pos) || Helpers.isBlock(level.getBlockState(pos), TFCFTags.Blocks.SAPLING_CAN_REPLACE))
        {
            if (Helpers.isItem(itemStack, TFCFTags.Items.SAPLINGS) || Block.byItem(itemStack.getItem()) instanceof TFCFSaplingBlock || Block.byItem(itemStack.getItem()) instanceof TFCSaplingBlock)
            {
                Block block = Block.byItem(itemStack.getItem());
                if (block != null && block != Blocks.AIR && block.defaultBlockState().canSurvive((LevelReader)level, pos) && level.setBlockAndUpdate(pos, block.defaultBlockState()))
                {
                    itemStack.shrink(1);
                    event.getEntity().setItem(itemStack);
                    if (event.getEntity().getItem().isEmpty())
                    {
                        event.getEntity().remove(Entity.RemovalReason.KILLED);
                    }
                    else
                    {
                        event.setExtraLife(3000);
                        event.setCanceled(true);
                    } 
                } 
            } 
        }
    }

    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        final Level level = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState state = level.getBlockState(pos);
        final Block block = state.getBlock();

        if (level.getBlockState(pos).isSolid() && (EnvironmentHelpers.isWorldgenReplaceable(level.getBlockState(pos.above())) || level.getBlockState(pos.above()).isAir()) && (level.getBlockState(pos.above()).getBlock() != TFCFBlocks.LOOSE_FLINT.get() || !(level.getBlockState(pos.above()).getBlock() instanceof LooseRockBlock)) && TFCFBlocks.LOOSE_FLINT.get().defaultBlockState().canSurvive(level, pos.above()))
        {
            if (event.getHand() == InteractionHand.MAIN_HAND && (Helpers.isItem(event.getItemStack(), TFCFTags.Items.FLINT_KNAPPING) || event.getItemStack().getItem() == Items.FLINT || event.getItemStack().getItem() == TFCFBlocks.LOOSE_FLINT.get().asItem()))
            {
                event.getItemStack().shrink(1);
                //event.getPlayer().playSound(SoundType.STONE.getPlaceSound(), 1f, 1f);
                Helpers.playSound(level, pos.above(), SoundType.STONE.getPlaceSound());
                level.setBlockAndUpdate(pos.above(), TFCFBlocks.LOOSE_FLINT.get().defaultBlockState().setValue(LooseFlintBlock.COUNT, 1));
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }

        for (SoilBlockType.Variant variant : SoilBlockType.Variant.values())
        {
            // Mud --> Compact Mud
            if (block == TFCBlocks.SOIL.get(SoilBlockType.MUD).get(variant).get())
            {
                if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().canPerformAction(ToolActions.SHOVEL_FLATTEN))
                {
                    level.setBlockAndUpdate(pos, TFCFBlocks.TFCSOIL.get(TFCFSoil.PACKED_MUD).get(variant).get().defaultBlockState());
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
            // Soil --> Compact Soil
            if (block == TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(variant).get())
            {
                if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().canPerformAction(ToolActions.SHOVEL_FLATTEN))
                {
                    level.setBlockAndUpdate(pos, TFCFBlocks.TFCSOIL.get(TFCFSoil.COMPACT_DIRT).get(variant).get().defaultBlockState());
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
        for (TFCFSoil.TFCFVariant variant : TFCFSoil.TFCFVariant.values())
        {
            // Soil --> Compact Soil
            if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant).get())
            {
                if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().canPerformAction(ToolActions.SHOVEL_FLATTEN))
                {
                    level.setBlockAndUpdate(pos, TFCFBlocks.TFCFSOIL.get(TFCFSoil.COMPACT_DIRT).get(variant).get().defaultBlockState());
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }

        // Soil --> Compact Soil
        for (Rock rockTFC : Rock.values())
        {
            // Soil
            for (SoilBlockType.Variant variant : SoilBlockType.Variant.values())
            {
                if (block == TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(variant).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCBlocks.ROCK_BLOCKS.get(rockTFC).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCROCKSOIL.get(TFCFRockSoil.PEBBLE_COMPACT_DIRT).get(variant).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                if (block == TFCFBlocks.ROCK_BLOCKS.get(rockTFC).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFItems.SOIL_PILE_TFC.get(variant).get()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCROCKSOIL.get(TFCFRockSoil.DIRTY_STONE_TILES).get(variant).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }
            for (TFCFSoil.TFCFVariant variant : TFCFSoil.TFCFVariant.values())
            {
                if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCBlocks.ROCK_BLOCKS.get(rockTFC).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCFROCKSOIL.get(TFCFRockSoil.PEBBLE_COMPACT_DIRT).get(variant).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                if (block == TFCFBlocks.ROCK_BLOCKS.get(rockTFC).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFItems.SOIL_PILE_TFCF.get(variant).get()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCFROCKSOIL.get(TFCFRockSoil.DIRTY_STONE_TILES).get(variant).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }

            // Sand
            for (Colors sandColor : Colors.values())
            {
                if (block == TFCBlocks.SAND.get(sandColor.toSandTFC(true)).get() || block == TFCFBlocks.SAND.get(sandColor.nonTFC()).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCBlocks.ROCK_BLOCKS.get(rockTFC).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.ROCKY_SAND_TFC.get(TFCFRockSand.PEBBLE).get(sandColor).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                else if (block == TFCFBlocks.ROCK_BLOCKS.get(rockTFC).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFBlocks.SAND_LAYERS.get(sandColor).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.ROCKY_SAND_TFC.get(TFCFRockSand.SANDY_TILES).get(sandColor).get(rockTFC).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }
        }
        for (TFCFRock rockTFCF : TFCFRock.values())
        {
            for (SoilBlockType.Variant variant : SoilBlockType.Variant.values())
            {
                if (block == TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(variant).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCROCKSOIL2.get(TFCFRockSoil.PEBBLE_COMPACT_DIRT).get(variant).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                if (block == TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rockTFCF).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFItems.SOIL_PILE_TFC.get(variant).get()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCROCKSOIL2.get(TFCFRockSoil.DIRTY_STONE_TILES).get(variant).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }
            for (TFCFSoil.TFCFVariant variant : TFCFSoil.TFCFVariant.values())
            {
                if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCFROCKSOIL2.get(TFCFRockSoil.PEBBLE_COMPACT_DIRT).get(variant).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                if (block == TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rockTFCF).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFItems.SOIL_PILE_TFCF.get(variant).get()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.TFCFROCKSOIL2.get(TFCFRockSoil.DIRTY_STONE_TILES).get(variant).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }

            // Sand
            for (Colors sandColor : Colors.values())
            {
                if (block == TFCBlocks.SAND.get(sandColor.toSandTFC(true)).get() || block == TFCFBlocks.SAND.get(sandColor.nonTFC()).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(Rock.BlockType.LOOSE).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.ROCKY_SAND_TFCF.get(TFCFRockSand.PEBBLE).get(sandColor).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
                else if (block == TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rockTFCF).get(TFCFRock.TFCFBlockType.STONE_TILES).get())
                {
                    if (Helpers.isItem(event.getItemStack(), TFCFBlocks.SAND_LAYERS.get(sandColor).get().asItem()))
                    {
                        event.getItemStack().shrink(1);
                        final BlockState placedBlock = TFCFBlocks.ROCKY_SAND_TFCF.get(TFCFRockSand.SANDY_TILES).get(sandColor).get(rockTFCF).get().defaultBlockState();
                        level.setBlockAndUpdate(pos, placedBlock);
                        Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        break;
                    }
                }
            }
        }
        event.setCancellationResult(InteractionResult.PASS);
    }
}
