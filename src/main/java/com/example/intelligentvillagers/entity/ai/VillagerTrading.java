package com.example.intelligentvillagers.entity.ai;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import com.example.intelligentvillagers.config.ModConfig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Manages trading for intelligent villagers.
 * Handles trade offers, pricing, and trade networks between villagers and with players.
 */
public class VillagerTrading {
    // Static map to store trading data for each villager
    private static final Map<UUID, VillagerTrading> VILLAGER_TRADING = new HashMap<>();
    
    // The villager this trading system belongs to
    private final UUID villagerUUID;
    
    // List of trade offers this villager can make
    private final List<TradeOffer> tradeOffers = new ArrayList<>();
    
    // Map of item values (base prices)
    private static final Map<Item, Float> BASE_ITEM_VALUES = new HashMap<>();
    
    // Map of supply and demand modifiers for the village economy
    private static final Map<Item, Float> SUPPLY_DEMAND_MODIFIERS = new HashMap<>();
    
    // Random for trade generation
    private static final Random RANDOM = new Random();
    
    // Trading cooldown (in ticks)
    private int tradingCooldown = 0;
    private static final int DEFAULT_COOLDOWN = 1200; // 1 minute
    
    // Trading reputation with other villagers
    private final Map<UUID, Float> tradingReputation = new HashMap<>();
    
    /**
     * Initialize the VillagerTrading system
     */
    public static void init() {
        IntelligentVillagersMod.LOGGER.info("Initializing VillagerTrading system");
        initializeBaseValues();
    }
    
    /**
     * Initialize base values for common items
     */
    private static void initializeBaseValues() {
        // Resources
        BASE_ITEM_VALUES.put(Items.COAL, 1.0f);
        BASE_ITEM_VALUES.put(Items.IRON_INGOT, 3.0f);
        BASE_ITEM_VALUES.put(Items.GOLD_INGOT, 6.0f);
        BASE_ITEM_VALUES.put(Items.DIAMOND, 16.0f);
        BASE_ITEM_VALUES.put(Items.EMERALD, 8.0f);
        
        // Food
        BASE_ITEM_VALUES.put(Items.BREAD, 1.0f);
        BASE_ITEM_VALUES.put(Items.APPLE, 1.0f);
        BASE_ITEM_VALUES.put(Items.COOKED_BEEF, 2.0f);
        BASE_ITEM_VALUES.put(Items.COOKED_CHICKEN, 1.5f);
        BASE_ITEM_VALUES.put(Items.COOKED_PORKCHOP, 2.0f);
        
        // Crops
        BASE_ITEM_VALUES.put(Items.WHEAT, 0.5f);
        BASE_ITEM_VALUES.put(Items.CARROT, 0.5f);
        BASE_ITEM_VALUES.put(Items.POTATO, 0.5f);
        BASE_ITEM_VALUES.put(Items.BEETROOT, 0.5f);
        
        // Tools
        BASE_ITEM_VALUES.put(Items.WOODEN_PICKAXE, 2.0f);
        BASE_ITEM_VALUES.put(Items.STONE_PICKAXE, 4.0f);
        BASE_ITEM_VALUES.put(Items.IRON_PICKAXE, 8.0f);
        BASE_ITEM_VALUES.put(Items.GOLDEN_PICKAXE, 6.0f);
        BASE_ITEM_VALUES.put(Items.DIAMOND_PICKAXE, 24.0f);
        
        // Initialize all supply/demand modifiers to 1.0 (neutral)
        for (Item item : BASE_ITEM_VALUES.keySet()) {
            SUPPLY_DEMAND_MODIFIERS.put(item, 1.0f);
        }
    }
    
    /**
     * Get or create trading system for a villager
     */
    public static VillagerTrading getTrading(Villager villager) {
        return VILLAGER_TRADING.computeIfAbsent(villager.getUUID(), uuid -> new VillagerTrading(uuid));
    }
    
    /**
     * Constructor for new villager trading system
     */
    private VillagerTrading(UUID villagerUUID) {
        this.villagerUUID = villagerUUID;
        generateInitialTrades();
    }
    
