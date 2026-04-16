package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.clayfactoria.utils.TaskHelper.getHeldItemstack;
import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

public class DepositTaskExecutor extends PointTaskExecutor {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean canPerformTask(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        Component<ChunkStore> blockEntity = TaskHelper.getBlockEntity(ref);
        if (blockEntity == null) {
            return false;
        }

        ItemStack heldItemStack = getHeldItemstack(store, ref);

        ItemContainer inputContainer =
            TaskHelper.getItemContainerFromComponent(blockEntity, ContainerSlot.Input);
        Objects.requireNonNull(inputContainer, "Unexpected null input ItemContainer");

        ItemContainer fuelContainer =
            TaskHelper.getItemContainerFromComponent(blockEntity, ContainerSlot.Fuel);
        Objects.requireNonNull(fuelContainer, "Unexpected null fuel ItemContainer");

        // There must be space in the container for the item stack, and there must be space in hands
        return heldItemStack == null
            || fuelContainer.canAddItemStack(heldItemStack)
            || inputContainer.canAddItemStack(heldItemStack);
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

    private boolean deposit(ContainerSlot containerSlot, NPCEntity npcEntity, Job currentJob,
                            List<String> hotbarItems) {
        Store<EntityStore> store = Objects.requireNonNull(npcEntity.getReference()).getStore();
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
}
