package com.clayfactoria.codecs.task;

import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;

public class PositionTaskExecutor extends PointTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    return true;
  }

  @Override
  public boolean execute(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();
    JobComponent jobComponent = Objects.requireNonNull(
        store.getComponent(entityRef, JobComponent.getComponentType()));
    jobComponent.setComplete(true);
    return true;
  }

}
