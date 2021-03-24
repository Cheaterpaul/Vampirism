package de.teamlapen.vampirism.world.raid;

import com.google.common.collect.Lists;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionRaidEntity;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.tileentity.TotemHelper;
import de.teamlapen.vampirism.tileentity.TotemTileEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class FactionRaidManager extends WorldSavedData {
    private final static String ID = "vampirism-faction-raids-";
    private final Map<Integer, FactionRaid<?>> byId = new HashMap<>();
    private final ServerWorld world;
    private int nextAvailableId;
    private int tick;

    public FactionRaidManager(ServerWorld world) {
        super(createName(world.getDimensionType()));
        this.world = world;
        this.nextAvailableId = 1;
        this.markDirty();
    }

    public static FactionRaidManager getManager(final ServerWorld world) {
        return world.getSavedData().getOrCreate(() -> new FactionRaidManager(world), ID + world.getDimensionType().getSuffix());
    }

    public FactionRaid<?> get(int id) {
        return this.byId.get(id);
    }

    public void tick() {
        ++this.tick;

        Iterator<FactionRaid<?>> iterator = this.byId.values().iterator();

        while (iterator.hasNext()) {
            FactionRaid<?> raid = iterator.next();
            if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
                raid.stop();
            }

            if (raid.isStopped()) {
                iterator.remove();
                this.markDirty();
            } else {
                raid.tick();
            }
        }

        if (this.tick % 200 == 0) {
            this.markDirty();
        }
    }

    public static <T extends LivingEntity & IFactionRaidEntity> boolean canJoinRaid(T entity, FactionRaid<?> raid) {
        if (entity != null && raid != null && raid.getWorld() != null) {
            return entity.isAlive() && entity.canJoinRaid() && entity.getIdleTime() <= 2400 && entity.world.getDimensionType() == raid.getWorld().getDimensionType();
        } else {
            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public FactionRaid<?> badOmenTick(ServerPlayerEntity playerEntity) {
        FactionPlayerHandler handler = FactionPlayerHandler.getOpt(playerEntity).orElse(null);
        IPlayableFaction<?> faction = handler == null ? null : handler.getCurrentFaction();
        if (faction == null) {
            return null;
        } else if (playerEntity.isSpectator()) {
            return null;
        } else if (this.world.getGameRules().getBoolean(GameRules.DISABLE_RAIDS)) {
            return null;
        } else {
            DimensionType dimensionType = playerEntity.world.getDimensionType();
            if (!dimensionType.isHasRaids()) {
                return null;
            } else {
                BlockPos blockPos = playerEntity.getPosition();
                List<PointOfInterest> list = this.world.getPointOfInterestManager().func_219146_b(PointOfInterestType.MATCH_ANY, blockPos, 64, PointOfInterestManager.Status.IS_OCCUPIED).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    Optional<TotemTileEntity> tile = TotemHelper.getTotemAtPos(world, list);
                    if (tile.isPresent()) {
                        IFaction<?> totemFaction = tile.get().getControllingFaction();
                        if (totemFaction == null || faction != totemFaction) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }

                int i = 0;
                Vector3d vector3d = Vector3d.ZERO;

                for (PointOfInterest pointOfInterest : list) {
                    BlockPos blockPos1 = pointOfInterest.getPos();
                    vector3d = vector3d.add(blockPos1.getX(), blockPos1.getY(), blockPos1.getZ());
                    ++i;
                }

                BlockPos blockPos1;
                if (i > 0) {
                    vector3d = vector3d.scale(1.0d / i);
                    blockPos1 = new BlockPos(vector3d);
                } else {
                    blockPos1 = blockPos;
                }
                IPlayableFaction<?> attackingFaction = getRandomAttackingFaction(faction);
                FactionRaid<?> raid = this.findOrCreateRaid(playerEntity.getServerWorld(), blockPos1, attackingFaction, faction);
                boolean flag = false;
                if (!raid.isStarted()) {
                    if (!this.byId.containsKey(raid.getId())) {
                        this.byId.put(raid.getId(), raid);
                    }

                    flag = true;
                } else if (raid.getBadOmenLevel() < raid.getMaxLevel()) {
                    flag = true;
                } else {
                    playerEntity.removePotionEffect(attackingFaction.getVillageData().getBadOmenEffect());//TODO change
                    playerEntity.connection.sendPacket(new SEntityStatusPacket(playerEntity, ((byte) 43)));
                }

                if (flag) {
                    raid.increaseLevel(playerEntity);
                    playerEntity.connection.sendPacket(new SEntityStatusPacket(playerEntity, (byte) 43));
                    if (!raid.alreadySpawned()) {
                        playerEntity.addStat(Stats.RAID_TRIGGER);
                        CriteriaTriggers.VOLUNTARY_EXILE.trigger(playerEntity);
                    }
                }

                this.markDirty();
                return raid;
            }
        }
    }

    private IPlayableFaction<?> getRandomAttackingFaction(IPlayableFaction<?> defendingFaction) {
        List<IPlayableFaction<?>> factions = Lists.newArrayList(VampirismAPI.factionRegistry().getPlayableFactions());
        factions.remove(defendingFaction);
        return factions.get(world.getRandom().nextInt(factions.size() - 1));
    }

    private FactionRaid<?> findOrCreateRaid(ServerWorld world, BlockPos pos, IPlayableFaction<?> attackingFaction, IPlayableFaction<?> defendingFaction) {
        FactionRaid<?> raid = findRaid(pos, 9216);
        if (raid != null) return raid;
        return new FactionRaid<>(this.incrementNextId(), world, pos, attackingFaction, defendingFaction);
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.nextAvailableId = nbt.getInt("next_available_id");
        this.tick = nbt.getInt("tick");
        ListNBT listNBT = nbt.getList("raids", 10);

        for (int i = 0; i < listNBT.size(); i++) {
            CompoundNBT compoundNBT = listNBT.getCompound(i);
            FactionRaid<?> raid = new FactionRaid<>(this.world, compoundNBT);
            this.byId.put(raid.getId(), raid);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("next_available_id", this.nextAvailableId);
        compound.putInt("tick", this.tick);
        ListNBT listNBT = new ListNBT();

        for (FactionRaid<?> value : this.byId.values()) {
            CompoundNBT compoundNBT = new CompoundNBT();
            value.write(compoundNBT);
            listNBT.add(compoundNBT);
        }
        compound.put("raids", listNBT);
        return compound;
    }

    private int incrementNextId() {
        return ++this.nextAvailableId;
    }

    @Nullable
    public FactionRaid<?> findRaid(BlockPos pos, int distance) {
        FactionRaid<?> raid = null;
        double d0 = distance;

        for (FactionRaid<?> value : this.byId.values()) {
            double d1 = value.getCenter().distanceSq(pos);
            if (value.isActive() && d1 < d0) {
                raid = value;
                d0 = d1;
            }
        }
        return raid;
    }

    public static String createName(DimensionType dimensionType) {
        return "faction_raids" + dimensionType.getSuffix();
    }
}
