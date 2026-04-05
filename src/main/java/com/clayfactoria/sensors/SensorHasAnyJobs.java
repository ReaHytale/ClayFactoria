package com.clayfactoria.sensors;

import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class SensorHasAnyJobs extends SensorBaseLogger {

    public SensorHasAnyJobs(@NotNull BuilderSensorBase builderSensorBase) {
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
        JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
        return jobComponent != null && jobComponent.getJobs() != null && !jobComponent.getJobs().isEmpty();
    }

}
