package com.clayfactoria.codecs.task;

import com.clayfactoria.codecs.Job;
import com.clayfactoria.codecs.Task;
import com.clayfactoria.components.JobComponent;
import com.clayfactoria.utils.TaskHelper;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialExtraResourcesSection;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bson.BsonDocument;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.clayfactoria.utils.TaskHelper.getNPCEntity;

public class WorkTaskExecutor extends PointTaskExecutor {

    @Override
    public boolean canPerformTask(Ref<EntityStore> ref) {
        Component<ChunkStore> blockEntity = TaskHelper.getBlockEntity(ref);
        if (blockEntity == null) {
            return true;
        }

        NPCEntity npcEntity = getNPCEntity(ref);
        Store<EntityStore> store = ref.getStore();
        JobComponent jobComponent = Objects.requireNonNull(
            store.getComponent(ref  , JobComponent.getComponentType()));
        Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());
        World world = Objects.requireNonNull(npcEntity.getWorld());
        assert currentJob.getLocation() != null;
        BlockType blockType = world.getBlockType(currentJob.getLocation());
        if (blockType == null) {
            return false;
        }
        //FIXME: should check if the ingredients are there
        return blockType.getBench() != null;
    }

    @Override
    public boolean execute(Ref<EntityStore> entityRef) {
        NPCEntity npcEntity = getNPCEntity(entityRef);
        Store<EntityStore> store = entityRef.getStore();
        JobComponent jobComponent = Objects.requireNonNull(
            store.getComponent(entityRef, JobComponent.getComponentType()));
        Job currentJob = Objects.requireNonNull(jobComponent.getCurrentJob());
        World world = Objects.requireNonNull(npcEntity.getWorld());

        Vector3i pos = currentJob.getLocation();
        Ref<ChunkStore> blockRef = TaskHelper.getBlockComponentHolderDirectReference(world, pos.x,
            pos.y, pos.z);
        assert blockRef != null;

        // try benches with Turn On/Off options, like furnaces
        ProcessingBenchBlock processingBenchBlock = blockRef.getStore().getComponent(blockRef,
            ProcessingBenchBlock.getComponentType());
        BenchBlock benchBlock = blockRef.getStore()
            .getComponent(blockRef, BenchBlock.getComponentType());
        if (processingBenchBlock != null && benchBlock != null && !processingBenchBlock.getInputContainer().isEmpty()) {
            return processingBenchBlock.setActive(true, benchBlock, null);
        }

        // try auto-crafting
        if (jobComponent.getFilterItem() == null) {
            return false;
        }
        BlockType blockType = world.getBlockType(currentJob.getLocation());
        assert blockType != null;

        CraftingManager craftingManager = entityRef.getStore().ensureAndGetComponent(entityRef, CraftingManager.getComponentType());
        craftingManager.setBench(currentJob.getLocation().x, currentJob.getLocation().y, currentJob.getLocation().z, blockType);

        CraftingRecipe foundRecipe = findCraftingRecipe(blockType, jobComponent);
        assert foundRecipe != null;

        MaterialExtraResourcesSection extraResourcesSection = new MaterialExtraResourcesSection();
        CraftingManager.feedExtraResourcesSection(world,
            currentJob.getLocation().x, currentJob.getLocation().y, currentJob.getLocation().z, blockType,
            world.getBlockRotationIndex(currentJob.getLocation().x, currentJob.getLocation().y, currentJob.getLocation().z),
            blockType.getBench(), benchBlock != null ? benchBlock.getTierLevel() : 1, extraResourcesSection);

        CombinedItemContainer npcInventory = InventoryComponent.getCombined(entityRef.getStore(), entityRef);
        CombinedItemContainer combinedContainer = new CombinedItemContainer(npcInventory, extraResourcesSection.getItemContainer());

        boolean wasCrafted = craftItem(entityRef, entityRef.getStore(), foundRecipe, 1, combinedContainer);
        craftingManager.clearBench(entityRef, entityRef.getStore());
        return wasCrafted;
    }

    private static @Nullable CraftingRecipe findCraftingRecipe(BlockType blockType, JobComponent jobComponent) {
        List<CraftingRecipe> benchRecipes = CraftingPlugin.getBenchRecipes(blockType.getBench());
        CraftingRecipe foundRecipe = null;
        for (CraftingRecipe recipe : benchRecipes) {
            String itemId = recipe.getPrimaryOutput().getItemId();
            assert itemId != null;
            if (itemId.equals(jobComponent.getFilterItem())) {
                foundRecipe = recipe;
                break;
            }
        }
        return foundRecipe;
    }

    public boolean craftItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor,
                             @Nonnull CraftingRecipe recipe, int quantity, @Nonnull ItemContainer itemContainer) {
        CraftRecipeEvent.Pre preEvent = new CraftRecipeEvent.Pre(recipe, quantity);
        componentAccessor.invoke(ref, preEvent);
        if (preEvent.isCancelled()) {
            return false;
        } else {
            if (!removeInputFromInventory(itemContainer, recipe, quantity)) {
                return false;
            }

            CraftRecipeEvent.Post postEvent = new CraftRecipeEvent.Post(recipe, quantity);
            componentAccessor.invoke(ref, postEvent);
            if (!postEvent.isCancelled()) {
                giveOutput(ref, componentAccessor, recipe, quantity);
            }
            return true;
        }
    }

    private static boolean removeInputFromInventory(@Nonnull ItemContainer itemContainer, @Nonnull CraftingRecipe craftingRecipe, int quantity) {
        List<MaterialQuantity> materialsToRemove = getInputMaterials(craftingRecipe, quantity);
        if (materialsToRemove.isEmpty()) {
            return true;
        } else {
            ListTransaction<MaterialTransaction> materialTransactions = itemContainer.removeMaterials(materialsToRemove, true, true, true);
            return materialTransactions.succeeded();
        }
    }

    public static List<MaterialQuantity> getInputMaterials(@Nonnull CraftingRecipe recipe, int quantity) {
        Objects.requireNonNull(recipe);
        return recipe.getInput() == null ? Collections.emptyList() : getInputMaterials(recipe.getInput(), quantity);
    }

    private static List<MaterialQuantity> getInputMaterials(@Nonnull MaterialQuantity[] input, int quantity) {
        ObjectList<MaterialQuantity> materials = new ObjectArrayList<>();

        for (MaterialQuantity craftingMaterial : input) {
            String itemId = craftingMaterial.getItemId();
            String resourceTypeId = craftingMaterial.getResourceTypeId();
            int materialQuantity = craftingMaterial.getQuantity();
            BsonDocument metadata = craftingMaterial.getMetadata();
            materials.add(new MaterialQuantity(itemId, resourceTypeId, null, materialQuantity * quantity, metadata));
        }

        return materials;
    }

    public static List<ItemStack> getOutputItemStacks(@Nonnull CraftingRecipe recipe, int quantity) {
        Objects.requireNonNull(recipe);
        MaterialQuantity[] output = recipe.getOutputs();
        if (output == null) {
            return List.of();
        } else {
            ObjectList<ItemStack> outputItemStacks = new ObjectArrayList<>();

            for(MaterialQuantity outputMaterial : output) {
                ItemStack outputItemStack = getOutputItemStack(outputMaterial, quantity);
                if (outputItemStack != null) {
                    outputItemStacks.add(outputItemStack);
                }
            }

            return outputItemStacks;
        }
    }

    public static ItemStack getOutputItemStack(@Nonnull MaterialQuantity outputMaterial, int quantity) {
        String itemId = outputMaterial.getItemId();
        if (itemId == null) {
            return null;
        } else {
            int materialQuantity = outputMaterial.getQuantity() <= 0 ? 1 : outputMaterial.getQuantity();
            return new ItemStack(itemId, materialQuantity * quantity, outputMaterial.getMetadata());
        }
    }

    private static void giveOutput(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull CraftingRecipe craftingRecipe, int quantity) {
        assert NPCEntity.getComponentType() != null;
        NPCEntity npcEntity = componentAccessor.getComponent(ref, NPCEntity.getComponentType());
        if (npcEntity != null) {
            List<ItemStack> itemStacks = getOutputItemStacks(craftingRecipe, quantity);
            for (ItemStack itemStack : itemStacks) {
                if (!ItemStack.isEmpty(itemStack)) {
                    SimpleItemContainer.addOrDropItemStack(componentAccessor, ref,
                        InventoryComponent.getCombined(ref.getStore(), ref), itemStack);
                }
            }

        }
    }

    @Override
    public Task relevantNextTask(List<Task> availableOptions) {
        return Task.WORK;
    }

}
