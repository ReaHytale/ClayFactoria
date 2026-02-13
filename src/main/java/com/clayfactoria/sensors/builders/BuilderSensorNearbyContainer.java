package com.clayfactoria.sensors.builders;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.sensors.SensorNearbyContainer;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

public class BuilderSensorNearbyContainer extends BuilderSensorBase {
  protected final EnumHolder<Action> action = new EnumHolder<>();

  public Action getAction(@Nonnull BuilderSupport builderSupport) {
    return this.action.get(builderSupport.getExecutionContext());
  }

  @Override
  public @Nullable String getShortDescription() {
    return "Sensor to path type for Brush Component.";
  }

  @Override
  public @Nullable String getLongDescription() {
    return this.getShortDescription();
  }

  @Override
  public @Nullable Sensor build(BuilderSupport builderSupport) {
    return new SensorNearbyContainer(this, builderSupport);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
    this.getEnum(
        data,
        "Action",
        this.action,
        Action.class,
        Action.TAKE,
        BuilderDescriptorState.Stable,
        "Action to take place at the location",
        null
    );
    return this;
  }
}
