package com.clayfactoria.sensors;

import com.clayfactoria.sensors.builders.BuilderSensorBlockUnder;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class SensorBlockUnder extends SensorBaseLogger {
  protected final String block;

  public SensorBlockUnder(@Nonnull BuilderSensorBlockUnder builder, @Nonnull
  BuilderSupport builderSupport) {
    super(builder);
    this.block = builder.getBlock(builderSupport);
  }

  @Override
  public boolean matchesNullChecked(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull
  Store<EntityStore> store) {
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    Objects.requireNonNull(component);
    NPCEntity npcEntity = store.getComponent(ref, component);
    Objects.requireNonNull(npcEntity);

    Vector3d pos = npcEntity.getOldPosition();
    World world = npcEntity.getWorld();
    Objects.requireNonNull(world);

    BlockType blockType = world.getBlockType(pos.toVector3i().add(0,-1,0));
    Objects.requireNonNull(blockType);
    return blockType.getId().equals(block);
  }

  @Override
  public @Nullable InfoProvider getSensorInfo() {return null;}
}
