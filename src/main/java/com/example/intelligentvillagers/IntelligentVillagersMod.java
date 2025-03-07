package com.example.intelligentvillagers;

import com.example.intelligentvillagers.config.ModConfig;
import com.example.intelligentvillagers.entity.ModEntityTypes;
import com.example.intelligentvillagers.entity.ai.VillagerBrain;
import com.example.intelligentvillagers.event.ModEvents;
import com.example.intelligentvillagers.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(IntelligentVillagersMod.MOD_ID)
public class IntelligentVillagersMod {
    public static final String MOD_ID = "intelligent_villagers";
    public static final Logger LOGGER = LogManager.getLogger();

    public IntelligentVillagersMod() {
        // Register the setup method for modloading
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register mod items, blocks, entities, etc.
        ModItems.register(eventBus);
        ModEntityTypes.register(eventBus);
        
        // Register config
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);
        
        // Register mod setup methods
        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ModEvents());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialization code that should run on both client and server
        LOGGER.info("Intelligent Villagers mod initialization started");
        
        // Initialize the villager brain system
        VillagerBrain.init();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Client-specific initialization code
    }
    
    // Helper method to create resource locations with the mod ID
    public static ResourceLocation location(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}