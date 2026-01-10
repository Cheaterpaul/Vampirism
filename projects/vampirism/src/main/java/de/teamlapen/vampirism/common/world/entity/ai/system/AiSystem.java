package de.teamlapen.vampirism.common.world.entity.ai.system;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;
import java.util.Set;

/**
 * Base class for object-oriented AI systems.
 */
public abstract class AiSystem<E extends LivingEntity> {
    protected final E entity;
    protected final List<AiActivityProvider<E>> activityProviders;

    public AiSystem(E entity) {
        this.entity = entity;
        this.activityProviders = this.createActivityProviders();
    }

    /**
     * Define the list of activity providers for this AI system.
     */
    protected abstract List<AiActivityProvider<E>> createActivityProviders();

    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        return activityProviders.stream()
                .flatMap(p -> p.getSensors().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    public Set<MemoryModuleType<?>> getMemoryModules() {
        return activityProviders.stream()
                .flatMap(p -> p.getMemoryModules().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    public void initializeBrain(Brain<E> brain) {
        activityProviders.forEach(p -> p.initActivity(brain));
        this.setupBrainPriorities(brain);
    }

    /**
     * Define core, idle, and default activities.
     */
    protected abstract void setupBrainPriorities(Brain<E> brain);

    public abstract void update(ServerLevel level);

    public void stop(ServerLevel level) {
        Brain<E> brain = (Brain<E>) this.entity.getBrain();
        brain.stopAll(level, this.entity);
        brain.clearMemories();
    }
}
