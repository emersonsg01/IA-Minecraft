package com.example.intelligentvillagers.entity.ai;

import net.minecraft.world.item.ItemStack;

/**
 * Represents a trade offer that a villager can make.
 * Contains the input item(s) required and the output item(s) provided.
 */
public class TradeOffer {
    private final ItemStack input;
    private final ItemStack output;
    private int uses;
    private final int maxUses;
    
    /**
     * Create a new trade offer
     * 
     * @param input The item(s) required from the buyer
     * @param output The item(s) provided to the buyer
     */
    public TradeOffer(ItemStack input, ItemStack output) {
        this(input, output, 0, 12);
    }
    
    /**
     * Create a new trade offer with custom uses
     * 
     * @param input The item(s) required from the buyer
     * @param output The item(s) provided to the buyer
     * @param uses Current number of times this trade has been used
     * @param maxUses Maximum number of times this trade can be used before refreshing
     */
    public TradeOffer(ItemStack input, ItemStack output, int uses, int maxUses) {
        this.input = input.copy();
        this.output = output.copy();
        this.uses = uses;
        this.maxUses = maxUses;
    }
    
    /**
     * Get the input item(s) required for this trade
     */
    public ItemStack getInput() {
        return input.copy();
    }
    
    /**
     * Get the output item(s) provided by this trade
     */
    public ItemStack getOutput() {
        return output.copy();
    }
    
    /**
     * Get the number of times this trade has been used
     */
    public int getUses() {
        return uses;
    }
    
    /**
     * Get the maximum number of times this trade can be used
     */
    public int getMaxUses() {
        return maxUses;
    }
    
    /**
     * Increment the number of uses for this trade
     */
    public void incrementUses() {
        uses++;
    }
    
    /**
     * Check if this trade is still available (not maxed out)
     */
    public boolean isAvailable() {
        return uses < maxUses;
    }
    
    /**
     * Reset the uses counter for this trade
     */
    public void resetUses() {
        uses = 0;
    }
    
    /**
     * Calculate the price multiplier based on demand
     * 
     * @param demandBonus Additional price increase based on demand
     * @return The price multiplier (1.0 = normal price)
     */
    public float getPriceMultiplier(float demandBonus) {
        float usageMultiplier = 1.0f - ((float) uses / (float) maxUses) * 0.3f;
        return Math.max(0.7f, usageMultiplier + demandBonus);
    }
}