package com.clayfactoria.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.List;

public final class JobLocationHelper {

  private JobLocationHelper() {
  }

  public static Vector3d findValidWalkLocationForBlock(World world, Vector3i location,
      Vector3d from)
      throws IllegalStateException {

    BlockType blockType = world.getBlockType(location);
    if (blockType == null) {
      throw new IllegalStateException("Block Type is null at location " + location + "!");
    }

    BlockBoundingBoxes boundingBoxes =
        BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());

    if (boundingBoxes == null) {
      throw new IllegalStateException("Bounding boxes are null at location " + location + "!");
    }

    int rotationIndex = world.getBlockRotationIndex(location.x, location.y, location.z);
    RotatedVariantBoxes rotatedVariantBoxes = boundingBoxes.get(rotationIndex);

    Vector3i start =
        BlockUtils.roundToNearestIntegerLocation(
                location.toVector3d().add(rotatedVariantBoxes.getBoundingBox().min))
            .add(new Vector3i(0, -1, 0));
    Vector3i end =
        BlockUtils.roundToNearestIntegerLocation(
                location.toVector3d().add(rotatedVariantBoxes.getBoundingBox().max))
            .add(new Vector3i(-1, 0, -1));
    end.y = start.y;

    List<Vector3d> foundLocations = new ArrayList<>();
    foundLocations.addAll(tryLookingForLocationInXAxis(start, end, world));
    foundLocations.addAll(tryLookingForLocationInZAxis(start, end, world));

    Vector3d foundLocation = findNearestWalkLocation(foundLocations, from);

    if (foundLocation != null) {
      return foundLocation;
    }

    // not found!
    throw new IllegalStateException(
        "Could not find neighbouring valid location for target location " + location + "!");
  }

  private static List<Vector3d> tryLookingForLocationInXAxis(
      Vector3i start, Vector3i end, World world) {
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

  private static List<Vector3d> tryLookingForLocationInZAxis(
      Vector3i start, Vector3i end, World world) {
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

  private static Vector3d finalValidLocation(Vector3i location) {
    return location.toVector3d().add(new Vector3d(0.5, 1, 0.5));
  }

  private static boolean isValidLocation(World world, Vector3i location) {
    BlockType blockType = world.getBlockType(location);
    BlockType blockTypeAbove = world.getBlockType(location.clone().add(new Vector3i(0, 1, 0)));
    return blockType != null
        && blockType.isFullySupportive()
        && (blockTypeAbove == null || blockTypeAbove.getMaterial().equals(BlockMaterial.Empty));
  }

  private static Vector3d findNearestWalkLocation(List<Vector3d> foundLocations, Vector3d from) {
    if (foundLocations.isEmpty()) {
      return null;
    }
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
}
