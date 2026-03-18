package com.clayfactoria.codecs;

import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import lombok.Getter;

/**
 * A task which is performable by an NPC. It is split into two parts
 * <ul>
 *   <li>A location to navigate to</li>
 *   <li>An NPC Action to perform at that location.</li>
 * </ul>
 */
public class Task {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Getter private Vector3d location;
  @Getter private Vector3d walkLocation;
  @Getter private Action action;

  public static final BuilderCodec<Task> CODEC =
      BuilderCodec.builder(Task.class, Task::new)
          .append(
              new KeyedCodec<>("Location", Vector3d.CODEC),
              (comp, position) -> comp.location = position,
              (comp) -> comp.location)
          .documentation("The Vector3d location for the task to take place")
          .add()
          .append(
              new KeyedCodec<>("Action", Action.CODEC),
              (comp, action) -> comp.action = action,
              (comp) -> comp.action)
          .documentation("The action to take place for this task")
          .add()
          .append(
                  new KeyedCodec<>("Walk Location", Vector3d.CODEC),
                  (comp, position) -> comp.location = position,
                  (comp) -> comp.location)
          .documentation("The Vector3d location for where the automaton should walk to")
          .add()
          .build();

  private Task() {}

  public Task(Vector3d location, Action action, World world) throws IllegalStateException {
    this.location = location;
    this.action = action;
    this.walkLocation = findValidWalkLocation(world);
    LOGGER.atInfo().log("Found location: " + walkLocation + " (target is at " + location + ")");
  }

  public Vector3d findValidWalkLocation(World world) throws IllegalStateException {
    BlockType blockType = world.getBlockType(location.toVector3i());
    if (blockType == null) {
      throw new IllegalStateException("Block Type is null at location " + location + "!");
    }
    BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
    if (blockBoundingBoxes == null) {
      throw new IllegalStateException("Block bounding boxes is null at location " + location + "!");
    }
    Box boundingBox = blockBoundingBoxes.get(0).getBoundingBox();
    int minY = boundingBox.min.y < boundingBox.max.y ? (int) boundingBox.min.y : (int) boundingBox.max.y;
    Vector3i start = new Vector3i((int) Math.round(boundingBox.min.x), minY, (int) Math.round(boundingBox.min.z));
    Vector3i end = new Vector3i((int) Math.round(boundingBox.max.x), minY, (int) Math.round(boundingBox.max.z));
    Vector3i offset = location.toVector3i();
    offset.y = minY;

    Vector3d foundLocation = tryLookingForLocationInXAxis(start, end, minY, world, offset);
    if (foundLocation != null) {
      return foundLocation;
    }

    foundLocation = tryLookingForLocationInZAxis(start, end, minY, world, offset);
    if (foundLocation != null) {
      return foundLocation;
    }

    // not found!
    throw new IllegalStateException("Could not find neighbouring valid location for target location " + location + "!");
  }

  private Vector3d tryLookingForLocationInXAxis(Vector3i start, Vector3i end, int minY, World world, Vector3i offset) {
    int startX = end.subtract(start).squaredLength() <= 1 ? start.x : start.x + 1;
    int endX = end.subtract(start).squaredLength() <= 1 ? start.x : end.x - 1;
    for (int x = startX; x <= endX - 1; x++) {
      Vector3i topLineLocation = offset.add(new Vector3i(x, minY, start.z));
      if (isValidLocation(world, topLineLocation)) {
        return topLineLocation.add(new Vector3i(0, 1, 0)).toVector3d();
      }
      Vector3i bottomLineLocation =  offset.add(new Vector3i(x, minY, end.z));
      if (isValidLocation(world, bottomLineLocation)) {
        return bottomLineLocation.add(new Vector3i(0, 1, 0)).toVector3d();
      }
    }
    return null;
  }

  private Vector3d tryLookingForLocationInZAxis(Vector3i start, Vector3i end, int minY, World world, Vector3i offset) {
    int startZ = end.subtract(start).squaredLength() <= 1 ? start.z : start.z + 1;
    int endZ = end.subtract(start).squaredLength() <= 1 ? start.z : end.z - 1;
    for (int z = startZ; z <= endZ - 1; z++) {
      Vector3i leftLineLocation =  offset.add(new Vector3i(start.x, minY, z));
      if (isValidLocation(world, leftLineLocation)) {
        return leftLineLocation.add(new Vector3i(0, 1, 0)).toVector3d();
      }
      Vector3i rightLineLocation =  offset.add(new Vector3i(end.x, minY, z));
      if (isValidLocation(world, rightLineLocation)) {
        return rightLineLocation.add(new Vector3i(0, 1, 0)).toVector3d();
      }
    }
    return null;
  }

  private boolean isValidLocation(World world, Vector3i location) {
    BlockType blockType = world.getBlockType(location);
    return blockType != null && blockType.isFullySupportive();
  }

  @Override
  public String toString() {
    return String.format("<Task loc=%s action=%s>", location, action);
  }
}
