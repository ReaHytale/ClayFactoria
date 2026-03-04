package com.clayfactoria.actions;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.actions.builders.BuilderActionPosition;
import com.clayfactoria.components.TaskComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
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
    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    checkNull(taskComponent, "Task Component was null");
    taskComponent.setComplete(true);
    return true;
  }
}
