package de.teamlapen.vampirism.inventory.container;

import de.teamlapen.lib.lib.inventory.InventoryContainer;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.Task;
import de.teamlapen.vampirism.api.entity.player.task.TaskRequirement;
import de.teamlapen.vampirism.client.gui.TaskContainer;
import de.teamlapen.vampirism.client.gui.widget.TaskItem;
import de.teamlapen.vampirism.core.ModContainer;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.network.TaskActionPacket;
import de.teamlapen.vampirism.player.TaskManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class VampirismContainer extends InventoryContainer implements TaskContainer {

    public Map<UUID, Set<Task>> completableTasks = new HashMap<>();
    public Map<UUID, TaskManager.TaskBoardInfo> taskBoardInfos = new HashMap<>();
    public Map<UUID, Set<Task>> tasks = new HashMap<>();
    public Map<UUID, Map<Task, Map<ResourceLocation, Integer>>> completedRequirements = new HashMap<>();

    private final IFactionPlayer<?> factionPlayer;
    private final TextFormatting factionColor;

    private Runnable listener;

    public VampirismContainer(int id, PlayerInventory playerInventory) {
        super(ModContainer.vampirism, id, playerInventory, IWorldPosCallable.DUMMY, new Inventory(3), RemovingSelectorSlot::new, new SelectorInfo(stack -> true, 58, 8), new SelectorInfo(stack -> true, 58, 26), new SelectorInfo(stack -> true, 58, 44));
        this.factionPlayer = FactionPlayerHandler.get(playerInventory.player).getCurrentFactionPlayer().orElseThrow(() -> new IllegalStateException("Opening vampirism container without faction"));
        this.factionColor = factionPlayer.getFaction().getChatColor();
        addPlayerSlots(playerInventory, 37, 124);
    }

    @OnlyIn(Dist.CLIENT)
    public void init(@Nonnull Map<UUID, TaskManager.TaskBoardInfo> taskBoardInfos, @Nonnull Map<UUID, Set<Task>> tasks, @Nonnull Map<UUID, Set<Task>> completableTasks, @Nonnull Map<UUID, Map<Task, Map<ResourceLocation, Integer>>> completedRequirements) {
        this.taskBoardInfos = taskBoardInfos;
        this.tasks = tasks;
        this.completedRequirements = completedRequirements;
        this.completableTasks = completableTasks;
        if (this.listener != null) {
            this.listener.run();
        }
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }


    @Override
    public boolean isTaskNotAccepted(TaskItem.TaskInfo taskInfo) {
        return false;
    }

    @Override
    public boolean canCompleteTask(TaskItem.TaskInfo taskInfo) {
        return this.completableTasks.containsKey(taskInfo.getTaskBoard()) && this.completableTasks.get(taskInfo.getTaskBoard()).contains(taskInfo.getTask());
    }

    @Override
    public boolean pressButton(TaskItem.TaskInfo taskInfo) {
        VampirismMod.dispatcher.sendToServer(new TaskActionPacket(taskInfo.getTask(), taskInfo.getTaskBoard(), TaskAction.ABORT));
        this.tasks.get(taskInfo.getTaskBoard()).remove(taskInfo.getTask());
        if (this.listener != null) {
            this.listener.run();
        }
        return true;
    }

    @Override
    public TaskAction buttonAction(TaskItem.TaskInfo taskInfo) {
        return TaskContainer.TaskAction.ABORT;
    }

    @Override
    public boolean isCompleted(TaskItem.TaskInfo item) {
        return false;
    }

    @Override
    public TextFormatting getFactionColor() {
        return this.factionColor;
    }

    @Override
    public boolean areRequirementsCompleted(TaskItem.TaskInfo taskInfo, TaskRequirement.Type type) {
        Task task = taskInfo.getTask();
        if (this.completedRequirements != null) {
            if (this.completedRequirements.containsKey(taskInfo.getTaskBoard()) && this.completedRequirements.get(taskInfo.getTaskBoard()).containsKey(task)) {
                Map<ResourceLocation, Integer> data = this.completedRequirements.get(taskInfo.getTaskBoard()).get(task);
                for (TaskRequirement.Requirement<?> requirement : task.getRequirement().requirements().get(type)) {
                    if (!data.containsKey(requirement.getId()) || data.get(requirement.getId()) < requirement.getAmount(this.factionPlayer)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRequirementStatus(TaskItem.TaskInfo taskInfo, TaskRequirement.Requirement<?> requirement) {
        if (this.completedRequirements.containsKey(taskInfo.getTaskBoard()) && this.completedRequirements.get(taskInfo.getTaskBoard()).containsKey(taskInfo.getTask())) {
            return this.completedRequirements.get(taskInfo.getTaskBoard()).get(taskInfo.getTask()).get(requirement.getId());
        } else {
            return requirement.getAmount(this.factionPlayer);
        }
    }

    @Override
    public boolean isRequirementCompleted(TaskItem.TaskInfo taskInfo, TaskRequirement.Requirement<?> requirement) {
        if (this.completedRequirements != null) {
            if (this.completedRequirements.containsKey(taskInfo.getTaskBoard()) && this.completedRequirements.get(taskInfo.getTaskBoard()).containsKey(taskInfo.getTask())) {
                Map<ResourceLocation, Integer> data = this.completedRequirements.get(taskInfo.getTaskBoard()).get(taskInfo.getTask());
                return data.containsKey(requirement.getId()) && data.get(requirement.getId()) >= requirement.getAmount(this.factionPlayer);
            }
        }
        return false;
    }

    public static class RemovingSelectorSlot extends SelectorSlot {
        public RemovingSelectorSlot(IInventory inventoryIn, int index, SelectorInfo info, Consumer<IInventory> refreshInvFunc, Function<Integer, Boolean> activeFunc) {
            super(inventoryIn, index, info, refreshInvFunc, activeFunc);
        }

        @Override
        public boolean canTakeStack(@Nonnull PlayerEntity playerIn) {
            return false;
        }

        @Override
        public void onSlotChange(ItemStack oldStackIn, ItemStack newStackIn) {
            super.onSlotChange(oldStackIn, newStackIn);
            oldStackIn.shrink(1);
        }

        @Nonnull
        @Override
        public ItemStack onTake(@Nonnull PlayerEntity thePlayer, @Nonnull ItemStack stack) {
            return super.onTake(thePlayer, stack);
        }


        @Override
        public void putStack(@Nonnull ItemStack stack) {
            this.inventory.getStackInSlot(this.slotNumber).shrink(1);
            super.putStack(stack);
        }
    }

}
