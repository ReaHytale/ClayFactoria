package com.clayfactoria.codecs.task;

import com.hypixel.hytale.math.shape.Box;

public abstract class AreaTaskExecutor implements TaskExecutor {

  private static final double MAX_AREA_BOUNDS_WIDTH_LENGTH = 10;
  private static final double MAX_AREA_BOUNDS_HEIGHT = 5;

  @Override
  public boolean usesBounds() {
    return true;
  }

  @Override
  public void checkBounds(Box bounds) throws IllegalArgumentException {
    if (Math.abs(bounds.min.x - bounds.max.x) > MAX_AREA_BOUNDS_WIDTH_LENGTH
        || Math.abs(bounds.min.z - bounds.max.z) > MAX_AREA_BOUNDS_WIDTH_LENGTH) {
      throw new IllegalArgumentException(
          "Area must have a length less than "
              + MAX_AREA_BOUNDS_WIDTH_LENGTH
              + " blocks on either side!");
    }
    if (Math.abs(bounds.min.y - bounds.max.y) > MAX_AREA_BOUNDS_HEIGHT) {
      throw new IllegalArgumentException(
          "Area must have a height less than " + MAX_AREA_BOUNDS_HEIGHT + " blocks!");
    }
  }
}
