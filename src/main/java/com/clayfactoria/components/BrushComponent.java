package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.PathType;
import com.clayfactoria.codecs.Task;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrushComponent implements Component<EntityStore> {
  @Nonnull
  public static final BuilderCodec<BrushComponent> CODEC =
      BuilderCodec.builder(BrushComponent.class, BrushComponent::new)
          .append(
              new KeyedCodec<>("Tasks", new ArrayCodec<>(Task.CODEC, Task[]::new)),
              (comp, tasks) -> comp.tasks = new ArrayList<>(Arrays.asList(tasks)),
              (comp) -> comp.tasks.toArray(new Task[0]))
          .documentation("The tasks for pathing and actions for each location")
          .add()
          .append(
              new KeyedCodec<>("PathType", PathType.CODEC),
              (comp, value) -> comp.pathType = value,
              (comp) -> comp.pathType)
          .documentation("Path type (LOOP or ONCE)")
          .add()
          .build();

  @Getter @Setter private List<Task> tasks = new ArrayList<>();
  @Getter @Setter private PathType pathType = PathType.LOOP;

  public void addTask(Vector3d location, Action action) {
    this.tasks.add(new Task(location, action));
  }

  // TODO: Switch this to Action.TAKE || Action.DEPOSIT and use for switching between task types
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
    brushComponent.tasks = this.tasks;
    brushComponent.pathType = this.pathType;
    return brushComponent;
  }

  public static ComponentType<EntityStore, BrushComponent> getComponentType() {
    return ClayFactoria.brushComponentType;
  }
}
