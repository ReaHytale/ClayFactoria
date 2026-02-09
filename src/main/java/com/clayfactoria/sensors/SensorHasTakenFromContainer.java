package com.clayfactoria.sensors;

import com.clayfactoria.components.HasTakenFromContainerComponent;
import com.clayfactoria.sensors.builders.BuilderSensorHasTakenFromContainer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

/**
 * Check whether the value of a component matches a value
 */
public class SensorHasTakenFromContainer extends SensorBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public SensorHasTakenFromContainer(@Nonnull BuilderSensorHasTakenFromContainer builder, @Nonnull
  BuilderSupport builderSupport) {
    super(builder);
  }

  public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull
  Store<EntityStore> store) {
    HasTakenFromContainerComponent comp = store.getComponent(ref, HasTakenFromContainerComponent.getComponentType());
    if (comp == null) {
      return false;
    } else {
      return comp.isHasTakenFromContainer();
    }
  }

  @Override
  public @Nullable InfoProvider getSensorInfo() {return null;}
}
