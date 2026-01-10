package de.teamlapen.vampirism.common.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.common.world.entity.ai.sensing.LivingEntitySensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSensors {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<LivingEntitySensor>> NEAREST_ENTITY = SENSOR_TYPES.register("nearest_entity", () -> new SensorType<>(LivingEntitySensor::new));

    static void register(IEventBus bus) {
        SENSOR_TYPES.register(bus);
    }
}
