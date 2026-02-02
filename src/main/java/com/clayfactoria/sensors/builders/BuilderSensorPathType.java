package com.clayfactoria.sensors.builders;

import com.clayfactoria.codecs.PathType;
import com.clayfactoria.sensors.SensorPathType;
import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BuilderSensorPathType extends BuilderSensorBase {
    protected final EnumHolder<PathType> pathType = new EnumHolder<>();

    public PathType getPathType(@Nonnull BuilderSupport builderSupport) {
        return this.pathType.get(builderSupport.getExecutionContext());
    }

    @Override
    public @Nullable String getShortDescription() {
        return "Sensor to path type for Brush Component.";
    }

    @Override
    public @Nullable String getLongDescription() {
        return this.getShortDescription();
    }

    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorPathType(this, builderSupport);
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getEnum(
                data,
                "PathType",
                this.pathType,
                PathType.class,
                PathType.ONCE,
                BuilderDescriptorState.Stable,
                "Is entity done pathing or not",
                null
        );
        return this;
    }
}
