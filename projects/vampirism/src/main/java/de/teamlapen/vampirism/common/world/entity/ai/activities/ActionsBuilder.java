package de.teamlapen.vampirism.common.world.entity.ai.activities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ActionsBuilder<E extends LivingEntity> {

    private final List<ActionBuilder.Action<E>> actions = new ArrayList<>();
    private Supplier<Integer> cooldownSupplier = () -> 20;

    ActionsBuilder() {
    }

    List<ActionBuilder.Action<E>> actions() {
        return Collections.unmodifiableList(actions);
    }

    public void addAction(Activity activity, Consumer<ActionBuilder<E>> consumer) {
        ActionBuilder<E> builder = new ActionBuilder<>(activity, this.cooldownSupplier);
        consumer.accept(builder);
        this.actions.add(builder.build());
    }

    public void addAction(Supplier<Activity> activity, Consumer<ActionBuilder<E>> consumer) {
        addAction(activity.get(), consumer);
    }

    public ActionsBuilder<E> cooldown(Supplier<Integer> cooldownSupplier) {
        this.cooldownSupplier = cooldownSupplier;
        return this;
    }

}
