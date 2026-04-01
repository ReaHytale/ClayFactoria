package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ParticleLineComponent implements Component<EntityStore> {

  public static final BuilderCodec<ParticleLineComponent> CODEC = BuilderCodec.builder(
          ParticleLineComponent.class, ParticleLineComponent::new)
      .append(
          new KeyedCodec<>("ParticleName", Codec.STRING),
          (comp, name) -> comp.particleName = name,
          comp -> comp.particleName
      )
      .documentation("The name of the particle system to use for this particle line")
      .add()
      .append(
          new KeyedCodec<>("Start", Vector3d.CODEC),
          (comp, pos) -> comp.start = pos,
          comp -> comp.start
      )
      .documentation("Position of the start of the line")
      .add()
      .append(
          new KeyedCodec<>("End", Vector3d.CODEC),
          (comp, pos) -> comp.end = pos,
          comp -> comp.end
      )
      .documentation("Position of the end of the line")
      .add()
      .build();
  public static final ArrayCodec<ParticleLineComponent> ARRAY_CODEC = new ArrayCodec<>(CODEC,
      ParticleLineComponent[]::new);
  @Getter
  private String particleName;
  @Getter
  private Vector3d start;
  @Getter
  private Vector3d end;

  private ParticleLineComponent() {
  }

  public ParticleLineComponent(String particleName, Vector3d start, Vector3d end) {
    this.particleName = particleName;
    this.start = start;
    this.end = end;
  }

  public static ComponentType<EntityStore, ParticleLinesComponent> getComponentType() {
    return ClayFactoria.particleLinesComponentType;
  }

  @Override
  public @Nullable Component<EntityStore> clone() {
    return new ParticleLineComponent(particleName, start, end);
  }

  public static class ParticleLinesComponent implements Component<EntityStore> {

    public static final ArrayCodec<ParticleLineComponent> CODEC = new ArrayCodec<>(
        ParticleLineComponent.CODEC, ParticleLineComponent[]::new);
    public List<ParticleLineComponent> lines = new ArrayList<>();

    @Override
    public @Nullable ParticleLinesComponent clone() {
      ParticleLinesComponent particleLinesComponent = new ParticleLinesComponent();
      particleLinesComponent.lines = new ArrayList<>(lines);
      return particleLinesComponent;
    }


  }
}
