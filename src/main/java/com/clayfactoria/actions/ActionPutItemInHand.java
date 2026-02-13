package com.clayfactoria.actions;

import static com.clayfactoria.utils.Utils.checkNull;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * An action that moves the item in the interacting player's hand to the hand of the entity.
 *
 * @author Lordimass
 */
public class ActionPutItemInHand extends ActionBaseLogger {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public ActionPutItemInHand(@NotNull BuilderActionBase builderActionBase) {
    super(builderActionBase);
  }

  public boolean executeNullChecked(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      InfoProvider sensorInfo,
      double dt,
      @Nonnull Store<EntityStore> store) {
    // Get the player
    Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
    checkNull(playerRef, "playerRef was null");
    Player player = store.getComponent(playerRef, Player.getComponentType());
    checkNull(player, "Player was null");

    // Get the NPC
    ComponentType<EntityStore, NPCEntity> componentType = NPCEntity.getComponentType();
    checkNull(componentType, "componentType was null");
    NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
    checkNull(npc, "npc was null");

    // Get the Item Stack in the player's & NPC's active slots
    ItemStack playerActiveItemStack = player.getInventory().getItemInHand();

    // Get player itemStack with only 1 item, ready to transfer
    if (playerActiveItemStack == null || playerActiveItemStack.isEmpty()) {
      // Player hand empty
      return false;
    }
    ItemStack playerAtomicItemStack = playerActiveItemStack.withQuantity(1);
    checkNull(playerAtomicItemStack, "playerAtomicItemStack was null");

    // If the NPC inventory is full, drop item in hand
    if (npc.getInventory().getCombinedStorageFirst().canAddItemStack(playerAtomicItemStack)) {
      byte activeHotbarSlot = npc.getInventory().getActiveHotbarSlot();
      // TODO: Test that this actually drops the item and doesn't just destroy it.
      npc.getInventory().getHotbar().removeItemStackFromSlot(activeHotbarSlot);
    }

    // Move single item from player hand into the NPC inventory
    byte activeHotbarSlot = player.getInventory().getActiveHotbarSlot();
    MoveTransaction<ItemStackTransaction> moveTransaction =
        player
            .getInventory()
            .getHotbar()
            .moveItemStackFromSlot(
                activeHotbarSlot, 1, npc.getInventory().getCombinedStorageFirst());
    if (!moveTransaction.succeeded()) {
      LOGGER.atSevere().log("ActionPutItemInHand: execute -> Failed to put item in NPC Inventory");
      return false;
    }

    // Search through the NPC inventory to find the item that was just moved in
    short itemSlot = -1;
    int quantity = 0;
    for (short slot = 0; slot <= moveTransaction.getOtherContainer().getCapacity(); slot++) {
      ItemStack itemStack = moveTransaction.getOtherContainer().getItemStack(slot);
      if (itemStack == null) {
        continue;
      }
      Item item = itemStack.getItem();
      if (item.equals(playerAtomicItemStack.getItem())) {
        itemSlot = slot;
        quantity = itemStack.getQuantity();
      }
    }
    if (itemSlot == -1) {
      LOGGER.atSevere().log(
          "ActionPutItemInHand: execute -> Failed to find item in NPC Inventory after moving.");
      return false;
    }

    // Swap this into the active slot
    npc.getInventory()
        .getCombinedStorageFirst()
        .moveItemStackFromSlotToSlot(
            itemSlot,
            quantity,
            npc.getInventory().getHotbar(),
            npc.getInventory().getActiveHotbarSlot());
    return true;
  }
}
