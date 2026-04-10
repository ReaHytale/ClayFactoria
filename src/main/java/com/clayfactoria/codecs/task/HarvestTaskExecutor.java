package com.clayfactoria.codecs.task;

import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public class HarvestTaskExecutor extends AreaTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    return true;
  }

  @Override
  public boolean execute(Ref<EntityStore> entityRef) {
    NPCEntity entity = TaskHelper.getNPCEntity(entityRef);
    JobComponent job =
        entityRef.getStore().getComponent(entityRef, JobComponent.getComponentType());
    World world = entity.getWorld();
    assert job != null;
    assert job.getCurrentJob() != null;
    assert world != null;

    Vector3i location = job.getCurrentJob().getLocation();
    BlockType type = world.getBlockType(location.x, location.y, location.z);
    assert type != null;

    FarmingUtil.harvest(
        world,
        entityRef.getStore(),
        entityRef,
        type,
        world.getBlockRotationIndex(location.x, location.y, location.z),
        location);
    return true;
  }

  @Override
  protected boolean canDoTaskHere(Vector3i pos, World world) {
    Holder<ChunkStore> blockHolder = world.getBlockComponentHolder(pos.x, pos.y - 1, pos.z);
    if (blockHolder == null) {
      return false;
    }
    TilledSoilBlock tilledSoilBlock = blockHolder.getComponent(TilledSoilBlock.getComponentType());
    if (tilledSoilBlock == null) {
      return false;
    }
    if (!tilledSoilBlock.isPlanted()) {
      return false;
    }
    BlockType type = world.getBlockType(pos.x, pos.y, pos.z);
    if (type == null) {
      return false;
    }
    return type.getId().endsWith("_StageFinal");
  }
}
