package de.teamlapen.vampirism.common.world.entity.ai.system;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Set;

/**
 * Base class for object-oriented AI systems.
 */
public abstract class AiSystem<E extends LivingEntity> {
    protected final List<AiActivityProvider<E>> activityProviders;

    public AiSystem() {
        this.activityProviders = this.createActivityProviders();
    }

    /**
     * Define the list of activity providers for this AI system.
     */
    protected abstract List<AiActivityProvider<E>> createActivityProviders();

    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        return this.activityProviders.stream()
                .flatMap(p -> p.getSensors().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    public Set<MemoryModuleType<?>> getMemoryModules() {
        return this.activityProviders.stream()
                .flatMap(p -> p.getMemoryModules().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    public Brain<E> initializeBrain(Brain<E> brain) {
        this.activityProviders.forEach(p -> p.initActivity(brain));
        this.setupBrainPriorities(brain);

        return brain;
    }

    public Brain.Provider<E> brainProvider() {
        return Brain.provider(getMemoryModules(), getSensors());
    }

    /**
     * Define core, idle, and default activities.
     */
    protected void setupBrainPriorities(Brain<E> brain) {
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }

    public abstract void tick(ServerLevel level, E entity);

    public void stop(ServerLevel level, E entity) {
        Brain<E> brain = (Brain<E>) entity.getBrain();
        brain.stopAll(level, entity);
        brain.clearMemories();
    }
}
