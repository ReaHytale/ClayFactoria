package com.clayfactoria.sensors;

import com.clayfactoria.codecs.PathType;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.OwnerComponent;
import com.clayfactoria.sensors.builders.BuilderSensorPathType;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SensorPathType extends SensorBase {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    protected  final PathType pathType;

    public SensorPathType(@Nonnull BuilderSensorPathType builder, @Nonnull BuilderSupport builderSupport) {
        super(builder);
        this.pathType = builder.getPathType(builderSupport);
    }

    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {

        OwnerComponent ownerComponent = store.getComponent(ref, OwnerComponent.getComponentType());
        if (ownerComponent == null) {
            LOGGER.atSevere().log("Sensor Path Type: Owner Component was null");
            return false;
        }

        ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();
        if (npcEntityComponentType == null) {
            LOGGER.atSevere().log("Sensor Path Type: Failed to get NPC Entity Component Type of NPC");
            return false;
        }

        NPCEntity npcEntity = store.getComponent(ref, npcEntityComponentType);
        if (npcEntity == null) {
            LOGGER.atSevere().log("Sensor Path Type: Failed to get NPC Entity of NPC");
            return false;
        }

        World world = npcEntity.getWorld();
        if (world == null) {
            LOGGER.atSevere().log("Sensor Path Type: world was null");
            return false;
        }

        UUID playerId = ownerComponent.getPlayerId();
        if (playerId == null) {
            LOGGER.atSevere().log("Sensor Path Type: playerId was null");
            return false;
        }

        Ref<EntityStore> playerRef = world.getEntityRef(playerId);
        if (playerRef == null) {
            LOGGER.atSevere().log("Sensor Path Type: playerRef was null");
            return false;
        }

        BrushComponent brushComponent = store.getComponent(playerRef, BrushComponent.getComponentType());
        if (brushComponent == null) {
            LOGGER.atSevere().log("Sensor Path Type: Brush Component was null");
            return false;
        } else {
            boolean result = brushComponent.getPathType() == pathType;
//            LOGGER.atInfo().log(String.format("Sensor Path Type: brushComponent.getPathType() == pathType = %s\nBrush Components -> pathType: %s\npathType: %s", result, brushComponent.getPathType(), pathType));
            return super.matches(ref, role, dt, store) && result;
        }
    }

    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
