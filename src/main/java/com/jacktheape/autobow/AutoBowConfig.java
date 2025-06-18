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

    public String hudPosition = "Top Right";
    public int hudScale = 100;
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

    public void validateAndFix() {

        if (minDrawTime < 5) minDrawTime = 5;
        if (maxDrawTime > 100) maxDrawTime = 100;
        if (maxDrawTime < minDrawTime) maxDrawTime = minDrawTime;

        if (minCooldownTime < 0) minCooldownTime = 0;
        if (maxCooldownTime > 100) maxCooldownTime = 100;
        if (maxCooldownTime < minCooldownTime) maxCooldownTime = minCooldownTime;


        if (durabilityThreshold < 1) durabilityThreshold = 1;
        if (durabilityThreshold > 100) durabilityThreshold = 100;


        if (xpCheckInterval < 500) xpCheckInterval = 500;
        if (xpCheckInterval > 10000) xpCheckInterval = 5000;

        if (xpReductionThreshold < 0.3) xpReductionThreshold = 0.3;
        if (xpReductionThreshold > 0.9) xpReductionThreshold = 0.9;


        if (breakDuration < 1) breakDuration = 1;
        if (breakDuration > 30) breakDuration = 30;

        if (maxSessionDuration < 10) maxSessionDuration = 10;
        if (maxSessionDuration > 120) maxSessionDuration = 120;

        if (maxDailyFarmingSessions < 1) maxDailyFarmingSessions = 1;
        if (maxDailyFarmingSessions > 50) maxDailyFarmingSessions = 50;


        if (movementIntensity < 1) movementIntensity = 1;
        if (movementIntensity > 3) movementIntensity = 3;



        if (hudScale < 50) hudScale = 50;
        if (hudScale > 200) hudScale = 200;

        if (!hudPosition.equals("Top Right") && !hudPosition.equals("Top Left") &&
                !hudPosition.equals("Bottom Right") && !hudPosition.equals("Bottom Left")) {
            hudPosition = "Top Right";
        }

        System.out.println("[Config Debug] Validated settings - Movement enabled: " + enableMovementVariation +
                ", Intensity: " + movementIntensity);
    }


    public void saveConfig() {
        try {
            System.out.println("[Config Debug] Saving config - Movement enabled: " + enableMovementVariation +
                    ", Intensity: " + movementIntensity +
                    ", XP interval: " + xpCheckInterval +
                    ", Break duration: " + breakDuration);




            System.out.println("[Config Debug] Config saved successfully");
        } catch (Exception e) {
            System.err.println("[Config Error] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
