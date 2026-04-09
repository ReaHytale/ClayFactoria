package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionDropInventory;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class BuilderActionDropInventory extends BuilderActionWithDelay {

  BooleanHolder dropHotbarItems = new BooleanHolder();

  public boolean getDropHotbarItems(BuilderSupport builderSupport) {
    return dropHotbarItems.get(builderSupport.getExecutionContext());
  }

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
    return new ActionDropInventory(this, builderSupport);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Action> readConfig(@Nonnull JsonElement data) {
    this.getBoolean(
        data,
        "DropHotbarItems",
        this.dropHotbarItems,
        true,
        BuilderDescriptorState.Stable,
        "Whether to drop the items in the NPC role's HotbarItems",
        "Whether to drop the items which are specified in the NPC role's HotbarItems. Useful if you want to ensure that those items stay in the NPC inventory when others do not."
    );
    return this;
  }
}
