package com.clayfactoria.components;

import com.clayfactoria.ClayFactoria;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class DropInventoryOnDeathComponent implements Component<EntityStore> {
  @Nonnull
  public static final BooleanCodec CODEC = Codec.BOOLEAN;

  @Getter boolean dropInventoryOnDeath = false;

  @Override
  public @Nullable Component<EntityStore> clone() {
    DropInventoryOnDeathComponent duplicateComponent = new DropInventoryOnDeathComponent();
    duplicateComponent.dropInventoryOnDeath = this.dropInventoryOnDeath;
    return duplicateComponent;
  }

  public static ComponentType<EntityStore, DropInventoryOnDeathComponent> getComponentType() {
    return ClayFactoria.dropInventoryOnDeathComponentType;
  }
}
