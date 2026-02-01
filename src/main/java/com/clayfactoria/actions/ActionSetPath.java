package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.path.WorldPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
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

        if (brushComponent.getPathStart() == null) {
            LOGGER.atWarning().log("Action Set Path: execute -> Brush Component: Path Start was null");
            player.sendMessage(Message.raw("You must set at least one target position with the Brush").color(Color.YELLOW));
            return false;
        }

        Vector3d pathStartPosition = brushComponent.getPathStartPosition();
        Vector3f pathStartRotation = brushComponent.getPathStartRotation();

        if (brushComponent.getPathEnd() == null) {
            LOGGER.atInfo().log("Action Set Path: execute -> Brush Component: Path End was null. Targeting Path Start only");

            IPath<SimplePathWaypoint> path = WorldPath.buildPath(pathStartPosition, pathStartRotation, null, null);
            npcComponent.getPathManager().setTransientPath(path);

            String message = String.format("Set Single Point Pathing to: %s", brushComponent.getPathStart().toString());
            player.sendMessage(Message.raw(message));
            LOGGER.atInfo().log(message);
            return true;
        }

        Vector3d pathEndPosition = brushComponent.getPathEndPosition();
        Vector3f pathEndRotation = brushComponent.getPathEndRotation();

        IPath<SimplePathWaypoint> path = WorldPath.buildPath(pathStartPosition, pathStartRotation, pathEndPosition, pathEndRotation);
        npcComponent.getPathManager().setTransientPath(path);

        String message = String.format("Set Line Pathing from %s to %s", brushComponent.getPathStart().toString(), brushComponent.getPathEnd().toString());
        player.sendMessage(Message.raw(message));
        LOGGER.atInfo().log(message);
        return true;
    }
}
