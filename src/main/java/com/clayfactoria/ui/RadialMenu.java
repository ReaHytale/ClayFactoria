package com.clayfactoria.ui;

import com.clayfactoria.codecs.Action;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.ui.RadialMenu.RadialMenuEventData;
import com.clayfactoria.ui.RadialMenu.RadialMenuEventData.IsReset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Interactive command-selection page for command items. Presents a radial-style set of clickable
 * command buttons and returns the selected command id.
 *
 * @author Alechilles
 * @author Lordimass
 * @author bqkitcat
 */
public final class RadialMenu extends InteractiveCustomUIPage<RadialMenuEventData> {

  public static final String UI_PATH = "RadialMenu.ui";
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final String EVENT_COMMAND_ID = "CommandId";
  private static final String RESET_COMMAND_ID = "Reset";
  private static final int MAX_COMMAND_BUTTONS = 4;
  private static final long LINKED_PANEL_REFRESH_INTERVAL_MS = 1000L;

  private static final int[][][] IMAGE_SEGMENT_SIZES_WIDTH_HEIGHT = {
      {{504, 504}},
      {{252, 505}, {252, 505}},
      {{252, 377}, {437, 192}, {252, 377}},
      {{252, 252}, {252, 252}, {252, 252}, {252, 252}}
  };

  private static final int[][][] IMAGE_SEGMENT_ANCHORS_TOP_LEFT = {
      {{128, 208}},
      {{128, 460}, {128, 208}},
      {{128, 460}, {440, 241}, {128, 208}},
      {{128, 460}, {380, 460}, {380, 208}, {128, 208}}
  };

  private static final int[][][] ICON_ANCHORS_TOP_LEFT = {
      {{27, 215}},
      {{205, 140}, {205, 30}},
      {{120, 120}, {80, 179}, {120, 60}},
      {{80, 80}, {80, 80}, {80, 80}, {80, 80}}
  };

  private final BrushComponent brushComponent;
  private final List<Action> menuActions;
  private volatile boolean refreshLoopStarted;
  private volatile boolean dismissed;

  public RadialMenu(
      @Nonnull PlayerRef playerRef,
      @Nonnull BrushComponent brushComponent,
      @Nonnull List<Action> menuActions) {
    if (menuActions.isEmpty() || menuActions.size() > MAX_COMMAND_BUTTONS) {
      throw new IllegalArgumentException(
          "menuActions must supports at most "
              + MAX_COMMAND_BUTTONS
              + " actions only and "
              + "must not be empty!");
    }
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        RadialMenuEventData.CODEC);
    this.refreshLoopStarted = false;
    this.dismissed = false;
    this.brushComponent = brushComponent;
    this.menuActions = menuActions;
  }

  private static String getSliceButtonLabelStyle() {
    return "LabelStyle("
        + "FontSize: 14, "
        + "RenderBold: true, "
        + "RenderUppercase: true, "
        + "TextColor: #00000000, "
        + "HorizontalAlignment: Center, "
        + "VerticalAlignment: Center "
        + ")";
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
    } else if (data.reset == IsReset.Yes) {
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

    for (int i = 0; i < menuActions.size(); i++) {
      Action action = menuActions.get(i);
      String actionImageName = action.iconAssetPath;

      String commandButtonName = "#CommandButton" + i;
      String commandButtonIcon = "#CommandButton" + i + "Icon";
      String commandButtonGroup = commandButtonName + "Group";

      String groupContent =
          "Group "
              + commandButtonGroup
              + " { "
              + generateAnchor(i)
              + generateTextButton(i, commandButtonName)
              + generateImage(i, commandButtonIcon, actionImageName)
              + "}";

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          commandButtonName,
          EventData.of(EVENT_COMMAND_ID, action.toString()),
          false);

      commandBuilder.appendInline("#CommandMenuWheel", groupContent);
    }
  }

  private String generateAnchor(int i) {
    int[] anchors = IMAGE_SEGMENT_ANCHORS_TOP_LEFT[menuActions.size() - 1][i];
    int[] sizes = IMAGE_SEGMENT_SIZES_WIDTH_HEIGHT[menuActions.size() - 1][i];
    return "Anchor: (Top: "
        + anchors[0]
        + ", Left: "
        + anchors[1]
        + ", Width: "
        + sizes[0]
        + ", Height: "
        + sizes[1]
        + "); ";
  }

  private String generateTextButton(int i, String commandButtonName) {
    int[] sizes = IMAGE_SEGMENT_SIZES_WIDTH_HEIGHT[menuActions.size() - 1][i];
    return "TextButton "
        + commandButtonName
        + " { "
        + "Anchor: (Top: 0, Left: 0, Width: "
        + sizes[0]
        + ", Height: "
        + sizes[1]
        + "); "
        + "Text: \"\"; "
        + "Style: "
        + generateButtonStyle(i)
        + "; "
        + "TooltipText: \""
        + menuActions.get(i).name
        + "\"; "
        + "} ";
  }

  private String generateImage(int i, String commandButtonIcon, String actionImageName) {
    int[] iconAnchors = ICON_ANCHORS_TOP_LEFT[menuActions.size() - 1][i];
    return "Group "
        + commandButtonIcon
        + " { "
        + "Anchor: (Top: "
        + iconAnchors[0]
        + ", Left:"
        + iconAnchors[1]
        + " , Width: 80, Height: 80); "
        + "Background: (TexturePath: \""
        + actionImageName
        + "\"); "
        + "HitTestVisible: false; "
        + "} ";
  }

  private String generateButtonStyle(int i) {
    String buttonStyle =
        "TextButtonStyle("
            + "Default: (Background: (TexturePath: \"ImageAssets/{$n}Radial{$i}_Default.png\"), "
            + "LabelStyle: "
            + getSliceButtonLabelStyle()
            + "), "
            + "Hovered: (Background: (TexturePath: \"ImageAssets/{$n}Radial{$i}_Hover.png\"), "
            + "LabelStyle: "
            + getSliceButtonLabelStyle()
            + "), "
            + "Pressed: (Background: (TexturePath: \"ImageAssets/{$n}Radial{$i}_Click.png\"), "
            + "LabelStyle: "
            + getSliceButtonLabelStyle()
            + "), "
            + "Disabled: (Background: (TexturePath: \"ImageAssets/{$n}Radial{$i}_Default.png\", "
            + "Color: #ffffff(0.5)), "
            + "LabelStyle: "
            + getSliceButtonLabelStyle()
            + "), "
            //            + "Sounds: $C.@ButtonSounds"
            + ")";
    buttonStyle = buttonStyle.replace("{$n}", "" + menuActions.size());
    buttonStyle = buttonStyle.replace("{$i}", "" + i);
    return buttonStyle;
  }

  private void buildResetButton(
      @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#ResetButton",
        EventData.of(RESET_COMMAND_ID, IsReset.Yes.toString()),
        false);
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
            .append(
                new KeyedCodec<>(RESET_COMMAND_ID, IsReset.CODEC),
                (event, value) -> event.reset = value,
                event -> event.reset)
            .add()
            .build();

    private Action task;
    private IsReset reset;

    public enum IsReset {
      Yes,
      No;
      public static final Codec<IsReset> CODEC = new EnumCodec<>(IsReset.class);
    }
  }
}
