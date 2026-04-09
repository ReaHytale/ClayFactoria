package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSelectHeldItem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class ActionSelectHeldItem extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  protected final String item;

  public ActionSelectHeldItem(
      @NotNull BuilderActionSelectHeldItem builderActionSelectHeldItem,
      BuilderSupport builderSupport) {
    super(builderActionSelectHeldItem);
    item = builderActionSelectHeldItem.getItem(builderSupport);
  }

  @Override
  public boolean executeNullChecked(@NotNull Ref<EntityStore> ref, @NotNull Role role,
      InfoProvider sensorInfo, double dt, @NotNull Store<EntityStore> store)
      throws NullPointerException {
    CombinedItemContainer combinedItemContainer = InventoryComponent.getCombined(store, ref,
        InventoryComponent.EVERYTHING);
    InventoryComponent.Hotbar hotbar = store.getComponent(ref,
        InventoryComponent.Hotbar.getComponentType());
    assert hotbar != null;
    AtomicBoolean found = new AtomicBoolean(false);
    combinedItemContainer.forEach((i, itemStack) -> {
      if (itemStack.getItemId().equals(item)) {
        combinedItemContainer.swapItems(i, hotbar.getInventory(), hotbar.getActiveSlot(),
            (short) 1);
        found.set(true);
      }
    });
    return found.get();
  }
}
