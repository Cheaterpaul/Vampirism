package de.teamlapen.vampirism.common.world.entity.dracula.ai;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.activities.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DraculaAi {

    public static final ImmutableSet<? extends SensorType<? extends Sensor<? super Dracula>>> SENSOR_TYPES = Stream.of(
            CoreActivity.SENSORS,
            IdleActivity.SENSORS,
            Phase1Activities.SENSORS,
            Phase2Activities.SENSORS,
            Phase3Activities.SENSORS
    ).flatMap(Collection::stream).map(x -> (SensorType<? extends Sensor<? super Dracula>>) x).collect(ImmutableSet.toImmutableSet());

    public static final ImmutableSet<? extends MemoryModuleType<?>> MEMORY_TYPES = Stream.of(
            CoreActivity.MEMORY_MODULES.stream(),
            IdleActivity.MEMORY_MODULES.stream(),
            Phase1Activities.MEMORY_MODULES.stream(),
            Phase2Activities.MEMORY_MODULES.stream(),
            Phase3Activities.MEMORY_MODULES.stream(),
            Stream.of(
                    ModMemoryTypes.Dracula.ACTION_ACTIVE.get(),
                    ModMemoryTypes.Dracula.ACTION_COOLDOWN.get()
            )
    ).flatMap(x -> x).collect(ImmutableSet.toImmutableSet());

    public static void stop(Dracula dracula, ServerLevel level) {
        dracula.getBrain().stopAll(level, dracula);
        dracula.getBrain().clearMemories();
    }

    public static Brain<?> makeBrain(Dracula dracula, Brain<Dracula> brain) {
        CoreActivity.initActivity(brain);
        IdleActivity.initActivity(brain);
        Phase1Activities.initActivity(brain);
        Phase2Activities.initActivity(brain);
        Phase3Activities.initActivity(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateMemories(Dracula dracula) {
        Brain<Dracula> brain = dracula.getBrain();
        Set<MemoryModuleType<Unit>> stageMemories = Stream.of(ModMemoryTypes.Dracula.PHASE_1.get(), ModMemoryTypes.Dracula.PHASE_2.get(), ModMemoryTypes.Dracula.PHASE_3.get()).collect(Collectors.toSet());
        MemoryModuleType<Unit> unitMemoryModuleType = memoryForStage(dracula.getState());

        if (unitMemoryModuleType != null) {
            stageMemories.remove(unitMemoryModuleType);
            brain.setMemory(unitMemoryModuleType, Unit.INSTANCE);
        }
        stageMemories.forEach(brain::eraseMemory);
    }

    public static void updateActivity(Dracula dracula, ServerLevel level) {
        Brain<Dracula> brain = dracula.getBrain();
        brain.setActiveActivityToFirstValid(
                Stream.of(switch (dracula.getStage()) {
                    case PHASE_1 -> Phase1Activities.getActivities();
                    case PHASE_2 -> Phase2Activities.getActivities();
                    case PHASE_3 -> Phase3Activities.getActivities();
                    default -> Stream.<Activity>of();
                }, IdleActivity.getActivities())
                        .flatMap(x -> x)
                        .toList()
        );
    }

    @Nullable
    private static MemoryModuleType<Unit> memoryForStage(DraculaState state) {
        if (state == DraculaState.TRANSFORMING_TO_RANGED) {
            return null;
        } else if (state == DraculaState.RANGED) {
            return ModMemoryTypes.Dracula.PHASE_2.get();
        } else if (state == DraculaState.TRANSFORMING_TO_RAGED) {
            return null;
        } else if (state == DraculaState.RAGED) {
            return ModMemoryTypes.Dracula.PHASE_3.get();
        } else if (state == DraculaState.PASSIVE) {
            return ModMemoryTypes.Dracula.PHASE_1.get();
        } else {
            return null;
        }
    }

    public static void setActionCooldown(MemoryAccessor<Const.Mu<com.mojang.datafixers.util.Unit>, Unit> cooldown, MemoryAccessor<IdF.Mu, Unit> active, MemoryAccessor<Const.Mu<com.mojang.datafixers.util.Unit>, Unit> actionCooldown, MemoryAccessor<IdF.Mu, Unit> actionActive, int actionCooldownTicks) {
        active.erase();
        cooldown.setWithExpiry(Unit.INSTANCE, 20*20);
        actionActive.erase();
        actionCooldown.setWithExpiry(Unit.INSTANCE, actionCooldownTicks);
    }


}
