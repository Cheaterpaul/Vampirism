package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.BlindingBatEntity;
import de.teamlapen.vampirism.common.world.entity.ai.activities.IInformativeBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Set;

public class SummonVampireBats {

    public static Set<MemoryModuleType<?>> requires() {
        return Set.of(
                ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get(),
                ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get()
        );
    }

    public static class SummonVampireBatsInformativeOneShot<E extends Dracula> extends OneShot<E> implements IInformativeBehavior<E> {
        private final OneShot<E> delegate;

        public SummonVampireBatsInformativeOneShot(OneShot<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean trigger(ServerLevel level, E entity, long gameTime) {
            return delegate.trigger(level, entity, gameTime);
        }

        @Override
        public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
            return Set.of();
        }

        @Override
        public Set<MemoryModuleType<?>> getMemories() {
            return SummonVampireBats.memories();
        }
    }

    public static OneShot<Dracula> create() {
        return new SummonVampireBatsInformativeOneShot<>(BehaviorBuilder.create(
                inst -> inst.group(
                        inst.absent(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()),
                        inst.absent(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get())
                ).apply(inst, (cooldown, active, used, using) ->
                        (level, dracula, gameTime) -> {
                            summonBats(level, dracula);
                            return true;
                        })
        ));
    }

    public static Set<MemoryModuleType<?>> memories() {
        return Set.of(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get(), ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get());
    }

    protected static void summonBats(ServerLevel level, Dracula dracula) {
        for (int i = 0; i < 10; i++) {
            SpawnUtil.trySpawnMob(ModEntities.BLINDING_BAT.get(), EntitySpawnReason.EVENT, level, dracula.blockPosition(), 4, 3, 3, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER, false).ifPresent(BlindingBatEntity::restrictLiveSpan);
        }
    }
}
