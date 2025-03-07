package com.example.intelligentvillagers.entity.ai;

import net.minecraft.world.entity.npc.Villager;

/**
 * Interface for all tasks that intelligent villagers can perform.
 * Each task represents a specific activity that a villager can execute.
 */
public interface VillagerTask {
    /**
     * Execute this task for the given villager
     * 
     * @param villager The villager executing the task
     * @param brain The brain of the villager
     * @return true if the task was completed successfully, false otherwise
     */
    boolean execute(Villager villager, VillagerBrain brain);
    
    /**
     * Check if this task can be executed by the given villager
     * 
     * @param villager The villager to check
     * @param brain The brain of the villager
     * @return true if the task can be executed, false otherwise
     */
    boolean canExecute(Villager villager, VillagerBrain brain);
    
    /**
     * Get the priority of this task
     * Higher priority tasks will be chosen over lower priority ones
     * 
     * @return The priority value (higher is more important)
     */
    int getPriority();
    
    /**
     * Get the skill type that this task primarily uses and improves
     * 
     * @return The primary skill type for this task
     */
    VillagerBrain.SkillType getPrimarySkill();
    
    /**
     * Get the name of this task for display and logging purposes
     * 
     * @return The name of the task
     */
    String getName();
}