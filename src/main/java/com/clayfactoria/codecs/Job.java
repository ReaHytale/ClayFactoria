package com.clayfactoria.codecs;

import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class Job {

  public static final BuilderCodec<Job> CODEC =
      BuilderCodec.builder(Job.class, Job::new)
          .append(
              new KeyedCodec<>("Location", Vector3i.CODEC),
              (comp, position) -> comp.location = position,
              (comp) -> comp.location)
          .documentation("The Vector3i location of the block on which the task should be performed")
          .add()
          .append(
              new KeyedCodec<>("Task", Task.CODEC),
              (comp, task) -> comp.task = task,
              (comp) -> comp.task)
          .documentation("The task to take place for this job")
          .add()
          .append(
              new KeyedCodec<>("Walk Location", Vector3d.CODEC),
              (comp, position) -> comp.walkLocation = position,
              (comp) -> comp.walkLocation)
          .documentation("The Vector3d location for where the automaton should walk to")
          .add()
          .append(
              new KeyedCodec<>("Location equals Walk Location", Codec.BOOLEAN),
              (comp, locationEqualsWalkLocation) ->
                  comp.locationEqualsWalkLocation = locationEqualsWalkLocation,
              (comp) -> comp.locationEqualsWalkLocation)
          .documentation("Whether the walk location should instead be the Location")
          .add()
          .build();
  @Getter private Vector3i location;
  @Getter private Vector3d walkLocation;
  @Getter private Task task;
  @Getter private boolean locationEqualsWalkLocation;

  private Job() {}

  public Job(Vector3i location, Task task, World world, boolean locationEqualsWalkLocation)
      throws IllegalStateException {
    this.location = location;
    this.task = task;
    this.locationEqualsWalkLocation = locationEqualsWalkLocation;
    if (!locationEqualsWalkLocation) {
      findValidWalkLocation(world, location.toVector3d());
    } else {
      walkLocation = location.toVector3d().add(new Vector3d(0.5, 1, 0.5));
    }
  }

  public void findValidWalkLocation(World world, Vector3d from) throws IllegalStateException {

    BlockType blockType = world.getBlockType(location);
    if (blockType == null) {
      throw new IllegalStateException("Block Type is null at location " + location + "!");
    }

    BlockBoundingBoxes boundingBoxes =
        BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());

    if (boundingBoxes == null) {
      throw new IllegalStateException("Bounding boxes are null at location " + location + "!");
    }

    Vector3i roundedLocation = location;
    int rotationIndex =
        world.getBlockRotationIndex(roundedLocation.x, roundedLocation.y, roundedLocation.z);
    RotatedVariantBoxes rotatedVariantBoxes = boundingBoxes.get(rotationIndex);

    Vector3i start =
        BlockUtils.roundToNearestIntegerLocation(
                roundedLocation.toVector3d().add(rotatedVariantBoxes.getBoundingBox().min))
            .add(new Vector3i(0, -1, 0));
    Vector3i end =
        BlockUtils.roundToNearestIntegerLocation(
                roundedLocation.toVector3d().add(rotatedVariantBoxes.getBoundingBox().max))
            .add(new Vector3i(-1, 0, -1));
    end.y = start.y;

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

  @Override
  public String toString() {
    return String.format("<Task loc=%s task=%s walkLoc=%s>", location, task, walkLocation);
  }

  public Job clone() {
    Job clone = new Job();
    clone.task = task;
    clone.location = location.clone();
    clone.walkLocation = walkLocation.clone();
    clone.locationEqualsWalkLocation = locationEqualsWalkLocation;
    return clone;
  }
}
