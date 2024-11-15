package tfcflorae.common.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import tfcflorae.common.entities.Tadpole;

public class TadpoleAi
{
    public static Brain<?> create(Brain<Tadpole> brain)
    {
        TadpoleAi.addCoreActivities(brain);
        TadpoleAi.addIdleActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void addCoreActivities(Brain<Tadpole> brain)
    {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new AnimalPanic(2.0f), new LookAtTargetSink(45, 90), new MoveToTargetSink(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<Tadpole> brain)
    {
    /*    brain.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0,
                new RunSometimes<LivingEntity>(SetEntityLookTarget.create(EntityType.PLAYER, 6.0f),
                        UniformInt.of(30, 60))), Pair.of(3, TryFindWater.create(6, 0.15f)),
                Pair.of(4, new GateBehavior<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL,
                        ImmutableList.of(Pair.of(RandomStroll.swim(0.5F), 2), Pair.of(RandomStroll.stroll(0.15F), 2),
                                Pair.of(SetWalkTargetFromLookTarget.create(0.5f, 3), 3),
                                Pair.of(new RunIf<>(Entity::isInWaterOrBubble, new DoNothing(30, 60)), 5))))));*/
    }

    public static void updateActivities(Tadpole tadpole)
    {
        tadpole.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }
}
