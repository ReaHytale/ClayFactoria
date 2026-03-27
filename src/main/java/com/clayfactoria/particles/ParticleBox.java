package com.clayfactoria.particles;

import com.clayfactoria.utils.BlockUtils;
import com.clayfactoria.utils.ParticleBoxUtils;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes.RotatedVariantBoxes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * Manages the persistent drawing of a particle box in the world
 */
public class ParticleBox {

  private static final String DEFAULT_PARTICLE = "Box_Marker_System";

  @Getter
  private final Vector3d min;
  @Getter
  private final Vector3d max;
  private final Store<EntityStore> store;
  @Getter
  private final World world;
  @Getter
  private final ScheduledExecutorService scheduler;
  @Getter
  private boolean drawing;
  @Getter
  @Setter
  private String particle_name;

  public ParticleBox(BlockPosition blockPosition, Store<EntityStore> store, World world) {
    this(blockPosition, store, world, DEFAULT_PARTICLE);
  }

  public ParticleBox(BlockPosition blockPosition, Store<EntityStore> store, World world,
      String particle_name) {
    Vector3i location = new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
    RotatedVariantBoxes rotatedVariantBoxes = BlockUtils.getRotatedVariantBoxes(blockPosition,
        world);
    Vector3d min, max;
    if (rotatedVariantBoxes == null) {
      min = location.toVector3d().add(0.5, 0.5, 0.5);
      max = location.toVector3d().subtract(0.5, 0.5, 0.5);
    } else {
      min = rotatedVariantBoxes.getBoundingBox().min.add(location);
      max = rotatedVariantBoxes.getBoundingBox().max.add(location);
    }
    this(min, max, store, world, particle_name);
  }

  public ParticleBox(Vector3d min, Vector3d max, Store<EntityStore> store, World world) {
    this(min, max, store, world, DEFAULT_PARTICLE);
  }

  public ParticleBox(Vector3d min, Vector3d max, Store<EntityStore> store, World world,
      String particle_name) {
    this.min = min;
    this.max = max;
    this.store = store;
    this.world = world;
    this.particle_name = particle_name;
    this.scheduler = Executors.newScheduledThreadPool(2);
  }

  public void startDrawing() {
    if (drawing) {
      return;
    }
    drawing = true;
    draw();
  }

  public void stopDrawing() {
    if (!drawing) {
      return;
    }
    drawing = false;
  }

  private void draw() {
    if (!drawing) {
      return;
    }
    ParticleBoxUtils.drawParticleBox(min, max, particle_name, store);
    scheduler.schedule(this::draw, 1, TimeUnit.SECONDS);
  }
}
