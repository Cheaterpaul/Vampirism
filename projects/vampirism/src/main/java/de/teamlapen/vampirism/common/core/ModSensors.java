package de.teamlapen.vampirism.common.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.common.world.entity.ai.sensing.LivingEntitySensor;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.passive.PassiveActionSensor;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.rage.RageActionSensor;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged.RangedActionSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSensors {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<LivingEntitySensor>> NEAREST_ENTITY = SENSOR_TYPES.register("nearest_entity", () -> new SensorType<>(LivingEntitySensor::new));

    public static final DeferredHolder<SensorType<?>, SensorType<PassiveActionSensor>> DRACULA_PASSIVE_ACTION = SENSOR_TYPES.register("dracula_passive_action", () -> new SensorType<>(PassiveActionSensor::new));
    public static final DeferredHolder<SensorType<?>, SensorType<RangedActionSensor>> DRACULA_RANGED_ACTION = SENSOR_TYPES.register("dracula_ranged_action", () -> new SensorType<>(RangedActionSensor::new));
    public static final DeferredHolder<SensorType<?>, SensorType<RageActionSensor>> DRACULA_RAGE_ACTION = SENSOR_TYPES.register("dracula_rage_action", () -> new SensorType<>(RageActionSensor::new));

    static void register(IEventBus bus) {
        SENSOR_TYPES.register(bus);
    }
}
