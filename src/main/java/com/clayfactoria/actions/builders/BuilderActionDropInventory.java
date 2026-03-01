package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionDropInventory;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class BuilderActionDropInventory extends BuilderActionWithDelay {
  @Nonnull
  @Override
  public String getShortDescription() {
    return "Drop inventory";
  }

  @Nonnull
  @Override
  public String getLongDescription() {
    return "Drop the contents of the entities inventory";
  }

  @Nonnull
  public Action build(@Nonnull BuilderSupport builderSupport) {
    return new ActionDropInventory(this);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }
}
