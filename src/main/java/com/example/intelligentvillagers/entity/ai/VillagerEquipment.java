package com.example.intelligentvillagers.entity.ai;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ShieldItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages equipment for intelligent villagers.
 * Handles equipping/unequipping items, checking equipment compatibility,
 * and providing access to equipped items.
 */
public class VillagerEquipment {
    // Static map to store equipment data for each villager
    private static final Map<UUID, VillagerEquipment> VILLAGER_EQUIPMENT = new HashMap<>();
    
    // The villager this equipment belongs to
    private final UUID villagerUUID;
    
    // Equipment slots
    private final Map<EquipmentSlot, ItemStack> equippedItems = new HashMap<>();
    
    /**
     * Initialize the VillagerEquipment system
     */
    public static void init() {
        IntelligentVillagersMod.LOGGER.info("Initializing VillagerEquipment system");
    }
    
    /**
     * Get or create equipment for a villager
     */
    public static VillagerEquipment getEquipment(Villager villager) {
        return VILLAGER_EQUIPMENT.computeIfAbsent(villager.getUUID(), uuid -> new VillagerEquipment(uuid));
    }
    
    /**
     * Constructor for new villager equipment
     */
    private VillagerEquipment(UUID villagerUUID) {
        this.villagerUUID = villagerUUID;
        
        // Initialize all equipment slots with empty items
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equippedItems.put(slot, ItemStack.EMPTY);
        }
    }
    
    /**
     * Equip an item in the appropriate slot if possible
     * 
     * @param itemStack The item to equip
     * @return true if the item was equipped, false otherwise
     */
    public boolean equipItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        Item item = itemStack.getItem();
        EquipmentSlot slot = getAppropriateSlot(item);
        
        if (slot != null) {
            // Check if the item is better than what's currently equipped
            if (isBetterThan(itemStack, equippedItems.get(slot))) {
                equippedItems.put(slot, itemStack.copy());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Unequip an item from a specific slot
     * 
     * @param slot The equipment slot to unequip
     * @return The item that was unequipped, or empty if nothing was equipped
     */
    public ItemStack unequipItem(EquipmentSlot slot) {
        ItemStack current = equippedItems.get(slot);
        equippedItems.put(slot, ItemStack.EMPTY);
        return current;
    }
    
    /**
     * Get the item equipped in a specific slot
     * 
     * @param slot The equipment slot to check
     * @return The equipped item, or empty if nothing is equipped
     */
    public ItemStack getEquippedItem(EquipmentSlot slot) {
        return equippedItems.getOrDefault(slot, ItemStack.EMPTY);
    }
    
    /**
     * Check if the villager has any item equipped in a specific slot
     * 
     * @param slot The equipment slot to check
     * @return true if an item is equipped, false otherwise
     */
    public boolean hasEquipment(EquipmentSlot slot) {
        return !equippedItems.getOrDefault(slot, ItemStack.EMPTY).isEmpty();
    }
    
    /**
     * Determine the appropriate equipment slot for an item
     * 
     * @param item The item to check
     * @return The appropriate equipment slot, or null if the item can't be equipped
     */
    private EquipmentSlot getAppropriateSlot(Item item) {
        if (item instanceof ArmorItem) {
            return ((ArmorItem) item).getSlot();
        } else if (item instanceof SwordItem || item instanceof AxeItem) {
            return EquipmentSlot.MAINHAND;
        } else if (item instanceof ShieldItem) {
            return EquipmentSlot.OFFHAND;
        } else if (item instanceof PickaxeItem || item instanceof ShovelItem || item instanceof HoeItem) {
            return EquipmentSlot.MAINHAND;
        }
        
        return null;
    }
    
    /**
     * Check if one item is better than another for the same slot
     * 
     * @param newItem The new item to compare
     * @param currentItem The currently equipped item
     * @return true if the new item is better, false otherwise
     */
    private boolean isBetterThan(ItemStack newItem, ItemStack currentItem) {
        if (currentItem.isEmpty()) {
            return true; // Anything is better than nothing
        }
        
        Item newItemType = newItem.getItem();
        Item currentItemType = currentItem.getItem();
        
        // Compare armor items
        if (newItemType instanceof ArmorItem && currentItemType instanceof ArmorItem) {
            ArmorItem newArmor = (ArmorItem) newItemType;
            ArmorItem currentArmor = (ArmorItem) currentItemType;
            
            // Compare defense values
            return newArmor.getDefense() > currentArmor.getDefense();
        }
        
        // Compare tools/weapons
        if (newItemType instanceof TieredItem && currentItemType instanceof TieredItem) {
            TieredItem newTool = (TieredItem) newItemType;
            TieredItem currentTool = (TieredItem) currentItemType;
            
            // Compare tiers
            return newTool.getTier().getLevel() > currentTool.getTier().getLevel();
        }
        
        // Default comparison (durability)
        return newItem.getMaxDamage() > currentItem.getMaxDamage();
    }
    
    /**
     * Update method called regularly to process equipment activities
     */
    public void update(Villager villager, VillagerBrain brain) {
        // Check if equipment is appropriate for the villager's role
        VillagerRole role = brain.getCurrentRole();
        
        // Potentially find better equipment in the villager's inventory
        for (int i = 0; i < villager.getInventory().getContainerSize(); i++) {
            ItemStack stack = villager.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (equipItem(stack)) {
                    // If the item was equipped, remove it from inventory
                    villager.getInventory().removeItem(i, 1);
                }
            }
        }
    }
    
    /**
     * Get the best tool for a specific task from the villager's equipment
     * 
     * @param taskType The type of task
     * @return The best tool for the task, or empty if no suitable tool is equipped
     */
    public ItemStack getBestToolForTask(TaskType taskType) {
        switch (taskType) {
            case MINING:
                return getEquippedItem(EquipmentSlot.MAINHAND).getItem() instanceof PickaxeItem ? 
                       getEquippedItem(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
            case WOODCUTTING:
                return getEquippedItem(EquipmentSlot.MAINHAND).getItem() instanceof AxeItem ? 
                       getEquippedItem(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
            case FARMING:
                return getEquippedItem(EquipmentSlot.MAINHAND).getItem() instanceof HoeItem ? 
                       getEquippedItem(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
            case DIGGING:
                return getEquippedItem(EquipmentSlot.MAINHAND).getItem() instanceof ShovelItem ? 
                       getEquippedItem(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
            case COMBAT:
                return getEquippedItem(EquipmentSlot.MAINHAND).getItem() instanceof SwordItem ? 
                       getEquippedItem(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
            default:
                return ItemStack.EMPTY;
        }
    }
    
    /**
     * Task types for tool selection
     */
    public enum TaskType {
        MINING,
        WOODCUTTING,
        FARMING,
        DIGGING,
        COMBAT
    }
    
    /**
     * Calculate the protection value from all equipped armor
     * 
     * @return The total armor protection value
     */
    public int getTotalArmorValue() {
        int total = 0;
        
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, 
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = getEquippedItem(slot);
            if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem) {
                total += ((ArmorItem) armor.getItem()).getDefense();
            }
        }
        
        return total;
    }
}