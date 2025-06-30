package com.prikol.listensounds;

import com.mojang.logging.LogUtils;
import com.prikol.listensounds.client.ClientEventHandler;
import com.prikol.listensounds.config.ModConfig;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ViewSoundsMod.MODID)
public class ViewSoundsMod {
    public static final String MODID = "view_sounds";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static boolean isEnabled = false;
    public static boolean isCrouching = false;
    public static boolean isAutoEnabled = false;
    public static boolean isManuallyDisabled = false;
    private static int crouchTimer = 0;

    public ViewSoundsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.SPEC);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register config event
        modEventBus.addListener(this::onModConfigEvent);

        LOGGER.info("View Sounds mod initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("View Sounds mod common setup");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("View Sounds mod client setup");
            
            // Register client events
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }

    @SubscribeEvent
    public void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            LOGGER.info("View Sounds config loaded");
        }
    }

    public static void setCrouching(boolean crouching) {
        if (isCrouching != crouching) {
            isCrouching = crouching;
            if (!crouching) {
                isAutoEnabled = false;
                crouchTimer = 0;
                com.prikol.listensounds.client.SoundDetectionManager.clearSounds();
            }
        }
    }
    
    public static void updateCrouchTimer() {
        // Проверяем, включен ли мод вообще
        if (!ModConfig.MOD_ENABLED.get() || isManuallyDisabled) {
            isEnabled = false;
            isAutoEnabled = false;
            crouchTimer = 0;
            return;
        }
        
        if (isCrouching) {
            crouchTimer++;
            int requiredTicks = (int)(ModConfig.CROUCH_ACTIVATION_DELAY.get() * 20.0f); // 20 тиков в секунде
            
            if (crouchTimer >= requiredTicks && !isAutoEnabled) {
                isAutoEnabled = true;
                isEnabled = true;
                LOGGER.info("View Sounds visualization auto-enabled after " + ModConfig.CROUCH_ACTIVATION_DELAY.get() + " seconds of crouching");
            }
        } else {
            crouchTimer = 0;
            isAutoEnabled = false;
            isEnabled = false;
        }
    }
    
    public static void toggleMod() {
        isManuallyDisabled = !isManuallyDisabled;
        if (isManuallyDisabled) {
            isEnabled = false;
            isAutoEnabled = false;
            crouchTimer = 0;
            LOGGER.info("View Sounds mod manually disabled");
        } else {
            LOGGER.info("View Sounds mod manually enabled");
        }
    }
} 