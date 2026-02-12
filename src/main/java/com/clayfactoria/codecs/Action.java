package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.function.Supplier;

public enum Action implements Supplier<String> {
  TAKE("Take from container nearby"),
  DEPOSIT("Deposit to container or station nearby");

  public static final Codec<Action> CODEC = new EnumCodec<>(Action.class);

  private final String description;
  Action(String description) {
    this.description = description;
  }
  public String get() {
    return this.description;
  }
}
