package com.clayfactoria.utils;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ParticleBoxUtils {

  private static final String DEFAULT_PARTICLE = "Box_Marker_System";
  private static final double PADDING = 0.05D;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private ParticleBoxUtils() {
  }

  public static void drawParticleHitbox(Vector3i location, Store<EntityStore> store, World world) {
    drawParticleHitbox(location, DEFAULT_PARTICLE, store, world);
  }

  public static void drawParticleHitbox(Vector3i location, String particle_name,
      Store<EntityStore> store, World world) {
    RotatedVariantBoxes rotatedVariantBoxes = BlockUtils.getRotatedVariantBoxes(location, world);
    if (rotatedVariantBoxes == null) {
      drawParticleBox(location.toVector3d(), particle_name, store);
    }
    Box box = rotatedVariantBoxes.getBoundingBox();
    LOGGER.atInfo().log(box.toString());
    drawParticleBox(
        location.toVector3d().add(box.min).subtract(PADDING, PADDING, PADDING),
        location.toVector3d().add(box.max).add(PADDING, PADDING, PADDING),
        particle_name, store);
  }

  public static void drawParticleBox(Vector3d location, Store<EntityStore> store) {
    drawParticleBox(location, DEFAULT_PARTICLE, store);
  }

  public static void drawParticleBox(Vector3d location, String particle_name,
      Store<EntityStore> store) {
    double gap = 0.5 + PADDING;
    drawParticleBox(location.clone().add(gap, gap, gap), location.clone().add(-gap, -gap, -gap),
        particle_name, store);
  }

  private static void drawParticleRect(Vector3d p1, Vector3d p2, Vector3d p3, Vector3d p4,
      String particle_name, Store<EntityStore> store) {
    drawParticleLine(p1, p2, particle_name, store);
    drawParticleLine(p1, p3, particle_name, store);
    drawParticleLine(p2, p4, particle_name, store);
    drawParticleLine(p3, p4, particle_name, store);
  }

  private static void drawParticleLine(Vector3d start, Vector3d end, String particle_name,
      Store<EntityStore> store) {
    double step = 0.05 / start.distanceTo(end);
    for (double t = 0; t < 1; t += step) {
      ParticleUtil.spawnParticleEffect(particle_name, Vector3d.lerp(start, end, t), store);
    }
  }

  public static void drawParticleBox(Vector3d start, Vector3d end, String particle_name,
      Store<EntityStore> store) {
    LOGGER.atInfo().log(start.toString() + end.toString());
    // Bottom
    drawParticleRect(
        new Vector3d(start.x, start.y, start.z),
        new Vector3d(end.x, start.y, start.z),
        new Vector3d(start.x, start.y, end.z),
        new Vector3d(end.x, start.y, end.z),
        particle_name,
        store
    );
    // Top
    drawParticleRect(
        new Vector3d(start.x, end.y, start.z),
        new Vector3d(end.x, end.y, start.z),
        new Vector3d(start.x, end.y, end.z),
        new Vector3d(end.x, end.y, end.z),
        particle_name,
        store
    );
    // Connecting Lines
    drawParticleLine(start, new Vector3d(start.x, end.y, start.z), particle_name, store);
    drawParticleLine(new Vector3d(start.x, start.y, end.z), new Vector3d(start.x, end.y, end.z),
        particle_name, store);
    drawParticleLine(new Vector3d(end.x, start.y, start.z), new Vector3d(end.x, end.y, start.z),
        particle_name, store);
    drawParticleLine(new Vector3d(end.x, start.y, end.z), end, particle_name, store);
  }
}
