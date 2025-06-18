package com.jacktheape.autobow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class AutoBowConfig {
    private static AutoBowConfig instance;
    private static final String CONFIG_FILE_NAME = "autobow.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public int minDrawTime = 10;
    public int maxDrawTime = 20;
    public int minCooldownTime = 5;
    public int maxCooldownTime = 15;


    public int durabilityThreshold = 10;
    public boolean enableDurabilityProtection = true;


    public boolean enableMovementVariation = false;
    public int movementIntensity = 1;


    public String operatingMode = "SIMPLE";


    public int simpleShootDuration = 10;
    public int simpleBreakDuration = 5;
    public boolean simpleIgnoreDailyLimits = true;


    public boolean enableEfficiencyMode = false;
    public double xpReductionThreshold = 0.6;
    public int maxEfficiencySessionDuration = 30;
    public int efficiencyBreakDuration = 8;
    public int maxDailyEfficiencySessions = 8;


    public boolean enableLearningMode = false;
    public boolean enableServerAdaptation = true;
    public boolean showAdaptationMessages = true;


    public boolean enableBossbarXpMonitoring = true;
    public boolean showBossbarDebugInfo = false;
    public long xpCheckInterval = 1500;


    public boolean showHudOverlay = true;
    public String hudPosition = "Top Right";
    public int hudScale = 100;
    public boolean showXpRate = true;
    public boolean showEfficiency = true;
    public boolean showSessionInfo = true;
    public boolean showDurability = true;


    public boolean showStatusMessages = true;
    public boolean enableDebugMode = false;
    public boolean useAdvancedRandomization = true;
    public boolean showSessionNotifications = true;


    public boolean enableSessionManagement = true;
    public int farmingSessionDuration = 15;
    public int breakDuration = 8;
    public boolean useEfficiencyBasedSessions = true;
    public int maxSessionDuration = 30;
    public int maxDailyFarmingSessions = 8;


    public int sessionsCompletedToday = 0;
    public long totalFarmingTimeToday = 0;
    public long lastDayReset = System.currentTimeMillis();

    AutoBowConfig() {}

    public static AutoBowConfig getInstance() {
        if (instance == null) {
            instance = new AutoBowConfig();
            instance.loadConfig();
        }
        return instance;
    }

    public static AutoBowConfig createDefault() {
        return new AutoBowConfig();
    }

    private File getConfigFile() {
        String configDir = System.getProperty("user.dir") + "/config";
        return new File(configDir, CONFIG_FILE_NAME);
    }

    public void loadConfig() {
        File configFile = getConfigFile();
        System.out.println("[Config] Loading config from: " + configFile.getAbsolutePath());

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                AutoBowConfig loaded = GSON.fromJson(reader, AutoBowConfig.class);


                this.minDrawTime = loaded.minDrawTime;
                this.maxDrawTime = loaded.maxDrawTime;
                this.minCooldownTime = loaded.minCooldownTime;
                this.maxCooldownTime = loaded.maxCooldownTime;
                this.durabilityThreshold = loaded.durabilityThreshold;
                this.enableDurabilityProtection = loaded.enableDurabilityProtection;


                this.enableMovementVariation = loaded.enableMovementVariation;
                this.movementIntensity = loaded.movementIntensity;


                this.operatingMode = loaded.operatingMode;


                this.simpleShootDuration = loaded.simpleShootDuration;
                this.simpleBreakDuration = loaded.simpleBreakDuration;
                this.simpleIgnoreDailyLimits = loaded.simpleIgnoreDailyLimits;


                this.enableEfficiencyMode = loaded.enableEfficiencyMode;
                this.xpReductionThreshold = loaded.xpReductionThreshold;
                this.maxEfficiencySessionDuration = loaded.maxEfficiencySessionDuration;
                this.efficiencyBreakDuration = loaded.efficiencyBreakDuration;
                this.maxDailyEfficiencySessions = loaded.maxDailyEfficiencySessions;


                this.enableLearningMode = loaded.enableLearningMode;
                this.enableServerAdaptation = loaded.enableServerAdaptation;
                this.showAdaptationMessages = loaded.showAdaptationMessages;


                this.enableBossbarXpMonitoring = loaded.enableBossbarXpMonitoring;
                this.showBossbarDebugInfo = loaded.showBossbarDebugInfo;
                this.xpCheckInterval = loaded.xpCheckInterval;


                this.showHudOverlay = loaded.showHudOverlay;
                this.hudPosition = loaded.hudPosition;
                this.hudScale = loaded.hudScale;
                this.showXpRate = loaded.showXpRate;
                this.showEfficiency = loaded.showEfficiency;
                this.showSessionInfo = loaded.showSessionInfo;
                this.showDurability = loaded.showDurability;


                this.showStatusMessages = loaded.showStatusMessages;
                this.enableDebugMode = loaded.enableDebugMode;
                this.useAdvancedRandomization = loaded.useAdvancedRandomization;
                this.showSessionNotifications = loaded.showSessionNotifications;


                this.enableSessionManagement = loaded.enableSessionManagement;
                this.farmingSessionDuration = loaded.farmingSessionDuration;
                this.breakDuration = loaded.breakDuration;
                this.useEfficiencyBasedSessions = loaded.useEfficiencyBasedSessions;
                this.maxSessionDuration = loaded.maxSessionDuration;
                this.maxDailyFarmingSessions = loaded.maxDailyFarmingSessions;


                this.sessionsCompletedToday = loaded.sessionsCompletedToday;
                this.totalFarmingTimeToday = loaded.totalFarmingTimeToday;
                this.lastDayReset = loaded.lastDayReset;

                System.out.println("[Config] Successfully loaded config");
                System.out.println("[Config] Operating Mode: " + operatingMode);
                System.out.println("[Config] Movement enabled: " + enableMovementVariation + ", Intensity: " + movementIntensity);

            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[Config] Failed to load config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[Config] Config file not found, using default settings");
        }


        validateCriticalOnly();
    }

    public void saveConfig() {
        File configFile = getConfigFile();
        System.out.println("[Config] === SAVING CONFIG ===");
        System.out.println("[Config] Movement enabled: " + enableMovementVariation);
        System.out.println("[Config] Movement intensity: " + movementIntensity);
        System.out.println("[Config] Operating mode: " + operatingMode);
        System.out.println("[Config] Saving to: " + configFile.getAbsolutePath());

        try {
            File parentDir = configFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
                System.out.println("[Config] Successfully saved config");
                fixFilePermissions(configFile.toPath());
            }
        } catch (IOException e) {
            System.err.println("[Config] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[Config] === SAVE COMPLETE ===");
    }


    private void validateCriticalOnly() {

        if (minDrawTime < 1) minDrawTime = 1;
        if (maxDrawTime > 200) maxDrawTime = 200;
        if (maxDrawTime < minDrawTime) maxDrawTime = minDrawTime;

        if (minCooldownTime < 0) minCooldownTime = 0;
        if (maxCooldownTime > 200) maxCooldownTime = 200;
        if (maxCooldownTime < minCooldownTime) maxCooldownTime = minCooldownTime;


        if (durabilityThreshold < 1) durabilityThreshold = 1;
        if (durabilityThreshold > 100) durabilityThreshold = 100;


        if (xpCheckInterval < 100) xpCheckInterval = 100;
        if (xpCheckInterval > 10000) xpCheckInterval = 10000;


        if (hudScale < 50) hudScale = 50;
        if (hudScale > 200) hudScale = 200;


        if (operatingMode == null || (!operatingMode.equals("SIMPLE") && !operatingMode.equals("EFFICIENCY") && !operatingMode.equals("LEARNING"))) {
            operatingMode = "SIMPLE";
        }


        if (hudPosition == null || (!hudPosition.equals("Top Right") && !hudPosition.equals("Top Left") &&
                !hudPosition.equals("Bottom Right") && !hudPosition.equals("Bottom Left"))) {
            hudPosition = "Top Right";
        }


        System.out.println("[Config] Validation complete - Movement settings preserved: enabled=" + enableMovementVariation + ", intensity=" + movementIntensity);
    }


    private void fixFilePermissions(Path path) {
        try {
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(path, perms);
                System.out.println("[Config] Fixed file permissions for: " + path);
            }
        } catch (Exception e) {
            System.err.println("[Config] Could not fix file permissions: " + e.getMessage());
        }
    }


    public void resetToDefaults() {
        AutoBowConfig defaultConfig = createDefault();

        this.minDrawTime = defaultConfig.minDrawTime;
        this.maxDrawTime = defaultConfig.maxDrawTime;
        this.minCooldownTime = defaultConfig.minCooldownTime;
        this.maxCooldownTime = defaultConfig.maxCooldownTime;
        this.durabilityThreshold = defaultConfig.durabilityThreshold;
        this.enableDurabilityProtection = defaultConfig.enableDurabilityProtection;
        this.enableMovementVariation = defaultConfig.enableMovementVariation;
        this.movementIntensity = defaultConfig.movementIntensity;
        this.operatingMode = defaultConfig.operatingMode;
        this.simpleShootDuration = defaultConfig.simpleShootDuration;
        this.simpleBreakDuration = defaultConfig.simpleBreakDuration;
        this.simpleIgnoreDailyLimits = defaultConfig.simpleIgnoreDailyLimits;
        this.enableEfficiencyMode = defaultConfig.enableEfficiencyMode;
        this.xpReductionThreshold = defaultConfig.xpReductionThreshold;
        this.maxEfficiencySessionDuration = defaultConfig.maxEfficiencySessionDuration;
        this.efficiencyBreakDuration = defaultConfig.efficiencyBreakDuration;
        this.maxDailyEfficiencySessions = defaultConfig.maxDailyEfficiencySessions;
        this.enableLearningMode = defaultConfig.enableLearningMode;
        this.enableServerAdaptation = defaultConfig.enableServerAdaptation;
        this.showAdaptationMessages = defaultConfig.showAdaptationMessages;
        this.enableBossbarXpMonitoring = defaultConfig.enableBossbarXpMonitoring;
        this.showBossbarDebugInfo = defaultConfig.showBossbarDebugInfo;
        this.xpCheckInterval = defaultConfig.xpCheckInterval;
        this.showHudOverlay = defaultConfig.showHudOverlay;
        this.hudPosition = defaultConfig.hudPosition;
        this.hudScale = defaultConfig.hudScale;
        this.showXpRate = defaultConfig.showXpRate;
        this.showEfficiency = defaultConfig.showEfficiency;
        this.showSessionInfo = defaultConfig.showSessionInfo;
        this.showDurability = defaultConfig.showDurability;
        this.showStatusMessages = defaultConfig.showStatusMessages;
        this.enableDebugMode = defaultConfig.enableDebugMode;
        this.useAdvancedRandomization = defaultConfig.useAdvancedRandomization;
        this.showSessionNotifications = defaultConfig.showSessionNotifications;


        System.out.println("[Config] Reset to default settings");
    }
}
