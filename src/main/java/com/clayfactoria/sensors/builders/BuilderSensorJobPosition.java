package com.clayfactoria.sensors.builders;

import com.clayfactoria.sensors.SensorJobPosition;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BuilderSensorJobPosition extends BuilderSensorBase {
    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorJobPosition(this);
    }

    @Override
    public @Nullable String getShortDescription() {
        return "Provides the position of the entities current task.";
    }

    @Override
    public @Nullable String getLongDescription() {
        return getShortDescription() +
            "Fails if there is no current task or no position on the current task";
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.provideFeature(Feature.Position);
        return this;
    }
}
