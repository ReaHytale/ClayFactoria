package com.clayfactoria.events;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.ui.RadialMenu;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class OpenWandMenu extends SimpleInstantInteraction {

  public static final BuilderCodec<OpenWandMenu> CODEC =
      BuilderCodec.builder(OpenWandMenu.class, OpenWandMenu::new, SimpleInstantInteraction.CODEC)
          .documentation("Opens the Wand radial menu")
          .build();

  @Override
  protected void firstRun(
      @NotNull InteractionType interactionType,
      @NotNull InteractionContext interactionContext,
      @NotNull CooldownHandler cooldownHandler) {
    Ref<EntityStore> ref = interactionContext.getEntity();
    Store<EntityStore> store = ref.getStore();
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    BrushComponent brushComponent = store.getComponent(ref, BrushComponent.getComponentType());
    Player player = store.getComponent(ref, Player.getComponentType());

    assert playerRef != null;
    assert player != null;
    assert brushComponent != null;

    World world = player.getWorld();
    assert world != null;

    if (brushComponent.getEntityId() == null
        || world.getEntityRef(brushComponent.getEntityId()) == null) {
      player.sendMessage(
          Message.raw("You must first select the automaton you want to command!").color(Color.RED));
      return;
    }

    CompletableFuture.runAsync(
        () -> {
          CustomUIPage page = player.getPageManager().getCustomPage();
          if (page == null) {
            page =
                new RadialMenu(
                    playerRef,
                    brushComponent,
                    List.of(
                        Pair.of("ImageAssets/Take.png", Action.TAKE),
                        Pair.of("ImageAssets/Deposit.png", Action.DEPOSIT),
                        Pair.of("ImageAssets/Work.png", Action.WORK),
                        Pair.of("ImageAssets/Position.png", Action.POSITION)));
            player.getPageManager().openCustomPage(ref, store, page);
          }
        });
  }
}
