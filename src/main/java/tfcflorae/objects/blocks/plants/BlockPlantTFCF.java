package tfcflorae.objects.blocks.plants;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;

import net.dries007.tfc.ConfigTFC;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.objects.blocks.BlocksTFC;
import net.dries007.tfc.util.calendar.CalendarTFC;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.ClimateTFC;
import net.dries007.tfc.world.classic.chunkdata.ChunkDataTFC;

import tfcflorae.api.types.PlantTFCF;
import tfcflorae.objects.blocks.BlocksTFCF;
import tfcflorae.util.OreDictionaryHelper;

@ParametersAreNonnullByDefault
public class BlockPlantTFCF extends BlockBush implements IItemSize
{
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    /*
     * Time of day, used for rendering plants that bloom at different times
     * 0 = midnight-dawn
     * 1 = dawn-noon
     * 2 = noon-dusk
     * 3 = dusk-midnight
     */
    public final static PropertyInteger DAYPERIOD = PropertyInteger.create("dayperiod", 0, 3);
    private static final AxisAlignedBB PLANT_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    private static final Map<PlantTFCF, BlockPlantTFCF> MAP = new HashMap<>();

    public static BlockPlantTFCF get(PlantTFCF plantTFCF)
    {
        return MAP.get(plantTFCF);
    }

    /* Growth Stage of the plant, tied to the month of year */
    public final PropertyInteger growthStageProperty;
    protected final PlantTFCF plantTFCF;
    protected final BlockStateContainer blockState;

    public BlockPlantTFCF(PlantTFCF plantTFCF)
    {
        super(plantTFCF.getMaterial());
        if (MAP.put(plantTFCF, this) != null) throw new IllegalStateException("There can only be one.");

        plantTFCF.getOreDictName().ifPresent(name -> OreDictionaryHelper.register(this, name));

        this.plantTFCF = plantTFCF;
        this.growthStageProperty = PropertyInteger.create("stage", 0, plantTFCF.getNumStages());
        this.setTickRandomly(true);
        setSoundType(SoundType.PLANT);
        setHardness(0.0F);
        Blocks.FIRE.setFireInfo(this, 5, 20);
        blockState = this.createPlantBlockState();
        this.setDefaultState(this.blockState.getBaseState());
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(AGE);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(DAYPERIOD, getDayPeriod()).withProperty(growthStageProperty, plantTFCF.getStageForMonth());
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        if (!worldIn.isAreaLoaded(pos, 1)) return;
        Month currentMonth = CalendarTFC.CALENDAR_TIME.getMonthOfYear();
        int currentStage = state.getValue(growthStageProperty);
        int expectedStage = plantTFCF.getStageForMonth(currentMonth);
        int currentTime = state.getValue(DAYPERIOD);
        int expectedTime = getDayPeriod();

        if (currentTime != expectedTime)
        {
            worldIn.setBlockState(pos, state.withProperty(DAYPERIOD, expectedTime).withProperty(growthStageProperty, currentStage));
        }
        if (currentStage != expectedStage && random.nextDouble() < 0.5)
        {
            worldIn.setBlockState(pos, state.withProperty(DAYPERIOD, expectedTime).withProperty(growthStageProperty, expectedStage));
        }

        this.updateTick(worldIn, pos, state, random);
    }

    @Override
    public int tickRate(World worldIn)
    {
        return 10;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        world.setBlockState(pos, state.withProperty(DAYPERIOD, getDayPeriod()).withProperty(growthStageProperty, plantTFCF.getStageForMonth()));
        checkAndDropBlock(world, pos, state);
    }

    @Override
    @Nonnull
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        if (!plantTFCF.getOreDictName().isPresent())
        {
            return Items.AIR;
        }
        return Item.getItemFromBlock(this);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        // Entity X/Z motion is reduced by plants. Affine combination of age modifier and actual modifier
        if (!(entityIn instanceof EntityPlayer && ((EntityPlayer) entityIn).isCreative()))
        {
            double modifier = 0.25 * (4 - state.getValue(AGE));
            modifier = (1 - modifier) * plantTFCF.getMovementMod() + modifier;
            if (modifier < ConfigTFC.General.MISC.minimumPlantMovementModifier)
            {
                modifier = ConfigTFC.General.MISC.minimumPlantMovementModifier;
            }
            entityIn.motionX *= modifier;
            entityIn.motionZ *= modifier;
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        if (!plantTFCF.getOreDictName().isPresent() && !worldIn.isRemote && (stack.getItem().getHarvestLevel(stack, "knife", player, state) != -1 || stack.getItem().getHarvestLevel(stack, "scythe", player, state) != -1))
        {
            spawnAsEntity(worldIn, pos, new ItemStack(this, 1));
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (!canBlockStay(worldIn, pos, state) && placer instanceof EntityPlayer)
        {
            if (!((EntityPlayer) placer).isCreative() && !plantTFCF.getOreDictName().isPresent())
            {
                spawnAsEntity(worldIn, pos, new ItemStack(this));
            }
        }
    }

    @Nonnull
    @Override
    public BlockStateContainer getBlockState()
    {
        return this.blockState;
    }

    @Override
    @Nonnull
    public Block.EnumOffsetType getOffsetType()
    {
        return Block.EnumOffsetType.XYZ;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = player.getHeldItemMainhand();
        IBlockState state = world.getBlockState(pos);
        switch (plantTFCF.getPlantType())
        {
            default:
                return true;
        }
    }

    public PlantTFCF getPlant()
    {
        return plantTFCF;
    }

    @Nonnull
    @Override
    public Size getSize(ItemStack stack)
    {
        return Size.TINY; // Store anywhere
    }

    @Nonnull
    @Override
    public Weight getWeight(ItemStack stack)
    {
        return Weight.VERY_LIGHT; // Stacksize = 64
    }

    public double getGrowthRate(World world, BlockPos pos)
    {
        if (world.isRainingAt(pos)) return ConfigTFC.General.MISC.plantGrowthRate * 5d;
        else return ConfigTFC.General.MISC.plantGrowthRate;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        IBlockState soil = worldIn.getBlockState(pos.down());
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && worldIn.getBlockState(pos).getBlock() != this && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this);
    }

