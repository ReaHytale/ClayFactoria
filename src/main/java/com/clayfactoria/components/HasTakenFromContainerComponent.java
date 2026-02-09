package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

public class HasTakenFromContainerComponent implements Component<EntityStore> {
  @Nonnull
  public static final BuilderCodec<HasTakenFromContainerComponent>
      CODEC = BuilderCodec.builder(HasTakenFromContainerComponent.class, HasTakenFromContainerComponent::new)
      .append(
          new KeyedCodec<>("HasTakenFromContainer", Codec.BOOLEAN),
          (comp, hasTakenFromStorage) -> comp.hasTakenFromContainer = hasTakenFromStorage,
          comp -> comp.hasTakenFromContainer
      )
      .documentation("Whether this NPC has just taken an item from nearby container").add().build();

  @Getter
  @Setter
  private boolean hasTakenFromContainer;

  @Override
  public Component<EntityStore> clone() {
    HasTakenFromContainerComponent duplicateComponent = new HasTakenFromContainerComponent();
    duplicateComponent.hasTakenFromContainer = this.hasTakenFromContainer;
    return duplicateComponent;
  }

  public static ComponentType<EntityStore, HasTakenFromContainerComponent> getComponentType() {
    return ClayFactoria.hasTakenFromContainerComponentType;
  }
}
