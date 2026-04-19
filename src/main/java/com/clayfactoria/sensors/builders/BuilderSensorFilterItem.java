package com.clayfactoria.sensors.builders;

import com.clayfactoria.sensors.SensorFilterItem;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BuilderSensorFilterItem extends BuilderSensorBase {
    @Getter
    String filterItem;

    @Override
    public @Nullable String getShortDescription() {
        return "Checks if an automaton's filter item matches.";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "Checks if an automaton's filter item matches the given item.";
    }

    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorFilterItem(this);
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getString(
            data,
            "FilterItem",
            filterItem -> this.filterItem = filterItem,
            null,
            null,
            BuilderDescriptorState.Stable,
            "The ID of the item to check against.",
            null
        );
        return this;
    }
}
