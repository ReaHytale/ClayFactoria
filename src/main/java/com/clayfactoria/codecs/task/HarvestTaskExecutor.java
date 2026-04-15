package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.adventure.farming.FarmingUtil;
import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

public class HarvestTaskExecutor extends AreaTaskExecutor {

  @Override
  public boolean canPerformTask(Ref<EntityStore> entityRef) {
    Store<EntityStore> store = entityRef.getStore();
    JobComponent jobComponent = store.getComponent(entityRef, JobComponent.getComponentType());
    assert jobComponent != null;
    Job job = jobComponent.getCurrentJob();
    assert job != null;
    NPCEntity npc = TaskHelper.getNPCEntity(entityRef);
    World world = npc.getWorld();
    assert world != null;

    // Check if we can still do the task at this position
    if (!canDoTaskHere(job.getLocation(), world)) {
      return false;
    }

    // Check that it doesn't have any other items than the sickle in its inventory.
    CombinedItemContainer inventory = InventoryComponent.getCombined(store, entityRef,
        InventoryComponent.EVERYTHING);
    AtomicBoolean otherItemsFound = new AtomicBoolean(false);
    inventory.forEach((_, itemStack) -> {
      if (itemStack != null && !itemStack.getItemId().contains("Sickle")) {
        otherItemsFound.set(true);
      }
    });

    return !otherItemsFound.get();
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

    InventoryComponent.Hotbar hotbar = entityRef.getStore()
        .getComponent(entityRef, InventoryComponent.Hotbar.getComponentType());
    assert hotbar != null;
    hotbar.setActiveSlot((byte) (hotbar.getActiveSlot() + 1));
//    giveDrops(entityRef, type);
    return true;
  }

  private void giveDrops(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BlockType blockType
  ) {
    Store<EntityStore> store = ref.getStore();
    assert blockType.getGathering() != null;
    HarvestingDropType harvestingDropType = blockType.getGathering().getHarvest();
    String itemId = harvestingDropType.getItemId();
    String dropListId = harvestingDropType.getDropListId();

    for (ItemStack itemStack : BlockHarvestUtils.getDrops(blockType, 1, itemId, dropListId)) {
      LOGGER.atInfo().log(itemStack.toString());
      CombinedItemContainer hotbarFirstCombinedItemContainer = InventoryComponent.getCombined(store,
          ref, InventoryComponent.HOTBAR_FIRST);
      SimpleItemContainer.addOrDropItemStack(store, ref, hotbarFirstCombinedItemContainer,
          itemStack);
    }
  }

  @Override
  protected boolean canDoTaskHere(Vector3i pos, World world) {
    if (pos == null) {
      return false;
    }
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
