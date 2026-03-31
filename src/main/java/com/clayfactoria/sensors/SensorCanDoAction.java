package com.clayfactoria.sensors;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.sensors.builders.BuilderSensorCanDoAction;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SensorCanDoAction extends SensorBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final Action action;

  public SensorCanDoAction(
      @Nonnull BuilderSensorCanDoAction builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.action = builder.getAction(builderSupport);
  }

  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    Objects.requireNonNull(taskComponent, "TaskComponent was null");

    if (taskComponent.isComplete()) {
      return false;
    }

    Task currentTask = taskComponent.getCurrentTask();
    Objects.requireNonNull(currentTask, "Current Task was null");
    Action currentAction = currentTask.getAction();

    // Current queued action isn't the action we're sensing for in this case
    if (currentAction == null || currentAction != action) {
      return false;
    }

    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(ref, component);
    Objects.requireNonNull(npcEntity, "NPCEntity was null");

    // If the action is POSITION, we don't need to do anything and so it can always do the action.
    if (action == Action.POSITION) {
      return true;
    }

    // Otherwise, check that there's a nearby POI for the action.
    Component<ChunkStore> nearbyPOI = TaskHelper.findNearbyPOI(npcEntity, action);
    if (nearbyPOI == null) {
      return false;
    }

    // If this is a TAKE or DEPOSIT action, we have to check that the container is ready.
    if (action == Action.TAKE || action == Action.DEPOSIT) {
      // TODO: Replace with non-deprecated method of accessing NPC inventory.
      ItemStack heldItemStack = npcEntity.getInventory().getItemInHand();

      if (action == Action.TAKE) {
        ItemContainer container = TaskHelper.getItemContainerFromComponent(nearbyPOI,
            ContainerSlot.Output);
        Objects.requireNonNull(container, "Unexpected null ItemContainer");
        // There must be items available to be taken, and there must be space in hands
        return heldItemStack == null && !container.isEmpty();
      } else {
        ItemContainer inputContainer = TaskHelper.getItemContainerFromComponent(nearbyPOI,
            ContainerSlot.Input);
        Objects.requireNonNull(inputContainer, "Unexpected null input ItemContainer");
        ItemContainer fuelContainer = TaskHelper.getItemContainerFromComponent(
            nearbyPOI, ContainerSlot.Fuel);
        Objects.requireNonNull(fuelContainer, "Unexpected null fuel ItemContainer");
        // There must be space in the container for the item stack, and there must be space in hands
        Objects.requireNonNull(heldItemStack);
        return heldItemStack != null && (fuelContainer.canAddItemStack(heldItemStack)
            || inputContainer.canAddItemStack(heldItemStack));
      }
      // There must be items in the output container for TAKE to happen

    }

    return true;
  }

  @Override
  public InfoProvider getSensorInfo() {
    return null;
  }
}
