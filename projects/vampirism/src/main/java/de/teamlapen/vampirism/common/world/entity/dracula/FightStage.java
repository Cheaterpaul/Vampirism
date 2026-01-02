package de.teamlapen.vampirism.common.world.entity.dracula;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FightStage implements StringRepresentable {
    NONE,
    PHASE_1,
    PHASE_2,
    PHASE_3;

    public static Codec<FightStage> CODEC = StringRepresentable.fromEnum(FightStage::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}