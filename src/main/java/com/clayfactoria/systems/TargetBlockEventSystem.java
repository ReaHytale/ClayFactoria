package com.clayfactoria.systems;

import com.clayfactoria.components.BrushComponent;
import com.hypixel.hytale.builtin.path.path.TransientPathDefinition;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

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

        UUID targetEntityId = brushComponent.getTargetEntityId();
        if (targetEntityId == null) {
            player.sendMessage(Message.raw("Target Entity Id was null").color(Color.RED));
            LOGGER.atWarning().log("Target Block Event System: Brush Component -> Target Entity Id was null");
            return;
        }

        String blockId = damageBlockEvent.getBlockType().getId();
        Vector3i targetBlockLoc = damageBlockEvent.getTargetBlock();
        brushComponent.setTargetBlockId(blockId);

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
//
//        Store<EntityStore> entityStore = entityRef.getStore();
//        NPCEntity npcEntity = entityStore.getComponent(entityRef, this.npcEntityComponentType);
//        if (npcEntity == null) {
//            return;
//        }
//
//        Role role = npcEntity.getRole();
//        if (role == null){
//            LOGGER.atSevere().log("Target Block Event System: Target NPC Entity -> Role was null");
//            return;
//        }
//        role.setMarkedTarget("LockedTarget", entityRef);
//        npcEntity.getPathManager().setTransientPath();

//        TransformComponent entityTransformComp = store.getComponent(entityRef, TransformComponent.getComponentType());
//        if (entityTransformComp == null){
//            LOGGER.atSevere().log("Target Block Event System: entityTransformComp was null");
//            return;
//        }
//
//        HeadRotation headRotationComponent = store.getComponent(entityRef, HeadRotation.getComponentType());
//        if (headRotationComponent == null){
//            LOGGER.atSevere().log("Target Block Event System: headRotationComponent was null");
//            return;
//        }
//
//        IPath<SimplePathWaypoint> path = npcEntity.getPathManager().get

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

        player.sendMessage(Message.raw(String.format("Set Target Block: (%d, %d, %d)", targetBlockLoc.x, targetBlockLoc.y, targetBlockLoc.z)).color(Color.GREEN));
        damageBlockEvent.setDamage(0);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
