package com.clayfactoria.codecs;

import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * A task which is performable by an NPC. It is split into two parts
 *
 * <ul>
 *   <li>A location to navigate to
 *   <li>An NPC Action to perform at that location.
 * </ul>
 */
public class Task {
  public static final BuilderCodec<Task> CODEC =
      BuilderCodec.builder(Task.class, Task::new)
          .append(
              new KeyedCodec<>("Location", Vector3i.CODEC),
              (comp, position) -> comp.location = position,
              (comp) -> comp.location)
          .documentation("The Vector3i location of the block on which the action should be performed")
          .add()
          .append(
              new KeyedCodec<>("Action", Action.CODEC),
              (comp, action) -> comp.action = action,
              (comp) -> comp.action)
          .documentation("The action to take place for this task")
          .add()
          .append(
              new KeyedCodec<>("Walk Location", Vector3d.CODEC),
              (comp, position) -> comp.walkLocation = position,
              (comp) -> comp.walkLocation)
          .documentation("The Vector3d location for where the automaton should walk to")
          .add()
          .build();
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  @Getter private Vector3i location;
  @Getter private Vector3d walkLocation;
  @Getter private Action action;

  private Task() {}

  public Task(Vector3i location, Action action, World world) throws IllegalStateException {
    this.location = location;
    this.action = action;
    findValidWalkLocation(world, location.toVector3d());
  }

  public void findValidWalkLocation(World world, Vector3d from) throws IllegalStateException {

    BlockType blockType = world.getBlockType(location);
    if (blockType == null) {
      throw new IllegalStateException("Block Type is null at location " + location + "!");
    }

    Vector3i start = new Vector3i();
    Vector3i end = new Vector3i();
    findStartEndAndMinimumY(start, end, location, world);

    List<Vector3d> foundLocations = new ArrayList<>();
    foundLocations.addAll(tryLookingForLocationInXAxis(start, end, world));
    foundLocations.addAll(tryLookingForLocationInZAxis(start, end, world));

    Vector3d foundLocation = findNearestWalkLocation(foundLocations, from);

    if (foundLocation != null) {
      this.walkLocation = foundLocation;
      return;
    }

    // not found!
    throw new IllegalStateException(
        "Could not find neighbouring valid location for target location " + location + "!");
  }

  private void findStartEndAndMinimumY(
      Vector3i start, Vector3i end, Vector3i location, World world) {

    BlockPosition baseBlock =
        BlockUtils.getCorrectlyRoundedBaseBlock(world, location.x, location.y, location.z);

    int minY = baseBlock.y;
    for (int y = minY - 1; y >= 0; y--) {
      BlockPosition queryBlock =
          world.getBaseBlock(new BlockPosition(location.x, y, location.z));
      if (!queryBlock.equals(baseBlock)) {
        break;
      }
      minY = y;
    }

    BlockPosition left = baseBlock.clone();
    BlockPosition right = baseBlock.clone();
    BlockPosition backwards = baseBlock.clone();
    BlockPosition forwards = baseBlock.clone();
    while (world.getBaseBlock(left).equals(baseBlock)) {
      left.x--;
    }
    while (world.getBaseBlock(right).equals(baseBlock)) {
      right.x++;
    }
    while (world.getBaseBlock(backwards).equals(baseBlock)) {
      backwards.z--;
    }
    while (world.getBaseBlock(forwards).equals(baseBlock)) {
      forwards.z++;
    }

    start.x = left.x + 1;
    start.y = minY - 1;
    start.z = backwards.z + 1;

    end.x = right.x - 1;
    end.y = minY - 1;
    end.z = forwards.z - 1;

    if (start.x > end.x) {
      // swap
      start.x = start.x ^ end.x;
      end.x = start.x ^ end.x;
      start.x = start.x ^ end.x;
    }

    if (start.z > end.z) {
      // swap
      start.z = start.z ^ end.z;
      end.z = start.z ^ end.z;
      start.z = start.z ^ end.z;
    }
  }

  private List<Vector3d> tryLookingForLocationInXAxis(Vector3i start, Vector3i end, World world) {
    List<Vector3d> result = new ArrayList<>();
    for (int x = start.x; x <= end.x; x++) {
      Vector3i topLineLocation = new Vector3i(x, start.y, start.z - 1);
      if (isValidLocation(world, topLineLocation)) {
        result.add(finalValidLocation(topLineLocation));
      }
      Vector3i bottomLineLocation = new Vector3i(x, start.y, end.z + 1);
      if (isValidLocation(world, bottomLineLocation)) {
        result.add(finalValidLocation(bottomLineLocation));
      }
    }
    return result;
  }

  private List<Vector3d> tryLookingForLocationInZAxis(Vector3i start, Vector3i end, World world) {
    List<Vector3d> result = new ArrayList<>();
    for (int z = start.z; z <= end.z; z++) {
      Vector3i leftLineLocation = new Vector3i(start.x - 1, start.y, z);
      if (isValidLocation(world, leftLineLocation)) {
        result.add(finalValidLocation(leftLineLocation));
      }
      Vector3i rightLineLocation = new Vector3i(end.x + 1, start.y, z);
      if (isValidLocation(world, rightLineLocation)) {
        result.add(finalValidLocation(rightLineLocation));
      }
    }
    return result;
  }

  private Vector3d finalValidLocation(Vector3i location) {
    return location.toVector3d().add(new Vector3d(0.5, 1, 0.5));
  }

  private boolean isValidLocation(World world, Vector3i location) {
    BlockType blockType = world.getBlockType(location);
    BlockType blockTypeAbove = world.getBlockType(location.clone().add(new Vector3i(0, 1, 0)));
    return blockType != null
        && blockType.isFullySupportive()
        && (blockTypeAbove == null || blockTypeAbove.getMaterial().equals(BlockMaterial.Empty));
  }

  private Vector3d findNearestWalkLocation(List<Vector3d> foundLocations, Vector3d from) {
    if (foundLocations.isEmpty()) return null;
    Vector3d nearest = foundLocations.getFirst();
    double nearestDistance = nearest.distanceSquaredTo(from);
    for (int i = 1; i < foundLocations.size(); i++) {
      Vector3d location = foundLocations.get(i);
      double distance = location.distanceSquaredTo(from);
      if (distance < nearestDistance) {
        nearest = location;
        nearestDistance = distance;
      }
    }
    return nearest;
  }

  @Override
  public String toString() {
    return String.format("<Task loc=%s action=%s walkLoc=%s>", location, action, walkLocation);
  }
}
