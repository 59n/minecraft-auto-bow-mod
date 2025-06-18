package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SessionManager {
    private static boolean inFarmingSession = false;
    private static boolean inBreakPeriod = false;
    private static long sessionStartTime = 0;
    private static long breakStartTime = 0;
    private static int currentSessionNumber = 0;
    private static double sessionStartEfficiency = 100.0;
    private static int consecutiveLowEfficiencyReadings = 0;
    private static long lastEfficiencyCheck = 0;
    private static boolean efficiencyBasedMode = true;
    private static boolean sessionEndingDueToEfficiency = false;

    public static void onClientTick(MinecraftClient client) {
        AutoBowConfig config = AutoBowConfig.getInstance();

 
        if (!config.enableSessionManagement) {
            return;
        }

 
        checkDailyReset(config);

 
        if (config.sessionsCompletedToday >= config.maxDailyFarmingSessions) {
            if (config.showSessionNotifications && client.player != null) {
                client.player.sendMessage(
                        Text.literal("§c[Auto Bow] Daily farming limit reached. Sessions will resume tomorrow."),
                        false
                );
            }
            AutoBowHandler.disableDueToSessionLimit();
            return;
        }

 
        if (inFarmingSession) {
            handleEfficiencyBasedSession(client, config);
        } else if (inBreakPeriod) {
            handleBreakPeriod(client, config);
        } else if (AutoBowHandler.isEnabled() && !sessionEndingDueToEfficiency) {
 
            startFarmingSession(client, config);
        }
    }

    private static void handleEfficiencyBasedSession(MinecraftClient client, AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - sessionStartTime;

 
        if (currentTime - lastEfficiencyCheck > 15000) {
            checkEfficiencyAndAdapt(client, config, sessionDuration);
            lastEfficiencyCheck = currentTime;
        }

 
        if (config.enableMovementVariation && sessionDuration % 30000 == 0) {
            addMovementVariation(client);
        }

 
        long maxSessionDuration = config.maxSessionDuration * 60 * 1000;
        if (sessionDuration >= maxSessionDuration) {
            if (config.enableDebugMode) {
                System.out.println("[Session Manager] Maximum session duration reached, forcing break");
            }
            endFarmingSessionDueToEfficiency(client, config, "Maximum duration reached");
        }
    }

    private static void checkEfficiencyAndAdapt(MinecraftClient client, AutoBowConfig config, long sessionDuration) {
        if (!BossbarXpMonitor.isMonitoring()) {
            return; 
        }

        double currentEfficiency = BossbarXpMonitor.getEfficiencyPercentage();
        double thresholdPercentage = config.xpReductionThreshold * 100;

        if (config.enableDebugMode) {
            System.out.println("[Session Manager] Efficiency check: " + String.format("%.1f%%", currentEfficiency) +
                    " | Threshold: " + String.format("%.1f%%", thresholdPercentage) +
                    " | Session duration: " + (sessionDuration / 60000) + "m" +
                    " | Consecutive low readings: " + consecutiveLowEfficiencyReadings);
        }

 
        if (currentEfficiency < thresholdPercentage) {
            consecutiveLowEfficiencyReadings++;

            if (config.enableDebugMode) {
                System.out.println("[Session Manager] Low efficiency detected (" + consecutiveLowEfficiencyReadings + "/2) - " +
                        String.format("%.1f%% < %.1f%%", currentEfficiency, thresholdPercentage));
            }

 
            if (consecutiveLowEfficiencyReadings >= 2) {
                if (config.enableDebugMode) {
                    System.out.println("[Session Manager] TRIGGERING SESSION END due to low efficiency");
                }
                endFarmingSessionDueToEfficiency(client, config,
                        String.format("Efficiency dropped to %.1f%% (threshold: %.1f%%)", currentEfficiency, thresholdPercentage));
            }
        } else {
 
            if (consecutiveLowEfficiencyReadings > 0) {
                if (config.enableDebugMode) {
                    System.out.println("[Session Manager] Efficiency recovered, resetting counter");
                }
                consecutiveLowEfficiencyReadings = 0;
            }
        }

 
        if (config.showSessionNotifications && sessionDuration % 120000 == 0) {
            client.player.sendMessage(
                    Text.literal(String.format("§7[Auto Bow] Session efficiency: %.1f%% | Duration: %dm | Threshold: %.1f%%",
                            currentEfficiency, sessionDuration / 60000, thresholdPercentage)),
                    false
            );
        }
    }

    private static void handleBreakPeriod(MinecraftClient client, AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();
        long breakDuration = currentTime - breakStartTime;
        long maxBreakDuration = config.breakDuration * 60 * 1000;

        if (breakDuration >= maxBreakDuration) {
            endBreakPeriod(client, config);
        }

 
        if (config.showSessionNotifications && breakDuration % 60000 == 0) {
            int remainingMinutes = (int) ((maxBreakDuration - breakDuration) / 60000);
            if (remainingMinutes > 0 && client.player != null) {
                client.player.sendMessage(
                        Text.literal("§e[Auto Bow] Break time remaining: " + remainingMinutes + " minutes"),
                        false
                );
            }
        }
    }

    private static void startFarmingSession(MinecraftClient client, AutoBowConfig config) {
        inFarmingSession = true;
        inBreakPeriod = false;
        sessionEndingDueToEfficiency = false;
        sessionStartTime = System.currentTimeMillis();
        lastEfficiencyCheck = sessionStartTime;
        currentSessionNumber++;
        consecutiveLowEfficiencyReadings = 0;

 
        if (BossbarXpMonitor.isMonitoring()) {
            BossbarXpMonitor.resetEfficiencyBaseline();
            sessionStartEfficiency = 100.0;
        }

        if (config.showSessionNotifications && client.player != null) {
            String sessionType = config.useEfficiencyBasedSessions ? "efficiency-based" : "timed";
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Starting " + sessionType + " farming session " + currentSessionNumber),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Session Manager] Started efficiency-based farming session " + currentSessionNumber +
                    " | Threshold: " + (config.xpReductionThreshold * 100) + "%");
        }
    }

    private static void endFarmingSessionDueToEfficiency(MinecraftClient client, AutoBowConfig config, String reason) {
        if (sessionEndingDueToEfficiency) {
            return; 
        }

        sessionEndingDueToEfficiency = true;
        inFarmingSession = false;
        inBreakPeriod = true;
        breakStartTime = System.currentTimeMillis();

        long sessionDuration = breakStartTime - sessionStartTime;

 
        config.sessionsCompletedToday++;
        config.totalFarmingTimeToday += sessionDuration;
        config.saveConfig();

 
        AutoBowHandler.pauseForBreak();

 
        double finalEfficiency = BossbarXpMonitor.isMonitoring() ? BossbarXpMonitor.getEfficiencyPercentage() : 0.0;
        long totalXpGained = BossbarXpMonitor.isMonitoring() ? BossbarXpMonitor.getTotalXpToday() : 0;

        if (config.showSessionNotifications && client.player != null) {
            client.player.sendMessage(
                    Text.literal(String.format("§e[Auto Bow] Session %d ended: %s", currentSessionNumber, reason)),
                    false
            );
            client.player.sendMessage(
                    Text.literal(String.format("§7Duration: %dm | Final efficiency: %.1f%% | Total XP today: %d",
                            sessionDuration / 60000, finalEfficiency, totalXpGained)),
                    false
            );
            client.player.sendMessage(
                    Text.literal("§e[Auto Bow] Taking " + config.breakDuration + " minute break to restore XP rates"),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Session Manager] *** SESSION ENDED *** Session " + currentSessionNumber +
                    " after " + (sessionDuration / 60000) + " minutes due to: " + reason +
                    " | Final efficiency: " + String.format("%.1f%%", finalEfficiency));
        }

 
        consecutiveLowEfficiencyReadings = 0;
    }

    private static void endBreakPeriod(MinecraftClient client, AutoBowConfig config) {
        inBreakPeriod = false;
        sessionEndingDueToEfficiency = false; 

 
        AutoBowHandler.resumeFromBreak();

 
        if (BossbarXpMonitor.isMonitoring()) {
            BossbarXpMonitor.resetEfficiencyBaseline();
        }

        if (config.showSessionNotifications && client.player != null) {
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Break complete. XP rates should be restored. Resuming farming."),
                    false
            );
        }

        if (config.enableDebugMode) {
            System.out.println("[Session Manager] Break period ended, resuming efficiency-based farming");
        }
    }

    private static void addMovementVariation(MinecraftClient client) {
        if (client.player == null) return;

        try {
 
            float yawVariation = (float) (Math.random() * 10 - 5); 
            float pitchVariation = (float) (Math.random() * 6 - 3); 

            client.player.setYaw(client.player.getYaw() + yawVariation);
            client.player.setPitch(client.player.getPitch() + pitchVariation);

        } catch (Exception e) {
 
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
                System.out.println("[Session Manager] Daily session counters reset");
            }
        }
    }

    public static boolean isInFarmingSession() {
        return inFarmingSession;
    }

    public static boolean isInBreakPeriod() {
        return inBreakPeriod;
    }

    public static int getCurrentSessionNumber() {
        return currentSessionNumber;
    }

    public static long getSessionTimeRemaining(AutoBowConfig config) {
        if (!inFarmingSession) return 0;

 
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        return sessionDuration;
    }

    public static long getBreakTimeRemaining(AutoBowConfig config) {
        if (!inBreakPeriod) return 0;

        long breakDuration = System.currentTimeMillis() - breakStartTime;
        long maxBreakDuration = config.breakDuration * 60 * 1000;
        return Math.max(0, maxBreakDuration - breakDuration);
    }

    public static long getSessionStartTime() {
        return sessionStartTime;
    }

    public static void forceEndSession() {
        inFarmingSession = false;
        inBreakPeriod = false;
        sessionStartTime = 0;
        breakStartTime = 0;
        consecutiveLowEfficiencyReadings = 0;
        sessionEndingDueToEfficiency = false;
    }

    public static double getCurrentSessionEfficiency() {
        return BossbarXpMonitor.isMonitoring() ? BossbarXpMonitor.getEfficiencyPercentage() : 100.0;
    }

    public static boolean isEfficiencyBasedMode() {
        return efficiencyBasedMode;
    }

    public static boolean isSessionEndingDueToEfficiency() {
        return sessionEndingDueToEfficiency;
    }
}
