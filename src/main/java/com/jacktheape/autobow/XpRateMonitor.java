package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;

public class XpRateMonitor {
    private static boolean isMonitoring = false;
    private static long lastXpCheck = 0;
    private static double currentXpRate = 0.0;
    private static long totalXpGainedToday = 0;
    private static double baselineXpRate = 0.0;
    private static int consecutiveLowReadings = 0;
    private static Map<String, Integer> previousXpValues = new HashMap<>();
    private static String lastBossbarText = "";

 
    private static final Pattern XP_GAIN_PATTERN = Pattern.compile(".*?\\+(\\d+).*?XP.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARCHERY_XP_PATTERN = Pattern.compile(".*?Archery.*?(\\d+).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERAL_XP_PATTERN = Pattern.compile(".*?(\\d+)\\s*XP.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*?\\+(\\d+).*");

    public static void onClientTick(MinecraftClient client) {
        if (!isMonitoring || client.player == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (currentTime - lastXpCheck > config.xpCheckInterval) {
            checkBossbarForXp(client, currentTime);
            lastXpCheck = currentTime;
        }
    }

    private static void checkBossbarForXp(MinecraftClient client, long currentTime) {
        try {
 
            var bossBarHud = client.inGameHud.getBossBarHud();

 
            Field bossBarField = bossBarHud.getClass().getDeclaredField("bossBars");
            bossBarField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<?, ClientBossBar> bossBars = (Map<?, ClientBossBar>) bossBarField.get(bossBarHud);

            AutoBowConfig config = AutoBowConfig.getInstance();

            for (ClientBossBar bossBar : bossBars.values()) {
                String bossBarText = bossBar.getName().getString();

                if (config.showBossbarDebugInfo) {
                    System.out.println("[Bossbar Debug] Found bossbar: '" + bossBarText + "'");
                }

 
                if (containsXpInfo(bossBarText) && !bossBarText.equals(lastBossbarText)) {
                    processXpBossbar(bossBarText, currentTime);
                    lastBossbarText = bossBarText;
                }
            }
        } catch (NoSuchFieldException e) {
 
            tryAlternativeFieldAccess(client, currentTime);
        } catch (Exception e) {
            AutoBowConfig config = AutoBowConfig.getInstance();
            if (config.enableDebugMode) {
                System.out.println("[Bossbar XP Monitor] Error accessing bossbars: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void tryAlternativeFieldAccess(MinecraftClient client, long currentTime) {
        try {
            var bossBarHud = client.inGameHud.getBossBarHud();

 
            String[] possibleFieldNames = {"bossBars", "field_2068", "bars"};

            for (String fieldName : possibleFieldNames) {
                try {
                    Field field = bossBarHud.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    Map<?, ClientBossBar> bossBars = (Map<?, ClientBossBar>) field.get(bossBarHud);

                    AutoBowConfig config = AutoBowConfig.getInstance();
                    if (config.showBossbarDebugInfo) {
                        System.out.println("[Bossbar Debug] Successfully accessed field: " + fieldName);
                    }

                    for (ClientBossBar bossBar : bossBars.values()) {
                        String bossBarText = bossBar.getName().getString();

                        if (containsXpInfo(bossBarText) && !bossBarText.equals(lastBossbarText)) {
                            processXpBossbar(bossBarText, currentTime);
                            lastBossbarText = bossBarText;
                        }
                    }
                    return; 
                } catch (NoSuchFieldException ignored) {
 
                }
            }
        } catch (Exception e) {
            AutoBowConfig config = AutoBowConfig.getInstance();
            if (config.enableDebugMode) {
                System.out.println("[Bossbar XP Monitor] Alternative access failed: " + e.getMessage());
            }
        }
    }

    private static boolean containsXpInfo(String text) {
        if (text == null || text.isEmpty()) return false;

        String lowerText = text.toLowerCase();
        return lowerText.contains("xp") ||
                lowerText.contains("archery") ||
                lowerText.contains("experience") ||
                (lowerText.contains("+") && text.matches(".*\\d+.*"));
    }

    private static void processXpBossbar(String bossBarText, long currentTime) {
        AutoBowConfig config = AutoBowConfig.getInstance();

 
        int xpGained = extractXpFromText(bossBarText);

        if (xpGained > 0) {
 
            long timeDiff = currentTime - lastXpCheck;
            if (timeDiff > 0) {
                double rate = (xpGained / (double)timeDiff) * 60000; 

 
                updateCurrentRate(rate);

                totalXpGainedToday += xpGained;

 
                checkDiminishingReturns();

                if (config.enableDebugMode || config.showBossbarDebugInfo) {
                    System.out.println("[Bossbar XP Monitor] Detected " + xpGained +
                            " XP from: '" + bossBarText + "', Rate: " +
                            String.format("%.1f", currentXpRate) + " XP/min");
                }
            }
        } else if (config.showBossbarDebugInfo) {
            System.out.println("[Bossbar Debug] No XP extracted from: '" + bossBarText + "'");
        }
    }

    private static int extractXpFromText(String text) {
        if (text == null || text.isEmpty()) return 0;

 
        Matcher matcher = XP_GAIN_PATTERN.matcher(text);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

 
        matcher = ARCHERY_XP_PATTERN.matcher(text);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

 
        matcher = GENERAL_XP_PATTERN.matcher(text);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

 
        matcher = NUMBER_PATTERN.matcher(text);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        return 0;
    }

    private static void updateCurrentRate(double newRate) {
        if (currentXpRate == 0.0) {
            currentXpRate = newRate;
            baselineXpRate = newRate;
        } else {
 
            currentXpRate = (currentXpRate * 0.7) + (newRate * 0.3);
        }
    }

    private static void checkDiminishingReturns() {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (baselineXpRate > 0) {
            double efficiencyRatio = currentXpRate / baselineXpRate;

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
            ServerProfileManager.onDiminishingReturnsDetected();

            if (config.showStatusMessages) {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§e[Auto Bow] Diminishing returns detected from bossbar - adapting session timing"),
                        false
                );
            }
        }

        consecutiveLowReadings = 0;
    }

    public static void startMonitoring() {
        isMonitoring = true;
        lastXpCheck = System.currentTimeMillis();
        previousXpValues.clear();
        lastBossbarText = "";

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableDebugMode) {
            System.out.println("[Bossbar XP Monitor] Started monitoring bossbar XP changes");
        }
    }

    public static void stopMonitoring() {
        isMonitoring = false;

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableDebugMode) {
            System.out.println("[Bossbar XP Monitor] Stopped monitoring bossbar XP changes");
        }
    }

    public static boolean isMonitoring() {
        return isMonitoring;
    }

    public static double getCurrentXpRate() {
        return currentXpRate;
    }

    public static boolean isDiminishingReturnsActive() {
        if (baselineXpRate == 0) return false;

        AutoBowConfig config = AutoBowConfig.getInstance();
        return (currentXpRate / baselineXpRate) < config.xpReductionThreshold;
    }

    public static double getEfficiencyPercentage() {
        if (baselineXpRate == 0) return 100.0;
        return Math.min(100.0, (currentXpRate / baselineXpRate) * 100.0);
    }

    public static long getTotalXpToday() {
        return totalXpGainedToday;
    }

    public static void resetDailyStats() {
        totalXpGainedToday = 0;
        baselineXpRate = 0.0;
        consecutiveLowReadings = 0;
        previousXpValues.clear();
        lastBossbarText = "";
    }
}