    @Override
    protected boolean canSustainBush(IBlockState state)
    {
        if (plantTFCF.getIsClayMarking()) return BlocksTFC.isClay(state) || isValidSoil(state);
        else return isValidSoil(state);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isAreaLoaded(pos, 1)) return;

        if (plantTFCF.isValidGrowthTemp(ClimateTFC.getActualTemp(worldIn, pos)) && plantTFCF.isValidSunlight(Math.subtractExact(worldIn.getLightFor(EnumSkyBlock.SKY, pos), worldIn.getSkylightSubtracted())))
        {
            int j = state.getValue(AGE);

            if (rand.nextDouble() < getGrowthRate(worldIn, pos) && ForgeHooks.onCropsGrowPre(worldIn, pos.up(), state, true))
            {
                if (j < 3)
                {
                    worldIn.setBlockState(pos, state.withProperty(AGE, j + 1));
                }
                ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
        }
        else if (!plantTFCF.isValidGrowthTemp(ClimateTFC.getActualTemp(worldIn, pos)) || !plantTFCF.isValidSunlight(worldIn.getLightFor(EnumSkyBlock.SKY, pos)))
        {
            int j = state.getValue(AGE);

            if (rand.nextDouble() < getGrowthRate(worldIn, pos) && ForgeHooks.onCropsGrowPre(worldIn, pos, state, true))
            {
                if (j > 0)
                {
                    worldIn.setBlockState(pos, state.withProperty(AGE, j - 1));
                }
                ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
        }

        checkAndDropBlock(worldIn, pos, state);
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState soil = worldIn.getBlockState(pos.down());
        if (state.getBlock() == this)
        {
            return soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), net.minecraft.util.EnumFacing.UP, this) && plantTFCF.isValidTemp(ClimateTFC.getActualTemp(worldIn, pos)) && plantTFCF.isValidRain(ChunkDataTFC.getRainfall(worldIn, pos));
        }
        return this.canSustainBush(soil);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return PLANT_AABB.offset(state.getOffset(source, pos));
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        if (plantTFCF.getMovementMod() == 0.0D)
        {
            return blockState.getBoundingBox(worldIn, pos);
        }
        else
        {
            return NULL_AABB;
        }
    }

    @Override
    @Nonnull
    public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos)
    {
        switch (plantTFCF.getPlantType())
        {
            case DESERT_ROCK:
            case DESERT_TALL_PLANT_ROCK:
                return EnumPlantType.Desert;
            case HANGING_ROCK:
                return EnumPlantType.Cave;
            default:
                return EnumPlantType.Plains;
        }
    }

    @Nonnull
    public PlantTFCF.EnumPlantTypeTFC getPlantTypeTFC()
    {
        return plantTFCF.getEnumPlantTypeTFC();
    }

    @Nonnull
    protected BlockStateContainer createPlantBlockState()
    {
        return new BlockStateContainer(this, growthStageProperty, DAYPERIOD, AGE);
    }

    int getDayPeriod()
    {
        return CalendarTFC.CALENDAR_TIME.getHourOfDay() / (ICalendar.HOURS_IN_DAY / 4);
    }

    private boolean isValidSoil(IBlockState state)
    {
        switch (plantTFCF.getPlantType())
        {
            case DESERT_ROCK:
            case DESERT_TALL_PLANT_ROCK:
                return BlocksTFCF.isSand(state);
            case STANDARD_WOOD:
            case STANDARD_ROCK:
            case CREEPING_ROCK:
            case HANGING_ROCK:
            case HANGING_PLANT:
            case EPIPHYTE_ROCK:
                return BlocksTFCF.isSand(state) || BlocksTFCF.isSoilOrGravel(state) || BlocksTFCF.isRawStone(state);
            default:
                return BlocksTFCF.isSoil(state);
        }
    }
}