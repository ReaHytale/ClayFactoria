package com.clayfactoria.systems;

import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.models.PathWaypoint;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.awt.*;

public class TargetBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
  /** ID of the item to use as a wand for setting Automaton paths. */
  private static final String WAND_ITEM_ID = "Ingredient_Stick";

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, BrushComponent> brushComponentType =
      BrushComponent.getComponentType();
  @Nonnull private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;

  public TargetBlockEventSystem(
      @Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType) {
    super(DamageBlockEvent.class);
    this.npcEntityComponentType = npcEntityComponentType;
  }

  @Override
  public void handle(
      int index,
      @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
      @NonNull Store<EntityStore> store,
      @NonNull CommandBuffer<EntityStore> commandBuffer,
      @NonNull DamageBlockEvent damageBlockEvent) {

    Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);

    Player player = store.getComponent(entityStoreRef, Player.getComponentType());
    if (player == null) return;

    Ref<EntityStore> playerRef = player.getReference();
    if (playerRef == null) {
      LOGGER.atSevere().log("Target Block Event System: playerRef was null");
      return;
    }

    // Check that the player has the wand equipped
    if (!isWandEquipped(player)) {
      return;
    }

    BrushComponent brushComponent = store.getComponent(playerRef, this.brushComponentType);
    if (brushComponent == null) {
      LOGGER.atSevere().log("Target Block Event System: Brush Component on the player was null");
      return;
    }

    damageBlockEvent.setDamage(0);

    // TODO: May need to use entityRef instead of entityStoreRef
    HeadRotation headRotationComponent =
        store.getComponent(entityStoreRef, HeadRotation.getComponentType());
    if (headRotationComponent == null) {
      LOGGER.atSevere().log("Target Block Event System: headRotationComponent was null");
      return;
    }

    Vector3i targetBlockLoc = damageBlockEvent.getTargetBlock();
    Vector3f headRotation = headRotationComponent.getRotation();

    PathWaypoint pathWaypoint =
        new PathWaypoint(
            targetBlockLoc.x,
            targetBlockLoc.y,
            targetBlockLoc.z,
            headRotation.x,
            headRotation.y,
            headRotation.z);

    if (brushComponent.getPathStart() == null) {
      brushComponent.setPathStart(pathWaypoint);
    } else if (brushComponent.getPathEnd() == null) {
      brushComponent.setPathEnd(pathWaypoint);
    } else {
      brushComponent.setPathStart(null);
      brushComponent.setPathEnd(null);
      player.sendMessage(Message.raw("Resetting path...").color(Color.YELLOW));

      ParticleUtil.spawnParticleEffect(
          "Block_Break_Dust",
          new Vector3d(targetBlockLoc.x + 0.5, targetBlockLoc.y + 1, targetBlockLoc.z + 0.5),
          store);

      SoundUtil.playSoundEvent2d(
          SoundEvent.getAssetMap().getIndex("SFX_Drag_Items_Clay"),
          SoundCategory.SFX,
          commandBuffer);
      return;
    }

    ParticleUtil.spawnParticleEffect(
        "Block_Hit_Dirt",
        new Vector3d(targetBlockLoc.x + 0.5, targetBlockLoc.y + 1, targetBlockLoc.z + 0.5),
        store);

    SoundUtil.playSoundEvent2d(
        SoundEvent.getAssetMap().getIndex("SFX_Drop_Items_Clay"), SoundCategory.SFX, commandBuffer);

    String message =
        String.format(
            "Set Path Block: (%d, %d, %d)", targetBlockLoc.x, targetBlockLoc.y, targetBlockLoc.z);
    LOGGER.atInfo().log(message);
    player.sendMessage(Message.raw(message).color(Color.GREEN));
  }

  @Override
  public Query<EntityStore> getQuery() {
    return PlayerRef.getComponentType();
  }

  /**
   * Checks if the specified player is holding the wand item.
   * @param player The player to check the condition in relation to.
   * @return <code>true</code> if the player is holding the wand, <code>false</code> otherwise.
   */
  private boolean isWandEquipped(Player player) {
    // Get player inventory.
    Inventory inventory = player.getInventory();
    if (inventory == null) {
      return false;
    }

    // Get item in active hotbar slot.
    byte slot = inventory.getActiveHotbarSlot();
    ItemStack itemStack = inventory.getHotbar().getItemStack(slot);
    if (itemStack == null) {
      return false;
    }

    // Check if held item is the wand.
    return itemStack.getItemId().equals(WAND_ITEM_ID);
  }
}
