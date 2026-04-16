package com.clayfactoria.sensors;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.sensors.builders.BuilderSensorJobPosition;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SensorJobPosition extends SensorBaseLogger {
    protected final PositionProvider positionProvider = new PositionProvider();

    public SensorJobPosition(@NotNull BuilderSensorJobPosition builder) {
        super(builder);
    }

    @Override
    public boolean matchesNullChecked(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Role role,
        double dt,
        @Nonnull Store<EntityStore> store
    ) {
        JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
        Objects.requireNonNull(jobComponent);

        Job job = jobComponent.getCurrentJob();
        if (job == null) {
            return false;
        }

        Vector3i position = job.getLocation();
        if (position != null) {
            positionProvider.setTarget(position.toVector3d().add(0.5, 0.5, 0.5));
            return true;
        } else {
            positionProvider.clear();
            return false;
        }
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
