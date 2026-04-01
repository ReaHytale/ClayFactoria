package com.clayfactoria.systems;

import static com.clayfactoria.ClayFactoria.debugBoxesComponentType;

import com.clayfactoria.components.DebugBoxComponent;
import com.clayfactoria.components.DebugBoxComponent.DebugBoxesComponent;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DebugBoxSystem extends DelayedEntitySystem<EntityStore> {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  @Nonnull
  private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
  private boolean even = true;

  public DebugBoxSystem() {
    super(1F);
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
    DebugBoxesComponent debugBoxesComponent = archetypeChunk.getComponent(index,
        debugBoxesComponentType);

    if (debugBoxesComponent == null) {
      return;
    }

    even = !even;
    for (DebugBoxComponent box : debugBoxesComponent.boxes) {
      Matrix4d matrix = BlockUtils.getBoxMatrixFromBox(box.getBox());
      DebugUtils.add(
          commandBuffer.getExternalData().getWorld(),
          DebugShape.Cube,
          matrix.scale(1.15, 1.15, 1.15),
          box.getColour(),
          0.3F,
          1F,
          DebugUtils.FLAG_NO_WIREFRAME
      );
    }
  }

  @Override
  public boolean isParallel(int archetypeChunkSize, int taskCount) {
    return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
  }
}
