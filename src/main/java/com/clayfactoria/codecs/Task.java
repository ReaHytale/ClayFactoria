package com.clayfactoria.codecs;

import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent.Hotbar;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
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
      Task::canDoDepositTask),
  TAKE(
      "Take",
      "Take an item from an adjacent container",
      new Vector3f(0.92F, 0.27F, 0.84F), // Pink
      "ImageAssets/Take.png",
      Task::canDoTakeTask),
  POSITION(
      "Position",
      "Do nothing (After moving to a position)",
      new Vector3f(0.93F, 0.22F, 0.35F), // Red
      "ImageAssets/Position.png",
      Task::canDoPositionTask),
  WORK(
      "Work",
      "Work at an adjacent workstation",
      new Vector3f(0.33F, 0.45F, 0.9F), // Blue
      "ImageAssets/Work.png",
      Task::canDoWorkTask);

  public static final Codec<Task> CODEC = new EnumCodec<>(Task.class);
  public final String name;
  public final String description;
  public final Vector3f color;
  public final String iconAssetPath;
  public final Function<Ref<EntityStore>, Boolean> canDoTask;

  Task(
      String name,
      String description,
      Vector3f color,
      String iconAssetPath,
      Function<Ref<EntityStore>, Boolean> canDoTask) {
    this.name = name;
    this.description = description;
    this.color = color;
    this.iconAssetPath = iconAssetPath;
    this.canDoTask = canDoTask;
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }

  private static ItemStack getHeldItemstack(Store<EntityStore> store, Ref<EntityStore> entityRef) {
    Hotbar hotbar = store.getComponent(entityRef, Hotbar.getComponentType());
    assert hotbar != null;
    return hotbar.getActiveItem();
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

  private static boolean canDoWorkTask(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();

    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(entityRef, component);
    Objects.requireNonNull(npcEntity, "NPC Entity was null");

    Component<ChunkStore> nearbyPOI = TaskHelper.findNearbyPOI(npcEntity, Task.DEPOSIT);
    return nearbyPOI != null;
  }

  private static boolean canDoPositionTask(Ref<EntityStore> entityRef) {
    // Can always do Position task...
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
}
