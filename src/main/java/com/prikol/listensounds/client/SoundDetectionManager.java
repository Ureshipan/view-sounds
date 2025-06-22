package com.prikol.listensounds.client;

import com.mojang.logging.LogUtils;
import com.prikol.listensounds.ViewSoundsMod;
import com.prikol.listensounds.config.ModConfig;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ViewSoundsMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class SoundDetectionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SOUND_DURATION = 3000; // 3 seconds in milliseconds

    private static final List<SoundInfo> activeSounds = new ArrayList<>();

    public static class SoundInfo {
        public final Vec3 position;
        public final String soundName;
        public final long timestamp;
        public final float volume;
        public final SoundSource soundSource;

        public SoundInfo(Vec3 position, String soundName, float volume, SoundSource soundSource) {
            this.position = position;
            this.soundName = soundName;
            this.timestamp = System.currentTimeMillis();
            this.volume = volume;
            this.soundSource = soundSource;
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!ViewSoundsMod.isEnabled || !ViewSoundsMod.isCrouching) {
            return;
        }

        // Only track sounds with known positions
        if (event.getSound() != null && event.getSound().getLocation() != null) {
            Vec3 soundPos = new Vec3(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ());
            String soundName = event.getSound().getLocation().toString();
            
            // Безопасно получаем громкость с проверкой на null
            float volume = 1.0f; // Значение по умолчанию
            try {
                if (event.getSound() != null) {
                    volume = event.getSound().getVolume();
                }
            } catch (Exception e) {
                // Если не удалось получить громкость, используем значение по умолчанию
                volume = 1.0f;
            }

            // Add sound to active list
            activeSounds.add(new SoundInfo(soundPos, soundName, volume, event.getSound().getSource()));

            if (ModConfig.DEBUG_MODE.get()) {
                LOGGER.info("Sound detected: {} at {} with volume {}", soundName, soundPos, volume);
            }
        }
    }

    public static List<SoundInfo> getActiveSounds() {
        // Remove expired sounds
        long currentTime = System.currentTimeMillis();
        activeSounds.removeIf(sound -> currentTime - sound.timestamp > SOUND_DURATION);
        
        return new ArrayList<>(activeSounds);
    }

    public static void clearSounds() {
        activeSounds.clear();
    }
} 