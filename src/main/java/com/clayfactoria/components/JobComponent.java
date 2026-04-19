package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.codecs.Job;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JobComponent implements Component<EntityStore> {

    @Nonnull
    public static final BuilderCodec<JobComponent> CODEC =
        BuilderCodec.builder(JobComponent.class, JobComponent::new)
            .append(
                new KeyedCodec<>("PlayerId", Codec.UUID_BINARY),
                (comp, id) -> comp.playerId = id,
                comp -> comp.playerId)
            .documentation("The player id")
            .add()
            .append(
                new KeyedCodec<>("Jobs", new ArrayCodec<>(Job.CODEC, Job[]::new)),
                (comp, jobs) -> comp.jobs = new ArrayList<>(Arrays.asList(jobs)),
                (comp) -> comp.jobs.toArray(new Job[0]))
            .documentation("The jobs for pathing and actions for each location")
            .add()
            .append(
                new KeyedCodec<>("CurrentJob", Job.CODEC),
                (comp, job) -> comp.currentJob = job,
                (comp) -> comp.currentJob)
            .documentation("The current job with location and action")
            .add()
            .append(
                new KeyedCodec<>("CurrentTargetIndex", Codec.INTEGER),
                (comp, index) -> comp.currentTargetIndex = index,
                (comp) -> comp.currentTargetIndex)
            .documentation("The index for current target")
            .add()
            .append(
                new KeyedCodec<>("IsComplete", Codec.BOOLEAN),
                (comp, bool) -> comp.isComplete = bool,
                (comp) -> comp.isComplete)
            .documentation("Flag for when a task is complete or not")
            .add()
            .append(
                new KeyedCodec<>("FilterItem", Codec.STRING),
                (comp, item) -> comp.filterItem = item,
                (comp) -> comp.filterItem)
            .documentation("ID of the NPC's current filter item.")
            .add()
            .build();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Getter
    @Setter
    @Nullable
    private UUID playerId;
    @Getter
    @Setter
    private List<Job> jobs = new ArrayList<>();
    @Getter
    @Setter
    @Nullable
    private Job currentJob;
    @Getter
    @Setter
    private int currentTargetIndex;
    @Getter
    @Setter
    private boolean isComplete = false;
    @Getter
    @Setter
    private String filterItem;

    public static ComponentType<EntityStore, JobComponent> getComponentType() {
        return ClayFactoria.ownerComponentType;
    }

    @Nullable
    public Job nextJob() {
        isComplete = false;
        currentTargetIndex = (currentTargetIndex + 1) % jobs.size(); // Loop
        currentJob = jobs.get(currentTargetIndex);
        LOGGER.atInfo().log("Next job is: %s", currentJob);
        return currentJob;
    }

    @Override
    public Component<EntityStore> clone() {
        JobComponent jobComponent = new JobComponent();
        jobComponent.playerId = this.playerId;
        jobComponent.jobs = new ArrayList<>(this.jobs.stream().map(Job::clone).toList());
        jobComponent.currentTargetIndex = this.currentTargetIndex;
        if (jobComponent.currentTargetIndex < jobComponent.jobs.size()) {
            jobComponent.currentJob = jobComponent.jobs.get(jobComponent.currentTargetIndex);
        }
        jobComponent.isComplete = this.isComplete;
        this.filterItem = jobComponent.filterItem;
        return jobComponent;
    }
}
