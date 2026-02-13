package com.clayfactoria.actions;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;
import static com.clayfactoria.utils.Utils.checkNull;

import com.clayfactoria.actions.builders.BuilderActionDeposit;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionDeposit extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final int quantity;

  public ActionDeposit(
      @NotNull BuilderActionDeposit builder, @Nonnull BuilderSupport builderSupport) {
    super(builder);
    this.quantity = builder.getQuantity(builderSupport);
  }

  @Override
  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    NPCEntity npcEntity = getNPCEntity(ref, store);
    ItemContainer itemContainer = TaskHelper.getOrthogonalContainer(npcEntity);
    checkNull(itemContainer);

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    checkNull(taskComponent, "Task Component was null");

    // Deposit an item to the container or station
    boolean result =
        TaskHelper.transferItem(npcEntity.getInventory().getCombinedStorageFirst(), itemContainer);

    if (result) {
      LOGGER.atInfo().log("Action Deposit: Set Complete to true");
      taskComponent.setComplete(true);
    }

    return result;
  }
}
