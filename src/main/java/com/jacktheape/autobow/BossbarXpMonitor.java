package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;

public class BossbarXpMonitor {
    private static boolean isMonitoring = false;
    private static long lastXpCheck = 0;
    private static double currentXpRate = 0.0;
    private static long totalXpGainedToday = 0;
    private static double baselineXpRate = 0.0;
    private static int consecutiveLowReadings = 0;
    private static int previousCurrentXp = -1;
    private static String lastBossbarText = "";
    private static int tickCounter = 0;
    private static boolean debugInitialized = false;
    private static long sessionBaselineSetTime = 0;
    private static double sessionBaselineXpRate = 0.0;

 
    private static final Pattern PROGRESS_BAR_PATTERN = Pattern.compile("\\[([\\d,]+)\\s*/\\s*([\\d,]+)\\s*xp\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALTERNATIVE_PROGRESS_PATTERN = Pattern.compile("([\\d,]+)\\s*/\\s*([\\d,]+)\\s*xp", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIMPLE_PROGRESS_PATTERN = Pattern.compile("\\[([\\d,]+)\\s*/\\s*([\\d,]+)\\]");

    public static void onClientTick(MinecraftClient client) {
        tickCounter++;

        if (!isMonitoring || client.player == null) {
            return;
        }

 
        if (!debugInitialized) {
            initializeDebugSystem();
            debugInitialized = true;
        }

        long currentTime = System.currentTimeMillis();
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (currentTime - lastXpCheck > config.xpCheckInterval) {
            checkBossbarForXp(client, currentTime);
            lastXpCheck = currentTime;
        }
    }

    private static void initializeDebugSystem() {
        System.out.println("=== EFFICIENCY-BASED XP MONITOR INITIALIZATION ===");
        System.out.println("Monitor starting with efficiency-based session management");
        System.out.println("Target format: [current / total xp]");
        System.out.println("==================================================");
    }

    private static void checkBossbarForXp(MinecraftClient client, long currentTime) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        try {
 
            var bossBarHud = client.inGameHud.getBossBarHud();
            Field bossBarField = bossBarHud.getClass().getDeclaredField("field_2060");
            bossBarField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<?, ClientBossBar> bossBars = (Map<?, ClientBossBar>) bossBarField.get(bossBarHud);

            for (ClientBossBar bossBar : bossBars.values()) {
                String bossBarText = bossBar.getName().getString();

                if (containsProgressBarXp(bossBarText)) {
                    processProgressBarXp(bossBarText, currentTime, config);
                }
            }
        } catch (Exception e) {
            if (config.enableDebugMode) {
                System.out.println("[Bossbar XP Monitor] Error accessing bossbars: " + e.getMessage());
            }
        }
    }

    private static boolean containsProgressBarXp(String text) {
        if (text == null || text.isEmpty()) return false;

        return PROGRESS_BAR_PATTERN.matcher(text).find() ||
                ALTERNATIVE_PROGRESS_PATTERN.matcher(text).find() ||
                SIMPLE_PROGRESS_PATTERN.matcher(text).find();
    }

    private static void processProgressBarXp(String bossBarText, long currentTime, AutoBowConfig config) {
        int currentXp = extractCurrentXpFromProgressBar(bossBarText);

        if (currentXp > 0) {
 
            if (previousCurrentXp != -1 && currentXp > previousCurrentXp) {
                int xpGained = currentXp - previousCurrentXp;

 
                long timeDiff = currentTime - lastXpCheck;
                if (timeDiff > 0) {
                    double rate = (xpGained / (double)timeDiff) * 60000; 

 
                    updateCurrentRate(rate, currentTime);

                    totalXpGainedToday += xpGained;

 
                    checkDiminishingReturns();

                    if (config.enableDebugMode || config.showBossbarDebugInfo) {
                        System.out.println("[Bossbar XP Monitor] *** SUCCESS! *** Detected " + xpGained +
                                " XP gain (" + previousCurrentXp + " -> " + currentXp + "), Rate: " +
                                String.format("%.1f", currentXpRate) + " XP/min, Efficiency: " +
                                String.format("%.1f%%", getEfficiencyPercentage()));
                    }
                }
            } else if (previousCurrentXp == -1) {
                if (config.showBossbarDebugInfo) {
                    System.out.println("[Bossbar Debug] Setting initial XP baseline: " + currentXp);
                }
            } else if (currentXp < previousCurrentXp) {
                if (config.showBossbarDebugInfo) {
                    System.out.println("[Bossbar Debug] Level up detected! XP reset from " + previousCurrentXp + " to " + currentXp);
                }
            }

            previousCurrentXp = currentXp;
        }
    }

