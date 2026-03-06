package com.clayfactoria.sensors;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.sensors.builders.BuilderSensorCanDoAction;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
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
    checkNull(taskComponent, "TaskComponent was null");

    if (taskComponent.isComplete()) {
      return false;
    }

    Task currentTask = taskComponent.getCurrentTask();
    checkNull(currentTask, "Current Task was null");
    Action currentAction = currentTask.getAction();

    // Current queued action isn't the action we're sensing for in this case
    if (currentAction == null || currentAction != action) {
      return false;
    }

    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    checkNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(ref, component);
    checkNull(npcEntity, "NPCEntity was null");

    // If the action is POSITION, we don't need to do anything and so it can always do the action.
    if (action == Action.POSITION) {
      return true;
    }

    // Otherwise, check that there's a nearby POI for the action.
    Vector3i nearbyPOILocation = TaskHelper.findNearbyPOI(npcEntity, action);
    if (nearbyPOILocation == null) {
      return false;
    }

    // If this is a TAKE or DEPOSIT action, we have to check that the container is ready.
    if (action == Action.TAKE || action == Action.DEPOSIT) {
      ItemContainer container = TaskHelper.getItemContainerAtPos(npcEntity.getWorld(), nearbyPOILocation, null);
      checkNull(container);

      // Simple item container, no input/output/fuel.
      if (container.getClass() == SimpleItemContainer.class) {
        // There must be items available to be taken
        if (action == Action.TAKE) {
          return !container.isEmpty();
        }
        // Action is DEPOSIT, there must be space to deposit.
        ItemStack heldItemStack = npcEntity.getInventory().getItemInHand();
        checkNull(heldItemStack);
        return container.canAddItemStack(heldItemStack);
      }
      // Combined item container, we have different slots that items could go in.
      else if (container.getClass() == CombinedItemContainer.class) {
        CombinedItemContainer combinedItemContainer = (CombinedItemContainer) container;
        // There must be items available to be taken
        if (action == Action.TAKE) {
          ItemContainer output = combinedItemContainer.getContainer(2);
          return !output.isEmpty();
        }
        // There must be space in either the fuel or input containers for DEPOSIT to happen.
        ItemContainer input = combinedItemContainer.getContainer(0);
        ItemContainer fuel = combinedItemContainer.getContainer(1);
        ItemStack heldItemStack = npcEntity.getInventory().getItemInHand();
        checkNull(heldItemStack);
        return input.canAddItemStack(heldItemStack) || fuel.canAddItemStack(heldItemStack);
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
