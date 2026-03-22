package com.clayfactoria.sensors;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.sensors.builders.BuilderSensorLeashTarget;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Sense whether the NPC should continue to path or should move to action state.
 */
public class SensorLeashTarget extends SensorBaseLogger {

  private static final int RECOMPUTE_WALK_DISTANCE_POLL_SECONDS = 2;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final PositionProvider positionProvider = new PositionProvider();
  private double lastDistanceSquared;
  private long lastDistanceUpdateTime = System.currentTimeMillis();

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
          "Current Task was null. Clearing Position Provider");
      this.positionProvider.clear();
      return false;
    }

    Vector3d currentTarget = currentTask.getWalkLocation();
    if (currentTarget == null) {
      LOGGER.atInfo().log(
          "Current Target was null. Clearing Position Provider");
      this.positionProvider.clear();
      return false;
    }

    double distanceSquared = transformComponent.getPosition().distanceSquaredTo(currentTarget);
    if (distanceSquared == lastDistanceSquared) {
      if (System.currentTimeMillis() - lastDistanceUpdateTime >
          RECOMPUTE_WALK_DISTANCE_POLL_SECONDS * 1000L) {
        try {
          currentTask.findValidWalkLocation(
              Objects.requireNonNull(TaskHelper.getNPCEntity(ref, store).getWorld()));
        } catch (IllegalStateException exception) {
          // All fine, none was found
        }
        lastDistanceUpdateTime = System.currentTimeMillis();
        return false;
      }
    } else {
      lastDistanceSquared = distanceSquared;
      lastDistanceUpdateTime = System.currentTimeMillis();
    }

    if (distanceSquared > 0.1f) {
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
        return false;
      }

      Task nextTask = taskComponent.nextTask();

      if (nextTask == null) {
        this.positionProvider.clear();
        LOGGER.atInfo().log("nextTask was null. Clearing Position Provider");
        return false;
      }

      Vector3d nextTaskLocation = nextTask.getWalkLocation();
      if (nextTaskLocation == null) {
        this.positionProvider.clear();
        LOGGER.atInfo().log(
            "nextTaskLocation was null. Clearing Position Provider");
        return false;
      }

      this.positionProvider.setTarget(nextTaskLocation);
      LOGGER.atInfo().log(String.format(
          "Sensor Leash Target: Setting Next Target from %s to %s",
          currentTarget,
          nextTaskLocation
      ));
      return true;
    }
  }

  @Override
  public InfoProvider getSensorInfo() {
    return this.positionProvider;
  }
}
