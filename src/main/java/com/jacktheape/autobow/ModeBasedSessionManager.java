package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ModeBasedSessionManager {

    private static boolean simpleShootingActive = false;
    private static boolean simpleBreakActive = false;
    private static long simpleSessionStartTime = 0;
    private static long simpleBreakStartTime = 0;


    private static boolean efficiencySessionActive = false;
    private static boolean efficiencyBreakActive = false;
    private static long efficiencySessionStartTime = 0;
    private static long efficiencyBreakStartTime = 0;
    private static int consecutiveLowEfficiencyReadings = 0;
    private static long lastEfficiencyCheck = 0;


    private static boolean learningSessionActive = false;
    private static boolean learningBreakActive = false;
    private static long learningSessionStartTime = 0;
    private static long learningBreakStartTime = 0;


    private static int currentSessionNumber = 0;

    public static void onClientTick(MinecraftClient client) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        switch (config.operatingMode) {
            case "SIMPLE":
                handleSimpleMode(client, config);
                break;
            case "EFFICIENCY":
                handleEfficiencyMode(client, config);
                break;
            case "LEARNING":
                handleLearningMode(client, config);
                break;
        }
    }

    private static void handleSimpleMode(MinecraftClient client, AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();

        if (simpleShootingActive) {
            long shootDuration = currentTime - simpleSessionStartTime;
            long maxShootDuration = config.simpleShootDuration * 60 * 1000;

            if (shootDuration >= maxShootDuration) {

                simpleShootingActive = false;
                simpleBreakActive = true;
                simpleBreakStartTime = currentTime;

                AutoBowHandler.pauseForBreak();

                if (config.showSessionNotifications && client.player != null) {
                    client.player.sendMessage(
                            Text.literal("§e[Auto Bow] Simple Mode: Starting " + config.simpleBreakDuration + " minute break"),
                            false
                    );
                }

                if (config.enableDebugMode) {
                    System.out.println("[Simple Mode] Started break after " + config.simpleShootDuration + " minutes of shooting");
                }
            }
        } else if (simpleBreakActive) {
            long breakDuration = currentTime - simpleBreakStartTime;
            long maxBreakDuration = config.simpleBreakDuration * 60 * 1000;

            if (breakDuration >= maxBreakDuration) {

                simpleBreakActive = false;
                simpleShootingActive = true;
                simpleSessionStartTime = currentTime;

                AutoBowHandler.resumeFromBreak();

                if (config.showSessionNotifications && client.player != null) {
                    client.player.sendMessage(
                            Text.literal("§a[Auto Bow] Simple Mode: Resuming shooting for " + config.simpleShootDuration + " minutes"),
                            false
                    );
                }

                if (config.enableDebugMode) {
                    System.out.println("[Simple Mode] Resumed shooting after " + config.simpleBreakDuration + " minute break");
                }
            }
        } else if (AutoBowHandler.isEnabled()) {

            simpleShootingActive = true;
            simpleSessionStartTime = currentTime;

            if (config.showSessionNotifications && client.player != null) {
                client.player.sendMessage(
                        Text.literal("§a[Auto Bow] Simple Mode: Starting " + config.simpleShootDuration + " minute session"),
                        false
                );
            }

            if (config.enableDebugMode) {
                System.out.println("[Simple Mode] Started initial shooting session");
            }
        }
    }

    private static void handleEfficiencyMode(MinecraftClient client, AutoBowConfig config) {
        if (config.enableDailySessionLimits) {
            checkDailyReset(config);
            if (config.sessionsCompletedToday >= config.maxDailyEfficiencySessions) {
                handleDailyLimitReached(client, config);
                return;
            }
        }

        long currentTime = System.currentTimeMillis();

        if (efficiencySessionActive) {
            if (currentTime - lastEfficiencyCheck > 15000) {
                checkEfficiencyAndAdapt(client, config, currentTime);
                lastEfficiencyCheck = currentTime;
            }

            long sessionDuration = currentTime - efficiencySessionStartTime;
            long maxSessionDuration = config.maxEfficiencySessionDuration * 60 * 1000;
            if (sessionDuration >= maxSessionDuration) {
                endEfficiencySession(client, config, "Maximum duration reached");
            }
        } else if (efficiencyBreakActive) {
            long breakDuration = currentTime - efficiencyBreakStartTime;
            long maxBreakDuration = config.efficiencyBreakDuration * 60 * 1000;

            if (breakDuration >= maxBreakDuration) {
                endEfficiencyBreak(client, config);
            }
        } else if (AutoBowHandler.isEnabled()) {
            startEfficiencySession(client, config);
        }
    }

    private static void handleDailyLimitReached(MinecraftClient client, AutoBowConfig config) {
        if (AutoBowHandler.isEnabled()) {
            AutoBowHandler.disableDueToSessionLimit();

            if (config.showDailyLimitMessages && client.player != null) {
                client.player.sendMessage(
                        Text.literal("§c[Auto Bow] Daily limit reached (" +
                                config.sessionsCompletedToday + "/" + config.maxDailyEfficiencySessions + ") - Auto bow disabled"),
                        false
                );
            }

            if (config.enableDebugMode) {
                System.out.println("[Efficiency Mode] Daily limit reached - auto bow disabled silently");
            }
        }

    }

    private static void endEfficiencySession(MinecraftClient client, AutoBowConfig config, String reason) {
        efficiencySessionActive = false;
        efficiencyBreakActive = true;
        efficiencyBreakStartTime = System.currentTimeMillis();

        long sessionDuration = efficiencyBreakStartTime - efficiencySessionStartTime;

        if (config.enableDailySessionLimits) {
            config.sessionsCompletedToday++;
            config.totalFarmingTimeToday += sessionDuration;
            config.saveConfig();
        }

        AutoBowHandler.pauseForBreak();

        if (config.showSessionNotifications && client.player != null) {
            client.player.sendMessage(
                    Text.literal("§e[Auto Bow] Session ended: " + reason),
                    false
            );
            client.player.sendMessage(
                    Text.literal("§e[Auto Bow] Taking " + config.efficiencyBreakDuration + " minute break"),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Efficiency Mode] Session ended: " + reason);
        }

        consecutiveLowEfficiencyReadings = 0;
    }


    private static void handleLearningMode(MinecraftClient client, AutoBowConfig config) {

        handleEfficiencyMode(client, config);



    }

    private static void checkEfficiencyAndAdapt(MinecraftClient client, AutoBowConfig config, long currentTime) {
        if (!BossbarXpMonitor.isMonitoring()) {
            return;
        }

        double currentEfficiency = BossbarXpMonitor.getEfficiencyPercentage();
        double thresholdPercentage = config.xpReductionThreshold * 100;

        if (config.enableDebugMode) {
            System.out.println("[Efficiency Manager] Current: " + String.format("%.1f%%", currentEfficiency) +
                    " | Threshold: " + String.format("%.1f%%", thresholdPercentage) +
                    " | Consecutive low: " + consecutiveLowEfficiencyReadings);
        }

        if (currentEfficiency < thresholdPercentage) {
            consecutiveLowEfficiencyReadings++;

            if (consecutiveLowEfficiencyReadings >= 2) {
                endEfficiencySession(client, config,
                        String.format("Efficiency dropped to %.1f%% (threshold: %.1f%%)", currentEfficiency, thresholdPercentage));
            }
        } else {
            consecutiveLowEfficiencyReadings = 0;
        }
    }

    private static void startEfficiencySession(MinecraftClient client, AutoBowConfig config) {
        efficiencySessionActive = true;
        efficiencySessionStartTime = System.currentTimeMillis();
        lastEfficiencyCheck = efficiencySessionStartTime;
        consecutiveLowEfficiencyReadings = 0;
        currentSessionNumber++;

        if (BossbarXpMonitor.isMonitoring()) {
            BossbarXpMonitor.resetEfficiencyBaseline();
        }

        if (config.showSessionNotifications && client.player != null) {
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Efficiency session " + currentSessionNumber + " started"),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Efficiency Mode] Started session " + currentSessionNumber);
        }
    }

    private static void endEfficiencyBreak(MinecraftClient client, AutoBowConfig config) {
        efficiencyBreakActive = false;

        AutoBowHandler.resumeFromBreak();

        if (BossbarXpMonitor.isMonitoring()) {
            BossbarXpMonitor.resetEfficiencyBaseline();
        }

        if (config.showSessionNotifications && client.player != null) {
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Break complete. Resuming efficiency monitoring."),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Efficiency Mode] Break complete, resuming");
        }
    }

    private static void checkDailyReset(AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();
        long currentDay = currentTime / (24 * 60 * 60 * 1000);
        long lastDay = config.lastDayReset / (24 * 60 * 60 * 1000);

        if (currentDay > lastDay) {
            config.sessionsCompletedToday = 0;
            config.totalFarmingTimeToday = 0;
            config.lastDayReset = currentTime;
            config.saveConfig();

            if (BossbarXpMonitor.isMonitoring()) {
                BossbarXpMonitor.resetDailyStats();
            }

            if (config.enableDebugMode) {
                System.out.println("[Session Manager] Daily counters reset");
            }
        }
    }


    public static boolean isInSession() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        switch (config.operatingMode) {
            case "SIMPLE": return simpleShootingActive;
            case "EFFICIENCY": return efficiencySessionActive;
            case "LEARNING": return learningSessionActive;
            default: return false;
        }
    }

    public static boolean isInBreak() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        switch (config.operatingMode) {
            case "SIMPLE": return simpleBreakActive;
            case "EFFICIENCY": return efficiencyBreakActive;
            case "LEARNING": return learningBreakActive;
            default: return false;
        }
    }

    public static long getSessionTimeElapsed() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        long currentTime = System.currentTimeMillis();

        switch (config.operatingMode) {
            case "SIMPLE":
                return simpleShootingActive ? currentTime - simpleSessionStartTime : 0;
            case "EFFICIENCY":
                return efficiencySessionActive ? currentTime - efficiencySessionStartTime : 0;
            case "LEARNING":
                return learningSessionActive ? currentTime - learningSessionStartTime : 0;
            default: return 0;
        }
    }

    public static long getBreakTimeRemaining() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        long currentTime = System.currentTimeMillis();

        switch (config.operatingMode) {
            case "SIMPLE":
                if (simpleBreakActive) {
                    long breakDuration = currentTime - simpleBreakStartTime;
                    long maxBreakDuration = config.simpleBreakDuration * 60 * 1000;
                    return Math.max(0, maxBreakDuration - breakDuration);
                }
                break;
            case "EFFICIENCY":
                if (efficiencyBreakActive) {
                    long breakDuration = currentTime - efficiencyBreakStartTime;
                    long maxBreakDuration = config.efficiencyBreakDuration * 60 * 1000;
                    return Math.max(0, maxBreakDuration - breakDuration);
                }
                break;
            case "LEARNING":
                if (learningBreakActive) {
                    long breakDuration = currentTime - learningBreakStartTime;
                    long maxBreakDuration = config.efficiencyBreakDuration * 60 * 1000;
                    return Math.max(0, maxBreakDuration - breakDuration);
                }
                break;
        }
        return 0;
    }

    public static int getCurrentSessionNumber() {
        return currentSessionNumber;
    }

    public static double getCurrentSessionEfficiency() {
        return BossbarXpMonitor.isMonitoring() ? BossbarXpMonitor.getEfficiencyPercentage() : 100.0;
    }

    public static void forceEndSession() {
        simpleShootingActive = false;
        simpleBreakActive = false;
        efficiencySessionActive = false;
        efficiencyBreakActive = false;
        learningSessionActive = false;
        learningBreakActive = false;
        consecutiveLowEfficiencyReadings = 0;

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableDebugMode) {
            System.out.println("[Mode Manager] Force ended all sessions");
        }
    }
}
