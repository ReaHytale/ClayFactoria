package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.function.Supplier;

/** Types of task that can be completed by automata */
public enum Action implements Supplier<String> {
  DEPOSIT(
      "Deposit",
      "Deposit held item in an adjacent container",
      new String[] {"container", "processingBench"}),
  TAKE(
      "Take",
      "Take an item from an adjacent container",
      new String[] {"container", "processingBench"}),
  POSITION("Position", "Do nothing (After moving to a position)", new String[] {}),
  WORK("Work", "Work at an adjacent workstation", new String[] {"processingBench"});

  public static final Codec<Action> CODEC = new EnumCodec<>(Action.class);
  public final String name;
  public final String description;
  public final String[] blockStates;

  Action(String name, String description, String[] blockStates) {
    this.name = name;
    this.description = description;
    this.blockStates = blockStates;
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
