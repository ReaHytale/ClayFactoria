package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3f;
import java.util.function.Supplier;

/**
 * Types of task that can be completed by automata
 */
public enum Action implements Supplier<String> {
  DEPOSIT(
      "Deposit",
      "Deposit held item in an adjacent container",
      new Vector3f(0.59F, 0.29F, 0.89F) // Purple
  ),
  TAKE(
      "Take",
      "Take an item from an adjacent container",
      new Vector3f(0.92F, 0.27F, 0.84F) // Pink
  ),
  POSITION(
      "Position",
      "Do nothing (After moving to a position)",
      new Vector3f(0.93F, 0.22F, 0.35F)), // Red
  WORK(
      "Work",
      "Work at an adjacent workstation",
      new Vector3f(0.33F, 0.45F, 0.9F) // Blue
  );

  public static final Codec<Action> CODEC = new EnumCodec<>(Action.class);
  public final String name;
  public final String description;
  public final Vector3f color;

  Action(String name, String description, Vector3f color) {
    this.name = name;
    this.description = description;
    this.color = color;
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
