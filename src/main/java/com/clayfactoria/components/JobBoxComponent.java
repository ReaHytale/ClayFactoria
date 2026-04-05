package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class JobBoxComponent implements Component<EntityStore> {

  public static final BuilderCodec<JobBoxComponent> CODEC = BuilderCodec.builder(
          JobBoxComponent.class, JobBoxComponent::new)
      .append(
          new KeyedCodec<>("Colour", Vector3f.CODEC),
          (comp, colour) -> comp.colour = colour,
          comp -> comp.colour
      )
      .documentation("RGB colour to use for the box.")
      .add()
      .append(
          new KeyedCodec<>("Box", Box.CODEC),
          (comp, pos) -> comp.box = pos,
          comp -> comp.box
      )
      .documentation("Box to draw the debug box around")
      .add()
      .build();

  @Getter
  private Vector3f colour;
  @Getter
  private Box box;

  private JobBoxComponent() {
  }

  public JobBoxComponent(Vector3f colour, Box box) {
    this.colour = colour;
    this.box = box;
  }

  @Override
  public @Nullable Component<EntityStore> clone() {
    return new JobBoxComponent(colour, box);
  }

  public static class TaskBoxesComponent implements Component<EntityStore> {

    public static final ArrayCodec<JobBoxComponent> CODEC = new ArrayCodec<>(
        JobBoxComponent.CODEC, JobBoxComponent[]::new);
    public List<JobBoxComponent> boxes = new ArrayList<>();

    public static ComponentType<EntityStore, TaskBoxesComponent> getComponentType() {
      return ClayFactoria.debugBoxesComponentType;
    }

    @Override
    public @Nullable JobBoxComponent.TaskBoxesComponent clone() {
      TaskBoxesComponent taskBoxesComponent = new TaskBoxesComponent();
      taskBoxesComponent.boxes = new ArrayList<>(boxes);
      return taskBoxesComponent;
    }
  }
}
