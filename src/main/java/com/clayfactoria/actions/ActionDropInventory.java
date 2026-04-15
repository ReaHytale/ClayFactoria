package com.clayfactoria.actions;

import static com.clayfactoria.utils.TaskHelper.getHotbarItems;

import com.clayfactoria.actions.builders.BuilderActionDropInventory;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.List;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionDropInventory extends ActionBaseLogger {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  private final boolean dropHotbarItems;

  public ActionDropInventory(@NotNull BuilderActionDropInventory builder,
      BuilderSupport builderSupport) {
    super(builder);
    dropHotbarItems = builder.getDropHotbarItems(builderSupport);
  }

  @Override
  public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role,
      InfoProvider sensorInfo, double dt, @Nonnull
      Store<EntityStore> store) {
    return super.canExecute(ref, role, sensorInfo, dt, store);
  }

  @Override
  public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo,
      double dt, @Nonnull Store<EntityStore> store) {
    NPCEntity npc = TaskHelper.getNPCEntity(ref);
    List<String> hotbarItems = getHotbarItems(npc.getRole());
    CombinedItemContainer itemContainer = InventoryComponent.getCombined(store, ref,
        InventoryComponent.EVERYTHING);
    itemContainer.forEach((slot, item) -> {
      if (dropHotbarItems
          || hotbarItems != null && !hotbarItems.contains(item.getItemId())
      ) {
        ItemUtils.throwItem(ref, store, item, Vector3d.ZERO, 1.0F);
        itemContainer.removeItemStackFromSlot(slot);
      }
    });

    return true;
  }
}
