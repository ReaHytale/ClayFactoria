package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
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
import javax.annotation.Nonnull;

public class ActionSetPath extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionSetPath(@Nonnull BuilderActionSetPath builder) {
    super(builder);
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

    NPCEntity npcComponent = store.getComponent(ref,
        Objects.requireNonNull(NPCEntity.getComponentType()));
    Objects.requireNonNull(npcComponent, "npcComponent was null");

    Player player = store.getComponent(playerRef, Player.getComponentType());
    Objects.requireNonNull(player, "Player was null");

    UUIDComponent playerIdComp = store.getComponent(playerRef, UUIDComponent.getComponentType());
    Objects.requireNonNull(playerIdComp, "playerIdComp was null");

    JobComponent jobComponent =
        store.ensureAndGetComponent(ref, JobComponent.getComponentType());
    jobComponent.setPlayerId(playerIdComp.getUuid());
    LOGGER.atInfo().log(
        "Action Set Path: execute -> Player Id Set for Owner Component on the Entity you just interacted with");

    List<Job> jobs = brushComponent.getJobs();
    if (jobs == null || jobs.isEmpty()) {
      player.sendMessage(
          Message.raw("You must set at least one target task with the Brush")
              .color(Color.YELLOW));
      TaskHelper.idleAutomaton(ref, store);
      brushComponent.setEntityId(null);
      return false;
    }

    // Transfer paths from brush to entity
    jobComponent.setJobs(new ArrayList<>(jobs));
    brushComponent.setEntityId(null);
    brushComponent.resetTasks(store, playerRef);
    jobComponent.setCurrentJob(jobs.getFirst());

    String message = "Set Pathing";
    player.sendMessage(Message.raw(message));
    LOGGER.atInfo().log(message);
    return true;
  }
}
