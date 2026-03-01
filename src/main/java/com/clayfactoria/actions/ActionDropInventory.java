package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionDropInventory;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.List;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ActionDropInventory extends ActionBaseLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionDropInventory(@NotNull BuilderActionDropInventory builder) {
    super(builder);
  }

  @Override
  public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull
  Store<EntityStore> store) {
    return super.canExecute(ref, role, sensorInfo, dt, store);
  }

  @Override
  public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
    NPCEntity npc = TaskHelper.getNPCEntity(ref, store);
    // TODO: Figure out why this doesn't actually drop the items on the floor. It returns the items successfully, it just doesn't drop them like I'd expect it to.
    List<ItemStack> items = npc.getInventory().getCombinedStorageFirst().dropAllItemStacks();
    items.forEach(item -> LOGGER.atInfo().log("Dropping item: " + item.getItemId()));
    return true;
  }
}
