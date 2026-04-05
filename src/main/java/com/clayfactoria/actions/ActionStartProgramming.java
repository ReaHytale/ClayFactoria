package com.clayfactoria.actions;

import static com.clayfactoria.utils.TaskHelper.idleAutomaton;

import com.clayfactoria.codecs.Automaton;
import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionStartProgramming extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionStartProgramming(
      @NotNull BuilderActionBase builderActionBase) {
    super(builderActionBase);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
    Objects.requireNonNull(playerRef);

    BrushComponent brushComponent = store.getComponent(
        playerRef,
        BrushComponent.getComponentType()
    );
    Objects.requireNonNull(brushComponent);

    NPCEntity npcComponent = store.getComponent(ref,
        Objects.requireNonNull(NPCEntity.getComponentType()));
    Objects.requireNonNull(npcComponent);

    Player player = store.getComponent(playerRef, Player.getComponentType());
    Objects.requireNonNull(player);

    UUID entityUUID = brushComponent.getEntityId();
    if (entityUUID != null) {
      World world = Objects.requireNonNull(player.getWorld());
      idleAutomaton(world.getEntityRef(entityUUID), store);
    }
    LOGGER.atInfo().log("Reset brush component for Player " + player.getDisplayName());

    Automaton automaton = Automaton.getFromRole(role);
    assert automaton != null;

    brushComponent.setEntityId(npcComponent.getUuid());
    brushComponent.setTask(automaton.tasks.getFirst());
    if (brushComponent.getJobs() != null) {
      brushComponent.resetTasks(store, playerRef);
    }
    return true;
  }
}
