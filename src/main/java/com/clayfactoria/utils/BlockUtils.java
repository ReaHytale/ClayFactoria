package com.clayfactoria.utils;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.universe.world.World;

public final class BlockUtils {

  private BlockUtils() {
  }

  public static BlockPosition getCorrectlyRoundedBaseBlock(World world, double x, double y,
      double z) {
    int xx, zz;
    xx = x < 0 ? (int) x - 1 : (int) x;
    zz = z < 0 ? (int) z - 1 : (int) z;
    return world.getBaseBlock(new BlockPosition(xx, (int) y, zz));
  }

}
