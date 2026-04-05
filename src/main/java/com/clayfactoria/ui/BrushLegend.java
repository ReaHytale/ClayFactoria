package com.clayfactoria.ui;

import com.clayfactoria.codecs.Automaton;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrushLegend extends CustomUIHud {

  private final BrushComponent brushComponent;
  private final @Nullable Automaton selectedAutomaton;

  public BrushLegend(@NotNull PlayerRef playerRef, BrushComponent brushComponent,
      @Nullable Automaton selectedAutomaton) {
    super(playerRef);
    this.brushComponent = brushComponent;
    this.selectedAutomaton = selectedAutomaton;
  }

  @Override
  protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
    uiCommandBuilder.append("Hud/ToolsLegends/BrushLegend.ui");
    uiCommandBuilder.append("Hud/ToolsLegends/CustomToolsLegendsCommon.ui");

    if (selectedAutomaton == null) {
      uiCommandBuilder.set("#LeftClick.Visible", false);
      uiCommandBuilder.set("#RightClick.Visible", false);
      uiCommandBuilder.set("#Description.Visible", false);
      uiCommandBuilder.set("#SelectedIcon.Background", "Common/UnknownItemIcon.png");
      return;
    }

    uiCommandBuilder.set("#Description.Text", selectedAutomaton.description);
    uiCommandBuilder.set("#SelectedIcon.Background",
        "Hud/ToolsLegends/" + selectedAutomaton.roleName + ".png");
    uiCommandBuilder.set("#SelectToBegin.Text",
        "You have selected a " + selectedAutomaton.name + "!");
    uiCommandBuilder.set("#LeftClickLabel.Text",
        "Add a '" + this.brushComponent.getAction().name + "' task here");
    uiCommandBuilder.appendInline("#Page1",
        "Group {Anchor: (Height: 2, Vertical: 10); Background: #ffffff(0.15);}");
    buildTaskList(uiCommandBuilder);
  }

  private void buildTaskList(@NotNull UICommandBuilder uiCommandBuilder) {
    List<Task> tasks = brushComponent.getTasks();
    if (tasks.isEmpty()) {
      uiCommandBuilder.appendInline("#Page1", "Group {"
          + "          FlexWeight: 1;"
          + "          Anchor: (Top: 0);"
          + "          LayoutMode: Center;"
          + "          Label {"
          + "            Text: \"No tasks set yet\";"
          + "            Style: ("
          + "              FontSize: 14,"
          + "              TextColor: #96a9be,"
          + "              Wrap: true"
          + "            );"
          + "          }"
          + "        }");
      return;
    }
    for (Task task : tasks.subList(0, Math.min(8, tasks.size()))) {
      String label = "Group {LayoutMode: CenterMiddle; Anchor: (Vertical: 4);"
          + "Group {Padding: (Horizontal: 6, Vertical: 8); Anchor: (MinWidth: 24, Right: 10); Background: (TexturePath: \"Common/InputBinding.png\", Border: 6); LayoutMode: CenterMiddle;"
          + "Group {Anchor: (Width: 24, Height: 24); "
          + "Background: \"ImageAssets/" + task.getAction().name + ".png\";}}"
          + "Group {FlexWeight: 1; Anchor: (Top: 0); LayoutMode: Center;"
          + "Label {Text: \"'" + task.getAction().name + "' Task\"; "
          + "Style: (FontSize: 14, TextColor: #96a9be, Wrap: true);}}}";
      uiCommandBuilder.appendInline("#Page1", label);
    }
    if (tasks.size() > 8) {
      uiCommandBuilder.appendInline("#Page1", "Group {"
          + "          FlexWeight: 1;"
          + "          Anchor: (Top: 0);"
          + "          LayoutMode: Center;"
          + "          Label {"
          + "            Text: \"...\";"
          + "            Style: ("
          + "              FontSize: 14,"
          + "              TextColor: #96a9be,"
          + "              Wrap: true"
          + "            );"
          + "          }"
          + "        }");
    }
  }
}
