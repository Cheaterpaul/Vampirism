package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword;

import de.teamlapen.vampirism.common.core.ModItems;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.IInformativeBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Set;

public class EquipSword {

    public static class EquipSwordInformativeOneShot<E extends Dracula> extends OneShot<E> implements IInformativeBehavior<E> {
        private final OneShot<E> delegate;

        public EquipSwordInformativeOneShot(OneShot<E> delegate) {
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
            return Set.of(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get(), ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get());
        }
    }

    public static OneShot<Dracula> create() {
        return new EquipSwordInformativeOneShot<>(BehaviorBuilder.create(inst -> inst.group(
                inst.absent(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get()),
                inst.present(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get())
        ).apply(inst, (equipped, active) ->
                (level, dracula, gameTime) -> {
            dracula.setItemInHand(InteractionHand.MAIN_HAND, ModItems.HEART_SEEKER_ULTIMATE.toStack());
            equipped.set(Unit.INSTANCE);
            return true;
        })));
    }
}
