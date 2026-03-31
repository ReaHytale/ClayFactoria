package com.clayfactoria.utils;

import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

public enum ContainerSlot {
  Input,
  Fuel,
  Output;

  public ItemContainer getItemContainer(ProcessingBenchBlock processingBenchBlock) {
    return switch (this) {
      case Fuel -> processingBenchBlock.getFuelContainer();
      case Input -> processingBenchBlock.getInputContainer();
      case Output -> processingBenchBlock.getOutputContainer();
      default -> null;
    };
  }
}
