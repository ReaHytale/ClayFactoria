package com.clayfactoria.events;

import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.ui.RadialMenu;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class OpenWandMenu extends SimpleInstantInteraction {
  public static final BuilderCodec<OpenWandMenu> CODEC = BuilderCodec.builder(
      OpenWandMenu.class, OpenWandMenu::new, SimpleInstantInteraction.CODEC
  )
      .documentation("Opens the Wand radial menu")
      .build();

  @Override
  protected void firstRun(@NotNull InteractionType interactionType, @NotNull InteractionContext interactionContext,
                          @NotNull CooldownHandler cooldownHandler) {
    Ref<EntityStore> ref = interactionContext.getEntity();
    Store<EntityStore> store = ref.getStore();
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    BrushComponent brushComponent = store.getComponent(ref, BrushComponent.getComponentType());
    Player player = store.getComponent(ref, Player.getComponentType());

    assert playerRef != null;
    assert player != null;
    assert brushComponent != null;

    CompletableFuture.runAsync(() -> {
      CustomUIPage page = player.getPageManager().getCustomPage();
      if (page == null) {
        page = new RadialMenu(playerRef, brushComponent);
        player.getPageManager().openCustomPage(ref, store, page);
      }
    });
  }
}
