package com.clayfactoria.sensors;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.sensors.builders.BuilderSensorLeashTarget;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;

import javax.annotation.Nonnull;

public class SensorLeashTarget extends SensorBaseLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final PositionProvider positionProvider = new PositionProvider();

  public SensorLeashTarget(@Nonnull BuilderSensorLeashTarget builderSensorLeash) {
    super(builderSensorLeash);
  }

  @Override
  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent =
          store.getComponent(ref, TransformComponent.getComponentType());
      checkNull(transformComponent, "Transform Component was null");

      TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
      checkNull(taskComponent, "Task Component was null");

      Task currentTask = taskComponent.getCurrentTask();
      if (currentTask == null) {
        LOGGER.atInfo().log(
            "Sensor Leash Target: Current Task was null. Clearing Position Provider");
        this.positionProvider.clear();
        return false;
      }

      Vector3d currentTarget = currentTask.getLocation();
      if (currentTarget == null) {
        LOGGER.atInfo().log(
            "Sensor Leash Target: Current Target was null. Clearing Position Provider");
        this.positionProvider.clear();
        return false;
      }

      if (transformComponent.getPosition().distanceSquaredTo(currentTarget) > 0.2f) {
        Ref<EntityStore> target = this.positionProvider.getTarget();
        if (target == null) {
          this.positionProvider.setTarget(currentTarget);
          return true;
        }

        Store<EntityStore> targetStore = target.getStore();
        TransformComponent targetTransform =
            targetStore.getComponent(target, TransformComponent.getComponentType());
        assert targetTransform != null;

        if (targetTransform.getPosition() == currentTarget) {
          return false;
        } else {
          this.positionProvider.setTarget(currentTarget);
          return true;
        }
      } else {

        if (!taskComponent.isComplete()) {
          LOGGER.atInfo().log(
              "Sensor Leash Target: Has Action is true and there is an incomplete task to fulfill");
          return false;
        }

        Task nextTask = taskComponent.nextTask();

        if (nextTask == null) {
          this.positionProvider.clear();
          LOGGER.atInfo().log("Sensor Leash Target: nextTask was null. Clearing Position Provider");
          return false;
        }

        Vector3d nextTaskLocation = nextTask.getLocation();
        if (nextTaskLocation == null) {
          this.positionProvider.clear();
          LOGGER.atInfo().log(
              "Sensor Leash Target: nextTaskLocation was null. Clearing Position Provider");
          return false;
        }

        this.positionProvider.setTarget(nextTaskLocation);
        LOGGER.atInfo().log(
            String.format(
                "Sensor Leash Target: Setting Next Target from (%.0f, %.0f, %.0f) to (%.0f, %.0f, %.0f)",
                currentTarget.x,
                currentTarget.y,
                currentTarget.z,
                nextTaskLocation.x,
                nextTaskLocation.y,
                nextTaskLocation.z));
        return true;
      }
  }

  @Override
  public InfoProvider getSensorInfo() {
    return this.positionProvider;
  }
}
