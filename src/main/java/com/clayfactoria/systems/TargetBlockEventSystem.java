package com.clayfactoria.systems;

import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.JobBoxComponent;
import com.clayfactoria.components.JobBoxComponent.TaskBoxesComponent;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public class TargetBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {

  private static final String WAND_ITEM_ID = "Tool_Brush";

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private final ComponentType<EntityStore, BrushComponent> brushComponentType =
      BrushComponent.getComponentType();

  public TargetBlockEventSystem() {
    super(DamageBlockEvent.class);
  }

  public static boolean wandIsNotEquipped(
      ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
    InventoryComponent.Hotbar hotbarComponent =
        componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
    Objects.requireNonNull(hotbarComponent);
    ItemStack itemStack = hotbarComponent.getActiveItem();
    return itemStack == null || !itemStack.getItemId().equals(WAND_ITEM_ID);
  }

  @Override
  public void handle(
      int index,
      @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
      @NonNull Store<EntityStore> store,
      @NonNull CommandBuffer<EntityStore> commandBuffer,
      @NonNull DamageBlockEvent damageBlockEvent) {

    Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);

    Player player =
        Objects.requireNonNull(store.getComponent(entityStoreRef, Player.getComponentType()));
    Ref<EntityStore> playerRef =
        Objects.requireNonNull(player.getReference(), "playerRef was null");

    // Check that the player has the wand equipped
    if (wandIsNotEquipped(store, playerRef)) {
      return;
    }

    // Add the task to the task list, with the action being set to the one currently selected by the
    // player.
    BrushComponent brushComponent =
        Objects.requireNonNull(store.getComponent(playerRef, this.brushComponentType));

    World world = player.getWorld();
    assert world != null;

    // If no selected entity, let the user know...
    if (brushComponent.getEntityId() == null
        || world.getEntityRef(brushComponent.getEntityId()) == null) {
      player.sendMessage(
          Message.raw("You must first select the automaton you want to command!").color(Color.RED));
      return;
    }

    Vector3i targetBlockLoc = damageBlockEvent.getTargetBlock();

    try {
      if (brushComponent.getTask().usesBounds) {

        TaskBoxesComponent taskBoxesComponent =
            store.getComponent(playerRef, TaskBoxesComponent.getComponentType());
        assert taskBoxesComponent != null;

        if (brushComponent.getBoxPoint1() != null) {
          Box box = BlockUtils.makeSurroundingBox(brushComponent.getBoxPoint1(), targetBlockLoc);
          brushComponent.addTask(box, player.getWorld(), store, playerRef);
        } else {
          brushComponent.setBoxPoint1(targetBlockLoc, playerRef);
          taskBoxesComponent.boxes.add(
              new JobBoxComponent(
                  brushComponent.getTask().color, BlockUtils.getBlockBox(targetBlockLoc, world)));
        }
      } else {
        brushComponent.addTask(targetBlockLoc, player.getWorld(), store, playerRef);
      }
    } catch (IllegalStateException e) {
      player.sendMessage(Message.raw("Cannot place the target location here!").color(Color.RED));
      LOGGER.atInfo().log("Error when adding a task: " + e.getMessage());
    }

    damageBlockEvent.setDamage(0);
    SoundUtil.playSoundEvent2d(
        SoundEvent.getAssetMap().getIndex("SFX_Drop_Items_Clay"), SoundCategory.SFX, commandBuffer);
  }

  @Override
  public Query<EntityStore> getQuery() {
    return PlayerRef.getComponentType();
  }
}
