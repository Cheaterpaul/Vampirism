package de.teamlapen.vampirism.api.entity.factions;

public interface IFactionRaidEntity {
    boolean canJoinRaid();

    void setRaid(IFactionRaid<?> factionRaid);

    void setWave(int wave);

    void setCanJoinRaid(boolean b);

    void setJoinDelay(int i);

    void applyWaveBonus(int wave, boolean b);

    int getJoinDelay();

    int getWave();

    void setLeader(boolean b);

    boolean canBeLeader();
}
