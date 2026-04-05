package com.clayfactoria.sensors.builders;

import com.clayfactoria.codecs.Task;
import com.clayfactoria.sensors.SensorCanDoTask;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

public class BuilderSensorCanDoTask extends BuilderSensorBase {
  protected final EnumHolder<Task> task = new EnumHolder<>();

  public Task getAction(@Nonnull BuilderSupport builderSupport) {
    return this.task.get(builderSupport.getExecutionContext());
  }

  @Override
  public @Nullable String getShortDescription() {
    return "Checks if the automaton can perform its current task";
  }

  @Override
  public @Nullable String getLongDescription() {
    return this.getShortDescription();
  }

  @Override
  public @Nullable Sensor build(BuilderSupport builderSupport) {
    return new SensorCanDoTask(this, builderSupport);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
    this.getEnum(
        data,
        "Task",
        this.task,
        Task.class,
        Task.TAKE,
        BuilderDescriptorState.Stable,
        "Task to take place at the location",
        null
    );
    return this;
  }
}
