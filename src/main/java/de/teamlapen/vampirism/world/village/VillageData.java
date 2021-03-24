package de.teamlapen.vampirism.world.village;

import de.teamlapen.vampirism.api.entity.factions.FactionRaidWaveMember;
import de.teamlapen.vampirism.api.entity.factions.IFactionRaidEntity;
import de.teamlapen.vampirism.api.entity.factions.IVillageFactionData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class VillageData implements IVillageFactionData {

    private final Map<FactionRaidWaveMember, Pair<EntityType<?>, int[]>> members = new HashMap<>(FactionRaidWaveMember.values().length);
    private final ItemStack banner;

    public VillageData(ItemStack banner) {
        this.banner = banner;
    }

    /**
     * @throws ClassCastException if type is not instance of MobEntity
     */
    public <T extends MobEntity & IFactionRaidEntity> VillageData add(FactionRaidWaveMember member, EntityType<T> type, int[] waveCount) {
        members.put(member, Pair.of(type, waveCount));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends MobEntity & IFactionRaidEntity> Pair<EntityType<T>, int[]> getRaidWaveEntity(FactionRaidWaveMember waveMember) {
        return ((Pair<EntityType<T>, int[]>) (Object) members.get(waveMember));
    }

    @Nonnull
    @Override
    public ItemStack getBanner() {
        return banner.copy();
    }
}
