package com.clayfactoria.components;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Task;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Keeps track of a circular queue of {@link Task Tasks} for an entity.
 */
public class TaskComponent implements Component<EntityStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Nonnull
  public static final BuilderCodec<TaskComponent> CODEC =
      BuilderCodec.builder(TaskComponent.class, TaskComponent::new)
          .append(
              new KeyedCodec<>("PlayerId", Codec.UUID_BINARY),
              (comp, id) -> comp.playerId = id,
              comp -> comp.playerId)
          .documentation("The player id")
          .add()
          .append(
              new KeyedCodec<>("Tasks", new ArrayCodec<>(Task.CODEC, Task[]::new)),
              (comp, tasks) -> comp.tasks = new ArrayList<>(Arrays.asList(tasks)),
              (comp) -> comp.tasks.toArray(new Task[0]))
          .documentation("The tasks for pathing and actions for each location")
          .add()
          .append(
              new KeyedCodec<>("CurrentTask", Task.CODEC),
              (comp, task) -> comp.currentTask = task,
              (comp) -> comp.currentTask)
          .documentation("The current task with location and action")
          .add()
          .append(
              new KeyedCodec<>("CurrentTargetIndex", Codec.INTEGER),
              (comp, index) -> comp.currentTargetIndex = index,
              (comp) -> comp.currentTargetIndex)
          .documentation("The index for current target")
          .add()
          .append(
              new KeyedCodec<>("IsComplete", Codec.BOOLEAN),
              (comp, bool) -> comp.isComplete = bool,
              (comp) -> comp.isComplete)
          .documentation("Flag for when a task is complete or not")
          .add()
          .build();

  @Getter @Setter @Nullable private UUID playerId;
  @Getter @Setter private List<Task> tasks = new ArrayList<>();
  @Getter @Setter @Nullable private Task currentTask;
  @Getter @Setter private int currentTargetIndex;
  @Getter @Setter private boolean isComplete = false;

  /**
   * Move on to the next task in the queue
   * @return The next task in the queue
   */
  @Nullable
  public Task nextTask() {
    isComplete = false;
    currentTargetIndex = (currentTargetIndex + 1) % tasks.size(); // Loop
    currentTask = tasks.get(currentTargetIndex);
    LOGGER.atInfo().log("Next task is: %s", currentTask);
    return currentTask;
  }

  @Override
  public Component<EntityStore> clone() {
    TaskComponent taskComponent = new TaskComponent();
    taskComponent.playerId = this.playerId;
    taskComponent.tasks = this.tasks;
    taskComponent.currentTask = this.currentTask;
    taskComponent.currentTargetIndex = this.currentTargetIndex;
    taskComponent.isComplete = this.isComplete;
    return taskComponent;
  }

  public static ComponentType<EntityStore, TaskComponent> getComponentType() {
    return ClayFactoria.ownerComponentType;
  }
}
