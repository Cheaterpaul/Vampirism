package de.teamlapen.vampirism.common.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class LivingEntitySensor extends Sensor<LivingEntity> {

    @Override
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(@NotNull ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElseGet(NearestVisibleLivingEntities::empty);
        Optional<LivingEntity> closest = entities.findClosest(x -> isEntityAttackable(level, entity, x));
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get(), closest);
    }
}
