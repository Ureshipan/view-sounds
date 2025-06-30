package com.prikol.listensounds.config;

import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;
    
    // Основные настройки мода
    public static final ForgeConfigSpec.BooleanValue MOD_ENABLED;
    public static final ForgeConfigSpec.DoubleValue CROUCH_ACTIVATION_DELAY;
    public static final ForgeConfigSpec.ConfigValue<String> DISABLE_KEY;
    
    // Настройка максимального расстояния
    public static final ForgeConfigSpec.IntValue MAX_SOUND_DISTANCE;
    
    // Настройки включения/отключения для каждого типа звука
    public static final ForgeConfigSpec.BooleanValue ENABLE_HOSTILE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NEUTRAL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AMBIENT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VOICE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MUSIC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_RECORDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WEATHER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MASTER;
    
    // Настройки цветов для каждого типа звука
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_HOSTILE;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_NEUTRAL;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_PLAYERS;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_AMBIENT;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_VOICE;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_MUSIC;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_RECORDS;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_WEATHER;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<String> COLOR_MASTER;

    static {
        BUILDER.push("General");
        DEBUG_MODE = BUILDER
                .comment("Enable debug mode for development")
                .define("debugMode", false);
        MOD_ENABLED = BUILDER
                .comment("Enable the View Sounds mod completely")
                .define("modEnabled", true);
        CROUCH_ACTIVATION_DELAY = BUILDER
                .comment("Time in seconds to wait while crouching before activating sound visualization")
                .defineInRange("crouchActivationDelay", 1.0, 0.0, 10.0);
        MAX_SOUND_DISTANCE = BUILDER
                .comment("Maximum distance (in blocks) for sound visualization")
                .defineInRange("maxSoundDistance", 20, 1, 100);
        DISABLE_KEY = BUILDER
                .comment("Key to toggle the View Sounds mod on/off (default: LEFT_ALT)")
                .define("disableKey", "LEFT_ALT");
        BUILDER.pop();

        BUILDER.push("Sound Types");
        
        // Настройки включения/отключения
        ENABLE_HOSTILE = BUILDER
                .comment("Enable visualization for hostile mob sounds")
                .define("enableHostile", true);
        ENABLE_NEUTRAL = BUILDER
                .comment("Enable visualization for neutral mob sounds")
                .define("enableNeutral", true);
        ENABLE_PLAYERS = BUILDER
                .comment("Enable visualization for player sounds")
                .define("enablePlayers", true);
        ENABLE_AMBIENT = BUILDER
                .comment("Enable visualization for ambient sounds")
                .define("enableAmbient", false);
        ENABLE_VOICE = BUILDER
                .comment("Enable visualization for voice sounds")
                .define("enableVoice", false);
        ENABLE_MUSIC = BUILDER
                .comment("Enable visualization for music")
                .define("enableMusic", false);
        ENABLE_RECORDS = BUILDER
                .comment("Enable visualization for record sounds")
                .define("enableRecords", false);
        ENABLE_WEATHER = BUILDER
                .comment("Enable visualization for weather sounds")
                .define("enableWeather", false);
        ENABLE_BLOCKS = BUILDER
                .comment("Enable visualization for block sounds")
                .define("enableBlocks", false);
        ENABLE_MASTER = BUILDER
                .comment("Enable visualization for master sounds")
                .define("enableMaster", false);
        
        // Настройки цветов
        COLOR_HOSTILE = BUILDER
                .comment("Color for hostile mob sounds (hex format, e.g. #FF0000)")
                .define("colorHostile", "#FF0000");
        COLOR_NEUTRAL = BUILDER
                .comment("Color for neutral mob sounds (hex format, e.g. #00FF00)")
                .define("colorNeutral", "#00FF00");
        COLOR_PLAYERS = BUILDER
                .comment("Color for player sounds (hex format, e.g. #FFFFFF)")
                .define("colorPlayers", "#FFFFFF");
        COLOR_AMBIENT = BUILDER
                .comment("Color for ambient sounds (hex format, e.g. #87CEEB)")
                .define("colorAmbient", "#87CEEB");
        COLOR_VOICE = BUILDER
                .comment("Color for voice sounds (hex format, e.g. #FF69B4)")
                .define("colorVoice", "#FF69B4");
        COLOR_MUSIC = BUILDER
                .comment("Color for music (hex format, e.g. #FFD700)")
                .define("colorMusic", "#FFD700");
        COLOR_RECORDS = BUILDER
                .comment("Color for record sounds (hex format, e.g. #FFD700)")
                .define("colorRecords", "#FFD700");
        COLOR_WEATHER = BUILDER
                .comment("Color for weather sounds (hex format, e.g. #4682B4)")
                .define("colorWeather", "#4682B4");
        COLOR_BLOCKS = BUILDER
                .comment("Color for block sounds (hex format, e.g. #8B4513)")
                .define("colorBlocks", "#8B4513");
        COLOR_MASTER = BUILDER
                .comment("Color for master sounds (hex format, e.g. #FFFF00)")
                .define("colorMaster", "#FFFF00");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static boolean isSoundTypeEnabled(SoundSource source) {
        switch (source) {
            case HOSTILE: return ENABLE_HOSTILE.get();
            case NEUTRAL: return ENABLE_NEUTRAL.get();
            case PLAYERS: return ENABLE_PLAYERS.get();
            case AMBIENT: return ENABLE_AMBIENT.get();
            case VOICE: return ENABLE_VOICE.get();
            case MUSIC: return ENABLE_MUSIC.get();
            case RECORDS: return ENABLE_RECORDS.get();
            case WEATHER: return ENABLE_WEATHER.get();
            case BLOCKS: return ENABLE_BLOCKS.get();
            case MASTER: return ENABLE_MASTER.get();
            default: return false;
        }
    }

    public static int getSoundTypeColor(SoundSource source) {
        String colorHex;
        switch (source) {
            case HOSTILE: colorHex = COLOR_HOSTILE.get(); break;
            case NEUTRAL: colorHex = COLOR_NEUTRAL.get(); break;
            case PLAYERS: colorHex = COLOR_PLAYERS.get(); break;
            case AMBIENT: colorHex = COLOR_AMBIENT.get(); break;
            case VOICE: colorHex = COLOR_VOICE.get(); break;
            case MUSIC: colorHex = COLOR_MUSIC.get(); break;
            case RECORDS: colorHex = COLOR_RECORDS.get(); break;
            case WEATHER: colorHex = COLOR_WEATHER.get(); break;
            case BLOCKS: colorHex = COLOR_BLOCKS.get(); break;
            case MASTER: colorHex = COLOR_MASTER.get(); break;
            default: colorHex = "#FFFF00"; break;
        }
        
        try {
            // Убираем # если есть и конвертируем hex в int
            if (colorHex.startsWith("#")) {
                colorHex = colorHex.substring(1);
            }
            return Integer.parseInt(colorHex, 16);
        } catch (NumberFormatException e) {
            // Возвращаем желтый цвет при ошибке
            return 0xFFFF00;
        }
    }
} 