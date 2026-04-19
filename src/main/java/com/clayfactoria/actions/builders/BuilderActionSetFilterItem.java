package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionSetFilterItem;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BuilderActionSetFilterItem extends BuilderActionBase {
    @Override
    public @Nullable String getShortDescription() {
        return "Set the filter item for the automaton.";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "Sets a value on the Task component of the automaton to the item held by a nearby player.";
    }

    @Override
    public @Nullable Action build(BuilderSupport builderSupport) {
        return new ActionSetFilterItem(this);
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Action> readConfig(@Nonnull JsonElement data) {
        this.requireFeature(EnumSet.of(Feature.Player));
        return this;
    }
}
