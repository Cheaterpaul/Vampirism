package de.teamlapen.vampirism.api.entity.factions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IFactionRaid<T extends MobEntity & IFactionRaidEntity> {

    boolean isOver();

    boolean isBetweenWaves();

    boolean isStopped();

    boolean isVictory();

    boolean isLoss();

    World getWorld();

    boolean isStarted();

    void joinRaid(int wave, T raider, @Nullable BlockPos pos, boolean bool);

    boolean joinRaid(int wave, T newRaider, boolean modifyHealth);

    void updateBarPercentage();

    void leaveRaid(T raider, boolean updateHealth);

    BlockPos getCenter();

    int getId();

    boolean isActive();

    int getGroupsSpawned();

    void addHero(Entity entity);
}
