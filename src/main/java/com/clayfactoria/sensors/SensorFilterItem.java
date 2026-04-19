package com.clayfactoria.sensors;

import com.clayfactoria.components.JobComponent;
import com.clayfactoria.sensors.builders.BuilderSensorFilterItem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SensorFilterItem extends SensorBaseLogger {
    String filterItem;

    public SensorFilterItem(@NotNull BuilderSensorFilterItem builder) {
        super(builder);
        filterItem = builder.getFilterItem();
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
        if (jobComponent == null) {
            return filterItem == null;
        }
        return Objects.equals(jobComponent.getFilterItem(), filterItem);
    }
}
