package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetHasTakenFromContainer;
import com.clayfactoria.components.HasTakenFromContainerComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionSetHasTakenFromContainer extends ActionBaseLogger {
  protected final boolean newValue;

  public ActionSetHasTakenFromContainer(@NotNull BuilderActionSetHasTakenFromContainer builder, @Nonnull
  BuilderSupport builderSupport) {
    super(builder);
    this.newValue = builder.getNewValue(builderSupport);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store
  ) {
    HasTakenFromContainerComponent hasTakenFromContainerComp =
        store.ensureAndGetComponent(ref, HasTakenFromContainerComponent.getComponentType());
    hasTakenFromContainerComp.setHasTakenFromContainer(newValue);
    return true;
  }
}
