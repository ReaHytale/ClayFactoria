package com.clayfactoria.actions;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionDoTask extends ActionBaseLogger {

  public ActionDoTask(
      @NotNull BuilderActionBase builderActionBase) {
    super(builderActionBase);
  }

  @Override
  public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo,
      double dt, @Nonnull Store<EntityStore> store) {
    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    if (jobComponent == null) {
      return false;
    }
    Job job = jobComponent.getCurrentJob();
    if (job == null) {
      return false;
    }
    return job.getTask().doTask.apply(ref);
  }
}
