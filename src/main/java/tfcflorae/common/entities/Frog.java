package tfcflorae.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import java.util.function.Predicate;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.dries007.tfc.world.chunkdata.ChunkData;

import tfcflorae.client.TFCFSounds;
import tfcflorae.client.model.entity.animation.api.TFCFAnimationState;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.entities.access.api.Poses;
import tfcflorae.common.entities.ai.FrogAi;
import tfcflorae.common.entities.ai.TFCFBrain;

public class Frog extends Animal
{
    public static final Ingredient FOOD = Ingredient.of(TFCFTags.Items.FROG_TEMPTATION_ITEMS);
    public static final ImmutableList<? extends SensorType<? extends Sensor<? super Frog>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, TFCFBrain.FROG_ATTACKABLES.get(), TFCFBrain.FROG_TEMPTATIONS.get(), TFCFBrain.IS_IN_WATER_SENSOR.get());
    public static final ImmutableList<? extends MemoryModuleType<?>> MEMORIES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_ATTACKABLE, TFCFBrain.IS_IN_WATER_MEMORY.get(), TFCFBrain.IS_PREGNANT.get(), TFCFBrain.UNREACHABLE_TONGUE_TARGETS.get());
    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<OptionalInt> TARGET = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.FLOAT);

    public static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
    public static final int MAX_SIZE = 20;
    public static final int MIN_SIZE = 9;
    public long lastMated = Long.MIN_VALUE;
    public long nextFeedTime = Long.MIN_VALUE;

    public static final Predicate<LivingEntity> SCARY_MOB = (entity) -> {
        if (entity instanceof Player && ((Player)entity).isCreative())
        {
            return false;
        }
        else
        {
            return entity.getType() == EntityType.AXOLOTL || entity.getMobType() != MobType.WATER;
        }
    };
    public static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);
    public final TFCFAnimationState longJumpingAnimationState = new TFCFAnimationState();
    public final TFCFAnimationState croakingAnimationState = new TFCFAnimationState();
    public final TFCFAnimationState usingTongueAnimationState = new TFCFAnimationState();
    public final TFCFAnimationState walkingAnimationState = new TFCFAnimationState();
    public final TFCFAnimationState swimmingAnimationState = new TFCFAnimationState();
    public final TFCFAnimationState idlingInWaterAnimationState = new TFCFAnimationState();

    public Frog(EntityType<? extends Animal> type, Level level)
    {
        super(type, level);
        this.lookControl = new FrogLookController(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, 4.0F);
        this.setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.setMaxUpStep(1.0F);
    }

    @Override
    protected Brain.Provider<Frog> brainProvider()
    {
        return Brain.provider(MEMORIES, SENSORS);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic)
    {
        return FrogAi.create(this.brainProvider().makeBrain(dynamic));
    }

    @Override @SuppressWarnings("unchecked")
    public Brain<Frog> getBrain()
    {
        return (Brain<Frog>)super.getBrain();
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(TARGET, OptionalInt.empty());
        this.entityData.define(ID_SIZE, MIN_SIZE);
        this.entityData.define(DATA_IS_MALE, true);
        this.entityData.define(DATA_FAMILIARITY, 0f);
    }

    public void clearFrogTarget()
    {
        this.entityData.set(TARGET, OptionalInt.empty());
    }

    public Optional<Entity> getFrogTarget()
    {
        return this.entityData.get(TARGET).stream().mapToObj(this.level()::getEntity).filter(Objects::nonNull).findFirst();
    }

    public void setFrogTarget(Entity entity)
    {
        this.entityData.set(TARGET, OptionalInt.of(entity.getId()));
    }

    @Override
    public int getHeadRotSpeed()
    {
        return 35;
    }

    @Override
    public int getMaxHeadYRot()
    {
        return 5;
    }

    public Variant getVariant()
    {
        return Variant.byId(this.entityData.get(VARIANT));
    }

    public void setVariant(Variant variant)
    {
        this.entityData.set(VARIANT, variant.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putInt("variant", this.getVariant().getId());
        tag.putInt("size", getSize() - 1);
        tag.putBoolean("male", isMale());
        tag.putFloat("familiarity", getFamiliarity());
        tag.putLong("lastMated", lastMated);
        tag.putLong("nextFeed", nextFeedTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byId(tag.getInt("variant")));
        this.setSize(tag.getInt("size") + 1, false);
        this.setIsMale(tag.getBoolean("male"));
        this.setFamiliarity(tag.getFloat("familiarity"));
        this.lastMated = tag.getLong("lastMated");
        this.nextFeedTime = tag.getLong("nextFeed");
    }

    public float getFamiliarity()
    {
        return entityData.get(DATA_FAMILIARITY);
    }

    public void setFamiliarity(float familiarity)
    {
        entityData.set(DATA_FAMILIARITY, familiarity);
    }

    public void setIsMale(boolean male)
    {
        entityData.set(DATA_IS_MALE, male);
    }

    public boolean isMale()
    {
        return entityData.get(DATA_IS_MALE);
    }

    public void setMated(long ticks)
    {
        lastMated = ticks;
    }

    @Override
    public boolean canMate(Animal animal)
    {
        if (animal != this && animal instanceof Frog other)
        {
            final float min = TFCAnimalProperties.READY_TO_MATE_FAMILIARITY;
            return other.isMale() != isMale() && beenLongEnoughToMate() && other.beenLongEnoughToMate() && getFamiliarity() > min && other.getFamiliarity() > min && fedRecently() && other.fedRecently();
        }
        return false;
    }

    public boolean beenLongEnoughToMate()
    {
        return Calendars.get(level()).getTicks() > lastMated + (ICalendar.TICKS_IN_DAY * 12);
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    public boolean shouldWalk()
    {
        return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && !this.isInWaterOrBubble();
    }

    public boolean shouldSwim()
    {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && this.isInWaterOrBubble();
    }

    @Override
    public void refreshDimensions()
    {
        final double x = getX();
        final double y = getY();
        final double z = getZ();
        super.refreshDimensions();
        setPos(x, y, z);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return super.getDimensions(pose).scale(getVisualScale());
    }

    public float getVisualScale()
    {
        return 0.05F * getSize();
    }

    public Pair<Integer, Integer> getSizeRangeForSpawning()
    {
        return Pair.of(MIN_SIZE, Mth.floor(MAX_SIZE * sizePoisonous()));
    }

    public int getSize()
    {
        return entityData.get(ID_SIZE);
    }

    public void setSize(int size, boolean heal)
    {
        size = Mth.clamp(size, MIN_SIZE, Mth.floor(MAX_SIZE * sizePoisonous()));
        reapplyPosition();
        refreshDimensions();
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(size);
        if (heal) setHealth(getMaxHealth());
        entityData.set(ID_SIZE, size);
    }

    @Override
    protected void customServerAiStep()
    {
        this.level().getProfiler().push("frogBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("frogActivityUpdate");
        FrogAi.updateActivities(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick()
    {
        if (this.level().isClientSide())
        {
            if (this.shouldWalk())
            {
                this.walkingAnimationState.startIfNotRunning(this.tickCount);
            }
            else
            {
                this.walkingAnimationState.stop();
            }

            if (this.shouldSwim())
            {
                this.idlingInWaterAnimationState.stop();
                this.swimmingAnimationState.startIfNotRunning(this.tickCount);
            }
            else if (this.isInWaterOrBubble())
            {
                this.swimmingAnimationState.stop();
                this.idlingInWaterAnimationState.startIfNotRunning(this.tickCount);
            }
            else
            {
                this.swimmingAnimationState.stop();
                this.idlingInWaterAnimationState.stop();
            }
        }

        super.tick();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data)
    {
        if (DATA_POSE.equals(data))
        {
            if (this.isInPose(Pose.LONG_JUMPING))
            {
                this.longJumpingAnimationState.start(this.tickCount);
            }
            else
            {
                this.longJumpingAnimationState.stop();
            }

            if (this.isInPose(Poses.CROAKING.get()))
            {
                this.croakingAnimationState.start(this.tickCount);
            }
            else
            {
                this.croakingAnimationState.stop();
            }

            if (this.isInPose(Poses.USING_TONGUE.get()))
            {
                this.usingTongueAnimationState.start(this.tickCount);
            }
            else
            {
                this.usingTongueAnimationState.stop();
            }
        }
        if (ID_SIZE.equals(data))
        {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(data);
    }

    public boolean isInPose(Pose pose)
    {
        return this.getPose() == pose;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob)
    {
        if (mob == this)
        {
            Frog frog = TFCFEntities.FROG.get().create(level);
            if (frog != null)
            {
                frog.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, UniformInt.of(100, 140).sample(random));
            }
            return frog;
        }
        if (mob instanceof Frog animal)
        {
            spawnChildFromBreeding(level, animal);
        }
        return null;
    }

    @Override
    public boolean isBaby()
    {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {}

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner)
    {
        Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable(partner.getLoveCause())).ifPresent((player) -> {
            player.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(player, this, partner, null);
        });

        this.resetLove();
        partner.resetLove();
        level.broadcastEntityEvent(this, (byte)18);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag)
    {
        final SpawnGroupData spawn = super.finalizeSpawn(accessor, difficulty, spawnType, groupData, tag);

        Pair<Integer, Integer> pair = getSizeRangeForSpawning();
        setSize(Mth.nextInt(random, pair.getFirst(), pair.getSecond()), true);

        BlockPos pos = this.blockPosition();
        ServerLevel level = accessor.getLevel();

        float temperature = 10;
        if (level != null && pos != null && level.isLoaded(pos) && level.getChunkSource().hasChunk(pos.getX(), pos.getZ()))
        {
            final ChunkData data = EntityHelpers.getChunkDataForSpawning(level, pos);
            temperature = data.getAverageTemp(pos);
        }
        else if (level == null || pos == null)
        {
            temperature = random.nextInt(23);
        }
        int specialColour = random.nextInt(8);

        setVariantRandom(specialColour, temperature);

        FrogAi.coolDownLongJump(this, accessor.getRandom());

        setIsMale(random.nextBoolean());
        return spawn;
    }

    public float sizePoisonous()
    {
        if (isPoisonousFrog())
        {
            return 0.5F;
        }
        return 1F;
    }

    public void setVariantRandom(int chance, float temperature)
    {
        if (temperature <= OverworldClimateModel.ICICLE_MELT_TEMPERATURE)
        {
            if (chance == 0)
            {
                this.setVariant(Variant.MINI_MUM);
            }
            else if (chance == 1)
            {
                this.setVariant(Variant.BUSHVELD_RAIN);
            }
            else if (chance == 2)
            {
                this.setVariant(Variant.PARAMO_MARSUPIAL);
            }
            else if (chance == 3)
            {
                this.setVariant(Variant.CAATINGA_HORNED);
            }
            else
            {
                this.setVariant(Variant.GREEN_TREE);
            }
        }
        else if (temperature > 15f) //OverworldClimateModel.LATITUDE_TEMPERATURE_VARIANCE_MEAN)
        {
            if (chance == 0)
            {
                this.setVariant(Variant.RED_HEADED_POISON);
            }
            else if (chance == 1)
            {
                this.setVariant(Variant.TARAPOTO_POISON);
            }
            else if (chance == 2)
            {
                this.setVariant(Variant.PAINTED_MANTELLA);
            }
            else if (chance == 3)
            {
                this.setVariant(Variant.STRAWBERRY_POISON_DART);
            }
            else
            {
                this.setVariant(Variant.ARUM);
            }
        }
        else
        {
            if (chance == 0)
            {
                this.setVariant(Variant.BLUE_BACK_REED);
            }
            else if (chance == 1)
            {
                this.setVariant(Variant.GRAY_TREE);
            }
            else if (chance == 2)
            {
                this.setVariant(Variant.MARBLED_PYGMY);
            }
            else if (chance == 3)
            {
                this.setVariant(Variant.SONORAN_GREEN);
            }
            else
            {
                this.setVariant(Variant.WOOD);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0D).add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Nullable @Override
    protected SoundEvent getAmbientSound()
    {
        return TFCFSounds.FROG.ambient().get();
    }

    @Nullable @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return TFCFSounds.FROG.hurt().get();
    }

    @Nullable @Override
    protected SoundEvent getDeathSound()
    {
        return TFCFSounds.FROG.death().get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state)
    {
        this.playSound(TFCFSounds.FROG.step().get(), 0.15F, 1.0F);
    }

    @Override
    public boolean isPushedByFluid()
    {
        return false;
    }

    @Override
    protected void sendDebugPackets()
    {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier)
    {
        return super.calculateFallDamage(fallDistance, damageMultiplier) - 5;
    }

    @Override
    public void travel(Vec3 input)
    {
        if (this.isEffectiveAi() && this.isInWater())
        {
            this.moveRelative(this.getSpeed(), input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        }
        else
        {
            super.travel(input);
        }
    }

/*    @Override
    public boolean canCutCorner(BlockPathTypes type)
    {
        return super.canCutCorner(type) && type != BlockPathTypes.WATER_BORDER;
    }*/

    public static boolean isValidFrogFood(LivingEntity entity)
    {
        return (!(entity instanceof Slime slime) || slime.getSize() <= 1) && entity.getType().is(TFCFTags.Entities.FROG_FOOD);
    }

    public boolean isValidFrogFoodS(LivingEntity entity)
    {
        return (!(entity instanceof Slime slime) || slime.getSize() <= 1 * getSize()) && entity.getType().is(TFCFTags.Entities.FROG_FOOD);
    }

    @Override
    protected PathNavigation createNavigation(Level level)
    {
        return new FrogPathNavigator(this, level);
    }

    @Override
    public boolean isFood(ItemStack stack)
    {
        if (stack.getCapability(FoodCapability.CAPABILITY).filter(IFood::isRotten).isPresent())
        {
            return false;
        }
        return FOOD.test(stack);
    }

    public boolean fedRecently()
    {
        return Calendars.get(level()).getTicks() < nextFeedTime;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        final ItemStack held = player.getItemInHand(hand);
        if (isFood(held))
        {
            if (!level().isClientSide)
            {
                final long ticks = Calendars.SERVER.getTicks();
                if (ticks > nextFeedTime)
                {
                    setFamiliarity(getFamiliarity() + 0.1f);
                    nextFeedTime = ticks + ICalendar.TICKS_IN_DAY;
                    usePlayerItem(player, hand, held);
                    playSound(TFCFSounds.FROG_EAT.get(), 1.0F, 1.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        if (this.isAlive() && isPoisonousFrog())
        {
            for(Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(0.3D), (target) -> {
                return targetingConditions.test(this, target);
            }))
            {
                if (mob.isAlive() && !(mob instanceof Frog))
                {
                    this.touch(mob);
                }
            }
        }
    }

    public void touch(Mob mob)
    {
        if (isPoisonousFrog() && mob.hurt(mob.damageSources().mobAttack(this), (float)(2)) && !(mob instanceof Frog))
        {
            mob.addEffect(new MobEffectInstance(MobEffects.POISON, Mth.floor(60 / (getSize() * 0.3F)), 0));
            mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Mth.floor(30 / (getSize() * 0.3F)), 0));
            this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        }
    }

    @Override
    public void playerTouch(Player player)
    {
        if (isPoisonousFrog() && player instanceof ServerPlayer && player.hurt(player.damageSources().mobAttack(this), (float)(2)))
        {
            if (!this.isSilent())
            {
                ((ServerPlayer)player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }
            player.addEffect(new MobEffectInstance(MobEffects.POISON, Mth.floor(60 / (getSize() * 0.3F)), 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Mth.floor(30 / (getSize() * 0.3F)), 0));
        }
    }

    public boolean isPoisonousFrog()
    {
        return getVariant() == Variant.PAINTED_MANTELLA || getVariant() == Variant.RED_HEADED_POISON || getVariant() == Variant.STRAWBERRY_POISON_DART || getVariant() == Variant.TARAPOTO_POISON;
    }

    public boolean isColdFrog()
    {
        return getVariant() == Variant.MINI_MUM || getVariant() == Variant.BUSHVELD_RAIN || getVariant() == Variant.PARAMO_MARSUPIAL || getVariant() == Variant.CAATINGA_HORNED || getVariant() == Variant.GREEN_TREE;
    }

    public boolean isTemperateFrog()
    {
        return getVariant() == Variant.BLUE_BACK_REED || getVariant() == Variant.GRAY_TREE || getVariant() == Variant.MARBLED_PYGMY || getVariant() == Variant.SONORAN_GREEN || getVariant() == Variant.WOOD;
    }

    public boolean isWarmFrog()
    {
        return getVariant() == Variant.RED_HEADED_POISON || getVariant() == Variant.TARAPOTO_POISON || getVariant() == Variant.PAINTED_MANTELLA || getVariant() == Variant.STRAWBERRY_POISON_DART || getVariant() == Variant.ARUM;
    }

    public static boolean checkFrogSpawnRules(EntityType<? extends Animal> type, LevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random)
    {
        return accessor.getBlockState(pos.below()).is(TFCFTags.Blocks.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn(accessor, pos);
    }

    class FrogLookController extends LookControl
    {
        FrogLookController(Mob mobEntity)
        {
            super(mobEntity);
        }

        @Override
        protected boolean resetXRotOnTick()
        {
            return Frog.this.getFrogTarget().isEmpty();
        }
    }

    static class FrogNodeEvaluator extends AmphibiousNodeEvaluator
    {
        public final BlockPos.MutableBlockPos preferredBlock = new BlockPos.MutableBlockPos();

        public FrogNodeEvaluator(boolean penalizeDeepWater)
        {
            super(penalizeDeepWater);
        }

        @Override
        public BlockPathTypes getBlockPathType(BlockGetter getter, int x, int y, int z)
        {
            this.preferredBlock.set(x, y - 1, z);
            BlockState state = getter.getBlockState(this.preferredBlock);
            return state.is(TFCFTags.Blocks.FROG_PREFER_JUMP_TO) ? BlockPathTypes.OPEN : FrogNodeEvaluator.getBlockPathTypeStatic(getter, this.preferredBlock.move(Direction.UP));
        }
    }

    static class FrogPathNavigator extends WaterBoundPathNavigation
    {
        FrogPathNavigator(Frog frog, Level level)
        {
            super(frog, level);
        }

        @Override
        protected PathFinder createPathFinder(int range)
        {
            this.nodeEvaluator = new FrogNodeEvaluator(true);
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, range);
        }

        @Override
        protected boolean canUpdatePath()
        {
            return true;
        }

        @Override
        public boolean isStableDestination(BlockPos pos)
        {
            return !this.level.getBlockState(pos.below()).isAir();
        }
    }

    public enum Variant
    {
        ARUM(0, "arum"),
        WOOD(1, "wood"),
        GREEN_TREE(2, "green_tree"),
        BLUE_BACK_REED(3, "blue_back_reed"),
        CAATINGA_HORNED(4, "cattinga_horned"),
        GRAY_TREE(5, "gray_tree"),
        MARBLED_PYGMY(6, "marbled_pygmy"),
        PAINTED_MANTELLA(7, "painted_mantella"),
        PARAMO_MARSUPIAL(8, "paramo_marsupial"),
        RED_HEADED_POISON(9, "red_headed_poison"),
        SONORAN_GREEN(10, "sonoran_green"),
        STRAWBERRY_POISON_DART(11, "strawberry_poison_dart"),
        TARAPOTO_POISON(12, "tarapoto_poison"),
        MINI_MUM(13, "mini_mum"),
        BUSHVELD_RAIN(14, "bushveld_rain");

        public static final Variant[] VARIANTS = Arrays.stream(Variant.values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(Variant[]::new);
        public final int id;
        public final String name;

        Variant(int id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public int getId()
        {
            return this.id;
        }

        public String getName()
        {
            return this.name;
        }

        public static Variant byId(int id)
        {
            if (id < 0 || id >= VARIANTS.length)
            {
                id = 0;
            }

            return VARIANTS[id];
        }
    }
}
