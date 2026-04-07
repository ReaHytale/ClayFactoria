package com.clayfactoria.codecs.task;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HarvestTaskExecutor extends AreaTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    return false;
  }

  @Override
  public boolean execute(Ref<EntityStore> entityRef) {
    return false;
  }

  @Override
  public Vector3d findNextWalkLocationInBounds(Box bounds, World world, Vector3d from) {
    // TODO: find next location in bounds!
    return null;
  }

}
