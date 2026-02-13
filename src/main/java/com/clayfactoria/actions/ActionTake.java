package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionTake;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.helpers.TaskHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionTake extends ActionBase {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final int quantity;

  public ActionTake(@NotNull BuilderActionTake builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.quantity = builder.getQuantity(builderSupport);
  }

  public boolean execute(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    super.execute(ref, role, sensorInfo, dt, store);
    ComponentType<EntityStore, NPCEntity> component = NPCEntity.getComponentType();
    if (component == null) {
      LOGGER.atSevere().log("Action Taks -> NPCEntity Component Type was null");
      return false;
    }
    NPCEntity npcEntity = store.getComponent(ref, component);
    if (npcEntity == null) {
      LOGGER.atSevere().log("Action Taks -> NPCEntity was null");
      return false;
    }
    World world = npcEntity.getWorld();
    if (world == null) {
      LOGGER.atSevere().log("Action Taks -> World was null");
      return false;
    }

    // Get item container orthogonal to the entity.
    Vector3i containerPos = TaskHelper.findNearbyContainer(npcEntity);
    if (containerPos == null) {
      // No container found.
      LOGGER.atSevere().log("Action Taks -> No container found");
      return false;
    }
    ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(world, containerPos);
    if (itemContainer == null) {
      // Container not found at given position (should never occur)
      LOGGER.atSevere().log("Action Taks ->  Item Container not found at given position");
      return false;
    }

    // Take an item from the container
    boolean result =
        TaskHelper.transferItem(itemContainer, npcEntity.getInventory().getCombinedStorageFirst());

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    if (taskComponent == null) {
      LOGGER.atSevere().log("Action Take: Task Component was null");
      return false;
    }

    if (result) {
      LOGGER.atSevere().log("Action Take: Set Complete to true\n");
      taskComponent.setComplete(true);
    }

    return result;
  }
}
