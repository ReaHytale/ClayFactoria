package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.utils.BlockUtils;
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
    private static final Vector3i[] DIRECTIONS =
            new Vector3i[]{
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
        Vector3d clamped = new Vector3d(
                Math.clamp(p.x, box.min.x, box.max.x),
                Math.clamp(p.y, box.min.y, box.max.y),
                Math.clamp(p.z, box.min.z, box.max.z));
        if (clamped.x <= box.min.x) {
            clamped.x += 0.5;
        } else if (clamped.x >= box.max.x) {
            clamped.x -= 0.5;
        }
        if (clamped.y <= box.min.y) {
            clamped.y += 0.5;
        } else if (clamped.y >= box.max.y) {
            clamped.y -= 0.5;
        }
        if (clamped.z <= box.min.z) {
            clamped.z += 0.5;
        } else if (clamped.z >= box.max.z) {
            clamped.z -= 0.5;
        }
        return clamped;
    }

    @Override
    public Vector3d findNextWalkLocation(Job job, World world, Vector3d from) {
        // Might cause issues since the rounded value may not be in the bounds?
        if (job.getBounds() == null) {
            return null;
        }

        Box bounds = job.getBounds().clone();
        if (shouldSearchLocationsAboveBoundingBox()) {
            bounds.max.add(0, 1, 0);
        }

        Vector3i start =
                BlockUtils.getCorrectlyRoundedLocation(findClosestPointInBox(bounds, from));

        // Check first point before searching out
        if (canDoTaskHere(start, world)) {
            try {
                Vector3d validWalkLocationForBlock =
                        JobLocationHelper.findValidWalkLocationForBlock(world, start, from);
                job.setLocation(start);
                return validWalkLocationForBlock;
            } catch (IllegalStateException _) {
            }
        }

        // Flood fill algorithm with queue implementation, but stop as soon as we find a valid block.
        Queue<Vector3i> queue = new ArrayDeque<>();
        Set<Vector3i> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Vector3i p = queue.poll();
            for (Vector3i dir : DIRECTIONS) {
                Vector3i next = p.clone().add(dir);
                Vector3d center =
                        next.toVector3d().add(0.5, 0.5, 0.5);
                if (!bounds.containsPosition(center)) {
                    continue;
                }
                if (canDoTaskHere(next, world)) {
                    try {
                        Vector3d validWalkLocationForBlock =
                                JobLocationHelper.findValidWalkLocationForBlock(world, next, from);
                        job.setLocation(next);
                        return validWalkLocationForBlock;
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

    protected abstract boolean canDoTaskHere(Vector3i pos, World world);

    protected abstract boolean shouldSearchLocationsAboveBoundingBox();
}
