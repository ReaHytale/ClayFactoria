package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.clayfactoria.models.WorldWaypoint;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.UUID;

public class BrushComponent implements Component<EntityStore> {
    @Nonnull
    public static final BuilderCodec<BrushComponent> CODEC = BuilderCodec.builder(BrushComponent.class, BrushComponent::new)
            .append(
                    new KeyedCodec<>("Path", ArrayCodec <>),
                    (component, pos) -> component.pathStart = pos,
                    component -> component.pathStart
            )
            .documentation("The starting position for pathing").add()
            .append(
                    new KeyedCodec<>("PathEnd", WorldWaypoint.CODEC),
                    (component, pos) -> component.pathEnd = pos,
                    component -> component.pathEnd
            )
            .documentation("The ending position for pathing").add()
            .append(
                    new KeyedCodec<>("TargetEntityId", Codec.UUID_STRING),
                    (component, id) -> component.targetEntityId = id,
                    component -> component.targetEntityId
            )
            .documentation("The target entity id for a command").add()
            .build();

    @Getter
    @Setter
    private WorldWaypoint pathStart;

    @Getter
    @Setter
    private WorldWaypoint pathEnd;

    @Setter
    @Getter
    private UUID targetEntityId;

    public Component<EntityStore> clone() {
        BrushComponent brushComponent = new BrushComponent();
        brushComponent.pathStart = this.pathStart;
        brushComponent.pathEnd = this.pathEnd;
        brushComponent.targetEntityId = this.targetEntityId;
        return brushComponent;
    }

    public static ComponentType<EntityStore, BrushComponent> getComponentType() {
        return ClayFactoria.brushComponentType;
    }

    public Vector3d getPathStartPosition() {
        return new Vector3d(pathStart.getPositionX(), pathStart.getPositionY(), pathStart.getPositionZ());
    }

    public Vector3f getPathStartRotation() {
        return new Vector3f(pathStart.getRotationX(), pathStart.getRotationY(), pathStart.getRotationZ());
    }

    public Vector3d getPathEndPosition() {
        return new Vector3d(pathEnd.getPositionX(), pathEnd.getPositionY(), pathEnd.getPositionZ());
    }

    public Vector3f getPathEndRotation() {
        return new Vector3f(pathEnd.getRotationX(), pathEnd.getRotationY(), pathEnd.getRotationZ());
    }
}
