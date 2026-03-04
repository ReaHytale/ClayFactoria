package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.function.Supplier;

/**
 * Types of task that can be completed by automata
 */
public enum Action implements Supplier<String> {
  DEPOSIT("Deposit", "Deposit held item in an adjacent container"),
  TAKE("Take", "Take an item from an adjacent container"),
  POSITION("Position", "Do nothing (After moving to a position)"),
  WORK("Work", "Work at an adjacent workstation");

  public static final Codec<Action> CODEC = new EnumCodec<>(Action.class);
  private final String name;
  private final String description;

  Action(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
