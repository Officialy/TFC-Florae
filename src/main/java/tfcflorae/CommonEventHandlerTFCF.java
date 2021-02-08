package tfcflorae;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.GameRuleChangeEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.dries007.tfc.ConfigTFC;
import net.dries007.tfc.Constants;
import net.dries007.tfc.api.capability.damage.CapabilityDamageResistance;
import net.dries007.tfc.api.capability.damage.DamageType;
import net.dries007.tfc.api.capability.egg.CapabilityEgg;
import net.dries007.tfc.api.capability.egg.EggHandler;
import net.dries007.tfc.api.capability.food.*;
import net.dries007.tfc.api.capability.forge.CapabilityForgeable;
import net.dries007.tfc.api.capability.forge.ForgeableHeatableHandler;
import net.dries007.tfc.api.capability.heat.CapabilityItemHeat;
import net.dries007.tfc.api.capability.heat.IItemHeat;
import net.dries007.tfc.api.capability.metal.CapabilityMetalItem;
import net.dries007.tfc.api.capability.metal.IMetalItem;
import net.dries007.tfc.api.capability.player.CapabilityPlayerData;
import net.dries007.tfc.api.capability.player.IPlayerData;
import net.dries007.tfc.api.capability.player.PlayerDataHandler;
import net.dries007.tfc.api.capability.size.CapabilityItemSize;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.capability.worldtracker.CapabilityWorldTracker;
import net.dries007.tfc.api.capability.worldtracker.WorldTracker;
import net.dries007.tfc.api.types.*;
import net.dries007.tfc.network.PacketCalendarUpdate;
import net.dries007.tfc.network.PacketPlayerDataUpdate;
import net.dries007.tfc.objects.blocks.BlockFluidTFC;
import net.dries007.tfc.objects.blocks.BlocksTFC;
import net.dries007.tfc.objects.blocks.agriculture.BlockFruitTreeLeaves;
import net.dries007.tfc.objects.blocks.agriculture.BlockFruitTreeTrunk;
import net.dries007.tfc.objects.blocks.devices.BlockQuern;
import net.dries007.tfc.objects.blocks.metal.BlockAnvilTFC;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.dries007.tfc.objects.blocks.stone.BlockRockVariant;
import net.dries007.tfc.objects.blocks.stone.BlockStoneAnvil;
import net.dries007.tfc.objects.blocks.wood.BlockLogTFC;
import net.dries007.tfc.objects.blocks.wood.BlockSupport;
import net.dries007.tfc.objects.container.CapabilityContainerListener;
import net.dries007.tfc.objects.entity.animal.EntityAnimalTFC;
import net.dries007.tfc.objects.fluids.FluidsTFC;
import net.dries007.tfc.objects.items.ItemMisc;
import net.dries007.tfc.objects.items.ItemQuiver;
import net.dries007.tfc.objects.items.food.ItemFoodTFC;
import net.dries007.tfc.objects.potioneffects.PotionEffectsTFC;
import net.dries007.tfc.util.DamageSourcesTFC;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.MonsterEquipment;
import net.dries007.tfc.util.calendar.CalendarTFC;
import net.dries007.tfc.util.calendar.CalendarWorldData;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.ClimateTFC;
import net.dries007.tfc.util.skills.SmithingSkill;
import net.dries007.tfc.world.classic.WorldTypeTFC;
import net.dries007.tfc.world.classic.chunkdata.ChunkDataTFC;

import tfcflorae.objects.items.ItemsTFCF;
import tfcflorae.types.BlockTypesTFCF.RockTFCF;
import tfcflorae.objects.blocks.BlocksTFCF;
import tfcflorae.objects.blocks.blocktype.BlockRockVariantTFCF;

