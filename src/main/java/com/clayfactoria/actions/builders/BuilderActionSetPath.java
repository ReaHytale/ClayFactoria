package com.clayfactoria.actions.builders;

import com.clayfactoria.actions.ActionSetPath;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionSetPath extends BuilderActionBase {

    @Override
    @Nonnull
    public Action build(@Nonnull BuilderSupport builderSupport) {
        return new ActionSetPath(this, builderSupport);
    }

    @Override
    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Override
    @Nonnull
    public String getShortDescription() {
        return "Action set path";
    }

    @Override
    @Nonnull
    public String getLongDescription() {
        return this.getShortDescription();
    }
}
