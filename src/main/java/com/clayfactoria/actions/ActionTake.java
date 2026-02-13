package com.clayfactoria.actions;

import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.actions.builders.BuilderActionTake;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.helpers.TaskHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionTake extends ActionBaseLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final int quantity;

  public ActionTake(@NotNull BuilderActionTake builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.quantity = builder.getQuantity(builderSupport);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    checkNull(component, "NPCEntity Component Type was null");

    NPCEntity npcEntity = store.getComponent(ref, component);
    checkNull(npcEntity, "NPCEntity was null");

    World world = npcEntity.getWorld();
    checkNull(world, "World was null");

    // Get item container orthogonal to the entity.
    Vector3i containerPos = TaskHelper.findNearbyContainer(npcEntity);
    checkNull(containerPos, "No container found");
    ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(world, containerPos);
    checkNull(itemContainer, "Item container not found at expected position");

    // Take an item from the container
    boolean result = TaskHelper.transferItem(
        itemContainer,
        npcEntity.getInventory().getCombinedStorageFirst()
    );

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    checkNull(taskComponent, "Task Component was null");

    if (result) {
      LOGGER.atSevere().log("Action Take: Set Complete to true\n");
      taskComponent.setComplete(true);
    }

    return result;
  }
}