    /**
     * Generate initial trades based on villager role and skills
     */
    private void generateInitialTrades() {
        // Will be populated based on the villager's role and skills
        // This will be called when the trading system is first created
    }
    
    /**
     * Update method called regularly to process trading activities
     */
    public void update(Villager villager, VillagerBrain brain) {
        // Skip processing if economy is disabled
        if (!ModConfig.COMMON.enableEconomy.get()) {
            return;
        }
        
        // Decrease cooldown if active
        if (tradingCooldown > 0) {
            tradingCooldown--;
        }
        
        // Periodically update trade offers based on role and skills
        if (villager.level.getGameTime() % 12000 == 0) { // Once per minecraft day
            updateTradeOffers(villager, brain);
        }
        
        // Try to trade with nearby villagers
        if (tradingCooldown <= 0 && ModConfig.COMMON.enableSocialInteractions.get()) {
            tryTradeWithNearbyVillagers(villager, brain);
        }
    }
    
    /**
     * Update trade offers based on villager's role and skills
     */
    private void updateTradeOffers(Villager villager, VillagerBrain brain) {
        // Clear old offers if we have too many
        while (tradeOffers.size() > 10) {
            tradeOffers.remove(0);
        }
        
        // Generate new offers based on role
        VillagerRole role = brain.getCurrentRole();
        int tradingSkill = brain.getSkillLevel(VillagerBrain.SkillType.SOCIAL);
        
        // Add role-specific trades
        switch (role) {
            case FARMER:
                addTradeOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.WHEAT, 20));
                addTradeOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.CARROT, 15));
                addTradeOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.POTATO, 15));
                break;
                
            case MINER:
                addTradeOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.COAL, 15));
                addTradeOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.IRON_INGOT, 5));
                addTradeOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.GOLD_INGOT, 3));
                break;
                
            case TOOLSMITH:
                addTradeOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.IRON_PICKAXE));
                addTradeOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.IRON_AXE));
                addTradeOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.IRON_SHOVEL));
                break;
                
            case TRADER:
                // Traders have more varied and better value trades
                addTradeOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BREAD, 6));
                addTradeOffer(new ItemStack(Items.COAL, 10), new ItemStack(Items.EMERALD, 1));
                addTradeOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.DIAMOND, 1));
                break;
                
            default:
                // Basic trades for other roles
                addTradeOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BREAD, 4));
                break;
        }
        
        // Add some random trades based on trading skill
        if (tradingSkill > 10) {
            // Higher skill means better trades
            int numRandomTrades = 1 + (tradingSkill / 20); // 1-6 random trades
            for (int i = 0; i < numRandomTrades; i++) {
                addRandomTrade(tradingSkill);
            }
        }
    }
    
    /**
     * Add a random trade based on trading skill
     */
    private void addRandomTrade(int tradingSkill) {
        // List of possible items to trade
        List<Item> possibleItems = new ArrayList<>(BASE_ITEM_VALUES.keySet());
        
        // Select random items for input and output
        Item inputItem = possibleItems.get(RANDOM.nextInt(possibleItems.size()));
        Item outputItem = possibleItems.get(RANDOM.nextInt(possibleItems.size()));
        
        // Determine quantities based on relative values and trading skill
        float inputValue = getAdjustedValue(inputItem);
        float outputValue = getAdjustedValue(outputItem);
        
        // Calculate fair exchange rate
        int inputCount = 1;
        int outputCount = Math.max(1, Math.round((inputValue * inputCount) / outputValue));
        
        // Apply trading skill bonus (better deals with higher skill)
        float skillBonus = 1.0f + (tradingSkill / 100.0f); // Up to 100% bonus at max skill
        outputCount = Math.max(1, Math.round(outputCount * skillBonus));
        
        // Create and add the trade offer
        addTradeOffer(new ItemStack(inputItem, inputCount), new ItemStack(outputItem, outputCount));
    }
    
    /**
     * Get the adjusted value of an item based on supply and demand
     */
    private float getAdjustedValue(Item item) {
        float baseValue = BASE_ITEM_VALUES.getOrDefault(item, 1.0f);
        float modifier = SUPPLY_DEMAND_MODIFIERS.getOrDefault(item, 1.0f);
        return baseValue * modifier;
    }
    
    /**
     * Add a trade offer to this villager's available trades
     */
    public void addTradeOffer(ItemStack input, ItemStack output) {
        tradeOffers.add(new TradeOffer(input, output));
    }
    
    /**
     * Try to trade with nearby villagers
     */
    private void tryTradeWithNearbyVillagers(Villager villager, VillagerBrain brain) {
        // This would find nearby villagers and attempt to trade with them
        // based on what each villager needs and can offer
        
        // Set cooldown after trading
        tradingCooldown = DEFAULT_COOLDOWN;
    }
    
    /**
     * Execute a trade with a player or another villager
     * 
     * @param offer The trade offer to execute
     * @param sourceInventory The inventory providing the input items
     * @param targetInventory The inventory receiving the output items
     * @return true if the trade was successful, false otherwise
     */
    public boolean executeTrade(TradeOffer offer, VillagerInventory sourceInventory, VillagerInventory targetInventory) {
        // Check if source has the required input items
        ItemStack input = offer.getInput();
        Item inputItem = input.getItem();
        int inputCount = input.getCount();
        
        if (sourceInventory.countItem(inputItem) < inputCount) {
            return false; // Not enough input items
        }
        
        // Check if target has space for output
        ItemStack output = offer.getOutput().copy();
        if (!targetInventory.addItem(output)) {
            return false; // No space for output
        }
        
        // Remove input items from source
        // This is simplified and would need to be more complex in practice
        // to handle removing items from specific slots
        for (int i = 0; i < sourceInventory.getContainerSize(); i++) {
            ItemStack stack = sourceInventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == inputItem) {
                int toRemove = Math.min(stack.getCount(), inputCount);
                sourceInventory.removeItem(i, toRemove);
                inputCount -= toRemove;
                
                if (inputCount <= 0) {
                    break;
                }
            }
        }
        
        // Update supply/demand modifiers
        updateSupplyDemand(inputItem, output.getItem());
        
        // Set cooldown
        tradingCooldown = DEFAULT_COOLDOWN;
        
        return true;
    }
    
    /**
     * Update supply and demand modifiers based on a completed trade
     */
    private void updateSupplyDemand(Item boughtItem, Item soldItem) {
        // Increase value of bought item (higher demand)
        float boughtModifier = SUPPLY_DEMAND_MODIFIERS.getOrDefault(boughtItem, 1.0f);
        SUPPLY_DEMAND_MODIFIERS.put(boughtItem, Math.min(2.0f, boughtModifier * 1.05f));
        
        // Decrease value of sold item (higher supply)
        float soldModifier = SUPPLY_DEMAND_MODIFIERS.getOrDefault(soldItem, 1.0f);
        SUPPLY_DEMAND_MODIFIERS.put(soldItem, Math.max(0.5f, soldModifier * 0.95f));
    }
    
    /**
     * Get all available trade offers
     */
    public List<TradeOffer> getTradeOffers() {
        return new ArrayList<>(tradeOffers);
    }
    
    /**
     * Update trading reputation with another villager
     */
    public void updateReputation(UUID otherVillager, float amount) {
        float current = tradingReputation.getOrDefault(otherVillager, 0.0f);
        tradingReputation.put(otherVillager, Math.max(-1.0f, Math.min(1.0f, current + amount)));
    }
    
    /**
     * Get the trading reputation with another villager
     */
    public float getReputation(UUID otherVillager) {
        return tradingReputation.getOrDefault(otherVillager, 0.0f);
    }
    
    /**
     * Get a trade offer by index
     */
    public TradeOffer getTradeOffer(int index) {
        if (index >= 0 && index < tradeOffers.size()) {
            return tradeOffers.get(index);
        }
        return null;
    }
    
    /**
     * Get the number of available trade offers
     */
    public int getTradeOfferCount() {
        return tradeOffers.size();
    }
    
    /**
     * Check if this villager is currently on trading cooldown
     */
    public boolean isOnCooldown() {
        return tradingCooldown > 0;
    }
    
    /**
     * Get the remaining cooldown time
     */
    public int getCooldownTime() {
        return tradingCooldown;
    }