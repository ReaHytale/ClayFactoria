package com.clayfactoria.systems;

import com.clayfactoria.components.JobBoxComponent;
import com.clayfactoria.components.JobBoxComponent.JobBoxesComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static com.clayfactoria.ClayFactoria.debugBoxesComponentType;

public class TaskBoxSystem extends DelayedEntitySystem<EntityStore> {

    public static final Vector3d BOX_PADDING = new Vector3d(0.05F, 0.05F, 0.05F);
    @Nonnull
    private static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();

    public TaskBoxSystem() {
        super(0.5F);
    }

    private static void drawBox(JobBoxComponent boxComponent, World world) {
        Vector3d min = boxComponent.getBox().min;
        Vector3d max = boxComponent.getBox().max;
        // Bottom
        drawRect(
            new Vector3d(min.x, min.y, min.z),
            new Vector3d(max.x, min.y, min.z),
            new Vector3d(min.x, min.y, max.z),
            new Vector3d(max.x, min.y, max.z),
            world, boxComponent.getColour()
        );
        // Top
        drawRect(
            new Vector3d(min.x, max.y, min.z),
            new Vector3d(max.x, max.y, min.z),
            new Vector3d(min.x, max.y, max.z),
            new Vector3d(max.x, max.y, max.z),
            world, boxComponent.getColour()
        );
        // Connecting Lines
        drawLine(min, new Vector3d(min.x, max.y, min.z), world, boxComponent.getColour());
        drawLine(new Vector3d(max.x, min.y, max.z), max, world, boxComponent.getColour());
        drawLine(new Vector3d(min.x, min.y, max.z), new Vector3d(min.x, max.y, max.z), world,
            boxComponent.getColour());
        drawLine(new Vector3d(max.x, min.y, min.z), new Vector3d(max.x, max.y, min.z), world,
            boxComponent.getColour());
    }

    private static void drawRect(Vector3d p1, Vector3d p2, Vector3d p3, Vector3d p4, World world,
                                 Vector3f color) {
        drawLine(p1, p2, world, color);
        drawLine(p1, p3, world, color);
        drawLine(p2, p4, world, color);
        drawLine(p3, p4, world, color);
    }

    private static void drawLine(Vector3d start, Vector3d end,
                                 World world, Vector3f color) {
        DebugUtils.addLine(world, start, end, color, 0.03D, 0.6F, DebugUtils.FLAG_NO_WIREFRAME);
    }

    private static void drawArrow(Vector3d start, Vector3d end,
                                  World world, Vector3f color) {
        Vector3d direction = end.clone().subtract(start).normalize();
        double half = start.distanceTo(end) / 2;
        Vector3d position = start.clone().add(direction.clone().scale(half));
        DebugUtils.addLine(world, start, end, color, 0.03D, 0.6F, DebugUtils.FLAG_NO_WIREFRAME);
        DebugUtils.addArrow(world, position, direction, color, 1F,
            0.6F,
            DebugUtils.FLAG_NO_WIREFRAME);
    }

    public static boolean wandIsNotEquipped(
        ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> ref) {
        InventoryComponent.Hotbar hotbarComponent =
            componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        Objects.requireNonNull(hotbarComponent);
        ItemStack itemStack = hotbarComponent.getActiveItem();
        return itemStack == null || !itemStack.getItemId().equals("Tool_Brush");
    }

    @Override
    public Query<EntityStore> getQuery() {
        return PLAYER_COMPONENT_TYPE;
    }

    @Override
    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
        @Nonnull Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        JobBoxesComponent jobBoxesComponent = archetypeChunk.getComponent(index,
            debugBoxesComponentType);
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (jobBoxesComponent == null
            || jobBoxesComponent.boxes.isEmpty()
            || player == null
            || wandIsNotEquipped(commandBuffer, player.getReference())
        ) {
            return;
        }
        World world = commandBuffer.getExternalData().getWorld();

        List<JobBoxComponent> boxes = jobBoxesComponent.boxes;
        JobBoxComponent prev = boxes.getFirst();
        drawBox(prev, world);
        for (int i = 1; i < boxes.size(); i++) {
            JobBoxComponent box = boxes.get(i);
            drawBox(box, world);

            Vector3d boxMid = box.getBox().min.clone().add(box.getBox().max).scale(0.5);
            Vector3d prevBoxMid = prev.getBox().min.clone().add(prev.getBox().max).scale(0.5);
            drawArrow(prevBoxMid, boxMid, world, box.getColour());

            prev = box;
        }
    }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) {
        return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
    }
}
