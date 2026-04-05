package com.clayfactoria.actions.automataactions;

import com.clayfactoria.actions.ActionBaseLogger;
import com.clayfactoria.actions.automataactions.builders.BuilderActionPosition;
import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionPosition extends ActionBaseLogger {
  public ActionPosition(@NotNull BuilderActionPosition builder) {
    super(builder);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    Objects.requireNonNull(jobComponent, "Task Component was null");
    jobComponent.setComplete(true);
    return true;
  }
}
