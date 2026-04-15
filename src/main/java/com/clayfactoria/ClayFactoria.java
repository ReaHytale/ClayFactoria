package com.clayfactoria;

import com.clayfactoria.actions.builders.*;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.JobBoxComponent.JobBoxesComponent;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.interactions.ConsumeItemInteraction;
import com.clayfactoria.interactions.OpenWandMenu;
import com.clayfactoria.interactions.UseBrushOfLife;
import com.clayfactoria.sensors.builders.BuilderSensorCanDoTask;
import com.clayfactoria.sensors.builders.BuilderSensorHasAnyJobs;
import com.clayfactoria.sensors.builders.BuilderSensorLeashTarget;
import com.clayfactoria.systems.BrushLegendSystem;
import com.clayfactoria.systems.TaskBoxSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;

public class ClayFactoria extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, BrushComponent> brushComponentType;
    public static ComponentType<EntityStore, JobComponent> ownerComponentType;
    public static ComponentType<EntityStore, JobBoxesComponent> debugBoxesComponentType;

    public ClayFactoria(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Registering OpenWandMenu interaction");

        Interaction.CODEC.register("OpenWandMenu", OpenWandMenu.class, OpenWandMenu.CODEC);
        Interaction.CODEC.register("ConsumeItem", ConsumeItemInteraction.class, ConsumeItemInteraction.CODEC);
        Interaction.CODEC.register("UseBrushOfLife", UseBrushOfLife.class, UseBrushOfLife.CODEC);

        brushComponentType = this.getEntityStoreRegistry().registerComponent(
            BrushComponent.class,
            BrushComponent::new
        );
        ownerComponentType = this.getEntityStoreRegistry().registerComponent(
            JobComponent.class,
            "ClayFactoriaJobComponent",
            JobComponent.CODEC
        );
        debugBoxesComponentType = this.getEntityStoreRegistry().registerComponent(
            JobBoxesComponent.class,
            JobBoxesComponent::new
        );

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

        NPCPlugin.get().registerCoreComponentType("LeashTarget", BuilderSensorLeashTarget::new);
        NPCPlugin.get().registerCoreComponentType("CanDoTask", BuilderSensorCanDoTask::new);
        NPCPlugin.get().registerCoreComponentType("HasAnyJobs", BuilderSensorHasAnyJobs::new);

        NPCPlugin.get().registerCoreComponentType("PutItemInHand", BuilderActionPutItemInHand::new);
        NPCPlugin.get().registerCoreComponentType("DoTask", BuilderActionDoTask::new);
        NPCPlugin.get().registerCoreComponentType("DropInventory", BuilderActionDropInventory::new);
        NPCPlugin.get().registerCoreComponentType("ClearJobs", BuilderActionClearJobs::new);
        NPCPlugin.get().registerCoreComponentType("SetPath", BuilderActionSetPath::new);
        NPCPlugin.get().registerCoreComponentType("SelectHeldItem", BuilderActionSelectHeldItem::new);
        NPCPlugin.get().registerCoreComponentType("StartProgramming", BuilderActionStartProgramming::new);
    }

    @Override
    protected void start() {
        ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
        if (npcComponentType == null) {
            LOGGER.atSevere().log("Failed to Register Target Block Event System. NPC Entity ComponentType was null");
            return;
        }
        this.getEntityStoreRegistry().registerSystem(new TaskBoxSystem());
        this.getEntityStoreRegistry().registerSystem(new BrushLegendSystem());
    }

    private void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Player player = event.getPlayer();

        World world = player.getWorld();
        if (world == null) {
            LOGGER.atSevere().log("onPlayerReady Failed: world was null");
            return;
        }

        Ref<EntityStore> playerEntityRef = player.getReference();
        if (playerEntityRef == null) {
            LOGGER.atSevere().log("onPlayerReady Failed: playerEntityRef was null");
            return;
        }

        world.execute(
            () -> {
                Store<EntityStore> worldStore = world.getEntityStore().getStore();
                worldStore.ensureAndGetComponent(playerEntityRef, brushComponentType);
                LOGGER.atInfo().log("Successfully ensured Brush Component on Player");
            });

        world.execute(
            () -> {
                Store<EntityStore> worldStore = world.getEntityStore().getStore();
                worldStore.ensureAndGetComponent(playerEntityRef, debugBoxesComponentType);
                LOGGER.atInfo().log("Successfully ensured Particle Component on Player");
            });
    }
}
