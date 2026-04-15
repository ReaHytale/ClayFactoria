package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JobBoxComponent implements Component<EntityStore> {

    public static final BuilderCodec<JobBoxComponent> CODEC = BuilderCodec.builder(
            JobBoxComponent.class, JobBoxComponent::new)
        .append(
            new KeyedCodec<>("Colour", Vector3f.CODEC),
            (comp, colour) -> comp.colour = colour,
            comp -> comp.colour
        )
        .documentation("RGB colour to use for the box.")
        .add()
        .append(
            new KeyedCodec<>("Box", Box.CODEC),
            (comp, pos) -> comp.box = pos,
            comp -> comp.box
        )
        .documentation("Box to draw the debug box around")
        .add()
        .append(
            new KeyedCodec<>("Is Committed", Codec.BOOLEAN),
            (comp, isCommitted) -> comp.isCommitted = isCommitted,
            comp -> comp.isCommitted
        )
        .documentation("Whether this box is committed or should be redrawn until the player selects the second point")
        .add()
        .append(
            new KeyedCodec<>("Commit Start Location", Vector3d.CODEC),
            (comp, commitStartLocation) -> comp.commitStartLocation = commitStartLocation,
            comp -> comp.commitStartLocation
        )
        .documentation("If the component is not committed, then represents the first location, else is ignored")
        .add()
        .build();

    @Getter
    private Vector3f colour;
    @Getter
    @Setter
    private Box box;
    @Getter
    @Setter
    private boolean isCommitted;
    @Getter
    private Vector3d commitStartLocation;

    private JobBoxComponent() {
    }

    public JobBoxComponent(Vector3f colour, Box box, boolean isCommitted, Vector3d commitStartLocation) {
        this.colour = colour;
        this.box = box;
        this.isCommitted = isCommitted;
        this.commitStartLocation = commitStartLocation;
    }

    public JobBoxComponent(Vector3f colour, Box box) {
        this(colour, box, true, null);
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new JobBoxComponent(colour, box, isCommitted, commitStartLocation.clone());
    }

    public static class JobBoxesComponent implements Component<EntityStore> {

        public static final ArrayCodec<JobBoxComponent> CODEC = new ArrayCodec<>(
            JobBoxComponent.CODEC, JobBoxComponent[]::new);
        public List<JobBoxComponent> boxes = new ArrayList<>();

        public static ComponentType<EntityStore, JobBoxesComponent> getComponentType() {
            return ClayFactoria.debugBoxesComponentType;
        }

        @Override
        public @Nullable JobBoxComponent.JobBoxesComponent clone() {
            JobBoxesComponent jobBoxesComponent = new JobBoxesComponent();
            jobBoxesComponent.boxes = new ArrayList<>(boxes);
            return jobBoxesComponent;
        }
    }
}
