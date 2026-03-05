package com.clayfactoria.actions.automataactions;

import static com.clayfactoria.utils.TaskHelper.getBlockStateAtPos;
import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.actions.ActionBaseLogger;
import com.clayfactoria.actions.automataactions.builders.BuilderActionWork;
import com.clayfactoria.codecs.Action;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionWork extends ActionBaseLogger {

  public ActionWork(@NotNull BuilderActionWork builderActionWork) {
    super(builderActionWork);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    NPCEntity npc = TaskHelper.getNPCEntity(ref, store);

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    checkNull(taskComponent, "Task Component was null");
    taskComponent.setComplete(true);
    Vector3i pos = checkNull(TaskHelper.findNearbyPOI(npc, Action.WORK));

    // Find processing bench
    BlockState blockState = checkNull(
        getBlockStateAtPos(checkNull(npc.getWorld()), pos),
        "null BlockState at position where container was expected: " + pos
    );
    ProcessingBenchState processingBenchState;
    if (blockState.getClass() == ProcessingBenchState.class) {
      processingBenchState = (ProcessingBenchState) blockState;
    } else {
      return false;
    }

    // Activate processing bench
    processingBenchState.setActive(true);
    return true;
  }
}
