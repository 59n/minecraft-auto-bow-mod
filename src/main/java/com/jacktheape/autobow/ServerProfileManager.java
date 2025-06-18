package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;

public class ServerProfileManager {
    private static final Map<String, ServerProfile> serverProfiles = new HashMap<>();
    private static ServerProfile currentProfile = null;
    private static String currentServerAddress = "";
    private static long lastXpGain = 0;
    private static int xpGainHistory[] = new int[10];
    private static int historyIndex = 0;
    private static boolean isLearningMode = true;

    public static class ServerProfile {
        public String serverName;
        public int diminishingReturnsThreshold = 20000;
        public int timeIntervalMinutes = 10;
        public int optimalSessionLength = 15;
        public int optimalBreakLength = 5;
        public double xpReductionTrigger = 0.5;
        public boolean hasCustomAntiAfk = false;
        public int maxDailySessions = 8;
        public boolean enableMovementVariation = true;
        public long lastXpRateCheck = 0;
        public double baselineXpRate = 0.0;
        public int consecutiveLowXpGains = 0;

        public ServerProfile(String name) {
            this.serverName = name;
        }

 
        public int getAdaptationConfidence() {
            if (consecutiveLowXpGains == 0 && baselineXpRate > 0) {
                return 100; 
            } else if (consecutiveLowXpGains <= 2) {
                return 75; 
            } else if (consecutiveLowXpGains <= 5) {
                return 50; 
            } else {
                return 25; 
            }
        }
    }

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        String serverAddress = getServerAddress(client);
        if (!serverAddress.equals(currentServerAddress)) {
            switchToServer(serverAddress);
        }

        if (isLearningMode && currentProfile != null) {
            monitorXpGains();
        }
    }

    private static String getServerAddress(MinecraftClient client) {
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        } else if (client.isIntegratedServerRunning()) {
            return "singleplayer";
        }
        return "unknown";
    }

    private static void switchToServer(String serverAddress) {
        currentServerAddress = serverAddress;

        if (serverProfiles.containsKey(serverAddress)) {
            currentProfile = serverProfiles.get(serverAddress);
            isLearningMode = false;
        } else {
            currentProfile = createNewServerProfile(serverAddress);
            isLearningMode = true;
        }

        applyProfileToConfig();

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.showStatusMessages) {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("§a[Auto Bow] Switched to server profile: " + currentProfile.serverName),
                    false
            );
        }
    }

    private static ServerProfile createNewServerProfile(String serverAddress) {
        ServerProfile profile = new ServerProfile(serverAddress);
        profile.optimalSessionLength = 10;
        profile.optimalBreakLength = 8;
        profile.xpReductionTrigger = 0.7;
        profile.maxDailySessions = 6;

        serverProfiles.put(serverAddress, profile);
        saveServerProfiles();

        return profile;
    }

    private static void monitorXpGains() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastXpGain > 5000) {
            int simulatedXpGain = getSimulatedXpGain();
            recordXpGain(simulatedXpGain);
            lastXpGain = currentTime;
        }
    }

    private static void recordXpGain(int xpGain) {
        xpGainHistory[historyIndex] = xpGain;
        historyIndex = (historyIndex + 1) % xpGainHistory.length;

        if (currentProfile != null) {
            analyzeXpTrends();
        }
    }

    private static void analyzeXpTrends() {
        double averageXp = calculateAverageXp();

        if (currentProfile.baselineXpRate == 0.0) {
            currentProfile.baselineXpRate = averageXp;
            return;
        }

        double xpRatio = averageXp / currentProfile.baselineXpRate;

        if (xpRatio < currentProfile.xpReductionTrigger) {
            currentProfile.consecutiveLowXpGains++;

            if (currentProfile.consecutiveLowXpGains >= 3) {
                adaptSessionSettings();
            }
        } else {
            currentProfile.consecutiveLowXpGains = 0;
        }
    }

    private static void adaptSessionSettings() {
        if (currentProfile == null) return;

        currentProfile.optimalSessionLength = Math.max(5, currentProfile.optimalSessionLength - 2);
        currentProfile.optimalBreakLength = Math.min(15, currentProfile.optimalBreakLength + 2);

        applyProfileToConfig();

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.showStatusMessages) {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("§e[Auto Bow] Adapted session timing: " +
                            currentProfile.optimalSessionLength + "m farm / " +
                            currentProfile.optimalBreakLength + "m break"),
                    false
            );
        }

        saveServerProfiles();
        currentProfile.consecutiveLowXpGains = 0;
    }

    private static double calculateAverageXp() {
        int sum = 0;
        int count = 0;

        for (int xp : xpGainHistory) {
            if (xp > 0) {
                sum += xp;
                count++;
            }
        }

        return count > 0 ? (double) sum / count : 0.0;
    }

    private static int getSimulatedXpGain() {
        long sessionTime = System.currentTimeMillis() - SessionManager.getSessionStartTime();

        if (sessionTime > 600000) {
            return 50 + (int)(Math.random() * 100);
        } else {
            return 200 + (int)(Math.random() * 300);
        }
    }

    private static void applyProfileToConfig() {
        if (currentProfile == null) return;

        AutoBowConfig config = AutoBowConfig.getInstance();
        config.farmingSessionDuration = currentProfile.optimalSessionLength;
        config.breakDuration = currentProfile.optimalBreakLength;
        config.maxDailyFarmingSessions = currentProfile.maxDailySessions;
        config.enableMovementVariation = currentProfile.enableMovementVariation;

        config.saveConfig();
    }

    private static void saveServerProfiles() {
 
    }


    public static void onDiminishingReturnsDetected() {
        if (currentProfile == null) return;

        // Don't send adaptation messages during break periods
        if (SessionManager.isInBreakPeriod()) {
            AutoBowConfig config = AutoBowConfig.getInstance();
            if (config.enableDebugMode) {
                System.out.println("[Server Profile] Skipping adaptation - currently in break period");
            }
            return;
        }

        AutoBowConfig config = AutoBowConfig.getInstance();

        // Trigger immediate session adaptation only during active farming
        if (SessionManager.isInFarmingSession() && currentProfile.optimalSessionLength > 5) {
            currentProfile.optimalSessionLength = Math.max(5, currentProfile.optimalSessionLength - 3);
            currentProfile.optimalBreakLength = Math.min(20, currentProfile.optimalBreakLength + 3);

            // Apply changes immediately
            applyProfileToConfig();
            saveServerProfiles();

            if (config.showAdaptationMessages) {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§c[Auto Bow] Server adapted: " +
                                currentProfile.optimalSessionLength + "m sessions / " +
                                currentProfile.optimalBreakLength + "m breaks"),
                        false
                );
            }

            if (config.enableDebugMode) {
                System.out.println("[Server Profile] Adapted session timing due to diminishing returns during active farming");
            }
        }
    }

    public static ServerProfile getCurrentProfile() {
        return currentProfile;
    }

    public static boolean isInLearningMode() {
        return isLearningMode;
    }

    public static void forceOptimizeCurrentServer() {
        if (currentProfile != null) {
            isLearningMode = true;
            currentProfile.baselineXpRate = 0.0;
            currentProfile.consecutiveLowXpGains = 0;
        }
    }
}
