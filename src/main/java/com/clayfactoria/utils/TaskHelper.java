package com.clayfactoria.utils;

import static com.clayfactoria.utils.Utils.checkNull;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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
  public static ItemContainer getOrthogonalContainer(NPCEntity npcEntity) {
    World world = npcEntity.getWorld();
    checkNull(world);
    Vector3i pos = findNearbyContainer(npcEntity, world);
    checkNull(pos);
    return getItemContainerAtPos(world, pos);
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
      StateData blockState = type.getState();
      if (blockState == null
          || blockState.getId() == null
          || !blockState.getId().equals("container")) {
        continue;
      }
      return pos.add(dir);
    }
    return null;
  }

  public static @Nullable ItemContainer getItemContainerAtPos(World world, Vector3i pos) {
    long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
    WorldChunk worldChunk = world.getChunk(chunkIndex);
    assert worldChunk != null;
    EntityChunk entityChunk = worldChunk.getEntityChunk();
    assert entityChunk != null;
    BlockState blockState = world.getState(pos.x, pos.y, pos.z, false);
    if (blockState == null) {
      LOGGER.atSevere().log(
          "ActionTakeFromNearbyStorage -> null BlockState at position where container was expected: "
              + pos);
      return null;
    }
    if (blockState.getClass() != ItemContainerState.class) {
      LOGGER.atSevere().log(
          String.format(
              "ActionTakeFromNearbyStorage -> BlockState at %s was %s where ItemContainerState was expected",
              pos, blockState.getClass()));
      return null;
    }
    ItemContainerState itemContainerState = (ItemContainerState) blockState;
    return itemContainerState.getItemContainer();
  }

  public static boolean transferItem(ItemContainer source, ItemContainer target) {
    for (short slot = 0; slot < source.getCapacity() - 1; slot++) {
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
