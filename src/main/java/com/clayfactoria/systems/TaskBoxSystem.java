package com.clayfactoria.systems;

import static com.clayfactoria.ClayFactoria.debugBoxesComponentType;

import com.clayfactoria.components.TaskBoxComponent;
import com.clayfactoria.components.TaskBoxComponent.TaskBoxesComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TaskBoxSystem extends DelayedEntitySystem<EntityStore> {

  public static final Vector3d BOX_PADDING = new Vector3d(0.05F, 0.05F, 0.05F);
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  @Nonnull
  private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();

  public TaskBoxSystem() {
    super(0.5F);
  }

  private static void drawBox(TaskBoxComponent boxComponent, World world) {
    Vector3d min = boxComponent.getBox().min;
    Vector3d max = boxComponent.getBox().max;
    // Bottom
    drawRect(
        new Vector3d(min.x, min.y, min.z),
        new Vector3d(max.x, min.y, min.z),
        new Vector3d(min.x, min.y, max.z),
        new Vector3d(max.x, min.y, max.z),
        world, boxComponent.getColour()
    );
    // Top
    drawRect(
        new Vector3d(min.x, max.y, min.z),
        new Vector3d(max.x, max.y, min.z),
        new Vector3d(min.x, max.y, max.z),
        new Vector3d(max.x, max.y, max.z),
        world, boxComponent.getColour()
    );
    // Connecting Lines
    drawLine(min, new Vector3d(min.x, max.y, min.z), world, boxComponent.getColour());
    drawLine(new Vector3d(max.x, min.y, max.z), max, world, boxComponent.getColour());
    drawLine(new Vector3d(min.x, min.y, max.z), new Vector3d(min.x, max.y, max.z), world,
        boxComponent.getColour());
    drawLine(new Vector3d(max.x, min.y, min.z), new Vector3d(max.x, max.y, min.z), world,
        boxComponent.getColour());
  }

  public static void drawRect(Vector3d p1, Vector3d p2, Vector3d p3, Vector3d p4, World world,
      Vector3f color) {
    drawLine(p1, p2, world, color);
    drawLine(p1, p3, world, color);
    drawLine(p2, p4, world, color);
    drawLine(p3, p4, world, color);
  }

  public static void drawLine(Vector3d start, Vector3d end,
      World world, Vector3f color) {
    DebugUtils.addLine(world, start, end, color, 0.03D, 0.6F, DebugUtils.FLAG_NO_WIREFRAME);
  }

  public static void drawArrow(Vector3d start, Vector3d end,
      World world, Vector3f color) {
    Vector3d direction = end.clone().subtract(start).normalize();
    double half = start.distanceTo(end) / 2;
    Vector3d position = start.clone().add(direction.clone().scale(half));
    DebugUtils.addLine(world, start, end, color, 0.03D, 0.6F, DebugUtils.FLAG_NO_WIREFRAME);
    DebugUtils.addArrow(world, position, direction, color, 1F,
        0.6F,
        DebugUtils.FLAG_NO_WIREFRAME);
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
    TaskBoxesComponent taskBoxesComponent = archetypeChunk.getComponent(index,
        debugBoxesComponentType);
    Player player = archetypeChunk.getComponent(index, Player.getComponentType());
    if (taskBoxesComponent == null || player == null || !TargetBlockEventSystem.isWandEquipped(
        player)) {
      return;
    }
    World world = commandBuffer.getExternalData().getWorld();

    List<TaskBoxComponent> boxes = taskBoxesComponent.boxes;
    for (int i = 0; i < boxes.size(); i++) {
      TaskBoxComponent box = boxes.get(i);
      drawBox(box, world);

      if (i + 1 < boxes.size()) {
        TaskBoxComponent nextBox = boxes.get(i + 1);
        Vector3d boxMid = box.getBox().min.clone().add(box.getBox().max).scale(0.5);
        Vector3d nextBoxMid = nextBox.getBox().min.clone().add(nextBox.getBox().max).scale(0.5);
        drawArrow(boxMid, nextBoxMid, world, box.getColour());
      }
    }
  }

  @Override
  public boolean isParallel(int archetypeChunkSize, int taskCount) {
    return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
  }
}
