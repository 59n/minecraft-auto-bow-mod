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

 
    public int minDrawTime = 20; 
    public int maxDrawTime = 40; 
    public int minCooldownTime = 10; 
    public int maxCooldownTime = 20; 
    public int durabilityThreshold = 10;
    public boolean enableDurabilityProtection = true;
    public boolean showStatusMessages = true;
    public boolean enableDebugMode = false;
    public boolean showHudOverlay = true;
    public boolean enableAmmoWarnings = true;
    public boolean useAdvancedRandomization = true;
    public int lowAmmoThreshold = 16;
    public boolean enableAfkMode = true; 

    public boolean enableSessionManagement = false; 
    public int farmingSessionDuration = 15; 
    public int breakDuration = 5; 
    public boolean enableMovementVariation = false; 
    public int maxDailyFarmingSessions = 8;
    public boolean enableXpRateMonitoring = false;

    public boolean enableXpMonitoring = false;

    public boolean autoDetectDiminishingReturns = true;
    public int learningSessionCount = 5;
    public boolean saveServerProfiles = true;
    public boolean showAdaptationMessages = true;

    public int xpHistorySize = 20;
    public double adaptationSensitivity = 0.3; 

    public long lastSessionStartTime = 0;
    public long totalFarmingTimeToday = 0;
    public int sessionsCompletedToday = 0;
    public long lastDayReset = 0;
    
    public int movementIntensity = 2; 
    public boolean enableAdvancedMovement = true;

    public boolean enableDetailedXpLogging = false;

    public boolean enableBossbarXpMonitoring = true;
    public boolean showBossbarDebugInfo = false;
    public long xpCheckInterval = 500; 
    public boolean enableServerAdaptation = true;

    public boolean useEfficiencyBasedSessions = true;
    public double xpReductionThreshold = 0.6; 
    public int maxSessionDuration = 30; 
    public boolean showSessionNotifications = true;
    public boolean enableEfficiencyTracking = true;

    public boolean useAdvancedXpPatterns = true;
    public String customXpPattern = "";

    public boolean useEnhancedGui = true;
    public int guiScrollSensitivity = 20;
    public boolean showTooltips = true;
    public boolean useColorCoding = true;
    // HUD Customization Options
    public String hudPosition = "Top Right"; // "Top Right", "Top Left", "Bottom Right", "Bottom Left"
    public int hudScale = 100; // 50-150%
    public boolean showXpRate = true;
    public boolean showEfficiency = true;
    public boolean showSessionInfo = true;
    public boolean showDurability = true;


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

 
    public void validateAndFix() {
        minDrawTime = Math.max(5, Math.min(minDrawTime, 100)); 
        maxDrawTime = Math.max(minDrawTime, Math.min(maxDrawTime, 200)); 
        minCooldownTime = Math.max(0, Math.min(minCooldownTime, 100)); 
        maxCooldownTime = Math.max(minCooldownTime, Math.min(maxCooldownTime, 200)); 
        durabilityThreshold = Math.max(1, Math.min(durabilityThreshold, 100)); 
    }

    public int getRandomDrawTime() {
        if (useAdvancedRandomization) {
            return AdvancedRandomizer.getRandomizedDrawTime();
        } else {
 
            if (minDrawTime == maxDrawTime) return minDrawTime;
            return minDrawTime + (int)(Math.random() * (maxDrawTime - minDrawTime + 1));
        }
    }

    public int getRandomCooldownTime() {
        if (useAdvancedRandomization) {
            return AdvancedRandomizer.getRandomizedCooldownTime();
        } else {
 
            if (minCooldownTime == maxCooldownTime) return minCooldownTime;
            return minCooldownTime + (int)(Math.random() * (maxCooldownTime - minCooldownTime + 1));
        }
    }


}
