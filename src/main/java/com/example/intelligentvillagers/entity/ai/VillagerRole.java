package com.example.intelligentvillagers.entity.ai;

/**
 * Defines the different roles that intelligent villagers can take in their society.
 * Each role specializes in different tasks and has different priorities.
 */
public enum VillagerRole {
    // Basic roles
    UNEMPLOYED("unemployed", "Villager without a specialized role"),
    CHILD("child", "Young villager still developing skills"),
    
    // Resource gathering roles
    MINER("miner", "Specializes in mining and resource collection"),
    LUMBERJACK("lumberjack", "Specializes in wood collection"),
    HUNTER("hunter", "Specializes in hunting animals for food and resources"),
    
    // Production roles
    FARMER("farmer", "Specializes in crop cultivation"),
    SHEPHERD("shepherd", "Specializes in animal husbandry"),
    FISHERMAN("fisherman", "Specializes in fishing"),
    
    // Crafting roles
    TOOLSMITH("toolsmith", "Specializes in creating tools and weapons"),
    BUILDER("builder", "Specializes in construction and building"),
    CRAFTSMAN("craftsman", "Specializes in general crafting"),
    
    // Advanced roles
    EXPLORER("explorer", "Specializes in exploration and mapping"),
    TRADER("trader", "Specializes in trading and economy"),
    GUARD("guard", "Specializes in village protection"),
    LEADER("leader", "Village leader who coordinates activities");
    
    private final String id;
    private final String description;
    
    VillagerRole(String id, String description) {
        this.id = id;
        this.description = description;
    }
    
    /**
     * Get the ID string for this role
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the description of this role
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the most suitable role based on skill levels
     */
    public static VillagerRole getBestRoleForSkills(VillagerBrain brain) {
        // Get skill levels
        int mining = brain.getSkillLevel(VillagerBrain.SkillType.MINING);
        int crafting = brain.getSkillLevel(VillagerBrain.SkillType.CRAFTING);
        int farming = brain.getSkillLevel(VillagerBrain.SkillType.FARMING);
        int animalHusbandry = brain.getSkillLevel(VillagerBrain.SkillType.ANIMAL_HUSBANDRY);
        int exploration = brain.getSkillLevel(VillagerBrain.SkillType.EXPLORATION);
        int building = brain.getSkillLevel(VillagerBrain.SkillType.BUILDING);
        int social = brain.getSkillLevel(VillagerBrain.SkillType.SOCIAL);
        
        // Find the highest skill
        int maxSkill = Math.max(mining, Math.max(crafting, Math.max(farming, 
                     Math.max(animalHusbandry, Math.max(exploration, Math.max(building, social))))));
        
        // Assign role based on highest skill
        if (maxSkill < 5) {
            return UNEMPLOYED; // Not skilled enough for specialization
        }
        
        // Assign based on highest skill
        if (maxSkill == mining) return MINER;
        if (maxSkill == crafting) {
            // Differentiate between different crafting roles
            if (building > crafting / 2) return BUILDER;
            return TOOLSMITH;
        }
        if (maxSkill == farming) return FARMER;
        if (maxSkill == animalHusbandry) return SHEPHERD;
        if (maxSkill == exploration) return EXPLORER;
        if (maxSkill == building) return BUILDER;
        if (maxSkill == social) {
            // Social villagers can be traders or leaders
            if (social > 50) return LEADER;
            return TRADER;
        }
        
        // Default fallback
        return UNEMPLOYED;
    }
}