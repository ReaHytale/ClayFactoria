package com.clayfactoria;

import com.clayfactoria.actions.builders.BuilderActionSetPath;
import com.clayfactoria.actions.builders.BuilderActionTakeFromNearbyStorage;
import com.clayfactoria.actions.builders.BuilderPutItemInHand;
import com.clayfactoria.components.BrushComponent;
import com.clayfactoria.components.HasTakenFromContainerComponent;
import com.clayfactoria.components.OwnerComponent;
import com.clayfactoria.sensors.builders.BuilderSensorBlockUnder;
import com.clayfactoria.sensors.builders.BuilderSensorHasTakenFromContainer;
import com.clayfactoria.sensors.builders.BuilderSensorPathComplete;
import com.clayfactoria.sensors.builders.BuilderSensorPathType;
import com.clayfactoria.systems.TargetBlockEventSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
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
  public static ComponentType<EntityStore, OwnerComponent> ownerComponentType;
  public static ComponentType<EntityStore, HasTakenFromContainerComponent>
      hasTakenFromContainerComponentType;

  public ClayFactoria(JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log(
        "Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
  }

  @Override
  protected void setup() {
    LOGGER.atInfo().log("Registering Brush Component");
    brushComponentType =
        this.getEntityStoreRegistry().registerComponent(BrushComponent.class, BrushComponent::new);
    LOGGER.atInfo().log("Registering Owner Component");
    ownerComponentType =
        this.getEntityStoreRegistry().registerComponent(OwnerComponent.class, OwnerComponent::new);
    LOGGER.atInfo().log("Registering HasTakenFromStorage Component");
    hasTakenFromContainerComponentType =
        this.getEntityStoreRegistry().registerComponent(HasTakenFromContainerComponent.class, HasTakenFromContainerComponent::new);
    LOGGER.atInfo().log("Registering on Player Ready Event");
    this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
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
    this.getEntityStoreRegistry().registerSystem(new TargetBlockEventSystem(npcComponentType));

    LOGGER.atInfo().log("Registering Set Path Action");
    NPCPlugin.get().registerCoreComponentType("SetPath", BuilderActionSetPath::new);

    LOGGER.atInfo().log("Registering Sensor Path Complete");
    NPCPlugin.get().registerCoreComponentType("PathComplete", BuilderSensorPathComplete::new);

    LOGGER.atInfo().log("Registering Sensor Path Type");
    NPCPlugin.get().registerCoreComponentType("PathType", BuilderSensorPathType::new);

    LOGGER.atInfo().log("Registering Sensor Block Under");
    NPCPlugin.get().registerCoreComponentType("BlockUnder", BuilderSensorBlockUnder::new);

    LOGGER.atInfo().log("Registering Put Item In Hand Action");
    NPCPlugin.get().registerCoreComponentType("PutItemInHand", BuilderPutItemInHand::new);

    LOGGER.atInfo().log("Registering Take From Nearby Storage Action");
    NPCPlugin.get().registerCoreComponentType("TakeFromNearbyStorage", BuilderActionTakeFromNearbyStorage::new);

    LOGGER.atInfo().log("Registering Has Taken From Container Sensor");
    NPCPlugin.get().registerCoreComponentType("HasTakenFromContainer", BuilderSensorHasTakenFromContainer::new);
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