import static tfcflorae.TFCFlorae.MODID;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MODID)
public final class CommonEventHandlerTFCF
{
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event)
    {
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        if (!stack.isEmpty());
    }
    
    @SubscribeEvent
    public static void onBlockHarvestDrops(BlockEvent.HarvestDropsEvent event)
    {
        final EntityPlayer player = event.getHarvester();
        final ItemStack held = player == null ? ItemStack.EMPTY : player.getHeldItemMainhand();
        final IBlockState state = event.getState();
        final Block block = state.getBlock();

        if (block instanceof BlockFruitTreeLeaves)
        {
            event.getDrops().add(new ItemStack(ItemsTFCF.FRUIT_LEAF, 2 + Constants.RNG.nextInt(4)));
        }
        else if (block instanceof BlockFruitTreeTrunk)
        {
            IFruitTree tree = ((BlockFruitTreeTrunk) block).getTree();
            String poleName = MODID + "wood/fruit_tree/pole" + tree.getName().toLowerCase();
            Item pole = ItemMisc.getByNameOrId(poleName);
            if (pole != null)
                event.getDrops().add(new ItemStack(pole));
        }
    }

    @SubscribeEvent
    public static void onBreakProgressEvent(BreakSpeed event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null)
        {
            ItemStack stack = player.getHeldItemMainhand();
            float skillModifier = SmithingSkill.getSkillBonus(stack, SmithingSkill.Type.TOOLS);
            if (skillModifier > 0)
            {
                // Up to 2x modifier for break speed for skill bonuses on tools
                // New speed, so it will take into account other mods' modifications
                event.setNewSpeed(event.getNewSpeed() + (event.getNewSpeed() * skillModifier));
            }
        }
        if (event.getState().getBlock() instanceof BlockRockVariantTFCF)
        {
            event.setNewSpeed((float) (event.getNewSpeed() / ConfigTFC.General.MISC.rockMiningTimeModifier));
        }
    }

    @SubscribeEvent
    public static void onUseHoe(UseHoeEvent event)
    {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);

        if (ConfigTFC.General.OVERRIDES.enableHoeing)
        {
            if (state.getBlock() instanceof BlockRockVariantTFCF)
            {
                BlockRockVariantTFCF blockRock = (BlockRockVariantTFCF) state.getBlock();
                if 
                    (
                        blockRock.getType() == RockTFCF.COARSE_DIRT || 
                        blockRock.getType() == RockTFCF.LOAMY_SAND || 
                        blockRock.getType() == RockTFCF.SANDY_LOAM || 
                        blockRock.getType() == RockTFCF.LOAM || 
                        blockRock.getType() == RockTFCF.SILT_LOAM || 
                        blockRock.getType() == RockTFCF.SILT || 
                        blockRock.getType() == RockTFCF.COARSE_LOAMY_SAND || 
                        blockRock.getType() == RockTFCF.COARSE_SANDY_LOAM || 
                        blockRock.getType() == RockTFCF.COARSE_LOAM || 
                        blockRock.getType() == RockTFCF.COARSE_SILT_LOAM || 
                        blockRock.getType() == RockTFCF.COARSE_SILT || 
                        blockRock.getType() == RockTFCF.PODZOL || 
                        blockRock.getType() == RockTFCF.LOAMY_SAND_GRASS || 
                        blockRock.getType() == RockTFCF.LOAMY_SAND_PODZOL || 
                        blockRock.getType() == RockTFCF.SANDY_LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.SANDY_LOAM_PODZOL || 
                        blockRock.getType() == RockTFCF.LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.LOAM_PODZOL || 
                        blockRock.getType() == RockTFCF.SILT_LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.SILT_LOAM_PODZOL || 
                        blockRock.getType() == RockTFCF.SILT_GRASS || 
                        blockRock.getType() == RockTFCF.SILT_PODZOL || 
                        blockRock.getType() == RockTFCF.DRY_LOAMY_SAND_GRASS || 
                        blockRock.getType() == RockTFCF.DRY_SANDY_LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.DRY_LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.DRY_SILT_LOAM_GRASS || 
                        blockRock.getType() == RockTFCF.DRY_SILT_GRASS
                    )
                {
                    if (!world.isRemote)
                    {
                        world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.setBlockState(pos, BlockRockVariant.get(blockRock.getRock(), Rock.Type.FARMLAND).getDefaultState());
                    }
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event)
    {
        if (event.getEntityPlayer() instanceof EntityPlayerMP)
        {
            // Capability Sync Handler
            CapabilityContainerListener.addTo(event.getContainer(), (EntityPlayerMP) event.getEntityPlayer());
        }
    }
}