package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import lombok.Getter;

/**
 * A task which is performable by an NPC. It is split into two parts
 * <ul>
 *   <li>A location to navigate to</li>
 *   <li>An NPC Action to perform at that location.</li>
 * </ul>
 */
public class Task {
  @Getter private Vector3d location;
  @Getter private Action action;

  public static final BuilderCodec<Task> CODEC =
      BuilderCodec.builder(Task.class, Task::new)
          .append(
              new KeyedCodec<>("Location", Vector3d.CODEC),
              (comp, position) -> comp.location = position,
              (comp) -> comp.location)
          .documentation("The Vector3d location for the task to take place")
          .add()
          .append(
              new KeyedCodec<>("Action", Action.CODEC),
              (comp, action) -> comp.action = action,
              (comp) -> comp.action)
          .documentation("The action to take place for this task")
          .add()
          .build();

  public Task() {}

  public Task(Vector3d location, Action action) {
    this.location = location;
    this.action = action;
  }

  @Override
  public String toString() {
    return String.format("<Task loc=%s action=%s>", location, action);
  }
}
