package com.example.intelligentvillagers.entity.ai;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages inventory for intelligent villagers.
 * Handles storing, retrieving, and managing items that villagers collect or craft.
 * This is separate from the vanilla villager inventory and provides extended functionality.
 */
public class VillagerInventory {
    // Static map to store inventory data for each villager
    private static final Map<UUID, VillagerInventory> VILLAGER_INVENTORIES = new HashMap<>();
    
    // The villager this inventory belongs to
    private final UUID villagerUUID;
    
    // The actual inventory contents
    private final List<ItemStack> items = new ArrayList<>();
    
    // Maximum inventory size
    private static final int DEFAULT_INVENTORY_SIZE = 27; // 3 rows of 9 slots
    private int maxSize = DEFAULT_INVENTORY_SIZE;
    
    /**
     * Initialize the VillagerInventory system
     */
    public static void init() {
        IntelligentVillagersMod.LOGGER.info("Initializing VillagerInventory system");
    }
    
    /**
     * Get or create inventory for a villager
     */
    public static VillagerInventory getInventory(Villager villager) {
        return VILLAGER_INVENTORIES.computeIfAbsent(villager.getUUID(), uuid -> new VillagerInventory(uuid));
    }
    
    /**
     * Constructor for new villager inventory
     */
    private VillagerInventory(UUID villagerUUID) {
        this.villagerUUID = villagerUUID;
    }
    
    /**
     * Add an item to the inventory
     * 
     * @param itemStack The item to add
     * @return true if the item was added, false if inventory is full
     */
    public boolean addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        // Check if we can stack this item with existing ones
        for (int i = 0; i < items.size(); i++) {
            ItemStack existingStack = items.get(i);
            if (!existingStack.isEmpty() && ItemStack.isSameItemSameTags(existingStack, itemStack)) {
                int spaceLeft = existingStack.getMaxStackSize() - existingStack.getCount();
                if (spaceLeft > 0) {
                    int amountToAdd = Math.min(spaceLeft, itemStack.getCount());
                    existingStack.grow(amountToAdd);
                    itemStack.shrink(amountToAdd);
                    
                    // If we've added all items, return success
                    if (itemStack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        
        // If we still have items to add and space in inventory, add as new stack
        if (items.size() < maxSize) {
            items.add(itemStack.copy());
            return true;
        }
        
        return false; // Inventory is full
    }
    
    /**
     * Remove an item from the inventory
     * 
     * @param index The index of the item to remove
     * @return The removed item, or empty if index is invalid
     */
    public ItemStack removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            ItemStack result = items.get(index);
            items.set(index, ItemStack.EMPTY);
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Remove a specific number of items from a stack
     * 
     * @param index The index of the item stack
     * @param count The number of items to remove
     * @return The removed items, or empty if index is invalid
     */
    public ItemStack removeItem(int index, int count) {
        if (index >= 0 && index < items.size()) {
            ItemStack stack = items.get(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            
            ItemStack result = stack.split(count);
            if (stack.isEmpty()) {
                items.set(index, ItemStack.EMPTY);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Get an item from the inventory without removing it
     * 
     * @param index The index of the item to get
     * @return The item, or empty if index is invalid
     */
    public ItemStack getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Set an item in the inventory
     * 
     * @param index The index to set
     * @param itemStack The item to set
     */
    public void setItem(int index, ItemStack itemStack) {
        if (index >= 0 && index < items.size()) {
            items.set(index, itemStack);
        } else if (index >= 0 && index < maxSize) {
            // Fill with empty stacks up to the index
            while (items.size() <= index) {
                items.add(ItemStack.EMPTY);
            }
            items.set(index, itemStack);
        }
    }
    
    /**
     * Get the size of the inventory
     * 
     * @return The number of slots in the inventory
     */
    public int getContainerSize() {
        return maxSize;
    }
    
    /**
     * Check if the inventory is empty
     * 
     * @return true if all slots are empty, false otherwise
     */
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Clear the inventory
     */
    public void clearContent() {
        items.clear();
    }
    
    /**
     * Check if the inventory contains a specific item
     * 
     * @param item The item to check for
     * @return true if the inventory contains the item, false otherwise
     */
    public boolean hasItem(Item item) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Count how many of a specific item are in the inventory
     * 
     * @param item The item to count
     * @return The total count of the item
     */
    public int countItem(Item item) {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    /**
     * Set the maximum size of the inventory
     * 
     * @param size The new maximum size
     */
    public void setMaxSize(int size) {
        this.maxSize = Math.max(1, size);
    }
    
    /**
     * Get all items in the inventory
     * 
     * @return A list of all item stacks
     */
    public List<ItemStack> getAllItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Update method called regularly to process inventory activities
     */
    public void update(Villager villager, VillagerBrain brain) {
        // This could handle automatic sorting, item degradation, etc.
    }
}