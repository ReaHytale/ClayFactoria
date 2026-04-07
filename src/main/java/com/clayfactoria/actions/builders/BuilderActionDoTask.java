package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionDoTask;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jetbrains.annotations.Nullable;

public class BuilderActionDoTask extends BuilderActionBase {

  @Override
  public @Nullable String getShortDescription() {
    return "Perform the current task.";
  }

  @Override
  public @Nullable String getLongDescription() {
    return "Performs the current task for the job in progress as determined by the JobComponent";
  }

  @Override
  public @Nullable Action build(BuilderSupport builderSupport) {
    return new ActionDoTask(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return null;
  }
}
