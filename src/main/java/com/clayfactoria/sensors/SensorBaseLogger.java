package com.clayfactoria.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public abstract class SensorBaseLogger extends SensorBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public SensorBaseLogger(@NotNull BuilderSensorBase builderSensorBase) {
    super(builderSensorBase);
  }

  /**
   * Deprecated, override <code>matchesNullChecked</code> instead
   */
  @Deprecated
  public boolean matches(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
    if (!super.matches(ref, role, dt, store)) {
      return false;
    }
    try {
      return this.matchesNullChecked(ref, role, dt, store);
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

  /**
   * Called by `matches`
   */
  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) throws NullPointerException {
    return true;
  }
}
