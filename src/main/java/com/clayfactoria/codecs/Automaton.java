package com.clayfactoria.codecs;

import com.hypixel.hytale.server.npc.role.Role;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public enum Automaton {
  CLAY_TRORK("Clay Trork", "Trork_Clay", ""),
  CLAY_KWEEBEC("Clay Kweebec", "Kweebec_Clay", "");

  public String name;
  public String role_name;
  public String description;

  Automaton(String name, String role_name, String description) {
    this.name = name;
    this.role_name = role_name;
    this.description = description;
  }

  @Nullable
  public static Automaton getFromRole(@Nullable Role role) {
    if (role == null) {
      return null;
    }
    String roleName = role.getRoleName();
    for (Automaton automaton : Automaton.values()) {
      if (Objects.equals(automaton.role_name, roleName)) {
        return automaton;
      }
    }
    return null;
  }
}
