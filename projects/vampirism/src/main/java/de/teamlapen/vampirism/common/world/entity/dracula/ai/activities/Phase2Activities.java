package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonVampireBats;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.EquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.UnEquipSword;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phase2Activities extends IdleActivity {

    public static Set<SensorType<? extends Sensor<? super Dracula>>> SENSORS = Stream.of(
            IdleActivity.SENSORS.stream(),
            Stream.of(
                    ModSensors.DRACULA_RANGED_ACTION.get()
            )
    ).flatMap(x -> x).collect(Collectors.toUnmodifiableSet());

    public static Set<? extends MemoryModuleType<?>> MEMORY_MODULES = Stream.of(
            IdleActivity.MEMORY_MODULES.stream(),
            SummonVampireBats.requires(),
            Stream.of(
                    ModMemoryTypes.Dracula.PHASE_2.get()
            )
    ).flatMap(x -> x).collect(Collectors.toUnmodifiableSet());

    public static void initActivity(Brain<Dracula> brain) {
        ActivityBuilder.<Dracula>create(ModActivities.DRACULA_PHASE_2)
                .add(SummonVampireBats.create())
                .add(createIdleLookBehaviors())
                .add(createIdleMovementBehaviors())
                .requires(ModMemoryTypes.Dracula.PHASE_2, MemoryStatus.VALUE_PRESENT)
                .register(brain);
        initActions(brain);
    }

    public static void initActions(Brain<Dracula> brain) {
        ActivityBuilder.<Dracula>create(ModActivities.DRACULA_FLYING_SWORD)
                .add(UnEquipSword.create())

                .add(EquipSword.create())
                .register(brain);
    }

    public static Stream<Activity> getActivities() {
        return Stream.of(
                ModActivities.DRACULA_PHASE_2.get()
        );
    }

    protected static RunOne<Dracula> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.3F), 1),
                        Pair.of(new DoNothing(20, 40), 1))
        );
    }

    protected static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder()
                        .addAll(createLookBehaviors())
                        .add(Pair.of(new DoNothing(20, 40), 1))
                        .build()
        );
    }
}
