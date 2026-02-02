package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.builtin.path.path.TransientPathDefinition;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Action triggered to finalise a created path and set it on the target entity.
 */
public class ActionSetPath extends ActionBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Nullable protected final TransientPathDefinition pathDefinition;

  public ActionSetPath(@Nonnull BuilderActionSetPath builder, @Nonnull BuilderSupport support) {
    super(builder);
    this.pathDefinition = builder.getPath(support);
  }

  @Override
  public boolean canExecute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    return super.canExecute(ref, role, sensorInfo, dt, store);
  }

  public boolean execute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    super.execute(ref, role, sensorInfo, dt, store);
    Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
    if (playerRef == null) {
      return false;
    }

    Player player = store.getComponent(playerRef, Player.getComponentType());
    if (player == null) {
      return false;
    }

    BrushComponent brushComponent =
        store.getComponent(playerRef, BrushComponent.getComponentType());
    if (brushComponent == null) {
      LOGGER.atSevere().log("Action Set Path: execute -> brushComponent was null");
      return false;
    }

    ComponentType<EntityStore, NPCEntity> componentType = NPCEntity.getComponentType();
    if (componentType == null) {
      LOGGER.atSevere().log("Action Set Path: execute -> componentType was null");
      return false;
    }

    NPCEntity npcComponent = store.getComponent(ref, componentType);
    if (npcComponent == null) {
      LOGGER.atSevere().log("Action Set Path: execute -> npcComponent was null");
      return false;
    }

    Vector3d pathStartPosition = brushComponent.getPathStartPosition();
    Vector3f pathStartRotation = brushComponent.getPathStartRotation();

    if (this.pathDefinition == null) {
      return false;
    }

    IPath<SimplePathWaypoint> path =
        this.pathDefinition.buildPath(pathStartPosition, pathStartRotation);
    npcComponent.getPathManager().setTransientPath(path);

    LOGGER.atInfo().log(
        String.format(
            "Action Set Path: execute -> Successfully set path %s",
            brushComponent.getPathStart().toString()));
    return true;
  }
}
