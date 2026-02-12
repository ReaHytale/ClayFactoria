package com.clayfactoria.sensors;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.helpers.TaskHelper;
import com.clayfactoria.sensors.builders.BuilderSensorNearbyContainer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorNearbyContainer extends SensorBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final Action action;

  public SensorNearbyContainer(
      @Nonnull BuilderSensorNearbyContainer builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.action = builder.getAction(builderSupport);
  }

  public boolean matches(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
    if (!super.matches(ref, role, dt, store)) {
      return false;
    }

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    if (taskComponent == null) {
      LOGGER.atSevere().log("Sensor Nearby Container: Task Component was null");
      return false;
    }

    if (taskComponent.isComplete()){
      LOGGER.atSevere().log("Sensor Nearby Container: Task is complete");
      return false;
    }

    Task currentTask = taskComponent.getCurrentTask();
    if (currentTask == null) {
      LOGGER.atSevere().log("Sensor Nearby Container: Current Task was null");
      return false;
    }

    Action currentAction = currentTask.getAction();
    if (currentAction == null || currentAction != action) {
      LOGGER.atSevere().log("Sensor Nearby Container: Current action != action to sense");
      return false;
    }

    LOGGER.atInfo().log(
        String.format(
            "Sensor Nearby Container: Current action ('%s') == action ('%s') to sense",
            currentAction, action));
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    if (component == null) {
      LOGGER.atSevere().log("Sensor Nearby Container-> NPCEntity Component Type was null");
      return false;
    }

    NPCEntity npcEntity = store.getComponent(ref, component);
    if (npcEntity == null) {
      LOGGER.atSevere().log("Sensor Nearby Container -> NPCEntity was null");
      return false;
    }

    Vector3i nearbyContainerLocation = TaskHelper.findNearbyContainer(npcEntity);
    if (nearbyContainerLocation == null) {
      LOGGER.atInfo().log("Sensor Nearby Container -> Nearby Container Location was null");
      return false;
    } else {
      LOGGER.atInfo().log("Sensor Nearby Container -> Nearby Container Location was found");
      return true;
    }
  }

  @Override
  public InfoProvider getSensorInfo() {
    return null;
  }
}
