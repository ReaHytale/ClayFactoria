package com.clayfactoria.systems;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.components.DropInventoryOnDeathComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NPCDeathSystem extends DeathSystems.OnDeathSystem {

  @Override
  public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent component,
                               @NotNull Store<EntityStore> store,
                               @NotNull CommandBuffer<EntityStore> commandBuffer) {
    ComponentType<EntityStore, NPCEntity> componentType = NPCEntity.getComponentType();
    if (componentType == null) {
      return;
    }
    NPCEntity entity = store.getComponent(ref, componentType);
    if (entity == null) {
      return;
    }

    ComponentType<EntityStore, DropInventoryOnDeathComponent> dropOnDeathCompType =
        DropInventoryOnDeathComponent.getComponentType();
    if (dropOnDeathCompType == null) {
      return;
    }
    DropInventoryOnDeathComponent dropOnDeathComp = store.getComponent(ref, dropOnDeathCompType);
    if (dropOnDeathComp == null || !dropOnDeathComp.isDropInventoryOnDeath()) {
      return;
    }

    entity.getInventory().getCombinedStorageFirst().dropAllItemStacks();

    Universe.get().sendMessage(Message.raw("Hello from the " + entity.getNPCTypeId()));
  }

  @Override
  public @Nullable Query<EntityStore> getQuery() {
    return Query.and(NPCEntity.getComponentType(), DropInventoryOnDeathComponent.getComponentType());
  }
}
