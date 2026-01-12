package de.teamlapen.vampirism.common.world.entity.ai.sensing;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.List;
import java.util.Set;

public class LivingTargetableLivingEntitySensor<T extends LivingEntity> extends Sensor<T> {

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(ModMemoryTypes.NEAREST_ATTACKABLE.get(), ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get(), MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(ServerLevel level, T entity) {
        Brain<?> brain = entity.getBrain();
        var entities = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElseGet(List::of);

        // the list is already sorted by distance
        List<LivingEntity> list = entities.stream().filter(x -> isAttackable(level, entity, x)).toList();

        brain.setMemory(ModMemoryTypes.NEAREST_ATTACKABLE.get(), list);
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get(), new NearestVisibleLivingEntities(level, entity, list));
    }

    private boolean isAttackable(ServerLevel level, LivingEntity entity, LivingEntity target) {
        return isEntityAttackable(level, entity, target);
    }
}
