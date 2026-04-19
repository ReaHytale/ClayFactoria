package com.clayfactoria.actions;

import com.clayfactoria.actions.builders.BuilderActionSetFilterItem;
import com.clayfactoria.components.JobComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Objects;

public class ActionSetFilterItem extends ActionBaseLogger {
    public ActionSetFilterItem(@NotNull BuilderActionSetFilterItem builder) {
        super(builder);
    }

    @Override
    public boolean executeNullChecked(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) throws NullPointerException {
        Ref<EntityStore> playerRef = sensorInfo != null && sensorInfo.hasPosition()
            ? Objects.requireNonNull(sensorInfo.getPositionProvider()).getTarget()
            : null;
        Objects.requireNonNull(playerRef, "No player found");
        Store<EntityStore> playerStore = playerRef.getStore();
        JobComponent jobComponent = store.ensureAndGetComponent(ref, JobComponent.getComponentType());
        ItemStack itemInHand = InventoryComponent.getItemInHand(playerStore, playerRef);
        jobComponent.setFilterItem(itemInHand != null ? itemInHand.getItemId() : null);

        Player player = playerStore.getComponent(playerRef, Player.getComponentType());
        PlayerRef playerRefComp = playerStore.getComponent(playerRef, PlayerRef.getComponentType());
        assert playerRefComp != null;
        assert player != null;
        if (itemInHand == null) {
            player.sendMessage(Message.raw("Reset filter item").color(Color.RED));
            SoundUtil.playSoundEvent2dToPlayer(playerRefComp, SoundEvent.getAssetMap().getIndex("SFX_Drop_Items_Leather"), SoundCategory.SFX);
        } else {
            Message itemName = Message.translation(Objects.requireNonNull(itemInHand.getItem().getTranslationProperties().getName()));
            player.sendMessage(Message.raw("Set filter item to ").insert(itemName).color(Color.GREEN));
            SoundUtil.playSoundEvent2dToPlayer(playerRefComp, SoundEvent.getAssetMap().getIndex("SFX_Drag_Items_Gems"), SoundCategory.SFX);
        }

        return true;
    }
}
