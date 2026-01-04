package de.teamlapen.vampirism.common.world.entity.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BehaviorBuilder<E extends LivingEntity> {

    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();

    public static <E extends LivingEntity> BehaviorBuilder<E> create(int priority) {
        return new BehaviorBuilder<>(priority);
    }

    private final int priority;

    public BehaviorBuilder(int priority) {
        this.priority = priority;
    }

    public List<BehaviorControl<? super E>> getBehaviors() {
        return Collections.unmodifiableList(behaviors);
    }

    public BehaviorBuilder<E> add(BehaviorControl<? super E> control) {
        this.behaviors.add(control);
        return this;
    }

    public BehaviorBuilder<E> add(BehaviorBuilder<E> builder) {
        this.behaviors.addAll(builder.behaviors);
        return this;
    }

    public ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> build() {
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

        int priority = this.priority;

        for (BehaviorControl<? super E> behavior : behaviors) {
            builder.add(Pair.of(priority++, behavior));
        }

        return builder.build();
    }
}