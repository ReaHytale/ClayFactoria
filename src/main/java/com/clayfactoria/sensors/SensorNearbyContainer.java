package com.clayfactoria.sensors;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.utils.TaskHelper;
import com.clayfactoria.sensors.builders.BuilderSensorNearbyContainer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorNearbyContainer extends SensorBaseLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final Action action;

  public SensorNearbyContainer(
      @Nonnull BuilderSensorNearbyContainer builder, @Nonnull BuilderSupport builderSupport) {
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
      LOGGER.atSevere().log("Task is complete");
      return false;
    }

    Task currentTask = taskComponent.getCurrentTask();
    checkNull(currentTask, "Current Task was null");

    Action currentAction = currentTask.getAction();

    LOGGER.atInfo().log(
        "Current Task: Action ('%s') at position: (%.0f, %.0f, %.0f) for sensor, sensing: %s",
        currentAction,
        currentTask.getLocation().x,
        currentTask.getLocation().y,
        currentTask.getLocation().z,
        action);

    if (currentAction == null || currentAction != action) {
      LOGGER.atSevere().log(
          String.format(
              "Current action ('%s') != action to sense ('%s')",
              currentAction, action));
      return false;
    }

    LOGGER.atInfo().log(
        String.format(
            "Current action ('%s') == action ('%s') to sense",
            currentAction, action));
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    checkNull(component, "NPC Entity Component Type was null");

    NPCEntity npcEntity = store.getComponent(ref, component);
    checkNull(npcEntity, "NPCEntity was null");

    Vector3i nearbyContainerLocation = TaskHelper.findNearbyContainer(npcEntity);
    return nearbyContainerLocation != null;
  }

  @Override
  public InfoProvider getSensorInfo() {
    return null;
  }
}
