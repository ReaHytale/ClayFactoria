package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Objects;

import static com.clayfactoria.utils.TaskHelper.getHeldItemstack;
import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

public class TakeTaskExecutor extends PointTaskExecutor {

    @Override
    public boolean canPerformTask(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        Component<ChunkStore> blockEntity = TaskHelper.getBlockEntity(ref);
        if (blockEntity == null) {
            return false;
        }

        ItemStack heldItemStack = getHeldItemstack(store, ref);

        ItemContainer container =
            TaskHelper.getItemContainerFromComponent(blockEntity, ContainerSlot.Output);
        Objects.requireNonNull(container, "Unexpected null ItemContainer");

        // There must be items available to be taken, and there must be space in hands
        return heldItemStack == null && !container.isEmpty();
    }

    @Override
    public boolean execute(Ref<EntityStore> entityRef) {
        NPCEntity npcEntity = getNPCEntity(entityRef);
        Store<EntityStore> store = entityRef.getStore();
        JobComponent jobComponent = Objects.requireNonNull(
            store.getComponent(entityRef, JobComponent.getComponentType()));
        Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());

        ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(
            Objects.requireNonNull(npcEntity.getWorld()),
            currentJob.getLocation(), ContainerSlot.Output);
        Objects.requireNonNull(itemContainer);

        ItemContainer npcInventory = TaskHelper.getNPCInventory(npcEntity, store);
        return TaskHelper.transferItem(itemContainer, npcInventory, 1);
    }

    @Override
    public Task relevantNextTask() {
        return Task.DEPOSIT;
    }

}
