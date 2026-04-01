package com.clayfactoria.sensors;

import com.clayfactoria.components.TaskComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class SensorHasAnyTasks extends SensorBaseLogger {

    public SensorHasAnyTasks(@NotNull BuilderSensorBase builderSensorBase) {
        super(builderSensorBase);
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return null;
    }

    public boolean matchesNullChecked(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Role role,
            double dt,
            @Nonnull Store<EntityStore> store) {
        TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
        return taskComponent != null && taskComponent.getTasks() != null && !taskComponent.getTasks().isEmpty();
    }

}
