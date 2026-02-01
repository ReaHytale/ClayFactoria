package com.clayfactoria.models;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;

import javax.annotation.Nonnull;

public class WorldWaypoint {
    @Nonnull
    public static final BuilderCodec<WorldWaypoint> CODEC = BuilderCodec.builder(WorldWaypoint.class, WorldWaypoint::new)
            .append(
                    new KeyedCodec<>("PositionX", Codec.DOUBLE),
                    (component, x) -> component.positionX = x,
                    component -> component.positionX
            )
            .documentation("The x position").add()
            .append(
                    new KeyedCodec<>("PositionY", Codec.DOUBLE),
                    (component, y) -> component.positionY = y,
                    component -> component.positionY
            )
            .documentation("The y position").add()
            .append(
                    new KeyedCodec<>("PositionZ", Codec.DOUBLE),
                    (component, z) -> component.positionZ = z,
                    component -> component.positionZ
            )
            .documentation("The z position").add()
            .append(
                    new KeyedCodec<>("RotationX", Codec.FLOAT),
                    (component, x) -> component.rotationX = x,
                    component -> component.rotationX
            )
            .documentation("The x rotation").add()
            .append(
                    new KeyedCodec<>("RotationYY", Codec.FLOAT),
                    (component, y) -> component.rotationY = y,
                    component -> component.rotationY
            )
            .documentation("The y rotation").add()
            .append(
                    new KeyedCodec<>("RotationZ", Codec.FLOAT),
                    (component, z) -> component.rotationZ = z,
                    component -> component.rotationZ
            )
            .documentation("The z rotation").add()
            .build();

    @Getter
    private double positionX;

    @Getter
    private double positionY;

    @Getter
    private double positionZ;

    @Getter
    private float rotationX;

    @Getter
    private float rotationY;

    @Getter
    private float rotationZ;

    public WorldWaypoint() {
        positionX = 0f;
        positionY = 0f;
        positionZ = 0f;

        rotationX = 0f;
        rotationY = 0f;
        rotationZ = 0f;
    }

    public WorldWaypoint(double positionX, double positionY, double positionZ, float rotationX, float rotationY, float rotationZ) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;

        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    @Override
    public String toString() {
        return String.format("PathWaypoint {\nPosition(%f, %f, %f)\nRotation(%f, %f, %f)\n}", positionX, positionY, positionZ, rotationX, rotationY, rotationZ);
    }
}
