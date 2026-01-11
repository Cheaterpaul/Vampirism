package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingneedle;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class FlyingNeedleAttack extends Behavior<Dracula> {

    private enum Phase {
        CHARGING,
        FIRING
    }

    private Phase phase = Phase.CHARGING;
    private int ticks = 0;
    private final List<FlyingNeedleEntity> needles = new ArrayList<>();
    private final List<LivingEntity> targets = new ArrayList<>();
    private static final int MAX_NEEDLES = 6;

    public static Set<SensorType<? extends Sensor<? super Dracula>>> sensors() {
        return Set.of(SensorType.NEAREST_LIVING_ENTITIES);
    }

    public static Set<MemoryModuleType<?>> memories() {
        return Set.of(
                ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN.get(),
                ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get(),
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ModMemoryTypes.Dracula.FLYING_NEEDLES.get()
        );
    }

    public static FlyingNeedleAttack create() {
        return new FlyingNeedleAttack();
    }
    public FlyingNeedleAttack() {
        super(Map.of(
                ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.Dracula.FLYING_NEEDLES.get(), MemoryStatus.REGISTERED
        ), 400);
    }



    @Override
    protected void start(@NonNull ServerLevel level, Dracula entity, long gameTime) {
        this.phase = Phase.CHARGING;
        this.ticks = 0;
        this.needles.clear();
        this.targets.clear();
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(@NonNull ServerLevel level, Dracula entity, long gameTime) {
        this.ticks++;
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        if (phase == Phase.CHARGING) {
            if (ticks % 10 == 0 && needles.size() < MAX_NEEDLES) {
                FlyingNeedleEntity needle = new FlyingNeedleEntity(level, entity, 4.0f, needles.size(), MAX_NEEDLES);
                level.addFreshEntity(needle);
                needles.add(needle);
                entity.getBrain().setMemory(ModMemoryTypes.Dracula.FLYING_NEEDLES.get(), needles.stream().map(Entity::getUUID).toList());
            }

            if (needles.size() == MAX_NEEDLES && ticks >= 80) { // Give some time for charging
                NearestVisibleLivingEntities visibleEntities = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
                visibleEntities.findAll(e -> e.distanceToSqr(entity) < 20 * 20 && e != entity).forEach(targets::add);

                if (targets.isEmpty()) {
                    for (LivingEntity e : visibleEntities.findAll(e -> e != entity)) {
                        targets.add(e);
                        break;
                    }
                }

                if (targets.isEmpty()) {
                    doStop(entity);
                } else {
                    phase = Phase.FIRING;
                    ticks = 0;
                }
            }
        } else if (phase == Phase.FIRING) {
            if (ticks % 10 == 0) {
                needles.removeIf(n -> !n.isAlive() || n.isFlying());
                if (needles.isEmpty()) {
                    doStop(entity);
                    return;
                }

                targets.removeIf(e -> !e.isAlive());
                if (targets.isEmpty()) {
                    doStop(entity);
                    return;
                }

                FlyingNeedleEntity needle = needles.getFirst();
                LivingEntity target = targets.get(entity.getRandom().nextInt(targets.size()));
                BehaviorUtils.lookAtEntity(entity, target);
                needle.shoot(target);
                ticks = 0; // Reset ticks to wait 5 for next firing
            }
        }
    }

    private void doStop(Dracula entity) {
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get());
    }

    @Override
    protected boolean canStillUse(@NonNull ServerLevel level, Dracula entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get());
    }

    @Override
    protected void stop(@NonNull ServerLevel level, Dracula entity, long gameTime) {
        Brain<Dracula> brain = entity.getBrain();
        var uuids = brain.getMemory(ModMemoryTypes.Dracula.FLYING_NEEDLES.get()).stream().flatMap(Collection::stream).map(level::getEntity).toList();
        for (Entity uuid : uuids) {
            if (uuid instanceof FlyingNeedleEntity needle && needle.isAlive() && !needle.isFlying()) {
                needle.discard();
            }
        }
        brain.eraseMemory(ModMemoryTypes.Dracula.FLYING_NEEDLES.get());

//        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN.get(), Unit.INSTANCE, 300);
//        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get());
//        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
//        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 100);
//        for (FlyingNeedleEntity needle : needles) {
//            if (needle.isAlive() && !needle.isFlying()) {
//                needle.discard();
//            }
//        }
    }
}
