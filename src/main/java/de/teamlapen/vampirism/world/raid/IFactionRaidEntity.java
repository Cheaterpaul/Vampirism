package de.teamlapen.vampirism.world.raid;

public interface IFactionRaidEntity {
    boolean canJoinRaid();

    void setRaid(FactionRaid<?> factionRaid);

    void setWave(int wave);

    void setCanJoinRaid(boolean b);

    void setJoinDelay(int i);

    void applyWaveBonus(int wave, boolean b);

    int getJoinDelay();

    int getWave();

    void setLeader(boolean b);

    boolean canBeLeader();
}
