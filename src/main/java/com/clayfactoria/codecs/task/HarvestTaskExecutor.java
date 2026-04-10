package com.clayfactoria.codecs.task;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HarvestTaskExecutor extends AreaTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    return true;
  }

  @Override
  public boolean execute(Ref<EntityStore> entityRef) {
    // Likely can use something in ChangeFarmingStageInteraction.
    return true;
  }

  @Override
  protected boolean canDoTaskHere(Vector3i pos, World world) {
    Holder<ChunkStore> blockHolder = world.getBlockComponentHolder(pos.x, pos.y, pos.z);
    if (blockHolder == null) {
      return false;
    }
    FarmingBlock farmingBlock = blockHolder.getComponent(FarmingBlock.getComponentType());
    return farmingBlock != null;
    // Also need to check that the crop is fully grown.
  }
}
