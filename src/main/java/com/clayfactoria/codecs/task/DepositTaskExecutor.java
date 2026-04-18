package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

public class DepositTaskExecutor extends PointTaskExecutor {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean canPerformTask(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        NPCEntity npc = getNPCEntity(ref);
        List<String> hotbarItems = TaskHelper.getHotbarItems(npc.getRole());

        JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
        assert jobComponent != null;
        Job job = jobComponent.getCurrentJob();
        assert job != null;

        // There must be space in the container for the item stack, and there must be space in hands
        return canDeposit(ContainerSlot.Input, npc, job, hotbarItems)
            || canDeposit(ContainerSlot.Fuel, npc, job, hotbarItems);
    }

    @Override
    public boolean execute(Ref<EntityStore> entityRef) {
        NPCEntity npcEntity = getNPCEntity(entityRef);
        Store<EntityStore> store = entityRef.getStore();
        JobComponent jobComponent = Objects.requireNonNull(
            store.getComponent(entityRef, JobComponent.getComponentType()));
        Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());

        List<String> hotbarItems = TaskHelper.getHotbarItems(npcEntity.getRole());

        // Attempt to deposit as fuel first (if this is a station with a fuel slot)
        if (deposit(ContainerSlot.Fuel, npcEntity, currentJob, hotbarItems)) {
            return true;
        } else {
            return deposit(ContainerSlot.Input, npcEntity, currentJob, hotbarItems);
        }
    }

    @Override
    public Task relevantNextTask(List<Task> availableOptions) {
        if (availableOptions.contains(Task.TAKE)) {
            return Task.TAKE;
        } else if (availableOptions.contains(Task.WORK)) {
            return Task.WORK;
        } else if (availableOptions.contains(Task.HARVEST)) {
            return Task.HARVEST;
        }
        return Task.DEPOSIT;
    }

    private boolean deposit(ContainerSlot containerSlot, NPCEntity npcEntity, Job currentJob,
                            List<String> hotbarItems) {
        Store<EntityStore> store = Objects.requireNonNull(npcEntity.getReference()).getStore();
        assert currentJob.getLocation() != null;
        ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(
            Objects.requireNonNull(npcEntity.getWorld()),
            currentJob.getLocation(),
            containerSlot);
        Objects.requireNonNull(itemContainer);

        ItemContainer npcInventory = TaskHelper.getNPCInventory(npcEntity, store);
        AtomicBoolean fail = new AtomicBoolean(false);
        npcInventory.forEach((slot, itemStack) -> {
            if (!hotbarItems.contains(itemStack.getItemId())) {
                fail.set(!TaskHelper.transferItem(npcInventory, itemContainer, slot));
            }
        });
        return !fail.get();
    }

    private boolean canDeposit(ContainerSlot containerSlot, NPCEntity npcEntity, Job currentJob,
                               List<String> hotbarItems) {
        Store<EntityStore> store = Objects.requireNonNull(npcEntity.getReference()).getStore();
        assert currentJob.getLocation() != null;
        ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(
            Objects.requireNonNull(npcEntity.getWorld()),
            currentJob.getLocation(),
            containerSlot);
        Objects.requireNonNull(itemContainer);

        ItemContainer npcInventory = TaskHelper.getNPCInventory(npcEntity, store);
        List<ItemStack> itemStacks = new ArrayList<>();
        npcInventory.forEach((_, itemStack) -> {
            if (!hotbarItems.contains(itemStack.getItemId())) {
                itemStacks.add(itemStack);
            }
        });
        return itemContainer.canAddItemStacks(itemStacks);
    }
}
