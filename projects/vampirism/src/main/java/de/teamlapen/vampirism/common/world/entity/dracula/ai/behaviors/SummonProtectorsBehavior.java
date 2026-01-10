package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.IInformativeBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaAiSystem;
import de.teamlapen.vampirism.common.world.entity.vampire.BasicVampireEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class SummonProtectorsBehavior {

    public static Stream<MemoryModuleType<?>> requires() {
        return Stream.of(
                ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN.get(),
                ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE.get(),
                ModMemoryTypes.SUMMONS.get()
        );
    }

    public static final int MAX_SUMMONS = 10;

    public static OneShot<Dracula> create() {
        return BehaviorBuilder.create(
                inst -> inst.group(
                        inst.absent(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()),
                        inst.absent(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE.get()),
                        inst.registered(ModMemoryTypes.SUMMONS.get())
                ).apply(inst, (cooldown, active, used, using, summons) ->
                        ((level, dracula, gameTime) -> {
                            Brain<Dracula> brain = dracula.getBrain();
                            List<UUID> uuids = brain.getMemory(ModMemoryTypes.SUMMONS.get()).orElseGet(List::of);
                            List<LivingEntity> entities = new ArrayList<>();
                            for (UUID uuid : uuids) {
                                Entity entity = level.getEntity(uuid);
                                if (entity instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(entity) < 100 * 100) {
                                    entities.add(livingEntity);
                                }
                            }
                            int spawned = 0;
                            while (entities.size() < MAX_SUMMONS && spawned < 4) {
                                LivingEntity summon = summon(level, dracula);
                                if (summon != null) {
                                    entities.add(summon);
                                }
                                spawned++;
                            }
                            summons.set(entities.stream().map(Entity::getUUID).toList());
                            DraculaAiSystem.setActionCooldown(cooldown, active, used, using, 20*20);
                            return true;
                        })

                ));
    }

    public static Set<MemoryModuleType<?>> memories() {
        return Set.of(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN.get(), ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE.get(), ModMemoryTypes.SUMMONS.get());
    }


    protected static LivingEntity summon(ServerLevel level, Dracula dracula) {
        BasicVampireEntity basicVampireEntity = ModEntities.VAMPIRE.get().create(level, EntitySpawnReason.EVENT);
        if (basicVampireEntity != null) {
            RandomSource random = dracula.getRandom();
            basicVampireEntity.moveOrInterpolateTo(dracula.position().add( (random.nextDouble() - random.nextDouble()) * 20 + 0.5, random.nextInt(3) - 1, (random.nextDouble() - random.nextDouble()) * 20 + 0.5), random.nextFloat() * 360f, 0);
//            if (level.noCollision(basicVampireEntity)) {
                level.addFreshEntity(basicVampireEntity);
//            }
        }
        return basicVampireEntity;
    }
}
