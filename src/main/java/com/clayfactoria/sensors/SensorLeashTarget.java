package com.clayfactoria.sensors;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.sensors.builders.BuilderSensorLeashTarget;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.jspecify.annotations.NonNull;

/**
 * Senses whether the NPC should continue to path or should move to action state.
 * <br><br>
 * TODO: make comment more descriptive; is left here because name "SensorLeashTarget"
 *       is not too clear
 */
public class SensorLeashTarget extends SensorBaseLogger {

  private static final int RECOMPUTE_WALK_DISTANCE_POLL_SECONDS = 2;
  private static final double EQUAL_DISTANCE_EPSILON = 0.1;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final PositionProvider positionProvider = new PositionProvider();
  private double lastDistanceSquared;
  private long lastDistanceUpdateTime = System.currentTimeMillis();
  private boolean recomputeFirstWalkDistance = true;

  public SensorLeashTarget(@Nonnull BuilderSensorLeashTarget builderSensorLeash) {
    super(builderSensorLeash);
  }

  private static void recomputeWalkLocation(@NonNull Ref<EntityStore> ref,
      @NonNull Store<EntityStore> store,
      Job currentJob) {
    if (currentJob.getTask().locationEqualsWalkLocation) {
      return;
    }
    try {
      NPCEntity entity = TaskHelper.getNPCEntity(ref);
      currentJob.updateWalkLocation(entity.getWorld(), entity.getOldPosition());
    } catch (IllegalStateException exception) {
      // All fine, none was found
    }
  }

  @Override
  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {

    TransformComponent transformComponent =
        store.getComponent(ref, TransformComponent.getComponentType());
    Objects.requireNonNull(transformComponent);

    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    Objects.requireNonNull(jobComponent);

    Job currentJob = jobComponent.getCurrentJob();
    if (currentJob == null) {
      this.positionProvider.clear();
      return false;
    }

    if (recomputeFirstWalkDistance) {
      recomputeWalkLocation(ref, store, currentJob);
      recomputeFirstWalkDistance = false;
    }

    Vector3d currentTarget = currentJob.getWalkLocation();
    if (currentTarget == null) {
      this.positionProvider.clear();
      return false;
    }

    double distanceSquared = transformComponent.getPosition().distanceSquaredTo(currentTarget);

    if (Math.abs(distanceSquared - lastDistanceSquared) <= EQUAL_DISTANCE_EPSILON) {
      if (hasUpdatedWalkLocation(ref, store, currentJob)) {
        return false;
      }
    } else {
      lastDistanceSquared = distanceSquared;
      lastDistanceUpdateTime = System.currentTimeMillis();
    }

    if (distanceSquared > EQUAL_DISTANCE_EPSILON) { // walking to destination
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
    } else {  // reached destination
      if (!jobComponent.isComplete()) {
        return false;
      }

      Job nextJob = jobComponent.nextJob();

      if (nextJob == null) {
        this.positionProvider.clear();
        return false;
      }

      recomputeWalkLocation(ref, store, nextJob);

      Vector3d nextJobLocation = nextJob.getWalkLocation();
      if (nextJobLocation == null) {
        this.positionProvider.clear();
        return false;
      }

      this.positionProvider.setTarget(nextJobLocation);
      return true;
    }
  }

  private boolean hasUpdatedWalkLocation(@NonNull Ref<EntityStore> ref,
      @NonNull Store<EntityStore> store,
      Job currentJob) {
    if (hasNotChangedLocationInSomeTime()) {
      recomputeWalkLocation(ref, store, currentJob);
      lastDistanceUpdateTime = System.currentTimeMillis();
      return true;  // yes, the npc "has updated its walk location"
    } else {
      return false;
    }
  }

  private boolean hasNotChangedLocationInSomeTime() {
    return System.currentTimeMillis() - lastDistanceUpdateTime >
        RECOMPUTE_WALK_DISTANCE_POLL_SECONDS * 1000L;
  }

  @Override
  public InfoProvider getSensorInfo() {
    return this.positionProvider;
  }
}
