package com.clayfactoria.codecs;

import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.Getter;

/**
 * A task which is performable by an NPC. It is split into two parts
 * <ul>
 *   <li>A location to navigate to</li>
 *   <li>An NPC Action to perform at that location.</li>
 * </ul>
 */
public class Task {

  @Getter
  private Vector3d location;
  @Getter
  private Vector3d walkLocation;
  @Getter
  private Action action;

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
              (comp, position) -> comp.walkLocation = position,
              (comp) -> comp.walkLocation)
          .documentation("The Vector3d location for where the automaton should walk to")
          .add()
          .build();

  private Task() {
  }

  public Task(Vector3d location, Action action, World world) throws IllegalStateException {
    this.location = location;
    this.action = action;
    findValidWalkLocation(world);
  }

  public void findValidWalkLocation(World world) throws IllegalStateException {

    BlockType blockType = world.getBlockType(location.toVector3i());
    if (blockType == null) {
      throw new IllegalStateException("Block Type is null at location " + location + "!");
    }

    Vector3i start = new Vector3i();
    Vector3i end = new Vector3i();
    findStartEndAndMinimumY(start, end, location, world);

    Vector3d foundLocation = tryLookingForLocationInXAxis(start, end, world);
    if (foundLocation != null) {
      this.walkLocation = foundLocation;
      return;
    }

    foundLocation = tryLookingForLocationInZAxis(start, end, world);
    if (foundLocation != null) {
      this.walkLocation = foundLocation;
      return;
    }

    // not found!
    throw new IllegalStateException(
        "Could not find neighbouring valid location for target location " + location + "!");
  }

  private void findStartEndAndMinimumY(Vector3i start, Vector3i end, Vector3d location,
      World world) {

    BlockPosition baseBlock = BlockUtils.getCorrectlyRoundedBaseBlock(world, location.x,
        location.y - 1, location.z);

    int minY = baseBlock.y;
    for (int y = minY - 1; y >= 0; y--) {
      BlockPosition queryBlock = world.getBaseBlock(
          new BlockPosition((int) location.x, y, (int) location.z));
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

  private Vector3d tryLookingForLocationInXAxis(Vector3i start, Vector3i end, World world) {
    for (int x = start.x; x <= end.x; x++) {
      Vector3i topLineLocation = new Vector3i(x, start.y, start.z - 1);
      if (isValidLocation(world, topLineLocation)) {
        return finalValidLocation(topLineLocation);
      }
      Vector3i bottomLineLocation = new Vector3i(x, start.y, end.z + 1);
      if (isValidLocation(world, bottomLineLocation)) {
        return finalValidLocation(bottomLineLocation);
      }
    }
    return null;
  }

  private Vector3d tryLookingForLocationInZAxis(Vector3i start, Vector3i end, World world) {
    for (int z = start.z; z <= end.z; z++) {
      Vector3i leftLineLocation = new Vector3i(start.x - 1, start.y, z);
      if (isValidLocation(world, leftLineLocation)) {
        return finalValidLocation(leftLineLocation);
      }
      Vector3i rightLineLocation = new Vector3i(end.x + 1, start.y, z);
      if (isValidLocation(world, rightLineLocation)) {
        return finalValidLocation(rightLineLocation);
      }
    }
    return null;
  }

  private Vector3d finalValidLocation(Vector3i location) {
    return location.toVector3d().add(new Vector3d(0.5, 1, 0.5));
  }

  private boolean isValidLocation(World world, Vector3i location) {
    BlockType blockType = world.getBlockType(location);
    BlockType blockTypeAbove = world.getBlockType(location.clone().add(new Vector3i(0, 1, 0)));
    return blockType != null && blockType.isFullySupportive() &&
        (blockTypeAbove == null || !blockTypeAbove.isFullySupportive());
  }

  @Override
  public String toString() {
    return String.format("<Task loc=%s action=%s walkLoc=%s>", location, action, walkLocation);
  }
}
