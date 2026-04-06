package com.clayfactoria.actions.automataactions;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

import com.clayfactoria.actions.ActionBaseLogger;
import com.clayfactoria.actions.automataactions.builders.BuilderActionDeposit;
import com.clayfactoria.codecs.Job;
import com.clayfactoria.components.JobComponent;
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
import java.util.Objects;
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
    JobComponent jobComponent = store.getComponent(ref, JobComponent.getComponentType());
    Objects.requireNonNull(jobComponent, "Task Component was null");

    // Attempt to deposit as fuel first (if this is a station with a fuel slot)
    if (deposit(ContainerSlot.Fuel, npcEntity, jobComponent, store)) {
      return true;
    }
    return deposit(ContainerSlot.Input, npcEntity, jobComponent, store);
  }

  private boolean deposit(ContainerSlot containerSlot, NPCEntity npcEntity,
      JobComponent jobComponent, Store<EntityStore> store) {
    Job currentJob = jobComponent.getCurrentJob();
    Objects.requireNonNull(currentJob, "No task when trying to deposit");

    ItemContainer itemContainer = TaskHelper.getItemContainerAtPos(
        Objects.requireNonNull(npcEntity.getWorld()),
        currentJob.getLocation(),
        containerSlot);
    Objects.requireNonNull(itemContainer);

    ItemContainer npcInventory = TaskHelper.getNPCInventory(npcEntity, store);
    boolean result = TaskHelper.transferItem(npcInventory, itemContainer);

    if (result) {
      LOGGER.atInfo().log("Deposit action complete\n");
      jobComponent.setComplete(true);
    }
    return result;
  }
}
