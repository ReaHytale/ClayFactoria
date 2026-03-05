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
      LOGGER.atSevere().log("Task is complete");
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

    // If the task is anything but a position task, check that a POI is nearby first.
    if (action != Action.POSITION) {
      Vector3i nearbyContainerLocation = TaskHelper.findNearbyPOI(npcEntity, action);
      return nearbyContainerLocation != null;
    }
    return true;
  }

  @Override
  public InfoProvider getSensorInfo() {
    return null;
  }
}