    private static int extractCurrentXpFromProgressBar(String text) {
        if (text == null || text.isEmpty()) return 0;

 
        Matcher matcher = PROGRESS_BAR_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                String currentXpStr = matcher.group(1).replace(",", "");
                return Integer.parseInt(currentXpStr);
            } catch (NumberFormatException ignored) {}
        }

 
        matcher = ALTERNATIVE_PROGRESS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                String currentXpStr = matcher.group(1).replace(",", "");
                return Integer.parseInt(currentXpStr);
            } catch (NumberFormatException ignored) {}
        }

 
        matcher = SIMPLE_PROGRESS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                String currentXpStr = matcher.group(1).replace(",", "");
                return Integer.parseInt(currentXpStr);
            } catch (NumberFormatException ignored) {}
        }

        return 0;
    }

    private static void updateCurrentRate(double newRate, long currentTime) {
        if (currentXpRate == 0.0) {
            currentXpRate = newRate;
            baselineXpRate = newRate;

 
            if (sessionBaselineXpRate == 0.0 || (currentTime - sessionBaselineSetTime) > 300000) { 
                sessionBaselineXpRate = newRate;
                sessionBaselineSetTime = currentTime;
            }
        } else {
 
            currentXpRate = (currentXpRate * 0.7) + (newRate * 0.3);
        }
    }

    private static void checkDiminishingReturns() {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (sessionBaselineXpRate > 0) {
            double efficiencyRatio = currentXpRate / sessionBaselineXpRate;

            if (efficiencyRatio < config.xpReductionThreshold) {
                consecutiveLowReadings++;

                if (consecutiveLowReadings >= 3) {
                    triggerAdaptation();
                }
            } else {
                consecutiveLowReadings = 0;
            }
        }
    }

    private static void triggerAdaptation() {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (config.enableServerAdaptation) {
 
            if (config.enableDebugMode) {
                System.out.println("[XP Monitor] Triggering adaptation due to diminishing returns");
            }

            ServerProfileManager.onDiminishingReturnsDetected();

            if (config.showStatusMessages) {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§e[Auto Bow] Diminishing returns detected - session will end soon"),
                        false
                );
            }
        }

        consecutiveLowReadings = 0;
    }

    public static void startMonitoring() {
        System.out.println("=== STARTING EFFICIENCY-BASED XP MONITOR ===");

        isMonitoring = true;
        lastXpCheck = System.currentTimeMillis();
        previousCurrentXp = -1;
        lastBossbarText = "";
        tickCounter = 0;
        debugInitialized = false;

        AutoBowConfig config = AutoBowConfig.getInstance();
        System.out.println("Monitor enabled with efficiency-based sessions");
        System.out.println("XP check interval: " + config.xpCheckInterval + "ms");
        System.out.println("Efficiency threshold: " + (config.xpReductionThreshold * 100) + "%");
        System.out.println("============================================");
    }

    public static void stopMonitoring() {
        System.out.println("=== STOPPING EFFICIENCY-BASED XP MONITOR ===");
        isMonitoring = false;
        System.out.println("Monitor stopped after " + tickCounter + " ticks");
        System.out.println("===========================================");
    }

    public static boolean isMonitoring() {
        return isMonitoring;
    }

    public static double getCurrentXpRate() {
        return currentXpRate;
    }

    public static boolean isDiminishingReturnsActive() {
        if (sessionBaselineXpRate == 0) return false;

        AutoBowConfig config = AutoBowConfig.getInstance();
        return (currentXpRate / sessionBaselineXpRate) < config.xpReductionThreshold;
    }

    public static double getEfficiencyPercentage() {
        if (sessionBaselineXpRate == 0) return 100.0;
        return Math.min(100.0, (currentXpRate / sessionBaselineXpRate) * 100.0);
    }

    public static long getTotalXpToday() {
        return totalXpGainedToday;
    }

    public static void resetDailyStats() {
        totalXpGainedToday = 0;
        baselineXpRate = 0.0;
        consecutiveLowReadings = 0;
        previousCurrentXp = -1;
        lastBossbarText = "";
        tickCounter = 0;
        debugInitialized = false;
        sessionBaselineXpRate = 0.0;
        sessionBaselineSetTime = 0;
    }

    public static void resetEfficiencyBaseline() {
        sessionBaselineXpRate = 0.0;
        sessionBaselineSetTime = 0;
        consecutiveLowReadings = 0;
        currentXpRate = 0.0; 

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableDebugMode) {
            System.out.println("[XP Monitor] *** EFFICIENCY BASELINE RESET *** for new session");
        }
    }


    public static double getSessionBaselineXpRate() {
        return sessionBaselineXpRate;
    }
}
