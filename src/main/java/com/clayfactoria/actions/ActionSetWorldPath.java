package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.actions.builders.BuilderActionSetWorldPath;
import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;

public class ActionSetWorldPath extends ActionBase {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ActionSetWorldPath(@Nonnull BuilderActionSetWorldPath builder, @Nonnull BuilderSupport support) {
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

        Vector3d pathStartPosition = brushComponent.getPathStartPosition();
//        Vector3d pathEnd = brushComponent.getPathEndPosition();
        Vector3f pathStartRotation = brushComponent.getPathStartRotation();

        IPath<SimplePathWaypoint> path = this.pathDefinition.buildPath(pathStartPosition, pathStartRotation);
        npcComponent.getWorld().getWorldPathConfig().

        LOGGER.atInfo().log(String.format("Action Set Path: execute -> Successfully set path %s", brushComponent.getPathStart().toString()));
        return true;
    }
}
