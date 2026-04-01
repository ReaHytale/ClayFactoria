package com.clayfactoria.sensors.builders;

import com.clayfactoria.sensors.SensorHasAnyTasks;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import org.jspecify.annotations.Nullable;

public class BuilderSensorHasAnyTasks extends BuilderSensorBase {
    @Override
    public @Nullable String getShortDescription() {
        return "Checks if the automaton has any tasks to do";
    }

    @Override
    public @Nullable String getLongDescription() {
        return getShortDescription();
    }

    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorHasAnyTasks(this);
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }
}
