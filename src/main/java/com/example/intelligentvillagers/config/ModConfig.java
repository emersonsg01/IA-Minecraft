package com.example.intelligentvillagers.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    
    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new CommonConfig(commonBuilder);
        COMMON_SPEC = commonBuilder.build();
    }
    
    public static class CommonConfig {
        // General settings
        public final BooleanValue enableMod;
        public final IntValue villagerUpdateFrequency;
        
        // Learning and evolution settings
        public final DoubleValue learningRate;
        public final DoubleValue skillInheritanceRate;
        public final IntValue generationEvolutionBonus;
        
        // Skill settings
        public final IntValue maxSkillLevel;
        public final DoubleValue skillProgressionRate;
        
        // Social settings
        public final BooleanValue enableSocialInteractions;
        public final IntValue socialInteractionRadius;
        public final IntValue maxVillagerPopulation;
        public final IntValue reproductionCooldown;
        
        // Task settings
        public final BooleanValue enableMiningTasks;
        public final BooleanValue enableFarmingTasks;
        public final BooleanValue enableCraftingTasks;
        public final BooleanValue enableAnimalHusbandryTasks;
        public final BooleanValue enableExplorationTasks;
        public final BooleanValue enableBuildingTasks;
        
        // Advanced settings
        public final BooleanValue enableSeasons;
        public final BooleanValue enableEconomy;
        public final IntValue economyUpdateFrequency;
        public final BooleanValue enableCulturalDevelopment;
        
        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Intelligent Villagers Mod Configuration").push("general");
            
            enableMod = builder
                    .comment("Enable or disable the Intelligent Villagers mod functionality")
                    .define("enableMod", true);
            
            villagerUpdateFrequency = builder
                    .comment("How often (in ticks) villagers update their AI and learning (20 ticks = 1 second)")
                    .defineInRange("villagerUpdateFrequency", 20, 1, 100);
            
            builder.pop().push("learning");
            
            learningRate = builder
                    .comment("How quickly villagers learn new skills (higher values mean faster learning)")
                    .defineInRange("learningRate", 1.0, 0.1, 5.0);
            
            skillInheritanceRate = builder
                    .comment("Percentage of skills that offspring inherit from parents (0.0 to 1.0)")
                    .defineInRange("skillInheritanceRate", 0.7, 0.0, 1.0);
            
            generationEvolutionBonus = builder
                    .comment("Bonus skill points given to new generations")
                    .defineInRange("generationEvolutionBonus", 2, 0, 10);
            
            maxSkillLevel = builder
                    .comment("Maximum level a villager can achieve in any skill")
                    .defineInRange("maxSkillLevel", 100, 10, 1000);
            
            skillProgressionRate = builder
                    .comment("How quickly skills improve with use (higher values mean faster improvement)")
                    .defineInRange("skillProgressionRate", 1.0, 0.1, 5.0);
            
            builder.pop().push("social");
            
            enableSocialInteractions = builder
                    .comment("Enable social interactions between villagers")
                    .define("enableSocialInteractions", true);
            
            socialInteractionRadius = builder
                    .comment("Radius (in blocks) within which villagers can socially interact")
                    .defineInRange("socialInteractionRadius", 16, 4, 64);
            
            maxVillagerPopulation = builder
                    .comment("Maximum number of intelligent villagers allowed in a village")
                    .defineInRange("maxVillagerPopulation", 50, 10, 200);
            
            reproductionCooldown = builder
                    .comment("Time (in minecraft days) between reproduction attempts")
                    .defineInRange("reproductionCooldown", 2, 1, 10);
            
            builder.pop().push("tasks");
            
            enableMiningTasks = builder
                    .comment("Enable mining tasks for villagers")
                    .define("enableMiningTasks", true);
            
            enableFarmingTasks = builder
                    .comment("Enable farming tasks for villagers")
                    .define("enableFarmingTasks", true);
            
            enableCraftingTasks = builder
                    .comment("Enable crafting and toolmaking tasks for villagers")
                    .define("enableCraftingTasks", true);
            
            enableAnimalHusbandryTasks = builder
                    .comment("Enable animal husbandry tasks for villagers")
                    .define("enableAnimalHusbandryTasks", true);
            
            enableExplorationTasks = builder
                    .comment("Enable exploration and mapping tasks for villagers")
                    .define("enableExplorationTasks", true);
            
            enableBuildingTasks = builder
                    .comment("Enable building and construction tasks for villagers")
                    .define("enableBuildingTasks", true);
            
            builder.pop().push("advanced");
            
            enableSeasons = builder
                    .comment("Enable seasonal effects on villager behavior and tasks")
                    .define("enableSeasons", true);
            
            enableEconomy = builder
                    .comment("Enable economic system for villagers")
                    .define("enableEconomy", true);
            
            economyUpdateFrequency = builder
                    .comment("How often (in ticks) the economy system updates")
                    .defineInRange("economyUpdateFrequency", 1200, 200, 6000);
            
            enableCulturalDevelopment = builder
                    .comment("Enable cultural development for villages")
                    .define("enableCulturalDevelopment", true);
            
            builder.pop();
        }
    }
}