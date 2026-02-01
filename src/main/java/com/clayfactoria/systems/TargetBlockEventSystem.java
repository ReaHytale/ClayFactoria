package com.clayfactoria.systems;

import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.models.WorldWaypoint;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.awt.*;

public class TargetBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final ComponentType<EntityStore, BrushComponent> brushComponentType = BrushComponent.getComponentType();
    @Nonnull
    private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;

    public TargetBlockEventSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType) {
        super(DamageBlockEvent.class);
        this.npcEntityComponentType = npcEntityComponentType;
    }

    @Override
    public void handle(int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store,
                       @NonNull CommandBuffer<EntityStore> commandBuffer, @NonNull DamageBlockEvent damageBlockEvent) {

        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);

        Player player = store.getComponent(entityStoreRef, Player.getComponentType());
        if (player == null) return;

        Ref<EntityStore> playerRef = player.getReference();
        if (playerRef == null) {
            LOGGER.atSevere().log("Target Block Event System: playerRef was null");
            return;
        }

        BrushComponent brushComponent = store.getComponent(playerRef, this.brushComponentType);
        if (brushComponent == null) {
            LOGGER.atSevere().log("Target Block Event System: Brush Component on the player was null");
            return;
        }

        damageBlockEvent.setDamage(0);

//        UUID targetEntityId = brushComponent.getTargetEntityId();
//        if (targetEntityId == null) {
//            player.sendMessage(Message.raw("Target Entity Id was null").color(Color.RED));
//            LOGGER.atWarning().log("Target Block Event System: Brush Component -> Target Entity Id was null");
//            return;
//        }

//        World world = player.getWorld();
//        if (world == null) {
//            LOGGER.atSevere().log("Target Block Event System: world was null");
//            return;
//        }
//
//        Ref<EntityStore> entityRef = world.getEntityRef(targetEntityId);
//        if (entityRef == null) {
//            LOGGER.atSevere().log("Target Block Event System: entityRef was null");
//            return;
//        }

        // TODO: May need to use entityRef instead of entityStoreRef
        HeadRotation headRotationComponent = store.getComponent(entityStoreRef, HeadRotation.getComponentType());
        if (headRotationComponent == null){
            LOGGER.atSevere().log("Target Block Event System: headRotationComponent was null");
            return;
        }

        Vector3i targetBlockLoc = damageBlockEvent.getTargetBlock();
        Vector3f headRotation = headRotationComponent.getRotation();

        WorldWaypoint worldWaypoint = new WorldWaypoint(targetBlockLoc.x, targetBlockLoc.y, targetBlockLoc.z, headRotation.x, headRotation.y, headRotation.z);

        if (brushComponent.getPathStart() == null) {
            brushComponent.setPathStart(worldWaypoint);
        } else if (brushComponent.getPathEnd() == null) {
            brushComponent.setPathEnd(worldWaypoint);
        } else {
            brushComponent.setPathStart(null);
            brushComponent.setPathEnd(null);
            player.sendMessage(Message.raw("Resetting path...").color(Color.YELLOW));

            ParticleUtil.spawnParticleEffect(
                    "Block_Break_Dust",
                    new Vector3d(targetBlockLoc.x + 0.5, targetBlockLoc.y + 1, targetBlockLoc.z + 0.5),
                    store
            );

            SoundUtil.playSoundEvent2d(
                    SoundEvent.getAssetMap().getIndex("SFX_Drag_Items_Clay"),
                    SoundCategory.SFX,
                    commandBuffer
            );
            return;
        }

        ParticleUtil.spawnParticleEffect(
                "Block_Hit_Dirt",
                new Vector3d(targetBlockLoc.x + 0.5, targetBlockLoc.y + 1, targetBlockLoc.z + 0.5),
                store
        );

        SoundUtil.playSoundEvent2d(
                SoundEvent.getAssetMap().getIndex("SFX_Drop_Items_Clay"),
                SoundCategory.SFX,
                commandBuffer
        );

        String message = String.format("Set Path Block: (%d, %d, %d)", targetBlockLoc.x, targetBlockLoc.y, targetBlockLoc.z);
        LOGGER.atInfo().log(message);
        player.sendMessage(Message.raw(message).color(Color.GREEN));
    }

    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
