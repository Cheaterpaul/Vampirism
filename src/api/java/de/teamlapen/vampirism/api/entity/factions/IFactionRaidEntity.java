package de.teamlapen.vampirism.api.entity.factions;

public interface IFactionRaidEntity extends IFactionEntity {
    boolean canJoinRaid();

    IFactionRaid<?> getRaid();

    boolean isRaidActive();

    void setRaid(IFactionRaid<?> factionRaid);

    void setWave(int wave);

    void setCanJoinRaid(boolean b);

    void setJoinDelay(int i);

    void applyWaveBonus(int wave, boolean b);

    int getJoinDelay();

    int getWave();

    void setLeader(boolean b);

    boolean isLeader();

    boolean canBeLeader();
}
