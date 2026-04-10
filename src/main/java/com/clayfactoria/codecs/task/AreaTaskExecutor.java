package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.utils.JobLocationHelper;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public abstract class AreaTaskExecutor implements TaskExecutor {

  private static final double MAX_AREA_BOUNDS_WIDTH_LENGTH = 10;
  private static final double MAX_AREA_BOUNDS_HEIGHT = 5;
  private static final Vector3i[] directions = new Vector3i[]{
      Vector3i.POS_X, Vector3i.NEG_X,
      Vector3i.POS_Y, Vector3i.NEG_Y,
      Vector3i.POS_Z, Vector3i.NEG_Z
  };

  /**
   * Find the closest point inside the box to the given point <code>p</code>
   *
   * @param box The box that the resulting point will be inside.
   * @param p   The point to search from.
   * @return The closest point inside the box to <code>p</code>.
   */
  private static Vector3d findClosestPointInBox(Box box, Vector3d p) {
    return new Vector3d(
        Math.clamp(p.x, Math.min(box.min.x, box.max.x), Math.max(box.min.x, box.max.x)),
        Math.clamp(p.y, Math.min(box.min.y, box.max.y), Math.max(box.min.y, box.max.y)),
        Math.clamp(p.z, Math.min(box.min.z, box.max.z), Math.max(box.min.z, box.max.z))
    );
  }

  @Override
  public Vector3d findNextWalkLocation(Job job, World world, Vector3d from) {
    // Might cause issues since the rounded value may not be in the bounds?
    Vector3i start = findClosestPointInBox(job.getBounds(), from).toVector3i();

    // Flood fill algorithm with queue implementation, but stop as soon as we find a valid block.
    Queue<Vector3i> queue = new ArrayDeque<>();
    Set<Vector3i> visited = new HashSet<>();

    queue.add(start);
    visited.add(start);
    // Could use recursion here but this is fine.
    while (!queue.isEmpty()) {
      Vector3i p = queue.poll();
      for (Vector3i dir : directions) {
        Vector3i next = p.add(dir);
        if (canDoTaskHere(next, world)) {
          // Try to find and return the walk location, but if it fails, just continue search.
          try {
            return JobLocationHelper.findValidWalkLocationForBlock(world, next, from);
          } catch (IllegalStateException _) {
          }
        }
        if (!visited.contains(next)) {
          queue.add(next);
          visited.add(next);
        }
      }
    }
    return null;
  }

  @Override
  public boolean usesBounds() {
    return true;
  }

  @Override
  public void checkBounds(Box bounds) throws IllegalArgumentException {
    if (Math.abs(bounds.min.x - bounds.max.x) > MAX_AREA_BOUNDS_WIDTH_LENGTH
        || Math.abs(bounds.min.z - bounds.max.z) > MAX_AREA_BOUNDS_WIDTH_LENGTH) {
      throw new IllegalArgumentException(
          "Area must have a length less than "
              + MAX_AREA_BOUNDS_WIDTH_LENGTH
              + " blocks on either side!");
    }
    if (Math.abs(bounds.min.y - bounds.max.y) > MAX_AREA_BOUNDS_HEIGHT) {
      throw new IllegalArgumentException(
          "Area must have a height less than " + MAX_AREA_BOUNDS_HEIGHT + " blocks!");
    }
  }

  abstract protected boolean canDoTaskHere(Vector3i pos, World world);
}
