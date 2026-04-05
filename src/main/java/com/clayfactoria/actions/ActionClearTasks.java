package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionClearJobs;
import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionClearTasks extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionClearTasks(@NotNull BuilderActionClearJobs builder) {
    super(builder);
  }

  @Override
  public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role,
      InfoProvider sensorInfo, double dt, @Nonnull
      Store<EntityStore> store) {
    return super.canExecute(ref, role, sensorInfo, dt, store);
  }

  @Override
  public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo,
      double dt, @Nonnull Store<EntityStore> store) {
    ComponentType<EntityStore, JobComponent> componentType = JobComponent.getComponentType();
    if (store.getComponent(ref, componentType) != null) {
      store.removeComponent(ref, componentType);
      return true;
    } else {
      return false;
    }
  }
}
