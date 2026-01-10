package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class RegenerationBehavior extends Behavior<Dracula> {

    public static Set<MemoryModuleType<?>> memories() {
        return Set.of(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(), ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get(), ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
    }

    public static Set<SensorType<? extends Sensor<? super Dracula>>> sensors() {
        return Set.of();
    }

    public static Stream<MemoryModuleType<?>> requires() {
        return Stream.of(
                ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(),
                ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get());
    }

    private static final int DURATION = 10 * 20;
    public static final float HEALTH_PERCENTAGE = 0.3f;

    public static RegenerationBehavior create() {
        return new RegenerationBehavior();
    }

    public RegenerationBehavior() {
        super(Map.of(
                ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get(), MemoryStatus.VALUE_PRESENT
        ), DURATION);
    }

    @Override
    protected void start(ServerLevel level, Dracula entity, long gameTime) {
        Brain<Dracula> brain = entity.getBrain();
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Dracula entity, long gameTime) {
        return entity.getBrain().getMemory(ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get()).isPresent();
    }

    @Override
    protected void stop(ServerLevel level, Dracula entity, long gameTime) {
        Brain<Dracula> brain = entity.getBrain();
        brain.setMemoryWithExpiry(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(), Unit.INSTANCE, 60*20);
        brain.eraseMemory(ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get());
        brain.eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        brain.setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 10*20);
    }

    @Override
    protected void tick(ServerLevel level, Dracula owner, long gameTime) {
        owner.heal(owner.getMaxHealth() * HEALTH_PERCENTAGE / DURATION);
    }
}
