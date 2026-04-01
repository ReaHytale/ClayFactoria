package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.PathType;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.DebugBoxComponent.DebugBoxesComponent;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

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

          .append(
              new KeyedCodec<>("TaskType", Action.CODEC),
              (comp, value) -> comp.action = value,
              (comp) -> comp.action)
          .documentation("Type of task to be added to the tasks list on next brush paint.")
          .add()

          .append(
              new KeyedCodec<>("SelectedEntity", Codec.UUID_STRING),
              (comp, value) -> comp.entityId = value,
              (comp) -> comp.entityId)
          .documentation("The entity's internal UUID.")
          .add()

          .build();

  @Getter
  private List<Task> tasks = new ArrayList<>();
  @Getter
  @Setter
  private PathType pathType = PathType.LOOP;
  @Getter
  @Setter
  @Nonnull
  private Action action = Action.TAKE;
  @Getter
  @Setter
  private UUID entityId;

  public static ComponentType<EntityStore, BrushComponent> getComponentType() {
    return ClayFactoria.brushComponentType;
  }

  public void addTask(Vector3i location, World world,
      boolean locationEqualsWalkLocation, ComponentAccessor<EntityStore> componentAccessor,
      Ref<EntityStore> playerRef) {
    this.tasks.add(new Task(location, action, world, locationEqualsWalkLocation));
    DebugBoxesComponent debugBoxesComponent = componentAccessor.getComponent(playerRef,
        DebugBoxesComponent.getComponentType());
    if (debugBoxesComponent != null) {
      debugBoxesComponent.boxes.add(new DebugBoxComponent(
          new Vector3f(1F, 0, 0),
          BlockUtils.getBlockBox(location, world)));
    }
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
    brushComponent.action = this.action;
    return brushComponent;
  }

  public void resetTasks(ComponentAccessor<EntityStore> componentAccessor,
      Ref<EntityStore> playerRef) {
    Player player = Objects.requireNonNull(
        componentAccessor.getComponent(playerRef, Player.getComponentType()));
    player.sendMessage(Message.raw("Resetting path...").color(Color.RED));
    this.tasks = new ArrayList<>();
    DebugBoxesComponent debugBoxesComponent = componentAccessor.getComponent(playerRef,
        DebugBoxesComponent.getComponentType());
    if (debugBoxesComponent != null) {
      debugBoxesComponent.boxes.clear();
    }
  }
}
