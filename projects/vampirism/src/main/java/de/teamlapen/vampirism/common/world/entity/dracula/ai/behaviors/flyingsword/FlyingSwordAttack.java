package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingSwordEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FlyingSwordAttack extends Behavior<Dracula> {

    public static Stream<MemoryModuleType<?>> requires() {
        return Stream.of(
                ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get(),
                ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get(),
                ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get()
        );
    }

    private enum Phase {
        CHANNELING,
        ATTACKING
    }

    private Phase phase = Phase.CHANNELING;
    private int ticks = 0;
    private int attacksDone = 0;
    private int totalAttacks = 0;
    private final List<LivingEntity> targets = new ArrayList<>();

    public FlyingSwordAttack() {
        super(Map.of(
                ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get(), MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ), 400);
    }

    @Override
    protected void start(ServerLevel level, Dracula entity, long gameTime) {
        this.phase = Phase.CHANNELING;
        this.ticks = 0;
        this.attacksDone = 0;
        this.totalAttacks = entity.getRandom().nextInt(7) + 6;
        this.targets.clear();
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Dracula entity, long gameTime) {
        this.ticks++;
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        if (phase == Phase.CHANNELING) {
            if (ticks % 10 == 0) {
                 NearestVisibleLivingEntities visibleEntities = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
                 visibleEntities.findAll(e -> e.distanceToSqr(entity) < 15 * 15 && e != entity).forEach(target -> {
                     target.hurtServer(level, level.damageSources().magic(), 1.0f);
                     if (!targets.contains(target)) {
                         targets.add(target);
                     }
                 });
            }

            if (ticks >= 40) {
                if (targets.isEmpty()) {
                    NearestVisibleLivingEntities visibleEntities = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
                    for (LivingEntity e : visibleEntities.findAll(e -> e != entity)) {
                        targets.add(e);
                        break;
                    }
                }

                if (targets.isEmpty()) {
                    doStop(entity);
                } else {
                    phase = Phase.ATTACKING;
                    ticks = 0;
                }
            }
        } else if (phase == Phase.ATTACKING) {
            if (ticks % 15 == 0) {
                if (attacksDone < totalAttacks) {
                    targets.removeIf(e -> !e.isAlive());
                    if (targets.isEmpty()) {
                        doStop(entity);
                        return;
                    }
                    LivingEntity target = targets.get(entity.getRandom().nextInt(targets.size()));
                    
                    entity.swing(InteractionHand.MAIN_HAND);
                    BehaviorUtils.lookAtEntity(entity, target);

                    float damage = 5.0f + (targets.size() * 0.5f);
                    FlyingSwordEntity sword = new FlyingSwordEntity(level, entity, target, damage);
                    level.addFreshEntity(sword);

                    attacksDone++;
                } else {
                    doStop(entity);
                }
            }
        }
    }

    private void doStop(Dracula entity) {
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Dracula entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get());
    }

    @Override
    protected void stop(ServerLevel level, Dracula entity, long gameTime) {
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get(), Unit.INSTANCE, 400);
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get());
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 100);
    }
}
