package de.teamlapen.vampirism.api.entity.factions;

import de.teamlapen.vampirism.api.entity.CaptureEntityEntry;
import de.teamlapen.vampirism.api.entity.ITaskMasterEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public interface IVillageFactionData {

    IVillageFactionData INSTANCE = new IVillageFactionData() {
        @Override
        public Class<? extends MobEntity> getGuardSuperClass() {
            return MobEntity.class;
        }

        @Override
        public VillagerProfession getFactionVillageProfession() {
            return VillagerProfession.NONE;
        }

        @Override
        public List<CaptureEntityEntry> getCaptureEntries() {
            return Collections.emptyList();
        }

        @Override
        public Pair<Block, Block> getTotemTopBlock() {
            return Pair.of(Blocks.AIR, Blocks.AIR);
        }

        @Nullable
        @Override
        public EntityType<? extends ITaskMasterEntity> getTaskMasterEntity() {
            return null;
        }
    };

    Class<? extends MobEntity> getGuardSuperClass();

    VillagerProfession getFactionVillageProfession();

    List<CaptureEntityEntry> getCaptureEntries();

    /**
     * @return Pair of totem top blocks for faction. Left is the generated (crafted=false), right is the crafted
     */
    Pair<Block, Block> getTotemTopBlock();

    @Nullable
    EntityType<? extends ITaskMasterEntity> getTaskMasterEntity();

    @Nonnull
    default ItemStack getBanner() {
        return ItemStack.EMPTY; //TODO 1.17 move to INSTANCE
    }

    @Nullable
    default <T extends MobEntity & IFactionRaidEntity> Pair<EntityType<T>, int[]> getRaidWaveEntity(FactionRaidWaveMember waveMember) {
        return null; //TODO 1.17 move to INSTANCE
    }

    default <T extends MobEntity & IFactionRaidEntity> int getRaidWaveCount(FactionRaidWaveMember member, int wave) {
        Pair<EntityType<T>, int[]> data = getRaidWaveEntity(member);
        return data != null ? data.getValue()[MathHelper.clamp(wave, 0, data.getValue().length) - 1] : 0;
    }

    @Nullable
    default Effect getBadOmenEffect() {
        return null;
    }
}
