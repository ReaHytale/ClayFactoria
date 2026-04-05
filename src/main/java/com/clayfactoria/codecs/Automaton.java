package com.clayfactoria.codecs;

import com.hypixel.hytale.server.npc.role.Role;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public enum Automaton {
  CLAY_TRORK("Clay Trork", "Trork_Clay",
      "Clay Trorks can be programmed to carry items from place to place."
  ),
  CLAY_KWEEBEC("Clay Kweebec", "Kweebec_Clay",
      "Clay Kweebecs can be programmed to work at various stations to perform tasks like enabling furnaces.");

  public String name;
  public String roleName;
  public String description;

  Automaton(String name, String roleName, String description) {
    this.name = name;
    this.roleName = roleName;
    this.description = description;
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
