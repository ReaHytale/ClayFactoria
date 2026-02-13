package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionPutItemInHand;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderPutItemInHand extends BuilderActionBase {
  @Override
  @Nonnull
  public Action build(@Nonnull BuilderSupport builderSupport) {
    return new ActionPutItemInHand(this);
  }

  @Override
  @Nonnull
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.WorkInProgress;
  }

  @Override
  @Nonnull
  public String getShortDescription() {
    return "ActionPutItemInHand";
  }

  @Override
  @Nonnull
  public String getLongDescription() {
    return this.getShortDescription();
  }
}
