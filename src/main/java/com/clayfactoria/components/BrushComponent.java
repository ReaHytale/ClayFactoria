package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.PathType;
import com.clayfactoria.components.JobBoxComponent.TaskBoxesComponent;
import com.clayfactoria.systems.TaskBoxSystem;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

public class BrushComponent implements Component<EntityStore> {

  @Nonnull
  public static final BuilderCodec<BrushComponent> CODEC =
      BuilderCodec.builder(BrushComponent.class, BrushComponent::new)
          .append(
              new KeyedCodec<>("Tasks", new ArrayCodec<>(Job.CODEC, Job[]::new)),
              (comp, tasks) -> comp.jobs = new ArrayList<>(Arrays.asList(tasks)),
              (comp) -> comp.jobs.toArray(new Job[0]))
          .documentation("The tasks for pathing and actions for each location")
          .add()
          .append(
              new KeyedCodec<>("PathType", PathType.CODEC),
              (comp, value) -> comp.pathType = value,
              (comp) -> comp.pathType)
          .documentation("Path type (LOOP or ONCE)")
          .add()
          .append(
              new KeyedCodec<>("TaskType", Task.CODEC),
              (comp, value) -> comp.task = value,
              (comp) -> comp.task)
          .documentation("Type of task to be added to the tasks list on next brush paint.")
          .add()
          .append(
              new KeyedCodec<>("SelectedEntity", Codec.UUID_STRING),
              (comp, value) -> comp.entityId = value,
              (comp) -> comp.entityId)
          .documentation("The entity's internal UUID.")
          .add()
          .build();

  @Getter private List<Job> jobs = new ArrayList<>();
  @Getter @Setter private PathType pathType = PathType.LOOP;
  @Getter @Setter @Nonnull private Task task = Task.TAKE;
  @Getter @Setter private UUID entityId;

  public static ComponentType<EntityStore, BrushComponent> getComponentType() {
    return ClayFactoria.brushComponentType;
  }

  public void addTask(
      Vector3i location,
      World world,
      ComponentAccessor<EntityStore> componentAccessor,
      Ref<EntityStore> playerRef) {
    this.jobs.add(new Job(location, task, world));
    TaskBoxesComponent taskBoxesComponent =
        componentAccessor.getComponent(playerRef, TaskBoxesComponent.getComponentType());
    if (taskBoxesComponent != null) {
      Box box = BlockUtils.getBlockBox(location, world);
      Vector3d min = box.min.subtract(TaskBoxSystem.BOX_PADDING);
      Vector3d max = box.max.add(TaskBoxSystem.BOX_PADDING);
      taskBoxesComponent.boxes.add(new JobBoxComponent(task.color, new Box(min, max)));
    }
  }

  public PathType togglePathType() {
    if (pathType == PathType.ONCE) {
      pathType = PathType.LOOP;
    } else {
      pathType = PathType.ONCE;
    }

    return pathType;
  }

  public Component<EntityStore> clone() {
    BrushComponent brushComponent = new BrushComponent();
    brushComponent.jobs = this.jobs;
    brushComponent.pathType = this.pathType;
    brushComponent.task = this.task;
    return brushComponent;
  }

  public void resetTasks(
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> playerRef) {
    this.jobs = new ArrayList<>();
    TaskBoxesComponent taskBoxesComponent =
        componentAccessor.getComponent(playerRef, TaskBoxesComponent.getComponentType());
    if (taskBoxesComponent != null) {
      taskBoxesComponent.boxes.clear();
    }
  }
}
