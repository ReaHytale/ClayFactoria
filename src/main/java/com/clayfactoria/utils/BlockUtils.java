package com.clayfactoria.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

public final class BlockUtils {

  private BlockUtils() {
  }

  /**
   * Returns the correctly rounded location.<br> For a given location, if the X or Z component is
   * in the negatives, the rounding should use the ceiling function on the absolute value of the
   * component, whereas if in the positive range, it should just behave as a <code>int</code> cast.
   * <br><br>
   * <em>Example:</em><br>
   * The coordinate (-425.5, 100, 320.5) should round to (-426, 100, 320), whereas a simple casting
   * to the <code>int</code> primitive would result in (-425, 100, 320), which would be wrong -
   * hence why the "correctly rounded location" method is actually necessary.
   *
   * @param location - Vector3d location
   * @return A Vector3i rounded correctly
   */
  public static Vector3i getCorrectlyRoundedLocation(Vector3d location) {
    int xx, zz;
    xx = location.x < 0 ? (int) location.x - 1 : (int) location.x;
    zz = location.z < 0 ? (int) location.z - 1 : (int) location.z;
    return new Vector3i(xx, (int) location.y, zz);
  }

  /**
   * Rounds a Vector3d to the nearest integer Vector3i.
   * @param location - Vector3d location
   * @return A Vector3i that is rounded to the nearest integer location
   */
  public static Vector3i roundToNearestIntegerLocation(Vector3d location) {
    return new Vector3i(
        (int) Math.round(location.x),
        (int) Math.round(location.y),
        (int) Math.round(location.z)
    );
  }

}
