package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;


public class OwnerComponent implements Component<EntityStore> {
    @Nonnull
    public static final BuilderCodec<OwnerComponent> CODEC = BuilderCodec.builder(OwnerComponent.class, OwnerComponent::new)
            .append(
                    new KeyedCodec<>("PlayerId", Codec.UUID_BINARY),
                    (comp, id) -> comp.playerId = id,
                    comp -> comp.playerId
            )
            .documentation("The player id").add().build();

    @Getter
    @Setter
    @Nullable
    private UUID playerId;

    @Override
    public Component<EntityStore> clone() {
        OwnerComponent ownerComponent = new OwnerComponent();
        ownerComponent.playerId = this.playerId;
        return ownerComponent;
    }

    public static ComponentType<EntityStore, OwnerComponent> getComponentType() {
        return ClayFactoria.ownerComponentType;
    }
}
