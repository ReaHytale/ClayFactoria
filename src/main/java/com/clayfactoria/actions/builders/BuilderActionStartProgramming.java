package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionStartProgramming;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionStartProgramming extends BuilderActionBase {

  @Override
  @Nonnull
  public Action build(@Nonnull BuilderSupport builderSupport) {
    return new ActionStartProgramming(this);
  }

  @Override
  @Nonnull
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Override
  @Nonnull
  public String getShortDescription() {
    return "Start programming an automaton";
  }

  @Override
  @Nonnull
  public String getLongDescription() {
    return this.getShortDescription();
  }
}
