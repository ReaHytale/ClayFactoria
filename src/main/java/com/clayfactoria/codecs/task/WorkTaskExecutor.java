package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Objects;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

public class WorkTaskExecutor extends PointTaskExecutor {

    @Override
    public boolean canPerformTask(Ref<EntityStore> entityRef) {
        Store<EntityStore> store = entityRef.getStore();

        ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
        Objects.requireNonNull(component, "NPC Entity Component Type was null");

        NPCEntity npcEntity = store.getComponent(entityRef, component);
        Objects.requireNonNull(npcEntity, "NPC Entity was null");

        Component<ChunkStore> nearbyPOI = TaskHelper.findNearbyPOI(npcEntity, Task.WORK);
        return nearbyPOI != null;
    }

    @Override
    public boolean execute(Ref<EntityStore> entityRef) {
        NPCEntity npcEntity = getNPCEntity(entityRef);
        Store<EntityStore> store = entityRef.getStore();
        JobComponent jobComponent = Objects.requireNonNull(
            store.getComponent(entityRef, JobComponent.getComponentType()));
        Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());
        World world = Objects.requireNonNull(npcEntity.getWorld());

        Vector3i pos = currentJob.getLocation();
        Ref<ChunkStore> blockRef = TaskHelper.getBlockComponentHolderDirectReference(world, pos.x,
            pos.y, pos.z);
        assert blockRef != null;
        ProcessingBenchBlock processingBenchBlock = blockRef.getStore().getComponent(blockRef,
            ProcessingBenchBlock.getComponentType());
        BenchBlock benchBlock = blockRef.getStore()
            .getComponent(blockRef, BenchBlock.getComponentType());

        if (processingBenchBlock == null || benchBlock == null || processingBenchBlock.getInputContainer().isEmpty()) {
            return false;
        }
        return processingBenchBlock.setActive(true, benchBlock, null);
    }

}
