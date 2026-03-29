package com.clayfactoria.utils;

import com.clayfactoria.codecs.Action;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TaskHelper {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private TaskHelper() {
  }

  @Nonnull
  public static NPCEntity getNPCEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component, "NPCEntity Component Type was null");
    NPCEntity npcEntity = store.getComponent(ref, component);
    Objects.requireNonNull(npcEntity, "NPCEntity was null");
    return npcEntity;
  }

  @Nullable
  public static Holder<ChunkStore> findNearbyPOIHolder(NPCEntity npcEntity, Action action) {
    World world = Objects.requireNonNull(npcEntity.getWorld());
    Vector3i pos = npcEntity.getOldPosition().toVector3i();
    List<Vector3i> shuffled = getAdjacentDirections();
    for (Vector3i dir : shuffled) {
      BlockType type = world.getBlockType(pos.clone().add(dir));
      if (type == null) {
        continue;
      }
      Holder<ChunkStore> holder = type.getBlockEntity();
      if (holder != null) {
        return holder;
      }
    }
    return null;
  }

  @Nullable
  public static Component<ChunkStore> findNearbyPOI(NPCEntity npcEntity, Action action) {
    World world = Objects.requireNonNull(npcEntity.getWorld());
    Vector3i pos = npcEntity.getOldPosition().toVector3i();
    List<Vector3i> shuffled = getAdjacentDirections();
    for (Vector3i dir : shuffled) {
      Vector3i baseBlock = BlockUtils.getBaseBlock(new Vector3i(pos.add(dir)), world);
      Holder<ChunkStore> holder = world.getBlockComponentHolder(baseBlock.x, baseBlock.y,
          baseBlock.z);
      if (holder == null) {
        continue;
      }
      ItemContainerBlock itemContainerBlock = holder.getComponent(
          ItemContainerBlock.getComponentType());
      ProcessingBenchBlock processingBenchBlock = holder.getComponent(
          ProcessingBenchBlock.getComponentType());

      switch (action) {
        case POSITION:
          return null;
        case TAKE, DEPOSIT: // Find a container
          if (itemContainerBlock != null) {
            return itemContainerBlock;
          }
          if (processingBenchBlock != null) {
            return processingBenchBlock;
          }
        case WORK: // Find a processing bench
          if (processingBenchBlock != null) {
            return processingBenchBlock;
          }
      }
    }
    return null;
  }

  @Nullable
  public static ItemContainer getOrthogonalItemContainer(NPCEntity npcEntity,
      @Nullable ContainerSlot containerSlot) {
    World world = Objects.requireNonNull(npcEntity.getWorld());
    Component<ChunkStore> poi = Objects.requireNonNull(findNearbyPOI(npcEntity, Action.TAKE));
    return getItemContainerFromComponent(poi, containerSlot);
  }

  public static ItemContainer getItemContainerAtPos(
      World world,
      Vector3i pos,
      @Nullable ContainerSlot containerSlot
  ) {
    Ref<ChunkStore> ref = getBlockComponentHolderDirectReference(world, pos.x, pos.y, pos.z);
    assert ref != null;
    ItemContainerBlock itemContainerBlock = ref.getStore().getComponent(ref,
        ItemContainerBlock.getComponentType());
    if (itemContainerBlock != null) {
      // This is an item container, not a processing bench, so we return straight away
      return itemContainerBlock.getItemContainer();
    }
    ProcessingBenchBlock processingBenchBlock = ref.getStore().getComponent(ref,
        ProcessingBenchBlock.getComponentType());
    if (processingBenchBlock == null || containerSlot == null) {
      return null;
    }
    return getItemContainerFromComponent(processingBenchBlock, containerSlot);
  }

  private static Ref<ChunkStore> getBlockComponentHolderDirectReference(World world, int x,
      int y, int z) {
    LOGGER.atInfo().log("Get block component holder");
    WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
    LOGGER.atInfo().log("Chunk at x;z =" + x + ";" + z);
    assert chunk != null;
    LOGGER.atInfo().log("Chunk is not null");

    return y >= 0 && y < 320 ? internalGetBlockComponentHolderDirectReference(chunk, x, y, z)
        : null;
  }

  private static Ref<ChunkStore> internalGetBlockComponentHolderDirectReference(WorldChunk chunk,
      int x,
      int y,
      int z) {
    if (y >= 0 && y < 320) {
      if (!chunk.getWorld().isInThread()) {
        return CompletableFuture.supplyAsync(
                () -> internalGetBlockComponentHolderDirectReference(chunk, x, y, z), chunk.getWorld())
            .join();
      } else {
        int index = ChunkUtil.indexBlockInColumn(x, y, z);
        assert chunk.getBlockComponentChunk() != null;
        Ref<ChunkStore> entityReference = chunk.getBlockComponentChunk().getEntityReference(index);
        assert entityReference != null;
        return entityReference;
      }
    } else {
      return null;
    }
  }

  public static ItemContainer getItemContainerFromComponent(
      Component<ChunkStore> component,
      @Nullable ContainerSlot containerSlot
  ) {
    if (component.getClass() == ItemContainerBlock.class) {
      return ((ItemContainerBlock) component).getItemContainer();
    } else if (component.getClass() == ProcessingBenchBlock.class && containerSlot != null) {
      return containerSlot.getItemContainer((ProcessingBenchBlock) component);
    } else {
      return null;
    }
  }

  private static List<Vector3i> getAdjacentDirections() {
    // Check surrounding blocks
    Vector3i[] directions = {
        new Vector3i(0, 0, -1), new Vector3i(1, 0, 0), new Vector3i(0, 0, 1), new Vector3i(-1, 0, 0)
    };

    // Shuffle order to prevent order of check being predictable
    List<Vector3i> shuffled = Arrays.asList(directions);
    Collections.shuffle(shuffled);
    return shuffled;
  }

  public static boolean transferItem(ItemContainer source, ItemContainer target) {
    for (short slot = 0; slot < source.getCapacity(); slot++) {
      ItemStack itemStack = source.getItemStack(slot);
      if (itemStack == null) {
        continue;
      }
      MoveTransaction<ItemStackTransaction> itemStackTransactionMoveTransaction =
          source.moveItemStackFromSlot(slot, 1, target);
      return itemStackTransactionMoveTransaction.succeeded();
    }
    // No item found in storage, return false for failure.
    return false;
  }
}
