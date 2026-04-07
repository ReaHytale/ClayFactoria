package com.clayfactoria.codecs;

import static com.clayfactoria.utils.TaskHelper.getHeldItemstack;
import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Task implements Supplier<String> {
  DEPOSIT(
      "Deposit",
      "Deposit held item in an adjacent container",
      new Vector3f(0.59F, 0.29F, 0.89F), // Purple
      "ImageAssets/Deposit.png",
      Task::canDoDepositTask,
      Task::doDepositTask),
  TAKE(
      "Take",
      "Take an item from an adjacent container",
      new Vector3f(0.92F, 0.27F, 0.84F), // Pink
      "ImageAssets/Take.png",
      Task::canDoTakeTask,
      Task::doTakeTask),
  POSITION(
      "Position",
      "Do nothing (After moving to a position)",
      new Vector3f(0.93F, 0.22F, 0.35F), // Red
      "ImageAssets/Position.png",
      Task::canDoPositionTask,
      Task::doPositionTask),
  WORK(
      "Work",
      "Work at an adjacent workstation",
      new Vector3f(0.33F, 0.45F, 0.9F), // Blue
      "ImageAssets/Work.png",
      Task::canDoWorkTask,
      Task::doWorkTask);

  public static final Codec<Task> CODEC = new EnumCodec<>(Task.class);
  public final String name;
  public final String description;
  public final Vector3f color;
  public final String iconAssetPath;
  public final Function<Ref<EntityStore>, Boolean> canDoTask;
  public final Function<Ref<EntityStore>, Boolean> doTask;

  Task(
      String name,
      String description,
      Vector3f color,
      String iconAssetPath,
      Function<Ref<EntityStore>, Boolean> canDoTask,
      Function<Ref<EntityStore>, Boolean> doTask
  ) {
    this.name = name;
    this.description = description;
    this.color = color;
    this.iconAssetPath = iconAssetPath;
    this.canDoTask = canDoTask;
    this.doTask = doTask;
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

  private static boolean canDoDepositTask(Ref<EntityStore> entityRef) {
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

  private static boolean doDepositTask(Ref<EntityStore> entityRef) {
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

  private static boolean canDoWorkTask(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();

    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(entityRef, component);
    Objects.requireNonNull(npcEntity, "NPC Entity was null");

    Component<ChunkStore> nearbyPOI = TaskHelper.findNearbyPOI(npcEntity, Task.DEPOSIT);
    return nearbyPOI != null;
  }

  private static boolean doWorkTask(Ref<EntityStore> entityRef) {
    NPCEntity npcEntity = getNPCEntity(entityRef);
    Store<EntityStore> store = entityRef.getStore();
    JobComponent jobComponent = Objects.requireNonNull(
        store.getComponent(entityRef, JobComponent.getComponentType()));
    Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());
    World world = Objects.requireNonNull(npcEntity.getWorld());

    Vector3i pos = currentJob.getLocation();
    Ref<ChunkStore> blockRef = TaskHelper.getBlockComponentHolderDirectReference(world, pos.x,
        pos.y, pos.z);
    assert blockRef != null;
    ProcessingBenchBlock processingBenchBlock = blockRef.getStore().getComponent(blockRef,
        ProcessingBenchBlock.getComponentType());
    BenchBlock benchBlock = blockRef.getStore()
        .getComponent(blockRef, BenchBlock.getComponentType());

    if (processingBenchBlock == null || benchBlock == null) {
      return false;
    }
    processingBenchBlock.setActive(true, benchBlock, null);
    jobComponent.setComplete(true);
    return true;
  }

  private static boolean canDoPositionTask(Ref<EntityStore> entityRef) {
    // Can always do Position task...
    return true;
  }

  private static boolean doPositionTask(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();
    JobComponent jobComponent = Objects.requireNonNull(
        store.getComponent(entityRef, JobComponent.getComponentType()));
    jobComponent.setComplete(true);
    return true;
  }

  private static boolean canDoTakeTask(Ref<EntityStore> entityRef) {
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

    ItemContainer container =
        TaskHelper.getItemContainerFromComponent(nearbyPOI, ContainerSlot.Output);
    Objects.requireNonNull(container, "Unexpected null ItemContainer");

    // There must be items available to be taken, and there must be space in hands
    return heldItemStack == null && !container.isEmpty();
  }

  private static boolean doTakeTask(Ref<EntityStore> entityRef) {
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
    if (TaskHelper.transferItem(itemContainer, npcInventory)) {
      jobComponent.setComplete(true);
      return true;
    } else {
      return false;
    }
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
