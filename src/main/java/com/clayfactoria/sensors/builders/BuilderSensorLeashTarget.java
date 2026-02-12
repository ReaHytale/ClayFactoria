package com.clayfactoria.sensors.builders;

import com.clayfactoria.sensors.SensorLeashTarget;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

import javax.annotation.Nonnull;

public class BuilderSensorLeashTarget extends BuilderSensorBase {
  protected final BooleanHolder hasAction = new BooleanHolder();

  public boolean getHasAction(@Nonnull BuilderSupport support) {
    return this.hasAction.get(support.getExecutionContext());
  }

  @Nonnull
  public SensorLeashTarget build(@Nonnull BuilderSupport builderSupport) {
    builderSupport.setRequireLeashPosition();
    return new SensorLeashTarget(this, builderSupport);
  }

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Triggers when the NPC has a target position set";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return getShortDescription();
  }

  @Nonnull
  @Override
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  @Override
  public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
    this.provideFeature(Feature.Position);
    this.getBoolean(
        data,
        "HasAction",
        this.hasAction,
        true,
        BuilderDescriptorState.Stable,
        "Flag for when an action should take place in the .json config",
        null);
    return this;
  }
}
