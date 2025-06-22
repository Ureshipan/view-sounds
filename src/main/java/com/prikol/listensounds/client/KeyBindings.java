package com.prikol.listensounds.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import com.prikol.listensounds.ViewSoundsMod;
import com.prikol.listensounds.config.ModConfig;

@Mod.EventBusSubscriber(modid = ViewSoundsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static KeyMapping DISABLE_MOD;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        DISABLE_MOD = new KeyMapping(
                "key.view_sounds.disable",
                GLFW.GLFW_KEY_LEFT_ALT, // Левый Alt по умолчанию
                "key.categories.view_sounds"
        );
        event.register(DISABLE_MOD);
    }

    public static void register() {
        // This method is called from the main mod class
        // The actual registration happens in the event above
    }
} 