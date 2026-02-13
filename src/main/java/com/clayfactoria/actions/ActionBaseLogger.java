package com.clayfactoria.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public abstract class ActionBaseLogger extends ActionBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionBaseLogger(@NotNull BuilderActionBase builderActionBase) {
    super(builderActionBase);
  }

  public boolean execute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    if (!super.execute(ref, role, sensorInfo, dt, store)) {
      return false;
    }
    try {
      return this.executeNullChecked(ref, role, sensorInfo, dt, store);
    } catch (NullPointerException e) {
      if (e.getMessage() != null) {
        String className = e
            .getStackTrace()[1]
            .getClassName()
            .split("\\.")[0];
        LOGGER.atInfo().log(String.format("%s [Null]: %s", className, e.getMessage()));
      }
      return false;
    }
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) throws NullPointerException {
    return true;
  }


}
