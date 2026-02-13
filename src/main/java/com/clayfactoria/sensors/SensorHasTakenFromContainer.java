package com.clayfactoria.sensors;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.components.HasTakenFromContainerComponent;
import com.clayfactoria.sensors.builders.BuilderSensorHasTakenFromContainer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

/** Check whether the value of a component matches a value */
public class SensorHasTakenFromContainer extends SensorBaseLogger {

  public SensorHasTakenFromContainer(@Nonnull BuilderSensorHasTakenFromContainer builder) {
    super(builder);
  }

  public boolean matchesNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      double dt,
      @Nonnull Store<EntityStore> store) {
    HasTakenFromContainerComponent comp =
        store.getComponent(ref, HasTakenFromContainerComponent.getComponentType());
    checkNull(comp);
    return comp.isHasTakenFromContainer();
  }

  @Override
  public @Nullable InfoProvider getSensorInfo() {
    return null;
  }
}
