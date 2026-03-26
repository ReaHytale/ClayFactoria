package com.clayfactoria.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;

public final class BlockUtils {

  private BlockUtils() {
  }

  public static BlockPosition getCorrectlyRoundedBaseBlock(Vector3d location, World world) {
    return getCorrectlyRoundedBaseBlock(world, location.x, location.y, location.z);
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
   * @param world
   * @param x
   * @param y
   * @param z
   * @return
   */
  public static BlockPosition getCorrectlyRoundedBaseBlock(World world, double x, double y,
      double z) {
    int xx, zz;
    xx = x < 0 ? (int) x - 1 : (int) x;
    zz = z < 0 ? (int) z - 1 : (int) z;
    return world.getBaseBlock(new BlockPosition(xx, (int) y, zz));
  }

  public static RotatedVariantBoxes getRotatedVariantBoxes(Vector3d location, World world) {
    return getRotatedVariantBoxes(getCorrectlyRoundedBaseBlock(location, world), world);
  }

  public static RotatedVariantBoxes getRotatedVariantBoxes(BlockPosition location, World world) {
    return getRotatedVariantBoxes(
        new Vector3i(location.x, location.y, location.z),
        world);
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

}
