package com.clayfactoria.sensors.builders;

import com.clayfactoria.sensors.SensorHasTakenFromContainer;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class BuilderSensorHasTakenFromContainer extends BuilderSensorBase {
  @Override
  public @Nullable String getShortDescription() {
    return "Sensor to check whether the HasTakenFromContainer component is present and truthy";
  }

  @Override
  public @Nullable String getLongDescription() {
    return this.getShortDescription();
  }

  @Override
  public @Nullable Sensor build(BuilderSupport builderSupport) {
    return new SensorHasTakenFromContainer(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
    return this;
  }
}
