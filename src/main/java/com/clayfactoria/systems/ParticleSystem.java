package com.clayfactoria.systems;

import static com.clayfactoria.ClayFactoria.particleLinesComponentType;

import com.clayfactoria.components.ParticleLineComponent;
import com.clayfactoria.components.ParticleLineComponent.ParticleLinesComponent;
import com.clayfactoria.utils.ParticleShapeUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleSystem extends DelayedEntitySystem<EntityStore> {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  @Nonnull
  private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
  private boolean even = true;

  public ParticleSystem() {
    super(0.1F);
  }

  @Nullable
  @Override
  public SystemGroup<EntityStore> getGroup() {
    return DamageModule.get().getGatherDamageGroup();
  }

  @Override
  public Query<EntityStore> getQuery() {
    return TRANSFORM_COMPONENT_TYPE;
  }

  @Override
  public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
  ) {
    ParticleLinesComponent particleLinesComponent = archetypeChunk.getComponent(index,
        particleLinesComponentType);

    if (particleLinesComponent == null) {
      return;
    }
    even = !even;
    for (ParticleLineComponent line : particleLinesComponent.lines) {
      ParticleShapeUtils.drawParticleLine(line.getStart(), line.getEnd(), line.getParticleName(),
          commandBuffer, even);
    }
  }

  @Override
  public boolean isParallel(int archetypeChunkSize, int taskCount) {
    return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
  }
}
