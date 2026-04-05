package com.clayfactoria.sensors;

import com.clayfactoria.codecs.Task;
import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.sensors.builders.BuilderSensorCanDoTask;
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

public class SensorCanDoTask extends SensorBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final Task task;

  public SensorCanDoTask(
      @Nonnull BuilderSensorCanDoTask builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.task = builder.getAction(builderSupport);
  }

  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    Objects.requireNonNull(jobComponent, "TaskComponent was null");

    if (jobComponent.isComplete()) {
      return false;
    }

    Job currentJob = jobComponent.getCurrentJob();
    Objects.requireNonNull(currentJob, "Current Task was null");
    Task currentTask = currentJob.getTask();

    // Current queued action isn't the action we're sensing for in this case
    if (currentTask == null || currentTask != task) {
      return false;
    }

    return currentTask.canDoTask.apply(ref);
  }

  @Override
  public InfoProvider getSensorInfo() {
    return null;
  }
}
