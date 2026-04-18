package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public interface TaskExecutor {

    boolean canPerformTask(Ref<EntityStore> ref);

    boolean execute(Ref<EntityStore> entityRef);

    void checkBounds(Box bounds) throws IllegalArgumentException;

    Vector3d findNextWalkLocation(Job job, World world, Vector3d from);

    Task relevantNextTask(List<Task> availableOptions);
}
