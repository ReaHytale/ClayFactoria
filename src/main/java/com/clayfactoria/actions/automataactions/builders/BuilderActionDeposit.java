package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionDeposit;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionDeposit extends BuilderActionBase {
  private final IntHolder quantity = new IntHolder();

  public int getQuantity(@Nonnull BuilderSupport builderSupport) {
    return this.quantity.get(builderSupport.getExecutionContext());
  }

  @Override
  @Nonnull
  public String getShortDescription() {
    return "Deposit items to a chest or station adjacent to the entity.";
  }

  @Override
  @Nonnull
  public String getLongDescription() {
    return getShortDescription();
  }

  @Override
  @Nonnull
  public Action build(BuilderSupport builderSupport) {
    return new ActionDeposit(this, builderSupport);
  }

  @Override
  @Nonnull
  public BuilderDescriptorState getBuilderDescriptorState() {
    return BuilderDescriptorState.Stable;
  }

  @Nonnull
  public Builder<Action> readConfig(@Nonnull JsonElement data) {
    this.getInt(
        data,
        "Quantity",
        this.quantity,
        1,
        null,
        BuilderDescriptorState.Stable,
        "Quantity of the item that will be taken",
        null
    );
    return this;
  }
}
