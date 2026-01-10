package de.teamlapen.vampirism.common.world.entity.ai.activities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ActionsBuilder<E extends LivingEntity> {

    private final List<ActionBuilder.Action<E>> actions = new ArrayList<>();

    ActionsBuilder() {
    }

    public List<ActionBuilder.Action<E>> actions() {
        return actions;
    }

    public void addAction(Activity activity, Consumer<ActionBuilder<E>> consumer) {
        ActionBuilder<E> builder = new ActionBuilder<>(activity);
        consumer.accept(builder);
        this.actions.add(builder.build());
    }

    public void addAction(Supplier<Activity> activity, Consumer<ActionBuilder<E>> consumer) {
        addAction(activity.get(), consumer);
    }

}
