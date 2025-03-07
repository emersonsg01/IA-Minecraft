package com.example.intelligentvillagers.entity.ai.tasks;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import com.example.intelligentvillagers.config.ModConfig;
import com.example.intelligentvillagers.entity.ai.VillagerBrain;
import com.example.intelligentvillagers.entity.ai.VillagerTask;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Task for villagers to mine resources from the environment.
 * Villagers with this task will search for valuable blocks and mine them.
 */
public class MiningTask implements VillagerTask {
    private static final int SEARCH_RADIUS = 16;
    private static final int SEARCH_HEIGHT = 3;
    private static final int MINING_DURATION = 60; // ticks
    private static final Random RANDOM = new Random();
    
    private BlockPos targetBlock = null;
    private int miningProgress = 0;
    
    @Override
    public boolean execute(Villager villager, VillagerBrain brain) {
        Level level = villager.level;
        
        // If we don't have a target block, find one
        if (targetBlock == null) {
            targetBlock = findMineableBlock(villager);
            miningProgress = 0;
            
            // If we still don't have a target, we can't execute this task
            if (targetBlock == null) {
                return false;
            }
        }
        
        // Calculate distance to target
        double distance = villager.position().distanceTo(new Vec3(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5));
        
        // If we're close enough to the target, mine it
        if (distance < 2.0) {
            // Increment mining progress
            miningProgress++;
            
            // Mining speed is affected by the villager's mining skill
            int miningSkill = brain.getSkillLevel(VillagerBrain.SkillType.MINING);
            int adjustedDuration = Math.max(10, MINING_DURATION - (miningSkill / 2));
            
            // If we've been mining long enough, break the block
            if (miningProgress >= adjustedDuration) {
                // Get the block state
                BlockState blockState = level.getBlockState(targetBlock);
                Block block = blockState.getBlock();
                
                // Break the block and drop items
                level.destroyBlock(targetBlock, true);
                
                // Award experience based on the value of the block
                double expValue = getExperienceValue(blockState);
                brain.addSkillExperience(VillagerBrain.SkillType.MINING, expValue);
                
                // Log the mining activity
                IntelligentVillagersMod.LOGGER.debug("Villager mined {} and gained {} mining experience", 
                        block.getName().getString(), expValue);
                
                // Reset target for next execution
                targetBlock = null;
                return true;
            }
        } else {
            // Navigate to the target block
            PathNavigation navigator = villager.getNavigation();
            if (!navigator.isInProgress()) {
                navigator.moveTo(targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5, 1.0);
            }
        }
        
        return false; // Task is still in progress
    }
    
    @Override
    public boolean canExecute(Villager villager, VillagerBrain brain) {
        // Check if mining tasks are enabled in config
        if (!ModConfig.COMMON.enableMiningTasks.get()) {
            return false;
        }
        
        // Check if the villager has at least some mining skill
        int miningSkill = brain.getSkillLevel(VillagerBrain.SkillType.MINING);
        return miningSkill >= 2; // Require at least level 2 mining skill
    }
    
    @Override
    public int getPriority() {
        return 70; // Medium-high priority
    }
    
    @Override
    public VillagerBrain.SkillType getPrimarySkill() {
        return VillagerBrain.SkillType.MINING;
    }
    
    @Override
    public String getName() {
        return "Mining";
    }
    
    /**
     * Find a suitable block to mine near the villager
     */
    private BlockPos findMineableBlock(Villager villager) {
        Level level = villager.level;
        BlockPos villagerPos = villager.blockPosition();
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        // Search in a radius around the villager
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_HEIGHT; y <= SEARCH_HEIGHT; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = villagerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    
                    // Check if this block is mineable and valuable
                    if (isValuableBlock(state)) {
                        potentialTargets.add(pos);
                    }
                }
            }
        }
        
        // If we found potential targets, sort by distance and value
        if (!potentialTargets.isEmpty()) {
            return potentialTargets.stream()
                    .sorted(Comparator.comparingDouble(pos -> {
                        // Sort by a combination of distance and value
                        double distance = pos.distSqr(villagerPos);
                        double value = getExperienceValue(level.getBlockState(pos));
                        return distance / value; // Lower is better
                    }))
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }
    
    /**
     * Check if a block is valuable and worth mining
     */
    private boolean isValuableBlock(BlockState state) {
        // Check for ore blocks
        if (state.is(BlockTags.COAL_ORES) || 
            state.is(BlockTags.IRON_ORES) || 
            state.is(BlockTags.COPPER_ORES) || 
            state.is(BlockTags.GOLD_ORES) || 
            state.is(BlockTags.REDSTONE_ORES) || 
            state.is(BlockTags.LAPIS_ORES) || 
            state.is(BlockTags.DIAMOND_ORES) || 
            state.is(BlockTags.EMERALD_ORES)) {
            return true;
        }
        
        // Add other valuable blocks as needed
        return false;
    }
    
    /**
     * Get the experience value for mining a particular block
     */
    private double getExperienceValue(BlockState state) {
        // Base experience value
        double baseValue = 1.0;
        
        // Increase value based on ore type
        if (state.is(BlockTags.COAL_ORES)) return baseValue * 1.0;
        if (state.is(BlockTags.IRON_ORES)) return baseValue * 2.0;
        if (state.is(BlockTags.COPPER_ORES)) return baseValue * 1.5;
        if (state.is(BlockTags.GOLD_ORES)) return baseValue * 3.0;
        if (state.is(BlockTags.REDSTONE_ORES)) return baseValue * 2.5;
        if (state.is(BlockTags.LAPIS_ORES)) return baseValue * 2.5;
        if (state.is(BlockTags.DIAMOND_ORES)) return baseValue * 5.0;
        if (state.is(BlockTags.EMERALD_ORES)) return baseValue * 5.0;
        
        // Default value for other blocks
        return baseValue;
    }
}