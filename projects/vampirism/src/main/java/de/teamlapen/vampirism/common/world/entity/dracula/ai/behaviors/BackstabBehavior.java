package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BackstabBehavior extends Behavior<Dracula> {

    @Nullable
    private Vec3 originalPos;
    private int ticks = 0;

    public BackstabBehavior() {
        super(Map.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.Dracula.BACKSTAB_ACTIVE.get(), MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.Dracula.BACKSTAB_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT
        ), 100);
    }

    public static BackstabBehavior create() {
        return new BackstabBehavior();
    }

    @Override
    protected void start(ServerLevel level, Dracula entity, long gameTime) {
        this.originalPos = entity.position();
        this.ticks = 0;

        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
        Vec3 behind = target.position().add(target.getLookAngle().scale(-1.5));
        entity.teleportTo(behind.x, behind.y, behind.z);

        entity.doHurtTarget(level, target);
        entity.swing(entity.getUsedItemHand());
    }

    @Override
    protected void tick(ServerLevel level, Dracula entity, long gameTime) {
        this.ticks++;
        if (this.ticks >= 20) {
            assert originalPos != null;
            entity.teleportTo(originalPos.x, originalPos.y, originalPos.z);
            entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.BACKSTAB_ACTIVE.get());
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Dracula entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(ModMemoryTypes.Dracula.BACKSTAB_ACTIVE.get());
    }

    @Override
    protected void stop(ServerLevel level, Dracula entity, long gameTime) {
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.BACKSTAB_COOLDOWN.get(), Unit.INSTANCE, 300);
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.BACKSTAB_ACTIVE.get());
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 100);
    }
}
