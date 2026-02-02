package com.clayfactoria.sensors;

import com.clayfactoria.sensors.builders.BuilderSensorPathComplete;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.entities.PathManager;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;

public class SensorPathComplete extends SensorBase {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    protected final boolean isComplete;

    public SensorPathComplete(@Nonnull BuilderSensorPathComplete builder, @Nonnull BuilderSupport builderSupport) {
        super(builder);
        this.isComplete = builder.getIsComplete(builderSupport);
    }

    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        NPCEntity npcEntity = store.getComponent(ref, NPCEntity.getComponentType());
        if (npcEntity == null){
            LOGGER.atSevere().log("Sensor Path Complete: Failed. npcEntity was null");
            return false;
        }

        TransformComponent transformComp = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComp == null) {
            LOGGER.atSevere().log("Sensor Path Complete: Failed. transformComp was null");
            return false;
        }
        Vector3d currentPos = transformComp.getPosition();

        PathManager pathManager = npcEntity.getPathManager();
        IPath<?> path = pathManager.getPath(ref, store);
        if (path == null || path.length() == 0) {
            LOGGER.atSevere().log("Sensor Path Complete: Failed. Path was null or empty");
            return false;
        }

        IPathWaypoint lastWaypoint = path.get(path.length() - 1);
        Vector3d lastWaypointPos = lastWaypoint.getWaypointPosition(store);
        double distanceToLastWaypointPos = lastWaypointPos.distanceTo(currentPos);
        return super.matches(ref, role, dt, store) && distanceToLastWaypointPos < 1;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
