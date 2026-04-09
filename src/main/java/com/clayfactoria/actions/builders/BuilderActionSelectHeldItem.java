package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionSelectHeldItem;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class BuilderActionSelectHeldItem extends BuilderActionBase {

  private final StringHolder item = new StringHolder();

  public String getItem(@Nonnull BuilderSupport builderSupport) {
    return this.item.get(builderSupport.getExecutionContext());
  }

  @Override
  public @Nullable String getShortDescription() {
    return "Make the NPC hold the given item";
  }

  @Override
  public @Nullable String getLongDescription() {
    return "Change the currently held item so that it matches the specified item. If the item isn't already in the NPC's inventory, this will fail.";
  }

  @Override
  public @Nullable Action build(BuilderSupport builderSupport) {
    return new ActionSelectHeldItem(this, builderSupport);
  }

  @Override
  public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Action> readConfig(@Nonnull JsonElement data) {
    this.getString(
        data,
        "Item",
        this.item,
        null,
        StringNullOrNotEmptyValidator.get(),
        BuilderDescriptorState.Stable,
        "Name of the item to put in the NPC's hand",
        null
    );
    return this;
  }
}
