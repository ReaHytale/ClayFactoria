package com.clayfactoria.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;

public final class BlockUtils {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private BlockUtils() {
  }

  /**
   * Returns the correctly rounded base block.<br> For a given location, if the X or Z component is
   * in the negatives, the rounding should use the ceiling function on the absolute value of the
   * component, whereas if in the positive range, it should just behave as a <code>int</code> cast.
   * <br><br>
   * <em>Example:</em><br>
   * The coordinate (-425.5, 100, 320.5) should round to (-426, 100, 320), whereas a simple casting
   * to the <code>int</code> primitive would result in (-425, 100, 320), which would be wrong -
   * hence why the "correctly rounded base block" method is actually necessary.
   *
   * @param location - <code>Vector3d</code> location
   * @return A <code>Vector3i</code> rounded correctly
   */
  public static Vector3i getCorrectlyRoundedLocation(Vector3d location) {
    int xx, zz;
    xx = location.x < 0 ? (int) location.x - 1 : (int) location.x;
    zz = location.z < 0 ? (int) location.z - 1 : (int) location.z;
    return new Vector3i(xx, (int) location.y, zz);
  }

  /**
   * Rounds a <code>Vector3d</code> to the nearest integer <code>Vector3i</code>.
   *
   * @param location - Vector3d location
   * @return A <code>Vector3i</code> that is rounded to the nearest integer location
   */
  public static Vector3i roundToNearestIntegerLocation(Vector3d location) {
    return new Vector3i(
        (int) Math.round(location.x),
        (int) Math.round(location.y),
        (int) Math.round(location.z)
    );
  }

  /**
   * Get the base for a multiblock, given the position of one of its blocks in the world.
   *
   * @param position The position of the block.
   * @param world    The world of the block.
   * @return The base block of the multi-block at this position.
   */
  public static Vector3i getBaseBlock(Vector3i position, World world) {
    BlockPosition blockPosition = world.getBaseBlock(
        new BlockPosition(position.x, position.y, position.z));
    return new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
  }

  public static Vector3i blockPositionToVector3i(BlockPosition blockPosition) {
    return new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
  }

  public static RotatedVariantBoxes getRotatedVariantBoxes(Vector3d location, World world) {
    return getRotatedVariantBoxes(getCorrectlyRoundedLocation(location), world);
  }

  /**
   * Gets the {@link RotatedVariantBoxes} of a block at a given location in the world. This can be
   * used in turn to retrieve the bounding box(es) of said block.
   *
   * @param location The location of the block
   * @param world    The world in which the block is located
   * @return The {@link RotatedVariantBoxes} for the block at the given location in the world.
   */
  public static RotatedVariantBoxes getRotatedVariantBoxes(Vector3i location, World world) {
    BlockType blockType = world.getBlockType(location);
    if (blockType == null) {
      return null;
    }
    BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap()
        .getAsset(blockType.getHitboxTypeIndex());
    if (blockBoundingBoxes == null) {
      return null;
    }
    return blockBoundingBoxes.get(world.getBlockRotationIndex(location.x, location.y, location.z));
  }

  public static Box getBlockBox(Vector3i pos, World world) {
    Vector3d p1, p2;
    RotatedVariantBoxes rotatedVariantBoxes = BlockUtils.getRotatedVariantBoxes(pos, world);
    if (rotatedVariantBoxes == null) {
      p1 = pos.toVector3d();
      p2 = pos.toVector3d().add(1, 1, 1);
    } else {
      Box box = rotatedVariantBoxes.getBoundingBox();
      p1 = pos.toVector3d().add(box.min);
      p2 = pos.toVector3d().add(box.max);
    }
    return new Box(p1, p2);
  }

  public static Matrix4d getBoxMatrixFromBox(Box box) {
    Vector3d size = box.getMax().clone().subtract(box.getMin());
    Vector3d center = box.getMin().clone().add(box.getMax()).scale(0.5);
    Matrix4d matrix = new Matrix4d();
    matrix.identity();
    matrix.translate(center);
    matrix.scale(size.x, size.y, size.z);
    return matrix;
  }
}