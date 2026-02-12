package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionTake;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.helpers.TaskHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class ActionTake extends ActionBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final int quantity;

  public ActionTake(@NotNull BuilderActionTake builder, @Nonnull
  BuilderSupport builderSupport) {
    super(builder);
    this.quantity = builder.getQuantity(builderSupport);
  }

  public boolean execute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store
  ) {
    super.execute(ref, role, sensorInfo, dt, store);
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    if (component == null) {
      LOGGER.atSevere().log("Action Taks -> NPCEntity Component Type was null");
      return false;
    }
    NPCEntity npcEntity = store.getComponent(ref, component);
    if (npcEntity == null) {
      LOGGER.atSevere().log("Action Taks -> NPCEntity was null");
      return false;
    }
    World world = npcEntity.getWorld();
    if (world == null) {
      LOGGER.atSevere().log("Action Taks -> World was null");
      return false;
    }

    // Get item container orthogonal to the entity.
    Vector3i containerPos = TaskHelper.findNearbyContainer(npcEntity);
    if (containerPos == null) {
      // No container found.
      LOGGER.atSevere().log("Action Taks -> No container found");
      return false;
    }
    ItemContainer itemContainer = getItemContainerAtPos(world, containerPos);
    if (itemContainer == null) {
      // Container not found at given position (should never occur)
      LOGGER.atSevere().log("Action Taks ->  Item Container not found at given position");
      return false;
    }

    // Take an item from the container
    boolean result = moveFirstItemFromItemContainer(
        itemContainer,
        npcEntity.getInventory().getCombinedStorageFirst()
    );

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    if (taskComponent == null) {
      LOGGER.atSevere().log("Action Take: Task Component was null");
      return false;
    }

    if (result) {
      LOGGER.atSevere().log("Action Take: Set Complete to true");
      taskComponent.setComplete(true);
    }

    return result;
  }

  private @Nullable ItemContainer getItemContainerAtPos(World world, Vector3i pos) {
    long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
    WorldChunk worldChunk = world.getChunk(chunkIndex);
    assert worldChunk != null;
    EntityChunk entityChunk = worldChunk.getEntityChunk();
    assert entityChunk != null;
    BlockState blockState = world.getState(pos.x, pos.y, pos.z, false);
    if (blockState == null) {
      LOGGER.atSevere().log("ActionTakeFromNearbyStorage -> null BlockState at position where container was expected: " + pos);
      return null;
    }
    if (blockState.getClass() != ItemContainerState.class) {
      LOGGER.atSevere().log(String.format(
          "ActionTakeFromNearbyStorage -> BlockState at %s was %s where ItemContainerState was expected",
          pos, blockState.getClass()
      ));
      return null;
    }
    ItemContainerState itemContainerState = (ItemContainerState) blockState;
    return itemContainerState.getItemContainer();
  }

  private boolean moveFirstItemFromItemContainer(ItemContainer source, ItemContainer target) {
    for (short slot = 0; slot<source.getCapacity()-1; slot++) {
      ItemStack itemStack = source.getItemStack(slot);
      if (itemStack == null) {
        continue;
      }
      source.moveItemStackFromSlot(slot, 1, target);
      return true;
    }
    // No item found in storage, return false for failure.
    return false;
  }
}
