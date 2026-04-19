package com.clayfactoria.utils;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;

public final class BoxUtils {

  private BoxUtils() {}

  /**
   * Creates a Box, ensuring that the min and max coordinates represent the smaller and larger
   * number respectively. This is required due to an oversight in the <code>new Box()</code>
   * constructor which does not swap the coordinates, but then uses smaller values for checking
   * if a point is contained in a Box.
   *
   * @return A Box with the min and max X, Y, Z components are swapped where necessary
   */
  public static Box createMinMaxBox(Vector3d min, Vector3d max) {
    if (max.x < min.x) {
      double temp = min.x;
      min.x = max.x;
      max.x = temp;
    }
    if (max.y < min.y) {
      double temp = min.y;
      min.y = max.y;
      max.y = temp;
    }
    if (max.z < min.z) {
      double temp = min.z;
      min.z = max.z;
      max.z = temp;
    }
    return new Box(min, max);
  }

  /**
   * Checks if two boxes are equivalent, meaning they enclose the exact same area.<br>
   * Note that this is not equality, the min and max corners may be swapped and this still returns true.
   * @param box1 The first box
   * @param box2 The second box
   * @return True if the boxes are equivalent, false otherwise
   */
  public static boolean equivalent(Box box1, Box box2) {
    return (box1.min.equals(box2.min) || box1.min.equals(box2.max)) &&
        (box1.max.equals(box2.min) || box1.max.equals(box2.max));
  }

}
