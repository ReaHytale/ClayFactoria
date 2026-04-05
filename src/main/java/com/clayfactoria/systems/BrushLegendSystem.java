package com.clayfactoria.systems;

import com.clayfactoria.codecs.Automaton;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.ui.BrushLegend;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entityui.UIComponentList;
import com.hypixel.hytale.server.core.modules.entityui.UIComponentSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrushLegendSystem extends UIComponentSystems.Update {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();


  public BrushLegendSystem() {
    super(EntityTrackerSystems.Visible.getComponentType(), UIComponentList.getComponentType());
  }

  @Override
  public void tick(float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    Player player = archetypeChunk.getComponent(index, Player.getComponentType());
    if (player == null) {
      return;
    }
    InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index,
        InventoryComponent.Hotbar.getComponentType());
    Objects.requireNonNull(hotbarComponent);
    ItemStack itemStack = hotbarComponent.getActiveItem();

    if (itemStack != null && itemStack.getItemId().equals("Tool_Brush")) {
      update(index, archetypeChunk, store, commandBuffer);
    } else {
      player.getHudManager()
          .resetHud(player.getPlayerRef());
    }
  }

  @Override
  public @NotNull Query<EntityStore> getQuery() {
    return Query.any();
  }

  private void update(int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    BrushComponent brushComponent = archetypeChunk.getComponent(index,
        BrushComponent.getComponentType());
    if (brushComponent == null) {
      return;
    }
    Player player = archetypeChunk.getComponent(index, Player.getComponentType());
    Objects.requireNonNull(player);
    Automaton automaton = getAutomaton(commandBuffer, player, brushComponent);
    player.getHudManager()
        .setCustomHud(player.getPlayerRef(),
            new BrushLegend(player.getPlayerRef(), brushComponent, automaton));
  }

  private @Nullable Automaton getAutomaton(CommandBuffer<EntityStore> commandBuffer,
      Player player,
      BrushComponent brushComponent
  ) {
    UUID entityID = brushComponent.getEntityId();
    if (entityID == null) {
      return null;
    }
    Objects.requireNonNull(player.getWorld());
    Ref<EntityStore> npcRef = player.getWorld().getEntityRef(entityID);

    if (npcRef == null) {
      return null;
    }

    NPCEntity npcEntity = commandBuffer.getComponent(npcRef,
        Objects.requireNonNull(NPCEntity.getComponentType()));
    return Automaton.getFromRole(Objects.requireNonNull(npcEntity).getRole());
  }
}
