package com.clayfactoria.ui;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.ui.RadialMenu.RadialMenuEventData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Interactive command-selection page for command items. Presents a radial-style set of clickable
 * command buttons and returns the selected command id.
 *
 * @author Alechilles
 * @author Lordimass
 */
public final class RadialMenu
    extends InteractiveCustomUIPage<RadialMenuEventData> {

  public static final String UI_PATH = "RadialMenu.ui";
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final String EVENT_COMMAND_ID = "CommandId";
  private static final String RESET_COMMAND_ID = "Reset";
  private static final int MAX_COMMAND_BUTTONS = 4;
  private static final long LINKED_PANEL_REFRESH_INTERVAL_MS = 1000L;

  private final Action[] options;
  private final BrushComponent brushComponent;
  private volatile boolean refreshLoopStarted;
  private volatile boolean dismissed;

  public RadialMenu(@Nonnull PlayerRef playerRef, @Nonnull BrushComponent brushComponent) {
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        RadialMenuEventData.CODEC);
    this.options = new Action[]{Action.TAKE, Action.DEPOSIT, Action.WORK, Action.POSITION};
    this.refreshLoopStarted = false;
    this.dismissed = false;
    this.brushComponent = brushComponent;
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append(UI_PATH);
    commandBuilder.set("#CommandMenuWheel.Visible", true);
    commandBuilder.set("#CommandMenuCurrent.Text", "Current: " + brushComponent.getAction());

    buildCommandButtons(commandBuilder, eventBuilder);
    buildResetButton(commandBuilder, eventBuilder);
    startRefreshLoop();
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull RadialMenuEventData data) {
    if (data.task != null) {
      LOGGER.atInfo().log("Set Brush command to: " + data.task);
      brushComponent.setAction(data.task);
    } else if (data.reset != null) {
      LOGGER.atInfo().log("Resetting tasks");
      brushComponent.resetTasks(store, ref);
    }
    close();
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    dismissed = true;
  }

  private void buildCommandButtons(
      @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
    for (int i = 0; i < MAX_COMMAND_BUTTONS; i++) {
      String selector = "#CommandButton" + i;
      if (i >= options.length) {
        commandBuilder.set(selector + ".Visible", false);
        continue;
      }
      Action option = options[i];
      commandBuilder.set(selector + ".Visible", true);
      commandBuilder.set(selector + ".Text", "");
      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          selector,
          EventData.of(EVENT_COMMAND_ID, String.valueOf(option)),
          false);
    }
  }

  private void buildResetButton(@Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder) {
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#ResetButton",
        EventData.of(RESET_COMMAND_ID, ""),
        false
    );
  }

  private void startRefreshLoop() {
    if (refreshLoopStarted) {
      return;
    }
    refreshLoopStarted = true;
    scheduleRefreshTick();
  }

  private void scheduleRefreshTick() {
    CompletableFuture.runAsync(
        this::dispatchRefreshTick,
        CompletableFuture.delayedExecutor(LINKED_PANEL_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS));
  }

  private void dispatchRefreshTick() {
    if (dismissed) {
      return;
    }
    Ref<EntityStore> ref = playerRef.getReference();
    if (ref == null || !ref.isValid()) {
      return;
    }
    Store<EntityStore> store = ref.getStore();
    World world = store.getExternalData().getWorld();
    world.execute(this::runRefreshTickOnWorldThread);
  }

  private void runRefreshTickOnWorldThread() {
    if (!dismissed) {
      scheduleRefreshTick();
    }
  }

  /**
   * Event payload emitted by command-button clicks in the command selection page.
   */
  public static final class RadialMenuEventData {

    public static final BuilderCodec<RadialMenuEventData> CODEC =
        BuilderCodec.builder(RadialMenuEventData.class, RadialMenuEventData::new)
            .append(
                new KeyedCodec<>(EVENT_COMMAND_ID, Action.CODEC),
                (event, value) -> event.task = value,
                event -> event.task)
            .add()
            .append(new KeyedCodec<>(RESET_COMMAND_ID, Codec.STRING),
                (event, value) -> event.reset = value,
                event -> event.reset)
            .add()
            .build();

    private Action task;
    // Using a String here for ease, but it just means that if it's not null, we should reset, regardless of the value
    private String reset;
  }

  public static class ResetEventData {

    public static final BuilderCodec<ResetEventData> CODEC =
        BuilderCodec.builder(ResetEventData.class, ResetEventData::new).build();
  }
}
