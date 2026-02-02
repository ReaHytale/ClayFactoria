package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.OwnerComponent;
import com.clayfactoria.path.WorldPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ActionSetPath  extends ActionBase {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ActionSetPath(@Nonnull BuilderActionSetPath builder, @Nonnull BuilderSupport support) {
        super(builder);
    }

    @Override
    public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        return super.canExecute(ref, role, sensorInfo, dt, store);
    }

    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);
        Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
        if (playerRef == null) {
            LOGGER.atSevere().log("Action Set Path: execute -> playerRef was null");
            return false;
        }

        BrushComponent brushComponent = store.getComponent(playerRef, BrushComponent.getComponentType());
        if (brushComponent == null) {
            LOGGER.atSevere().log("Action Set Path: execute -> brushComponent was null");
            return false;
        }

        NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
        if (npcComponent == null) {
            LOGGER.atSevere().log("Action Set Path: execute -> npcComponent was null");
            return false;
        }

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) {
            LOGGER.atSevere().log("Action Set Path: execute -> Player was null");
            return false;
        }

        UUIDComponent playerIdComp = store.getComponent(playerRef, UUIDComponent.getComponentType());
        if (playerIdComp == null) {
            LOGGER.atSevere().log("Action Set Path: execute -> playerIdComp was null");
            return false;
        }

        OwnerComponent ownerComponent = store.ensureAndGetComponent(ref, OwnerComponent.getComponentType());
        ownerComponent.setPlayerId(playerIdComp.getUuid());
        LOGGER.atInfo().log("Action Set Path: execute -> Player Id Set for Owner Component on the Entity you just interacted with");

        if (brushComponent.getPaths() == null) {
            LOGGER.atWarning().log("Action Set Path: execute -> Brush Component: Path Start was null");
            player.sendMessage(Message.raw("You must set at least one target position with the Brush").color(Color.YELLOW));
            return false;
        }

        List<Transform> paths = brushComponent.getPaths();

        IPath<SimplePathWaypoint> path = WorldPath.buildPath(paths);
        npcComponent.getPathManager().setTransientPath(path);

        String message = "Set Pathing";
        player.sendMessage(Message.raw(message));
        LOGGER.atInfo().log(message);
        return true;
    }
}
