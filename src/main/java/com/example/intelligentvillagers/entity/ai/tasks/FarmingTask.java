package com.example.intelligentvillagers.entity.ai.tasks;

import com.example.intelligentvillagers.IntelligentVillagersMod;
import com.example.intelligentvillagers.config.ModConfig;
import com.example.intelligentvillagers.entity.ai.VillagerBrain;
import com.example.intelligentvillagers.entity.ai.VillagerTask;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Task for villagers to farm crops.
 * Villagers with this task will plant, tend, and harvest crops.
 */
public class FarmingTask implements VillagerTask {
    private static final int SEARCH_RADIUS = 16;
    private static final int FARMING_DURATION = 40; // ticks
    private static final Random RANDOM = new Random();
    
    private enum FarmingSubtask {
        FIND_FARMLAND,
        TILL_SOIL,
        PLANT_SEEDS,
        HARVEST_CROPS,
        BONE_MEAL_CROPS
    }
    
    private BlockPos targetBlock = null;
    private FarmingSubtask currentSubtask = FarmingSubtask.FIND_FARMLAND;
    private int farmingProgress = 0;
    
    @Override
    public boolean execute(Villager villager, VillagerBrain brain) {
        Level level = villager.level;
        
        // If we don't have a target block, find one
        if (targetBlock == null) {
            determineSubtask(villager);
            targetBlock = findTargetForSubtask(villager);
            farmingProgress = 0;
            
            // If we still don't have a target, we can't execute this task
            if (targetBlock == null) {
                return false;
            }
        }
        
        // Calculate distance to target
        double distance = villager.position().distanceTo(new Vec3(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5));
        
        // If we're close enough to the target, perform the farming action
        if (distance < 2.0) {
            // Increment farming progress
            farmingProgress++;
            
            // Farming speed is affected by the villager's farming skill
            int farmingSkill = brain.getSkillLevel(VillagerBrain.SkillType.FARMING);
            int adjustedDuration = Math.max(10, FARMING_DURATION - (farmingSkill / 2));
            
            // If we've been farming long enough, perform the action
            if (farmingProgress >= adjustedDuration) {
                boolean success = performFarmingAction(villager, level);
                
                if (success) {
                    // Award experience based on the action performed
                    double expValue = getExperienceValue(currentSubtask);
                    brain.addSkillExperience(VillagerBrain.SkillType.FARMING, expValue);
                    
                    // Log the farming activity
                    IntelligentVillagersMod.LOGGER.debug("Villager performed {} and gained {} farming experience", 
                            currentSubtask.name(), expValue);
                    
                    // Reset target for next execution
                    targetBlock = null;
                    return true;
                }
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
        // Check if farming tasks are enabled in config
        if (!ModConfig.COMMON.enableFarmingTasks.get()) {
            return false;
        }
        
        // Check if the villager has at least some farming skill
        int farmingSkill = brain.getSkillLevel(VillagerBrain.SkillType.FARMING);
        return farmingSkill >= 2; // Require at least level 2 farming skill
    }
    
    @Override
    public int getPriority() {
        return 65; // Medium-high priority
    }
    
    @Override
    public VillagerBrain.SkillType getPrimarySkill() {
        return VillagerBrain.SkillType.FARMING;
    }
    
    @Override
    public String getName() {
        return "Farming";
    }
    
    /**
     * Determine what farming subtask to perform
     */
    private void determineSubtask(Villager villager) {
        // Check if the villager has seeds to plant
        boolean hasSeeds = hasSeeds(villager);
        
        // Check if there are crops to harvest nearby
        boolean cropToHarvest = findHarvestableCrop(villager) != null;
        
        // Check if there's soil to till
        boolean soilToTill = findTillableSoil(villager) != null;
        
        // Check if there are growing crops that could use bone meal
        boolean cropToGrow = findGrowableCrop(villager) != null;
        
        // Prioritize harvesting, then planting, then tilling, then bone meal
        if (cropToHarvest) {
            currentSubtask = FarmingSubtask.HARVEST_CROPS;
        } else if (hasSeeds && findPlantableSoil(villager) != null) {
            currentSubtask = FarmingSubtask.PLANT_SEEDS;
        } else if (soilToTill) {
            currentSubtask = FarmingSubtask.TILL_SOIL;
        } else if (cropToGrow && villager.getInventory().hasAnyOf(List.of(Items.BONE_MEAL))) {
            currentSubtask = FarmingSubtask.BONE_MEAL_CROPS;
        } else {
            currentSubtask = FarmingSubtask.FIND_FARMLAND;
        }
    }
    
    /**
     * Find a target block based on the current subtask
     */
    private BlockPos findTargetForSubtask(Villager villager) {
        switch (currentSubtask) {
            case HARVEST_CROPS:
                return findHarvestableCrop(villager);
            case PLANT_SEEDS:
                return findPlantableSoil(villager);
            case TILL_SOIL:
                return findTillableSoil(villager);
            case BONE_MEAL_CROPS:
                return findGrowableCrop(villager);
            case FIND_FARMLAND:
            default:
                return findFarmland(villager);
        }
    }
    
    /**
     * Perform the farming action based on the current subtask
     */
    private boolean performFarmingAction(Villager villager, Level level) {
        BlockState blockState = level.getBlockState(targetBlock);
        
        switch (currentSubtask) {
            case HARVEST_CROPS:
                // Harvest the crop
                if (level instanceof ServerLevel) {
                    Block.dropResources(blockState, level, targetBlock);
                }
                level.setBlock(targetBlock, Blocks.AIR.defaultBlockState(), 3);
                return true;
                
            case PLANT_SEEDS:
                // Plant seeds
                ItemStack seedStack = findSeedStack(villager);
                if (!seedStack.isEmpty()) {
                    Block cropBlock = getCropBlockFromSeed(seedStack.getItem());
                    if (cropBlock != null) {
                        level.setBlock(targetBlock, cropBlock.defaultBlockState(), 3);
                        seedStack.shrink(1);
                        return true;
                    }
                }
                return false;
                
            case TILL_SOIL:
                // Till the soil
                level.setBlock(targetBlock, Blocks.FARMLAND.defaultBlockState(), 3);
                return true;
                
            case BONE_MEAL_CROPS:
                // Apply bone meal
                ItemStack boneMeal = findBoneMeal(villager);
                if (!boneMeal.isEmpty() && blockState.getBlock() instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock) blockState.getBlock();
                    if (!cropBlock.isMaxAge(blockState)) {
                        int currentAge = blockState.getValue(CropBlock.AGE);
                        level.setBlock(targetBlock, blockState.setValue(CropBlock.AGE, Math.min(currentAge + 1, cropBlock.getMaxAge())), 3);
                        boneMeal.shrink(1);
                        return true;
                    }
                }
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * Find a harvestable crop near the villager
     */
    private BlockPos findHarvestableCrop(Villager villager) {
        Level level = villager.level;
        BlockPos villagerPos = villager.blockPosition();
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        // Search in a radius around the villager
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                BlockPos pos = villagerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                
                // Check if this is a fully grown crop
                if (state.getBlock() instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock) state.getBlock();
                    if (cropBlock.isMaxAge(state)) {
                        potentialTargets.add(pos);
                    }
                }
            }
        }
        
        // Return the closest harvestable crop
        return findClosestBlock(potentialTargets, villagerPos);
    }
    
