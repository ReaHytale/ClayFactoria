package com.clayfactoria.ui;

import com.clayfactoria.codecs.Automaton;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrushLegend extends CustomUIHud {

  private final String taskName;
  private final @Nullable Automaton selectedAutomaton;

  public BrushLegend(@NotNull PlayerRef playerRef, String taskName,
      @Nullable Automaton selectedAutomaton) {
    super(playerRef);
    this.taskName = taskName;
    this.selectedAutomaton = selectedAutomaton;
  }

  @Override
  protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
    uiCommandBuilder.append("Hud/ToolsLegends/BrushLegend.ui");

    if (selectedAutomaton == null) {
      uiCommandBuilder.set("#LeftClick.Visible", false);
      uiCommandBuilder.set("#RightClick.Visible", false);
      uiCommandBuilder.set("#Description.Visible", false);
      return;
    }

    uiCommandBuilder.set("#Description.Text", "server.items.Egg_Spawner_Clay_Kweebec.description");
    uiCommandBuilder.set("#SelectToBegin.Text", "You have selected a " + selectedAutomaton.name);
    uiCommandBuilder.set("#LeftClickLabel.Text",
        "Add a '" + this.taskName + "' task at this position");
  }
}
