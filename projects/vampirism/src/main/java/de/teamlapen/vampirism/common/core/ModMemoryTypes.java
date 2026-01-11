package de.teamlapen.vampirism.common.core;

import com.mojang.serialization.Codec;
import de.teamlapen.vampirism.REFERENCE;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ModMemoryTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_ATTACKABLE = MEMORY_MODULES.register("nearest_visible_attackable", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<UUID>>> SUMMONS = MEMORY_MODULES.register("summons", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC.listOf())));

    public static class Dracula {
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> PHASE_1 = unit("dracula.phase1");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> PHASE_2 = unit("dracula.phase2");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> PHASE_3 = unit("dracula.phase3");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> ACTION_ACTIVE = unit("action.active");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> ACTION_COOLDOWN = unit("action.cooldown");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REGENERATION_ACTIVE = unit("action.active.regeneration");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REGENERATION_COOLDOWN = unit("action.cooldown.regeneration");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_PROTECTOR_ACTIVE = unit("action.active.summon");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_PROTECTOR_COOLDOWN = unit("action.cooldown.summon");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_VAMPIRE_BATS_ACTIVE = unit("action.active.vampire_bat");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_VAMPIRE_BATS_COOLDOWN = unit("action.cooldown.vampire_bat");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_ACTIVE = unit("action.active.flying_sword");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_COOLDOWN = unit("action.cooldown.flying_sword");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_EQUIPPED = unit("action.flying_sword.equipped");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_NEEDLE_ACTIVE = unit("action.active.flying_needle");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_NEEDLE_COOLDOWN = unit("action.cooldown.flying_needle");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<UUID>>> FLYING_NEEDLES = MEMORY_MODULES.register("action.flying_needle.needles", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC.listOf())));

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BACKSTAB_ACTIVE = unit("action.active.backstab");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BACKSTAB_COOLDOWN = unit("action.cooldown.backstab");

        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_PROJECTILES_ACTIVE = unit("action.active.blood_projectiles");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_PROJECTILES_COOLDOWN = unit("action.cooldown.blood_projectiles");
        public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> SURROUNDED_TIMER = integer("dracula.surrounded_timer");

        private static void init() { }
    }

    static void register(IEventBus bus) {
        Dracula.init();
        MEMORY_MODULES.register(bus);
    }

    private static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> unit(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.of(Unit.CODEC)));
    }

    private static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> integer(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    }
}
