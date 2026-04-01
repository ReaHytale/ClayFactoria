package com.clayfactoria.utils;

import static com.clayfactoria.ClayFactoria.particleLinesComponentType;

import com.clayfactoria.components.ParticleLineComponent;
import com.clayfactoria.components.ParticleLineComponent.ParticleLinesComponent;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ParticleShapeUtils {

  private static final String DEFAULT_PARTICLE = "Box_Marker_System";
  private static final double PADDING = 0.05D;
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private ParticleShapeUtils() {
  }

  public static void drawParticleLine(Vector3d start, Vector3d end, String particleName,
      ComponentAccessor<EntityStore> componentAccessor) {
    double step = 0.05 / start.distanceTo(end);
    for (double t = 0; t < 1; t += step) {
      ParticleUtil.spawnParticleEffect(particleName, Vector3d.lerp(start, end, t),
          componentAccessor);
    }
  }

  public static void drawParticleLine(Vector3d start, Vector3d end, String particleName,
      ComponentAccessor<EntityStore> componentAccessor, boolean evensOnly) {
    double step = 0.05 / start.distanceTo(end);
    boolean draw = evensOnly;
    for (double t = 0; t < 1; t += step) {
      if (draw) {
        ParticleUtil.spawnParticleEffect(particleName, Vector3d.lerp(start, end, t),
            componentAccessor);
      }
      draw = !draw;
    }
  }

  public static void addParticleHitbox(Vector3i location,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref, World world) {
    addParticleHitbox(location, DEFAULT_PARTICLE, componentAccessor, ref, world);
  }

  public static void addParticleHitbox(Vector3i location, String particleName,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref, World world) {
    RotatedVariantBoxes rotatedVariantBoxes = BlockUtils.getRotatedVariantBoxes(location, world);
    if (rotatedVariantBoxes == null) {
      addParticleBox(location.toVector3d(), particleName, componentAccessor, ref);
      return;
    }
    Box box = rotatedVariantBoxes.getBoundingBox();
    addParticleBox(
        location.toVector3d().add(box.min).subtract(PADDING, PADDING, PADDING),
        location.toVector3d().add(box.max).add(PADDING, PADDING, PADDING),
        particleName, componentAccessor,
        ref
    );
  }

  public static void addParticleBox(Vector3d location,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
    addParticleBox(location, DEFAULT_PARTICLE, componentAccessor, ref);
  }

  public static void addParticleBox(Vector3d location, String particleName,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
    double gap = 0.5 + PADDING;
    addParticleBox(location.clone().add(gap, gap, gap), location.clone().add(-gap, -gap, -gap),
        particleName, componentAccessor, ref);
  }

  public static void addParticleRect(Vector3d p1, Vector3d p2, Vector3d p3, Vector3d p4,
      String particleName, ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
    addParticleLine(p1, p2, particleName, componentAccessor, ref);
    addParticleLine(p1, p3, particleName, componentAccessor, ref);
    addParticleLine(p2, p4, particleName, componentAccessor, ref);
    addParticleLine(p3, p4, particleName, componentAccessor, ref);
  }

  public static void addParticleBox(Vector3d start, Vector3d end, String particleName,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
    LOGGER.atInfo().log(start.toString() + end.toString());
    // Bottom
    addParticleRect(
        new Vector3d(start.x, start.y, start.z),
        new Vector3d(end.x, start.y, start.z),
        new Vector3d(start.x, start.y, end.z),
        new Vector3d(end.x, start.y, end.z),
        particleName,
        componentAccessor,
        ref
    );
    // Top
    addParticleRect(
        new Vector3d(start.x, end.y, start.z),
        new Vector3d(end.x, end.y, start.z),
        new Vector3d(start.x, end.y, end.z),
        new Vector3d(end.x, end.y, end.z),
        particleName,
        componentAccessor,
        ref
    );
    // Connecting Lines
    addParticleLine(start, new Vector3d(start.x, end.y, start.z), particleName, componentAccessor,
        ref);
    addParticleLine(new Vector3d(start.x, start.y, end.z), new Vector3d(start.x, end.y, end.z),
        particleName, componentAccessor, ref);
    addParticleLine(new Vector3d(end.x, start.y, start.z), new Vector3d(end.x, end.y, start.z),
        particleName, componentAccessor, ref);
    addParticleLine(new Vector3d(end.x, start.y, end.z), end, particleName, componentAccessor, ref);
  }

  public static void addParticleLine(Vector3d start, Vector3d end, String particleName,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {

    ParticleLinesComponent particleLinesComponent = componentAccessor.getComponent(ref,
        particleLinesComponentType);
    assert particleLinesComponent != null;
    particleLinesComponent.lines.add(new ParticleLineComponent(particleName, start, end));
  }

  public static void addParticleLine(Vector3d start, Vector3d end,
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {

    addParticleLine(start, end, DEFAULT_PARTICLE, componentAccessor, ref);
  }
}
