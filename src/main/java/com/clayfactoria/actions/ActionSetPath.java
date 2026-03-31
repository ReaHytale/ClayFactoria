package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.TaskComponent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * Action triggered to finalise a created path and set it on the target entity.
 */
public class ActionSetPath extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionSetPath(@Nonnull BuilderActionSetPath builder) {
    super(builder);
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

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
    Objects.requireNonNull(playerRef, "playerRef was null");

    BrushComponent brushComponent = store.getComponent(
        playerRef,
        BrushComponent.getComponentType()
    );
    Objects.requireNonNull(brushComponent, "brushComponent was null");

    ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();
    Objects.requireNonNull(npcEntityComponentType,
        "Failed to get NPC Entity Component Type of NPC");

    NPCEntity npcComponent = store.getComponent(ref, npcEntityComponentType);
    Objects.requireNonNull(npcComponent, "npcComponent was null");

    Player player = store.getComponent(playerRef, Player.getComponentType());
    Objects.requireNonNull(player, "Player was null");

    UUIDComponent playerIdComp = store.getComponent(playerRef, UUIDComponent.getComponentType());
    Objects.requireNonNull(playerIdComp, "playerIdComp was null");

    UUID entityUUID = brushComponent.getEntityId();
    if (entityUUID == null || !entityUUID.equals(npcComponent.getUuid())) {
      LOGGER.atInfo().log("Reset brush component for Player " + player.getDisplayName());
      brushComponent.setEntityId(npcComponent.getUuid());
      if (brushComponent.getTasks() != null) {
        brushComponent.getTasks().clear();
      }
      player.sendMessage(
          Message.raw("Starting new Path...")
              .color(Color.YELLOW));
      return true;
    }

    TaskComponent taskComponent =
        store.ensureAndGetComponent(ref, TaskComponent.getComponentType());
    taskComponent.setPlayerId(playerIdComp.getUuid());
    LOGGER.atInfo().log(
        "Action Set Path: execute -> Player Id Set for Owner Component on the Entity you just interacted with");

    List<Task> tasks = brushComponent.getTasks();
    if (tasks == null || tasks.isEmpty()) {
      LOGGER.atWarning()
          .log("Action Set Path: execute -> Brush Component: Tasks were null or empty");
      player.sendMessage(
          Message.raw("You must set at least one target task with the Brush")
              .color(Color.YELLOW));
      return false;
    }

    // Transfer paths from brush to entity
    taskComponent.setTasks(new ArrayList<>(tasks));
    brushComponent.setEntityId(null);
    taskComponent.setCurrentTask(tasks.getFirst());

    String message = "Set Pathing";
    player.sendMessage(Message.raw(message));
    LOGGER.atInfo().log(message);
    return true;
  }
}
