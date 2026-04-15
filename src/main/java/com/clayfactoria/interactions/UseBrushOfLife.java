package com.clayfactoria.interactions;

import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.JobBoxComponent;
import com.clayfactoria.utils.BlockUtils;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemSoundEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Objects;

public class UseBrushOfLife extends SimpleInstantInteraction {
    public static final BuilderCodec<UseBrushOfLife> CODEC = BuilderCodec.builder(
            UseBrushOfLife.class,
            UseBrushOfLife::new,
            SimpleInstantInteraction.CODEC
        )
        .documentation("Performs a relevant action for the Brush of Life using the targeted block.")
        .build();

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Item ITEM_FOR_SOUND = Objects.requireNonNull(
        Item.getAssetMap().getAsset("Ingredient_Life_Essence")
    );

    @Override
    protected void firstRun(
        @NonNull InteractionType interactionType,
        @NonNull InteractionContext interactionContext,
        @NonNull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = ref.getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        assert player != null;
        World world = player.getWorld();
        assert world != null;

        // Add the task to the task list, with the action being set to the one currently selected by the player.
        BrushComponent brushComponent = Objects.requireNonNull(
            store.getComponent(ref, BrushComponent.getComponentType())
        );

        // If no selected entity, let the user know...
        if (brushComponent.getEntityId() == null
            || world.getEntityRef(brushComponent.getEntityId()) == null) {
            player.sendMessage(Message
                .raw("You must first select the automaton you want to command!")
                .color(Color.RED)
            );
            return;
        }

        BlockPosition targetBlockPos = interactionContext.getTargetBlock();
        if (targetBlockPos == null) return;
        Vector3i targetBlockLoc = new Vector3i(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);

        try {
            if (brushComponent.getTask().usesBounds) {
                JobBoxComponent.JobBoxesComponent jobBoxesComponent = store.getComponent(
                    ref, JobBoxComponent.JobBoxesComponent.getComponentType()
                );
                assert jobBoxesComponent != null;

                if (brushComponent.getBoxPoint1() != null) {
                    Box box = BlockUtils.makeSurroundingBox(brushComponent.getBoxPoint1(), targetBlockLoc);
                    brushComponent.addTask(box, player.getWorld(), store, ref);
                } else {
                    brushComponent.setBoxPoint1(targetBlockLoc, ref);
                    jobBoxesComponent.boxes.add(
                        new JobBoxComponent(
                            brushComponent.getTask().color, BlockUtils.getBlockBox(targetBlockLoc, world)));
                }
            } else brushComponent.addTask(targetBlockLoc, player.getWorld(), store, ref);
        } catch (IllegalStateException e) {
            player.sendMessage(Message.raw("Cannot place the target location here!").color(Color.RED));
            LOGGER.atInfo().log("Error when adding a task: " + e.getMessage());
        }

        SoundUtil.playItemSoundEvent(ref, store, ITEM_FOR_SOUND, ItemSoundEvent.Drop);
    }
}
