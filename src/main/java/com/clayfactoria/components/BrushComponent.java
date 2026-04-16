package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobBoxComponent.JobBoxesComponent;
import com.clayfactoria.systems.TaskBoxSystem;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BrushComponent implements Component<EntityStore> {

    @Nonnull
    public static final BuilderCodec<BrushComponent> CODEC =
        BuilderCodec.builder(BrushComponent.class, BrushComponent::new)
            .append(
                new KeyedCodec<>("Tasks", new ArrayCodec<>(Job.CODEC, Job[]::new)),
                (comp, tasks) -> comp.jobs = new ArrayList<>(Arrays.asList(tasks)),
                (comp) -> comp.jobs.toArray(new Job[0]))
            .documentation("The tasks for pathing and actions for each location")
            .add()
            .append(
                new KeyedCodec<>("TaskType", Task.CODEC),
                (comp, value) -> comp.task = value,
                (comp) -> comp.task)
            .documentation("Type of task to be added to the tasks list on next brush paint.")
            .add()
            .append(
                new KeyedCodec<>("SelectedEntity", Codec.UUID_STRING),
                (comp, value) -> comp.entityId = value,
                (comp) -> comp.entityId)
            .documentation("The entity's internal UUID.")
            .add()
            .append(
                new KeyedCodec<>("BoxPoint1", Vector3i.CODEC),
                (comp, value) -> comp.boxPoint1 = value,
                (comp) -> comp.boxPoint1)
            .documentation("The first point of the current box selection, when applicable")
            .add()
            .build();

    @Getter
    private List<Job> jobs = new ArrayList<>();
    @Getter
    @Setter
    @Nonnull
    private Task task = Task.TAKE;
    @Getter
    @Setter
    private UUID entityId;
    @Getter
    private Vector3i boxPoint1;

    public static ComponentType<EntityStore, BrushComponent> getComponentType() {
        return ClayFactoria.brushComponentType;
    }

    public void setBoxPoint1(Vector3i boxPoint1, Ref<EntityStore> playerRef) {
        this.boxPoint1 = boxPoint1;
        if (boxPoint1 == null) {
            JobBoxesComponent jobBoxesComponent =
                playerRef.getStore().getComponent(playerRef, JobBoxesComponent.getComponentType());
            if (jobBoxesComponent != null && !jobBoxesComponent.boxes.isEmpty()) {
                jobBoxesComponent.boxes.removeLast();
            }
        }
    }

    public void addTask(
        Vector3i location,
        World world,
        ComponentAccessor<EntityStore> componentAccessor,
        Ref<EntityStore> playerRef) {
        this.jobs.add(new Job(location, task, world));
        JobBoxesComponent jobBoxesComponent =
            componentAccessor.getComponent(playerRef, JobBoxesComponent.getComponentType());
        if (jobBoxesComponent != null) {
            Box box = BlockUtils.getBlockBox(location, world);
            Vector3d min = box.min.subtract(TaskBoxSystem.BOX_PADDING);
            Vector3d max = box.max.add(TaskBoxSystem.BOX_PADDING);
            jobBoxesComponent.boxes.add(new JobBoxComponent(task.color, new Box(min, max)));
        }
        task = task.taskExecutor.relevantNextTask();
    }

    public void addTask(
        Box box,
        World world,
        ComponentAccessor<EntityStore> componentAccessor,
        Ref<EntityStore> playerRef) {
        try {
            this.task.taskExecutor.checkBounds(box);
        } catch (IllegalArgumentException e) {
            Player player = componentAccessor.getComponent(playerRef, Player.getComponentType());
            assert player != null;
            player.sendMessage(Message.raw(e.getMessage()));
            return;
        }
        this.jobs.add(new Job(box, task, world));
        this.setBoxPoint1(null, playerRef);
        JobBoxesComponent jobBoxesComponent =
            componentAccessor.getComponent(playerRef, JobBoxesComponent.getComponentType());
        assert jobBoxesComponent != null;
        jobBoxesComponent.boxes.add(new JobBoxComponent(task.color, box));
        task = task.taskExecutor.relevantNextTask();
    }

    public Component<EntityStore> clone() {
        BrushComponent brushComponent = new BrushComponent();
        brushComponent.jobs = this.jobs;
        brushComponent.task = this.task;
        return brushComponent;
    }

    public void resetTasks(
        ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> playerRef) {
        this.jobs = new ArrayList<>();
        JobBoxesComponent jobBoxesComponent =
            componentAccessor.getComponent(playerRef, JobBoxesComponent.getComponentType());
        if (jobBoxesComponent != null) {
            jobBoxesComponent.boxes.clear();
        }
    }
}
