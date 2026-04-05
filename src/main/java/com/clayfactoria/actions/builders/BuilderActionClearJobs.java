package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionClearTasks;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class BuilderActionClearJobs extends BuilderActionBase {

  @Nonnull
  @Override
  public String getShortDescription() {
    return "Clears automaton jobs.";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Remove the Job component from the entity, resulting in the automaton having its job list wiped.";
  }

  @Nonnull
  public Action build(@Nonnull BuilderSupport builderSupport) {
    return new ActionClearTasks(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }
}
