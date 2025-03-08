package com.example.intelligentvillagers.entity.ai;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import com.example.intelligentvillagers.config.ModConfig;
import net.minecraft.world.entity.npc.Villager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Core class for the intelligent villager AI system.
 * Handles learning, skill progression, and decision-making.
 */
public class VillagerBrain {
    // Skill types that villagers can learn and improve
    public enum SkillType {
        MINING,
        CRAFTING,
        FARMING,
        ANIMAL_HUSBANDRY,
        EXPLORATION,
        BUILDING,
        SOCIAL
    }
    
    // Static map to store brain data for each villager
    private static final Map<UUID, VillagerBrain> VILLAGER_BRAINS = new HashMap<>();
    
    // Random instance for probabilistic decisions
    private static final Random RANDOM = new Random();
    
    // The villager this brain belongs to
    private final UUID villagerUUID;
    
    // Skill levels for this villager
    private final Map<SkillType, Integer> skills = new HashMap<>();
    
    // Experience points for each skill
    private final Map<SkillType, Double> skillExperience = new HashMap<>();
    
    // Generation of this villager (for inheritance and evolution)
    private int generation = 0;
    
    // Social connections to other villagers
    private final Map<UUID, Float> relationships = new HashMap<>();
    
    // Current task and role in society
    private VillagerRole currentRole = VillagerRole.UNEMPLOYED;
    private VillagerTask currentTask = null;
    
    /**
     * Initialize the VillagerBrain system
     */
    public static void init() {
        IntelligentVillagersMod.LOGGER.info("Initializing VillagerBrain system");
        
        // Initialize the equipment system
        VillagerEquipment.init();
        
        // Initialize the inventory system
        VillagerInventory.init();
        
        // Initialize the trading system
        VillagerTrading.init();
    }
    
    /**
     * Get or create a brain for a villager
     */
    public static VillagerBrain getBrain(Villager villager) {
        return VILLAGER_BRAINS.computeIfAbsent(villager.getUUID(), uuid -> new VillagerBrain(uuid));
    }
    
    /**
     * Constructor for a new villager brain
     */
    private VillagerBrain(UUID villagerUUID) {
        this.villagerUUID = villagerUUID;
        
        // Initialize all skills at level 1
        for (SkillType skill : SkillType.values()) {
            skills.put(skill, 1);
            skillExperience.put(skill, 0.0);
        }
    }
    
    /**
     * Update method called regularly to process brain activities
     */
    public void update(Villager villager) {
        // Skip processing if mod is disabled
        if (!ModConfig.COMMON.enableMod.get()) {
            return;
        }
        
        // Determine and execute the most appropriate task based on current conditions
        determineTask(villager);
        
        // Execute the current task if one exists
        if (currentTask != null) {
            currentTask.execute(villager, this);
        }
        
        // Process social interactions if enabled
        if (ModConfig.COMMON.enableSocialInteractions.get()) {
            processSocialInteractions(villager);
        }
        
        // Update equipment
        VillagerEquipment.getEquipment(villager).update(villager, this);
        
        // Update inventory
        VillagerInventory.getInventory(villager).update(villager, this);
        
        // Update trading
        VillagerTrading.getTrading(villager).update(villager, this);
    }
    
    /**
     * Determine the most appropriate task for the villager
     */
    private void determineTask(Villager villager) {
        // Task selection logic will go here
        // This will be based on villager's skills, needs of the village, time of day, etc.
    }
    
    /**
     * Process social interactions with nearby villagers
     */
    private void processSocialInteractions(Villager villager) {
        // Social interaction logic will go here
        // This includes relationship building, knowledge sharing, and reproduction
    }
    
    /**
     * Add experience to a skill and potentially level it up
     */
    public void addSkillExperience(SkillType skillType, double amount) {
        // Get current experience and add new amount
        double currentExp = skillExperience.getOrDefault(skillType, 0.0);
        double newExp = currentExp + (amount * ModConfig.COMMON.skillProgressionRate.get());
        skillExperience.put(skillType, newExp);
        
        // Check if skill should level up
        int currentLevel = skills.getOrDefault(skillType, 1);
        int maxLevel = ModConfig.COMMON.maxSkillLevel.get();
        
        // Simple level-up formula: each level requires more experience than the last
        double expNeeded = currentLevel * 10.0;
        
        if (newExp >= expNeeded && currentLevel < maxLevel) {
            // Level up the skill
            skills.put(skillType, currentLevel + 1);
            skillExperience.put(skillType, newExp - expNeeded);
            
            IntelligentVillagersMod.LOGGER.debug("Villager {} leveled up {} skill to {}", 
                    villagerUUID, skillType, currentLevel + 1);
        }
    }
    
    /**
     * Create a child brain from two parent brains
     */
    public static VillagerBrain createChildBrain(UUID childUUID, VillagerBrain parent1, VillagerBrain parent2) {
        VillagerBrain childBrain = new VillagerBrain(childUUID);
        
        // Set generation to be one higher than the highest parent generation
        int parentMaxGen = Math.max(parent1.generation, parent2.generation);
        childBrain.generation = parentMaxGen + 1;
        
        // Inherit skills from parents with some randomness
        for (SkillType skill : SkillType.values()) {
            int parent1Skill = parent1.getSkillLevel(skill);
            int parent2Skill = parent2.getSkillLevel(skill);
            
            // Calculate inherited skill level
            double inheritanceRate = ModConfig.COMMON.skillInheritanceRate.get();
            int baseSkill = (int) (((parent1Skill + parent2Skill) / 2.0) * inheritanceRate);
            
            // Add generation evolution bonus
            baseSkill += ModConfig.COMMON.generationEvolutionBonus.get();
            
            // Add some randomness
            int finalSkill = Math.max(1, baseSkill + RANDOM.nextInt(3) - 1);
            
            // Set the skill level
            childBrain.skills.put(skill, finalSkill);
        }
        
        return childBrain;
    }
    
    /**
     * Get the current level of a skill
     */
    public int getSkillLevel(SkillType skillType) {
        return skills.getOrDefault(skillType, 1);
    }
    
    /**
     * Get the current role of this villager
     */
    public VillagerRole getCurrentRole() {
        return currentRole;
    }
    
    /**
     * Set the current role of this villager
     */
    public void setCurrentRole(VillagerRole role) {
        this.currentRole = role;
    }
    
    /**
     * Get the generation of this villager
     */
    public int getGeneration() {
        return generation;
    }
    
    /**
     * Get relationship value with another villager
     */
    public float getRelationship(UUID otherVillager) {
        return relationships.getOrDefault(otherVillager, 0.0f);
    }
    
    /**
     * Modify relationship with another villager
     */
    public void modifyRelationship(UUID otherVillager, float amount) {
        float current = relationships.getOrDefault(otherVillager, 0.0f);
        relationships.put(otherVillager, Math.max(-1.0f, Math.min(1.0f, current + amount)));
    }
}