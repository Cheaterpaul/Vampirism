package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.BlindingBatEntity;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.core.BlockPos;
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

    public static Set<SensorType<? extends Sensor<? super Dracula>>> sensors() {
        return Set.of();
    }
    public static Set<MemoryModuleType<?>> memories() {
        return Set.of(
                ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get(),
                ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get(),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    public static OneShot<Dracula> create() {
        return BehaviorBuilder.create(
                inst -> inst.group(
                        inst.absent(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()),
                        inst.absent(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get()),
                        inst.present(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get()),
                        inst.registered(MemoryModuleType.ATTACK_TARGET)
                ).apply(inst, (cooldown, active, used, using, target) ->
                        (level, dracula, gameTime) -> {
                            summonBats(level, dracula);
                            return true;
                        })
        );
    }

    protected static void summonBats(ServerLevel level, Dracula dracula) {
        for (int i = 0; i < 10; i++) {
            double angle = i * (Math.PI * 2 / 10);
            double x = dracula.getX() + Math.cos(angle) * 2;
            double z = dracula.getZ() + Math.sin(angle) * 2;
            BlockPos pos = BlockPos.containing(x, dracula.getY() + 1.5, z);
            SpawnUtil.trySpawnMob(ModEntities.BLINDING_BAT.get(), EntitySpawnReason.EVENT, level, pos, 0, 1, 1, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER, false).ifPresent(bat -> {
                bat.restrictLiveSpan();
                bat.setTargeting();
            });
        }
    }
}
