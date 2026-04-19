package com.clayfactoria.utils;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent.Hotbar;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class TaskHelper {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private TaskHelper() {
    }

    @Nonnull
    public static NPCEntity getNPCEntity(
        @Nonnull Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
        Objects.requireNonNull(component, "NPCEntity Component Type was null");
        NPCEntity npcEntity = store.getComponent(ref, component);
        Objects.requireNonNull(npcEntity, "NPCEntity was null");
        return npcEntity;
    }

    @Nullable
    public static Component<ChunkStore> getBlockEntity(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        NPCEntity npcEntity = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        Objects.requireNonNull(npcEntity);
        JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
        assert jobComponent != null;
        World world = Objects.requireNonNull(npcEntity.getWorld());
        Job job = jobComponent.getCurrentJob();
        if (job == null) {
            return null;
        }
        Vector3i pos = job.getLocation();
        Vector3i baseBlock = BlockUtils.getBaseBlock(pos, world);
        Holder<ChunkStore> holder = world.getBlockComponentHolder(baseBlock.x, baseBlock.y,
            baseBlock.z);
        if (holder == null) return null;

        ItemContainerBlock itemContainerBlock = holder.getComponent(
            ItemContainerBlock.getComponentType());
        ProcessingBenchBlock processingBenchBlock = holder.getComponent(
            ProcessingBenchBlock.getComponentType());

        switch (job.getTask()) {
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

        return null;
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

    public static Ref<ChunkStore> getBlockComponentHolderDirectReference(World world, int x,
                                                                         int y, int z) {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
        assert chunk != null;

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
            boolean result = transferItem(source, target, slot);
            if (result) {
                return true;
            }
        }
        // No item found in storage, return false for failure.
        return false;
    }

    public static boolean transferItem(ItemContainer source, ItemContainer target, short slot) {
        ItemStack itemStack = source.getItemStack(slot);
        if (itemStack == null) {
            return false;
        }
        return transferItem(source, target, slot, itemStack.getQuantity());
    }

    public static boolean transferItem(ItemContainer source, ItemContainer target, int quantity, String filterItemId) {
        for (short slot = 0; slot < source.getCapacity(); slot++) {
            if (filterItemId != null) {
                if (source.getItemStack(slot) == null || !source.getItemStack(slot).getItemId().equals(filterItemId)) {
                    continue;
                }
            }
            boolean result = transferItem(source, target, slot, quantity);
            if (result) {
                return true;
            }
        }
        // No item found in storage, return false for failure.
        return false;
    }

    public static boolean transferItem(ItemContainer source, ItemContainer target, short slot,
                                       int quantity) {
        ItemStack itemStack = source.getItemStack(slot);
        if (itemStack == null) {
            return false;
        }
        int prevQuantity = itemStack.getQuantity();
        source.moveItemStackFromSlot(slot, quantity, target);
        // Check whether it actually succeeded to transfer
        itemStack = source.getItemStack(slot);
        if (itemStack == null) {
            return true;
        } else {
            return itemStack.getQuantity() == prevQuantity - quantity;
        }
    }

    public static ItemContainer getNPCInventory(NPCEntity npcEntity, Store<EntityStore> store) {
        assert npcEntity.getReference() != null;
        return InventoryComponent.getCombined(store, npcEntity.getReference(),
            InventoryComponent.EVERYTHING);
    }

    public static void idleAutomaton(Ref<EntityStore> npcRef, Store<EntityStore> store) {
        NPCEntity npcEntity = store.getComponent(npcRef,
            Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcEntity == null) {
            return;
        }
        Objects.requireNonNull(npcEntity.getRole())
            .getStateSupport()
            .setState(npcRef, "Idle", null, store);
    }

    public static ItemStack getHeldItemstack(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        Hotbar hotbar = store.getComponent(entityRef, Hotbar.getComponentType());
        assert hotbar != null;
        return hotbar.getActiveItem();
    }

    public static List<String> getHotbarItems(Role role) {
        try {
            Field hotbarItemsField = Role.class.getDeclaredField("hotbarItems");
            hotbarItemsField.setAccessible(true);
            String[] hotbarItemsArray = (String[]) hotbarItemsField.get(role);
            if (hotbarItemsArray != null) {
                return Arrays.asList(hotbarItemsArray);
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.atSevere().log(e.getMessage());
            return null;
        }
    }

}
