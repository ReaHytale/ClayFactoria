package com.clayfactoria.codecs.task;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;

public abstract class PointTaskExecutor implements TaskExecutor {

  @Override
  public boolean usesBounds() {
    return false;
  }

  @Override
  public void checkBounds(Box bounds) throws IllegalArgumentException {
    throw new UnsupportedOperationException("Bounds are not supported by Point task executor!");
  }

  @Override
  public Vector3d findNextWalkLocationInBounds(Box bounds, World world, Vector3d from) {
    throw new UnsupportedOperationException("Find location in bounds not supported by "
        + "Point task executor!");
  }

}
