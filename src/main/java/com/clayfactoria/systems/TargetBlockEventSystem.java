package com.clayfactoria.systems;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.List;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;

public class TargetBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
  /** ID of the item to use as a wand for setting Automaton paths. */
  private static final String WAND_ITEM_ID = "Tool_Brush";

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

    HeadRotation headRotationComponent =
        store.getComponent(entityStoreRef, HeadRotation.getComponentType());
    if (headRotationComponent == null) {
      LOGGER.atSevere().log("Target Block Event System: headRotationComponent was null");
      return;
    }

    Vector3i targetBlockLoc = damageBlockEvent.getTargetBlock();
    Vector3f headRotation = headRotationComponent.getRotation();

    TransformComponent entityTransformComp =
        store.getComponent(entityStoreRef, TransformComponent.getComponentType());
    if (entityTransformComp == null) {
      LOGGER.atSevere().log("Target Block Event System: entityTransformComp was null");
      return;
    }

    Transform targetTransform = entityTransformComp.getTransform().clone();
    Vector3d targetBlockLocOnTopOfBlock =
        new Vector3d(targetBlockLoc.x + 0.5, targetBlockLoc.y + 1, targetBlockLoc.z + 0.5);
    targetTransform.setPosition(targetBlockLocOnTopOfBlock);
    targetTransform.setRotation(headRotation);

    BlockType blockType = damageBlockEvent.getBlockType();
    if (blockType == BlockType.getAssetMap().getAsset("Rock_Crystal_Red_Block")) {

      player.sendMessage(Message.raw("Resetting path...").color(Color.RED));
      ParticleUtil.spawnParticleEffect("Block_Break_Dust", targetBlockLocOnTopOfBlock, store);
      SoundUtil.playSoundEvent2d(
          SoundEvent.getAssetMap().getIndex("SFX_Drag_Items_Clay"),
          SoundCategory.SFX,
          commandBuffer);
      brushComponent.setTasks(new ArrayList<>());
      damageBlockEvent.setDamage(0);
      return;
    } else if (blockType == BlockType.getAssetMap().getAsset("Rock_Crystal_Green_Block")) {

      brushComponent.togglePathType();
      player.sendMessage(
          Message.raw(String.format("Toggling PathType to: %s", brushComponent.getPathType()))
              .color(Color.GREEN));
      SoundUtil.playSoundEvent2d(
          SoundEvent.getAssetMap().getIndex("SFX_Clay_Pot_Small_Hit"),
          SoundCategory.SFX,
          commandBuffer);
      damageBlockEvent.setDamage(0);
      return;
    }

    List<Task> tasks = brushComponent.getTasks();
    if (tasks == null || tasks.isEmpty()) {
      brushComponent.addTask(targetBlockLocOnTopOfBlock, Action.TAKE);

      String message =
          String.format(
              "Set First Task Location: (%.0f, %.0f, %.0f) -> Action.TAKE",
              targetBlockLocOnTopOfBlock.x,
              targetBlockLocOnTopOfBlock.y,
              targetBlockLocOnTopOfBlock.z);
      LOGGER.atInfo().log(message);
      player.sendMessage(Message.raw(message).color(Color.GREEN));

    } else {
      brushComponent.addTask(targetBlockLocOnTopOfBlock, Action.DEPOSIT);
      // Add first point so it'll return to first point before continuing to second or third positions
      brushComponent.addTask(tasks.getFirst().getLocation(), Action.TAKE);

      String message =
          String.format(
              "Set Task Location: (%.0f, %.0f, %.0f) -> Action.DEPOSIT",
              targetBlockLocOnTopOfBlock.x,
              targetBlockLocOnTopOfBlock.y,
              targetBlockLocOnTopOfBlock.z);
      LOGGER.atInfo().log(message);
      player.sendMessage(Message.raw(message).color(Color.GREEN));
    }

    damageBlockEvent.setDamage(0);

    ParticleUtil.spawnParticleEffect("Block_Hit_Dirt", targetBlockLocOnTopOfBlock, store);

    SoundUtil.playSoundEvent2d(
        SoundEvent.getAssetMap().getIndex("SFX_Drop_Items_Clay"), SoundCategory.SFX, commandBuffer);
  }

  @Override
  public Query<EntityStore> getQuery() {
    return PlayerRef.getComponentType();
  }

  /**
   * Checks if the specified player is holding the wand item.
   *
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
