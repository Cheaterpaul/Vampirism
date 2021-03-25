package de.teamlapen.vampirism.api.entity.vampire;

import de.teamlapen.vampirism.api.difficulty.IAdjustableLevel;
import de.teamlapen.vampirism.api.entity.IVillageCaptureEntity;
import de.teamlapen.vampirism.api.entity.factions.IFactionRaidEntity;

/**
 * Interface for the basic vampire mob.
 * Do not implement yourself
 */
public interface IBasicVampire extends IVampireMob, IAdjustableLevel, IVillageCaptureEntity, IFactionRaidEntity {
    int TYPES = 126;

    /**
     * @return A randomly selected but permanent integer between 0 and {@link IBasicVampire#TYPES} or -1 if not selected yet.
     */
    int getEntityTextureType();

    @Override
    default boolean canBeLeader() {
        return false;
    }

    @Override
    default void setLeader(boolean b) {
    }

    @Override
    default boolean isLeader() {
        return false;
    }
}
