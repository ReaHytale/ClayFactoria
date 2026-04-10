package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.utils.JobLocationHelper;
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
  public Vector3d findNextWalkLocation(Job job, World world, Vector3d from) {
    return JobLocationHelper.findValidWalkLocationForBlock(world, job.getLocation(), from);
  }

}
