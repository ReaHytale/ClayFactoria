package com.clayfactoria.codecs;

import com.hypixel.hytale.server.npc.role.Role;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public enum Automaton {
  CLAY_TRORK(
      "Clay Trork",
      "Trork_Clay",
      "Clay Trorks can be programmed to carry items from place to place.",
      List.of(Task.TAKE, Task.DEPOSIT, Task.POSITION)),
  CLAY_KWEEBEC(
      "Clay Kweebec",
      "Kweebec_Clay",
      "Clay Kweebecs can be programmed to work at various stations to perform tasks like enabling furnaces.",
      List.of(Task.WORK, Task.POSITION)),
  CLAY_FERAN(
      "Clay Feran",
      "Feran_Clay",
      "Clay Ferans can be programmed to harvest crops in an area.",
      List.of(Task.TAKE, Task.DEPOSIT, Task.POSITION)
  );

  public final String name;
  public final String roleName;
  public final String description;
  public final List<Task> tasks;

  Automaton(String name, String roleName, String description, List<Task> tasks) {
    this.name = name;
    this.roleName = roleName;
    this.description = description;
    this.tasks = tasks;
  }

  @Nullable
  public static Automaton getFromRole(@Nullable Role role) {
    if (role == null) {
      return null;
    }
    String roleName = role.getRoleName();
    for (Automaton automaton : Automaton.values()) {
      if (Objects.equals(automaton.roleName, roleName)) {
        return automaton;
      }
    }
    return null;
  }
}
