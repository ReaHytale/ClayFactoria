package com.clayfactoria.codecs;

import static com.clayfactoria.utils.JobLocationHelper.findValidWalkLocation;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Job {

  public static final BuilderCodec<Job> CODEC =
      BuilderCodec.builder(Job.class, Job::new)
          .append(
              new KeyedCodec<>("Location", Vector3i.CODEC),
              (comp, position) -> comp.location = position,
              (comp) -> comp.location)
          .documentation("The Vector3i location of the block on which the task should be performed")
          .add()
          .append(
              new KeyedCodec<>("Task", Task.CODEC),
              (comp, task) -> comp.task = task,
              (comp) -> comp.task)
          .documentation("The task to take place for this job")
          .add()
          .append(
              new KeyedCodec<>("Walk Location", Vector3d.CODEC),
              (comp, position) -> comp.walkLocation = position,
              (comp) -> comp.walkLocation)
          .documentation("The Vector3d location for where the automaton should walk to")
          .add()
          .build();
  @Getter
  private Vector3i location;
  @Getter
  private Vector3d walkLocation;
  @Getter
  private Task task;
  @Getter
  private Box bounds;

  private Job() {
  }

  public Job(Vector3i location, Task task, World world) throws IllegalStateException {
    if (task.taskExecutor.usesBounds()) {
      throw new IllegalArgumentException("This job's task expects an area to work with!");
    }
    this.location = location;
    this.task = task;

    if (!task.locationEqualsWalkLocation) {
      walkLocation = findValidWalkLocation(world, location, location.toVector3d());
    } else {
      walkLocation = location.toVector3d().add(new Vector3d(0.5, 1, 0.5));
    }
  }

  public Job(Box bounds, Task task, World world) throws IllegalStateException {
    if (!task.taskExecutor.usesBounds()) {
      throw new IllegalArgumentException("This job's task expects a location to work with!");
    }
    task.taskExecutor.checkBounds(bounds);

    this.bounds = bounds;
    this.task = task;

    walkLocation =
        task.taskExecutor.findNextWalkLocationInBounds(
            bounds, world, bounds.min.clone().add(bounds.max).scale(0.5));
  }

  public void updateWalkLocation(World world, Vector3d from) {
    if (task.taskExecutor.usesBounds()) {
      walkLocation = task.taskExecutor.findNextWalkLocationInBounds(bounds, world, from);
    } else {
      walkLocation = findValidWalkLocation(world, location, from);
    }
  }

  public Job clone() {
    Job clone = new Job();
    clone.task = task;
    clone.location = location.clone();
    clone.walkLocation = walkLocation.clone();
    clone.bounds = bounds == null ? null : bounds.clone();
    return clone;
  }

}
