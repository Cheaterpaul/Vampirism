package de.teamlapen.vampirism.world.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.FactionRaidWaveMember;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IVillageFactionData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FactionRaid<T extends MobEntity & IFactionRaidEntity> {
    private static final ITextComponent RAID = new TranslationTextComponent("event.minecraft.raid");
    private static final ITextComponent VICTORY = new TranslationTextComponent("event.minecraft.raid.victory");
    private static final ITextComponent DEFEAT = new TranslationTextComponent("event.minecraft.raid.defeat");
    private static final ITextComponent RAID_VICTORY = RAID.deepCopy().appendString(" - ").append(VICTORY);
    private static final ITextComponent RAID_DEFEAT = RAID.deepCopy().appendString(" - ").append(DEFEAT);
    private final Map<Integer, T> leaders = Maps.newHashMap();
    private final Map<Integer, Set<T>> raiders = Maps.newHashMap();
    private final Set<UUID> heroes = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int badOmenLevel;
    private boolean active;
    private int groupsSpawned;
    private final ServerBossInfo bossInfo = new ServerBossInfo(RAID, BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10);
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random = new Random();
    private final int numGroups;
    private Status status;
    private int celebrationTicks;
    private Optional<BlockPos> waveSpawnPos = Optional.empty();
    private final IFaction<?> attackingFaction;
    private final IFaction<?> defendingFaction;
    private final IVillageFactionData villageData;


    public FactionRaid(int id, ServerWorld world, BlockPos pos, IFaction<?> attackingFaction, IFaction<?> defendingFaction) {
        this.id = id;
        this.world = world;
        this.active = true;
        this.preRaidTicks = 300;
        this.bossInfo.setPercent(0.0F);
        this.center = pos;
        this.numGroups = this.getWaves(world.getDifficulty());
        this.status = Status.ONGOING;
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.villageData = attackingFaction.getVillageData();
    }

    public FactionRaid(ServerWorld world, CompoundNBT nbt) {
        this.world = world;
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.badOmenLevel = nbt.getInt("BadOmenLevel");
        this.groupsSpawned = nbt.getInt("GroupsSpawned");
        this.preRaidTicks = nbt.getInt("PreRaidTicks");
        this.postRaidTicks = nbt.getInt("PostRaidTicks");
        this.totalHealth = nbt.getFloat("TotalHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.numGroups = nbt.getInt("NumGroups");
        this.status = Status.getByName(nbt.getString("Status"));
        this.heroes.clear();
        if (nbt.contains("HeroesOfTheVillage", 9)) {
            ListNBT listnbt = nbt.getList("HeroesOfTheVillage", 11);

            for (net.minecraft.nbt.INBT inbt : listnbt) {
                this.heroes.add(NBTUtil.readUniqueId(inbt));
            }
        }
        this.attackingFaction = VampirismAPI.factionRegistry().getFactionByID(new ResourceLocation(nbt.getString("attacking_faction")));
        this.defendingFaction = VampirismAPI.factionRegistry().getFactionByID(new ResourceLocation(nbt.getString("defending_faction")));
        this.villageData = this.attackingFaction.getVillageData();
    }

    public boolean isOver() {
        return this.isVictory() || this.isLoss();
    }

    public boolean isBetweenWaves() {
        return this.alreadySpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean isStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean isVictory() {
        return this.status == Status.VICTORY;
    }

    public boolean isLoss() {
        return this.status == Status.LOSS;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return groupsSpawned;
    }

    private Predicate<ServerPlayerEntity> getParticipantsPredicate() {
        return playerEntity -> {
            BlockPos pos = playerEntity.getPosition();
            return playerEntity.isAlive() && FactionRaidManager.getManager(this.world).findRaid(pos, 9216) == this;
        };
    }

    private void updateBossInfoVisibility() {
        Set<ServerPlayerEntity> set = Sets.newHashSet(this.bossInfo.getPlayers());
        List<ServerPlayerEntity> list = this.world.getPlayers(this.getParticipantsPredicate());

        for (ServerPlayerEntity serverPlayerEntity : list) {
            if (!set.contains(serverPlayerEntity)) {
                this.bossInfo.addPlayer(serverPlayerEntity);
            }
        }

        for (ServerPlayerEntity serverPlayerEntity : set) {
            if (!list.contains(serverPlayerEntity)) {
                this.bossInfo.removePlayer(serverPlayerEntity);
            }
        }
    }

    public int getMaxLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.badOmenLevel;
    }

    public void increaseLevel(ServerPlayerEntity playerEntity) {
        if (playerEntity.isPotionActive(Effects.BAD_OMEN)) {//TODO new effect
            this.badOmenLevel += playerEntity.getActivePotionEffect(Effects.BAD_OMEN).getAmplifier() + 1;
            this.badOmenLevel = MathHelper.clamp(this.badOmenLevel, 0, this.getMaxLevel());
        }

        playerEntity.removePotionEffect(Effects.BAD_OMEN);
    }

    public void stop() {
        this.active = false;
        this.bossInfo.removeAllPlayers();
        this.status = Status.STOPPED;
    }

    public void tick() {
        if (!this.isStopped()) {
            if (this.status == Status.ONGOING) {
                boolean flag = this.active;
                this.active = this.world.isBlockLoaded(this.center);
                if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                    this.stop();
                    return;
                }

                if (flag != this.active) {
                    this.bossInfo.setVisible(this.active);
                }

                if (!this.active) {
                    return;
                }

                if (!this.world.isVillage(this.center)) {
                    this.moveRaiderCenterToNearbyVillageSection();
                }

                if (!this.world.isVillage(this.center)) {
                    if (this.groupsSpawned > 0) {
                        this.status = Status.LOSS;
                    } else {
                        this.stop();
                    }
                }

                ++this.ticksActive;
                if (this.ticksActive >= 48000) {
                    this.stop();
                    return;
                }

                int i = this.getRaiderCount();
                if (i == 0 && this.hasMoreWaves()) {
                    if (this.preRaidTicks <= 0) {
                        if (this.preRaidTicks == 0 && this.groupsSpawned > 0) {
                            this.preRaidTicks = 300;
                            this.bossInfo.setName(RAID);
                            return;
                        }
                    } else {
                        boolean flag1 = this.waveSpawnPos.isPresent();
                        boolean flag2 = !flag1 && this.preRaidTicks % 5 == 0;
                        if (flag1 && !this.world.getChunkProvider().isChunkLoaded(new ChunkPos(this.waveSpawnPos.get()))) {
                            flag2 = true;
                        }

                        if (flag2) {
                            int j = 0;
                            if (this.preRaidTicks < 40) {
                                j = 2;
                            } else if (this.preRaidTicks < 100) {
                                j = 1;
                            }

                            this.waveSpawnPos = this.getValidSpawnPos(j);
                        }

                        if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                            this.updateBossInfoVisibility();
                        }

                        --this.preRaidTicks;
                        this.bossInfo.setPercent(MathHelper.clamp((300 - this.preRaidTicks) / 300F, 0f, 1f));
                    }
                }

                if (this.ticksActive % 20 == 0) {
                    this.updateBossInfoVisibility();
                    this.updateRaiders();
                    if (i > 0) {
                        if (i <= 2) {
                            this.bossInfo.setName(RAID.deepCopy().appendString(" - ").append(new TranslationTextComponent("event.minecraft.raid.raiders_remaining", i)));
                        } else {
                            this.bossInfo.setName(RAID);
                        }
                    } else {
                        this.bossInfo.setName(RAID);
                    }
                }

                boolean flag3 = false;
                int k = 0;

                while (this.shouldSpawnGroup()) {
                    int finalK = k;
                    BlockPos blockPos = this.waveSpawnPos.orElseGet(() -> this.findRandomSpawnPos(finalK, 20));
                    if (blockPos != null) {
                        this.started = true;
                        this.spawnNextWave(blockPos);
                        if (!flag3) {
                            this.playWaveStartSound(blockPos);
                            flag3 = true;
                        }
                    } else {
                        ++k;
                    }

                    if (k > 3) {
                        this.stop();
                        break;
                    }
                }

                if (this.isStarted() && !this.hasMoreWaves() && i == 0) {
                    if (this.postRaidTicks < 40) {
                        ++this.postRaidTicks;
                    } else {
                        this.status = Status.VICTORY;

                        for (UUID hero : this.heroes) {
                            Entity entity = this.world.getEntityByUuid(hero);
                            if (entity instanceof LivingEntity && !entity.isSpectator()) {
                                LivingEntity livingEntity = ((LivingEntity) entity);
                                livingEntity.addPotionEffect(new EffectInstance(Effects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                                if (livingEntity instanceof ServerPlayerEntity) {
                                    ServerPlayerEntity serverPlayerEntity = ((ServerPlayerEntity) livingEntity);
                                    serverPlayerEntity.addStat(Stats.RAID_WIN);
                                    CriteriaTriggers.HERO_OF_THE_VILLAGE.trigger(serverPlayerEntity);
                                }
                            }
                        }
                    }
                }
                this.markDirty();
            } else if (this.isOver()) {
                ++this.celebrationTicks;
                if (this.celebrationTicks >= 600) {
                    this.stop();
                    return;
                }

                if (this.celebrationTicks % 20 == 0) {
                    this.updateBossInfoVisibility();
                    this.bossInfo.setVisible(true);
                    if (this.isVictory()) {
                        this.bossInfo.setPercent(0);
                        this.bossInfo.setName(RAID_VICTORY);
                    } else {
                        this.bossInfo.setName(RAID_DEFEAT);
                    }
                }
            }
        }
    }

    private void moveRaiderCenterToNearbyVillageSection() {
        Stream<SectionPos> stream = SectionPos.getAllInBox(SectionPos.from(this.center), 2);
        stream.filter(this.world::isVillage).map(SectionPos::getCenter).min(Comparator.comparingDouble((pos) -> pos.distanceSq(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getValidSpawnPos(int poi) {
        for (int i = 0; i < 3; ++i) {
            BlockPos blockPos = this.findRandomSpawnPos(poi, 1);
            if (blockPos != null) {
                return Optional.of(blockPos);
            }
        }
        return Optional.empty();
    }

    private boolean hasMoreWaves() {
        if (this.hasBonusWaves()) {
            return !this.hasSpawnedBonusWave();
        } else {
            return !this.isFinalWave();
        }
    }

    private boolean isFinalWave() {
        return this.getGroupsSpawned() == this.numGroups;
    }

    private boolean hasBonusWaves() {
        return this.badOmenLevel > 1;
    }

    private boolean hasSpawnedBonusWave() {
        return this.getGroupsSpawned() > this.numGroups;
    }

    private boolean shouldSpawnBonusGroup() {
        return this.isFinalWave() && this.getRaiderCount() == 0 && this.hasBonusWaves();
    }

    private void updateRaiders() {
        Iterator<Set<T>> iterator = this.raiders.values().iterator();
        Set<T> set = new HashSet<>();

        while (iterator.hasNext()) {
            Set<T> set1 = iterator.next();

            for (T raider : set1) {
                BlockPos blockPos = raider.getPosition();
                if (raider.isAlive() && raider.world.getDimensionKey() == this.world.getDimensionKey() && !(this.center.distanceSq(blockPos) >= 12544)) {
                    if (raider.ticksExisted > 600) {
                        if (this.world.getEntityByUuid(raider.getUniqueID()) == null) {
                            set.add(raider);
                        }

                        if (!this.world.isVillage(blockPos) && raider.getIdleTime() > 2400) {
                            raider.setJoinDelay(raider.getJoinDelay() + 1);
                        }

                        if (raider.getJoinDelay() >= 30) {
                            set.add(raider);
                        }
                    } else {
                        set.add(raider);
                    }
                }
            }
        }

        for (T raider : set) {
            this.leaveRaid(raider, true);
        }
    }

    private void playWaveStartSound(BlockPos blockPos) {
        float f = 13.0F;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bossInfo.getPlayers();

        for (ServerPlayerEntity serverPlayerEntity : collection) {
            Vector3d vector3d = serverPlayerEntity.getPositionVec();
            Vector3d vector3d1 = Vector3d.copyCentered(blockPos);
            float f1 = MathHelper.sqrt((vector3d1.x - vector3d.x) * (vector3d1.x - vector3d.x) + (vector3d1.z - vector3d.z) * (vector3d1.z - vector3d.z));
            double d0 = vector3d.x + (double) (13.0F / f1) * (vector3d1.x - vector3d.x);
            double d1 = vector3d.z + (double) (13.0F / f1) * (vector3d1.z - vector3d.z);
            if (f1 <= 64.0F || collection.contains(serverPlayerEntity)) {
                serverPlayerEntity.connection.sendPacket(new SPlaySoundEffectPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, d0, serverPlayerEntity.getPosY(), d1, 64.0F, 1.0F));
            }
        }
    }

    private void spawnNextWave(BlockPos pos) {
        boolean flag = false;
        int i = this.groupsSpawned + 1;
        this.totalHealth = 0.0F;
        DifficultyInstance difficultyInstance = this.world.getDifficultyForLocation(pos);
        boolean flag1 = this.shouldSpawnBonusGroup();

        for (FactionRaidWaveMember value : FactionRaidWaveMember.values()) {
            int j = this.getDefaultNumSpawns(value, i, flag1) + this.getPotentialBonusSpawns(value, this.random, i, difficultyInstance, flag1);
            int k = 0;
            for (int l = 0; l < j; ++l) {
                T raider = (T) this.villageData.getRaidWaveEntity(value).getKey().create(this.world);
                if (!flag && raider.canBeLeader()) {
                    raider.setLeader(true);
                    this.setLeader(i, raider);
                    flag = true;
                }

                this.joinRaid(i, raider, pos, false);
            }
        }

        this.waveSpawnPos = Optional.empty();
        ++this.groupsSpawned;
        this.updateBarPercentage();
        this.markDirty();
    }

    public void joinRaid(int wave, T raider, @Nullable BlockPos pos, boolean bool) {
        boolean flag = this.joinRaid(wave, raider);
        if (flag) {
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setCanJoinRaid(true);
            raider.setJoinDelay(0);
            if (!bool && pos != null) {
                raider.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                raider.onInitialSpawn(this.world, this.world.getDifficultyForLocation(pos), SpawnReason.EVENT, null, null);
                raider.applyWaveBonus(wave, false);
                raider.setOnGround(true);
                this.world.func_242417_l(raider);
            }
        }
    }

    public void updateBarPercentage() {
        this.bossInfo.setPercent(MathHelper.clamp(this.getCurrentHealth() / this.totalHealth, 0, 1));
    }

    public float getCurrentHealth() {
        float f = 0;

        for (Set<T> raiders : this.raiders.values()) {
            for (T raider : raiders) {
                f += raider.getHealth();
            }
        }

        return f;
    }

    public boolean shouldSpawnGroup() {
        return this.preRaidTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getRaiderCount() == 0;
    }

    public int getRaiderCount() {
        return this.raiders.values().stream().mapToInt(Set::size).sum();
    }

    public void leaveRaid(T raider, boolean updateHealth) {
        Set<T> set = this.raiders.get(raider.getWave());
        if (set != null) {
            boolean flag = set.remove(raider);
            if (flag) {
                if (updateHealth) {
                    this.totalHealth -= raider.getHealth();
                }

                raider.setRaid(null);
                this.updateBarPercentage();
                this.markDirty();
            }
        }
    }

    private void markDirty() {
        FactionRaidManager.getManager(this.world).markDirty();
    }

    @Nullable
    public T getLeader(int s) {
        return this.leaders.get(s);
    }

    @Nullable
    private BlockPos findRandomSpawnPos(int a, int b) {
        int i = a == 0 ? 2 : 2 - a;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int i1 = 0; i1 < b; i1++) {
            float f = this.world.rand.nextFloat() * ((float) Math.PI * 2F);
            int j = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F * (float) i) + this.world.rand.nextInt(5);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F * (float) i) + this.world.rand.nextInt(5);
            int k = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, j, l);
            pos.setPos(j, k, l);
            if ((!this.world.isVillage(pos) || a >= 2) && this.world.isAreaLoaded(pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10, pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10) && this.world.getChunkProvider().isChunkLoaded(new ChunkPos(pos)) && (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, this.world, pos, EntityType.RAVAGER) || this.world.getBlockState(pos.down()).isIn(Blocks.SNOW) && this.world.getBlockState(pos).isAir())) {
                return pos;
            }
        }
        return null;
    }

    private boolean joinRaid(int wave, T raider) {
        return this.joinRaid(wave, raider, true);
    }

    private boolean joinRaid(int wave, T newRaider, boolean modifyHealth) {
        Set<T> set = this.raiders.computeIfAbsent(wave, wave1 -> new HashSet<>());
        T raider = null;
        for (T existingRaider : set) {
            if (existingRaider.getUniqueID().equals(newRaider.getUniqueID())) {
                raider = existingRaider;
                break;
            }
        }

        if (raider != null) {
            set.remove(raider);
            set.add(newRaider);
        }

        set.add(newRaider);
        if (modifyHealth) {
            this.totalHealth += newRaider.getHealth();
        }

        this.updateBarPercentage();
        this.markDirty();
        return true;
    }

    public void setLeader(int wave, T leader) {
        this.leaders.put(wave, leader);
        leader.setItemStackToSlot(EquipmentSlotType.HEAD, this.villageData.getBanner());
        leader.setDropChance(EquipmentSlotType.HEAD, 2.0F);
    }

    public void removeLeader(int wave) {
        this.leaders.remove(wave);
    }

    public BlockPos getCenter() {
        return null;
    }

    public void setCenter(BlockPos center) {
        this.center = center;
    }

    public int getId() {
        return this.id;
    }

    private int getDefaultNumSpawns(FactionRaidWaveMember member, int wave, boolean bool) {
        return villageData.getRaidWaveCount(member, bool ? this.numGroups : wave);
    }

    private int getPotentialBonusSpawns(FactionRaidWaveMember member, Random random, int wave, DifficultyInstance difficultyInstance, boolean bool) {
        Difficulty difficulty = difficultyInstance.getDifficulty();
        boolean flag = difficulty == Difficulty.EASY;
        boolean flag1 = difficulty == Difficulty.NORMAL;
        int i;
        switch (member) {
            case STANDARD:
            case ADVANCED:
                if (flag) {
                    i = random.nextInt(2);
                } else if (flag1) {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            case BEAST:
                i = !flag && bool ? 1 : 0;
                break;
            default:
                return 0;
        }

        return i > 0 ? random.nextInt(i + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Id", this.id);
        nbt.putBoolean("Started", this.started);
        nbt.putBoolean("Active", this.active);
        nbt.putLong("TicksActive", this.ticksActive);
        nbt.putInt("BadOmenLevel", this.badOmenLevel);
        nbt.putInt("GroupsSpawned", this.groupsSpawned);
        nbt.putInt("PreRaidTicks", this.preRaidTicks);
        nbt.putInt("PostRaidTicks", this.postRaidTicks);
        nbt.putFloat("TotalHealth", this.totalHealth);
        nbt.putInt("NumGroups", this.numGroups);
        nbt.putString("Status", this.status.getName());
        nbt.putInt("CX", this.center.getX());
        nbt.putInt("CY", this.center.getY());
        nbt.putInt("CZ", this.center.getZ());
        ListNBT listnbt = new ListNBT();

        for (UUID uuid : this.heroes) {
            listnbt.add(NBTUtil.func_240626_a_(uuid));
        }

        nbt.put("HeroesOfTheVillage", listnbt);

        nbt.putString("attacking_faction", this.attackingFaction.getID().toString());
        nbt.putString("defending_faction", this.defendingFaction.getID().toString());
        return nbt;
    }

    public int getWaves(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 3;
            case NORMAL:
                return 5;
            case HARD:
                return 7;
            default:
                return 0;
        }
    }

    public float getEnchantOdds() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1F;
        } else if (i == 3) {
            return 0.25F;
        } else if (i == 4) {
            return 0.5F;
        } else {
            return i == 5 ? 0.75F : 0.0F;
        }
    }

    public void addHero(Entity entity) {
        this.heroes.add(entity.getUniqueID());
    }

    public boolean alreadySpawned() {
        return false;
    }

    enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES = values();

        private static Status getByName(String name) {
            for (Status status : VALUES) {
                if (name.equalsIgnoreCase(status.name())) {
                    return status;
                }
            }

            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}
