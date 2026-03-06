package com.clayfactoria.actions.automataactions.builders;

import com.clayfactoria.actions.automataactions.ActionWork;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jetbrains.annotations.Nullable;

public class BuilderActionWork extends BuilderActionBase {
  @Override
  public @Nullable String getShortDescription() {
    return "Work at a nearby station";
  }

  @Override
  public @Nullable String getLongDescription() {
    return "Perform some work at a nearby station. This varies depending on the station.";
  }

  @Override
  public @Nullable Action build(BuilderSupport builderSupport) {
    return new ActionWork(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return null;
  }
}
