package com.jacktheape.autobow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AutoBowConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "autobow.json");

    // Default configuration values
    public int minDrawTime = 20; // 1 second (20 ticks)
    public int maxDrawTime = 40; // 2 seconds (40 ticks)
    public int minCooldownTime = 10; // 0.5 seconds (10 ticks)
    public int maxCooldownTime = 20; // 1 second (20 ticks)
    public int durabilityThreshold = 10;
    public boolean enableDurabilityProtection = true;
    public boolean showStatusMessages = true;
    public boolean enableDebugMode = false;
    public boolean showHudOverlay = true;
    public boolean enableAmmoWarnings = true;
    public boolean useAdvancedRandomization = true;
    public int lowAmmoThreshold = 16;
    public boolean enableAfkMode = true; // Allow shooting when chat is open or alt-tabbed


    // Singleton instance
    private static AutoBowConfig instance;

    public static AutoBowConfig getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    private static AutoBowConfig loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                AutoBowConfig config = GSON.fromJson(reader, AutoBowConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                System.err.println("[Auto Bow] Failed to load config: " + e.getMessage());
            }
        }

        // Return default config if loading fails
        AutoBowConfig defaultConfig = new AutoBowConfig();
        defaultConfig.saveConfig();
        return defaultConfig;
    }

    public void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[Auto Bow] Failed to save config: " + e.getMessage());
        }
    }

    // Validation methods
    public void validateAndFix() {
        minDrawTime = Math.max(5, Math.min(minDrawTime, 100)); // 0.25-5 seconds
        maxDrawTime = Math.max(minDrawTime, Math.min(maxDrawTime, 200)); // Up to 10 seconds
        minCooldownTime = Math.max(0, Math.min(minCooldownTime, 100)); // 0-5 seconds
        maxCooldownTime = Math.max(minCooldownTime, Math.min(maxCooldownTime, 200)); // Up to 10 seconds
        durabilityThreshold = Math.max(1, Math.min(durabilityThreshold, 100)); // 1-100
    }

    public int getRandomDrawTime() {
        if (useAdvancedRandomization) {
            return AdvancedRandomizer.getRandomizedDrawTime();
        } else {
            // Fallback to simple randomization
            if (minDrawTime == maxDrawTime) return minDrawTime;
            return minDrawTime + (int)(Math.random() * (maxDrawTime - minDrawTime + 1));
        }
    }

    public int getRandomCooldownTime() {
        if (useAdvancedRandomization) {
            return AdvancedRandomizer.getRandomizedCooldownTime();
        } else {
            // Fallback to simple randomization
            if (minCooldownTime == maxCooldownTime) return minCooldownTime;
            return minCooldownTime + (int)(Math.random() * (maxCooldownTime - minCooldownTime + 1));
        }
    }


}
