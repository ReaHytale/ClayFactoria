package com.clayfactoria.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ConsumeItemInteraction extends SimpleInstantInteraction {

  public static final BuilderCodec<ConsumeItemInteraction> CODEC = BuilderCodec.builder(
          ConsumeItemInteraction.class, ConsumeItemInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Consumes the item being held.")
      .build();

  @Nonnull
  @Override
  public WaitForDataFrom getWaitForDataFrom() {
    return WaitForDataFrom.Server;
  }

  @Override
  protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler) {
    CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

    assert commandBuffer != null;

    Ref<EntityStore> ref = context.getEntity();
    byte activeSlot = context.getHeldItemSlot();
    ItemStack itemInHand = context.getHeldItem();
    if (itemInHand != null) {
      Item item = itemInHand.getItem();
      context.setHeldItem(new ItemStack(item.getId(), itemInHand.getQuantity() - 1));
    }
  }

  @Nonnull
  @Override
  public String toString() {
    return "ConsumeItemInteraction{} " + super.toString();
  }
}
