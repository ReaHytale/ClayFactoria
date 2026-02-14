package com.clayfactoria.actions;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;
import static com.clayfactoria.utils.Utils.checkNull;
import com.clayfactoria.actions.builders.BuilderActionTake;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.utils.ContainerSlot;
import com.clayfactoria.utils.TaskHelper;
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
    NPCEntity npcEntity = getNPCEntity(ref, store);
    ItemContainer itemContainer = TaskHelper.getOrthogonalContainer(npcEntity, ContainerSlot.Output);
    checkNull(itemContainer);

    TaskComponent taskComponent = store.getComponent(ref, TaskComponent.getComponentType());
    checkNull(taskComponent, "Task Component was null");

    // Take an item from the container
    boolean result =
        TaskHelper.transferItem(itemContainer, npcEntity.getInventory().getCombinedStorageFirst());

    if (result) {
      LOGGER.atSevere().log("Action Take: Set Complete to true\n");
      taskComponent.setComplete(true);
    }

    return result;
  }
}
