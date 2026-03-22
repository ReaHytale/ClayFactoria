package com.clayfactoria.utils;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.universe.world.World;

public final class BlockUtils {

  private BlockUtils() {
  }

  /**
   * Returns the correctly rounded base block.<br>
   * For a given location, if the X or Z component is in the negatives, the rounding should use
   * the ceiling function on the absolute value of the component, whereas if in the positive
   * range, it should just behave as a <code>int</code> cast.
   * <br><br>
   * <em>Example:</em><br>
   * The coordinate (-425.5, 100, 320.5) should round to (-426, 100, 320), whereas a simple
   * casting to the <code>int</code> primitive would result in (-425, 100, 320), which would be
   * wrong - hence why the "correctly rounded base block" method is actually necessary.
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

}
