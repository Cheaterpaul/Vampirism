package de.teamlapen.vampirism.common.world.entity.ai.system;

import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Encapsulates specific AI behaviors, sensors, and memories for an entity.
 */
public abstract class AiActivityProvider<E extends LivingEntity> {

    private final Activity activity;
    protected final ActivityBuilder<E> builder;

    public AiActivityProvider(Activity activity) {
        this.activity = activity;
        createActivity(this.builder = new ActivityBuilder<>(activity));
    }

    public AiActivityProvider(Supplier<Activity> activitySupplier) {
        this(activitySupplier.get());
    }

    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        return this.builder.getSensors();
    }

    public Set<MemoryModuleType<?>> getMemoryModules() {
        return this.builder.getMemories();
    }

    public void initActivity(Brain<E> brain) {
        this.builder.register(brain);
    }

    public Activity getActivity() {
        return this.activity;
    }
    public Stream<Activity> allActivities() {
        return this.builder.activities();
    }

    protected abstract void createActivity(ActivityBuilder<E> builder);

}