    /**
     * Find farmland that can be planted near the villager
     */
    private BlockPos findPlantableSoil(Villager villager) {
        Level level = villager.level;
        BlockPos villagerPos = villager.blockPosition();
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        // Search in a radius around the villager
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                BlockPos pos = villagerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                
                // Check if this is farmland with air above it
                if (state.is(Blocks.FARMLAND) && level.getBlockState(pos.above()).is(Blocks.AIR)) {
                    potentialTargets.add(pos.above()); // Target the position above farmland for planting
                }
            }
        }
        
        // Return the closest plantable soil
        return findClosestBlock(potentialTargets, villagerPos);
    }
    
    /**
     * Find soil that can be tilled near the villager
     */
    private BlockPos findTillableSoil(Villager villager) {
        Level level = villager.level;
        BlockPos villagerPos = villager.blockPosition();
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        // Search in a radius around the villager
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                BlockPos pos = villagerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                
                // Check if this is dirt or grass with air above it
                if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) && 
                    level.getBlockState(pos.above()).is(Blocks.AIR)) {
                    potentialTargets.add(pos);
                }
            }
        }
        
        // Return the closest tillable soil
        return findClosestBlock(potentialTargets, villagerPos);
    }
    
    /**
     * Find a growing crop that could use bone meal
     */
    private BlockPos findGrowableCrop(Villager villager) {
        Level level = villager.level;
        BlockPos villagerPos = villager.blockPosition();
        List<BlockPos> potentialTargets = new ArrayList<>();
        
        // Search in a radius around the villager
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                BlockPos pos = villagerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                
                // Check if this is a crop that's not fully grown
                if (state.getBlock() instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock) state.getBlock();
                    if (!cropBlock.isMaxAge(state)) {
                        potentialTargets.add(pos);
                    }
                }
            }
        }
        
        // Return the closest growable crop
        return findClosestBlock(pot