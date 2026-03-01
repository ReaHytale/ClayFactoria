package com.clayfactoria.utils;

import static com.clayfactoria.utils.Utils.checkNull;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TaskHelper {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final String[] CONTAINER_STATE_IDS = {"container", "processingBench"};

  @Nonnull
  public static NPCEntity getNPCEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    checkNull(component, "NPCEntity Component Type was null");
    NPCEntity npcEntity = store.getComponent(ref, component);
    checkNull(npcEntity, "NPCEntity was null");
    return npcEntity;
  }

  @Nullable
  public static ItemContainer getOrthogonalContainer(NPCEntity npcEntity, @Nullable ContainerSlot containerSlot) {
    World world = npcEntity.getWorld();
    checkNull(world);
    Vector3i pos = findNearbyContainer(npcEntity, world);
    checkNull(pos);
    return getItemContainerAtPos(world, pos, containerSlot);
  }

  @Nullable
  public static ItemContainer getOrthogonalContainer(NPCEntity npcEntity) {
    return getOrthogonalContainer(npcEntity, null);
  }

  @Nullable
  public static Vector3i findNearbyContainer(NPCEntity npcEntity) {
    World world = npcEntity.getWorld();
    checkNull(world);
    return findNearbyContainer(npcEntity, world);
  }

  @Nullable
  public static Vector3i findNearbyContainer(NPCEntity npcEntity, World world) {
    Vector3i pos = npcEntity.getOldPosition().toVector3i();

    // Check surrounding blocks
    Vector3i[] directions = {
      new Vector3i(0, 0, -1), new Vector3i(1, 0, 0), new Vector3i(0, 0, 1), new Vector3i(-1, 0, 0)
    };

    // Shuffle order to prevent order of check being predictable
    List<Vector3i> shuffled = Arrays.asList(directions);
    Collections.shuffle(shuffled);
    for (Vector3i dir : shuffled) {
      BlockType type = world.getBlockType(pos.clone().add(dir));
      if (type == null) {
        continue;
      }
      StateData stateData = type.getState();
      if (stateData == null
          || stateData.getId() == null
          || Arrays.stream(CONTAINER_STATE_IDS).noneMatch(stateData.getId()::equals)) {
        continue;
      }
      return pos.add(dir);
    }
    return null;
  }

  public static ItemContainer getItemContainerAtPos(
      World world,
      Vector3i pos,
      @Nullable ContainerSlot containerSlot
  ) {
    BlockState blockState = checkNull(
        getBlockStateAtPos(world, pos),
        "null BlockState at position where container was expected: " + pos
    );
    // Normal Item Container
    if (blockState.getClass() == ItemContainerState.class) {
      return ((ItemContainerState) blockState).getItemContainer();
    }
    // Processing Bench
    else if (blockState.getClass() == ProcessingBenchState.class) {
      CombinedItemContainer comb = ((ProcessingBenchState) blockState).getItemContainer();
      switch (containerSlot) {
        case Fuel -> {
          return comb.getContainer(0);
        }
        case Input -> {
          return comb.getContainer(1);
        }
        case Output -> {
          return comb.getContainer(2);
        }
        default -> {
          return comb;
        }
      }
    }

    // Unexpected BlockState
    else {
      LOGGER.atSevere().log(String.format(
          "Unexpected BlockState \"%s\" at %s.",
          blockState.getClass(), pos));
      return null;
    }
  }

  @Nullable
  public static BlockState getBlockStateAtPos(World world, Vector3i pos) {
    long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
    WorldChunk worldChunk = world.getChunk(chunkIndex);
    checkNull(worldChunk);
    EntityChunk entityChunk = worldChunk.getEntityChunk();
    checkNull(entityChunk);
    BlockPosition base = world.getBaseBlock(new BlockPosition(pos.x, pos.y, pos.z));
    return world.getState(base.x, base.y, base.z, false);
  }

  public static boolean transferItem(ItemContainer source, ItemContainer target) {
    for (short slot = 0; slot < source.getCapacity() - 1; slot++) {
      ItemStack itemStack = source.getItemStack(slot);
      if (itemStack == null) {
        continue;
      }
      int prevQuantity = itemStack.getQuantity();
      source.moveItemStackFromSlot(slot, 1, target);
      // Check whether it actually succeeded to transfer
      itemStack = source.getItemStack(slot);
      if (itemStack == null) {
        return true;
      } else {
        return itemStack.getQuantity() == prevQuantity-1;
      }
    }
    // No item found in storage, return false for failure.
    return false;
  }
}
