package com.clayfactoria.actions.automataactions;

import com.clayfactoria.actions.ActionBaseLogger;
import com.clayfactoria.actions.automataactions.builders.BuilderActionWork;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
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

    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    Objects.requireNonNull(jobComponent, "Task Component was null");

    World world = npc.getWorld();
    assert world != null;
    assert jobComponent.getCurrentJob() != null;
    Vector3i pos = jobComponent.getCurrentJob().getLocation();
    Ref<ChunkStore> blockRef = TaskHelper.getBlockComponentHolderDirectReference(world, pos.x,
        pos.y, pos.z);
    assert blockRef != null;
    ProcessingBenchBlock processingBenchBlock = blockRef.getStore().getComponent(blockRef,
        ProcessingBenchBlock.getComponentType());
    BenchBlock benchBlock = blockRef.getStore()
        .getComponent(blockRef, BenchBlock.getComponentType());

    if (processingBenchBlock == null || benchBlock == null) {
      return false;
    }
    processingBenchBlock.setActive(true, benchBlock, null);
    jobComponent.setComplete(true);
    return true;
  }
}
