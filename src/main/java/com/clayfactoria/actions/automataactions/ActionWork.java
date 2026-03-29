package com.clayfactoria.actions.automataactions;

import com.clayfactoria.actions.ActionBaseLogger;
import com.clayfactoria.actions.automataactions.builders.BuilderActionWork;
import com.clayfactoria.codecs.Action;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
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
    Objects.requireNonNull(taskComponent, "Task Component was null");

    Holder<ChunkStore> poi = Objects.requireNonNull(
        TaskHelper.findNearbyPOIHolder(npc, Action.WORK));
    ProcessingBenchBlock processingBenchBlock = poi.getComponent(
        ProcessingBenchBlock.getComponentType());
    BenchBlock benchBlock = poi.getComponent(BenchBlock.getComponentType());

    if (processingBenchBlock == null || benchBlock == null) {
      return false;
    }
    processingBenchBlock.setActive(true, benchBlock, null);
    taskComponent.setComplete(true);
    return true;
  }
}
