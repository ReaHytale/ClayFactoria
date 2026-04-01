package com.clayfactoria;

import com.clayfactoria.actions.automataactions.builders.BuilderActionDeposit;
import com.clayfactoria.actions.automataactions.builders.BuilderActionPosition;
import com.clayfactoria.actions.automataactions.builders.BuilderActionTake;
import com.clayfactoria.actions.automataactions.builders.BuilderActionWork;
import com.clayfactoria.actions.builders.BuilderActionDropInventory;
import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.actions.builders.BuilderActionPutItemInHand;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.TaskComponent;
import com.clayfactoria.events.OpenWandMenu;
import com.clayfactoria.sensors.builders.BuilderSensorCanDoAction;
import com.clayfactoria.sensors.builders.BuilderSensorHasAnyTasks;
import com.clayfactoria.sensors.builders.BuilderSensorLeashTarget;
import com.clayfactoria.systems.TargetBlockEventSystem;
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
  public static ComponentType<EntityStore, TaskComponent> ownerComponentType;

  public ClayFactoria(JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log(
        "Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Registering OpenWandMenu interaction");

    Interaction.CODEC.register("OpenWandMenu", OpenWandMenu.class, OpenWandMenu.CODEC);

    LOGGER.atInfo().log("Registering Brush Component");
    brushComponentType =
        this.getEntityStoreRegistry().registerComponent(BrushComponent.class, BrushComponent::new);

    LOGGER.atInfo().log("Registering Task Component");
    ownerComponentType =
        this.getEntityStoreRegistry().registerComponent(TaskComponent.class, "ClayFactoriaTaskComponent",
                TaskComponent.CODEC);

    this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

    NPCPlugin.get().registerCoreComponentType("SetPath", BuilderActionSetPath::new);
    NPCPlugin.get().registerCoreComponentType("LeashTarget", BuilderSensorLeashTarget::new);
    NPCPlugin.get().registerCoreComponentType("CanDoAction", BuilderSensorCanDoAction::new);
    NPCPlugin.get().registerCoreComponentType("HasAnyTasks", BuilderSensorHasAnyTasks::new);

    NPCPlugin.get().registerCoreComponentType("PutItemInHand", BuilderActionPutItemInHand::new);
    NPCPlugin.get().registerCoreComponentType("Take", BuilderActionTake::new);
    NPCPlugin.get().registerCoreComponentType("Deposit", BuilderActionDeposit::new);
    NPCPlugin.get().registerCoreComponentType("Position", BuilderActionPosition::new);
    NPCPlugin.get().registerCoreComponentType("Work", BuilderActionWork::new);
    NPCPlugin.get().registerCoreComponentType("DropInventory", BuilderActionDropInventory::new);
  }

  @Override
  protected void start() {
    ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
    if (npcComponentType == null) {
      LOGGER.atSevere().log(
          "Failed to Register Target Block Event System. NPC Entity ComponentType was null");
      return;
    }

    LOGGER.atInfo().log("Registering Target Block Event System");
    this.getEntityStoreRegistry().registerSystem(new TargetBlockEventSystem());
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
          worldStore.ensureAndGetComponent(playerEntityRef, BrushComponent.getComponentType());
          LOGGER.atInfo().log("Successfully ensured Brush Component on Player");
        });
  }
}
