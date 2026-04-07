package com.clayfactoria.codecs.task;

import static com.clayfactoria.utils.TaskHelper.getHeldItemstack;
import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Objects;

public class DepositTaskExecutor extends PointTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();

    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(entityRef, component);
    Objects.requireNonNull(npcEntity, "NPC Entity was null");

    Component<ChunkStore> nearbyPOI = TaskHelper.findNearbyPOI(npcEntity, Task.DEPOSIT);
    if (nearbyPOI == null) {
      return false;
    }

    ItemStack heldItemStack = getHeldItemstack(store, entityRef);

    ItemContainer inputContainer =
        TaskHelper.getItemContainerFromComponent(nearbyPOI, ContainerSlot.Input);
    Objects.requireNonNull(inputContainer, "Unexpected null input ItemContainer");

    ItemContainer fuelContainer =
        TaskHelper.getItemContainerFromComponent(nearbyPOI, ContainerSlot.Fuel);
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

    // Attempt to deposit as fuel first (if this is a station with a fuel slot)
    if (deposit(ContainerSlot.Fuel, npcEntity, currentJob)) {
      jobComponent.setComplete(true);
      return true;
    } else if (deposit(ContainerSlot.Input, npcEntity, currentJob)) {
      jobComponent.setComplete(true);
      return true;
    } else {
      return false;
    }
  }

  private static boolean deposit(ContainerSlot containerSlot, NPCEntity npcEntity, Job currentJob) {
    Store<EntityStore> store = Objects.requireNonNull(npcEntity.getReference()).getStore();
    ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(
        Objects.requireNonNull(npcEntity.getWorld()),
        currentJob.getLocation(),
        containerSlot);
    Objects.requireNonNull(itemContainer);

    ItemContainer npcInventory = TaskHelper.getNPCInventory(npcEntity, store);
    return TaskHelper.transferItem(npcInventory, itemContainer);
  }

}
