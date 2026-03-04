package com.clayfactoria.actions.automataactions.builders;

import com.clayfactoria.actions.automataactions.ActionPosition;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jetbrains.annotations.Nullable;

public class BuilderActionPosition extends BuilderActionBase {
  @Override
  public @Nullable String getShortDescription() {
    return "Do nothing after moving to a position";
  }

  @Override
  public @Nullable String getLongDescription() {
    return "An ACTION for automatas. Will simply mark the task as complete, usually after they've moved to the position associated with the task.";
  }

  @Override
  public @Nullable Action build(BuilderSupport builderSupport) {
    return new ActionPosition(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }
}
