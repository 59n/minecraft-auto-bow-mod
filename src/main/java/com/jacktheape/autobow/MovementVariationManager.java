package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class MovementVariationManager {
    private static long lastMovement = 0;
    private static int movementPattern = 0;
    private static boolean isPerformingMovement = false;
    private static int movementCooldown = 0;

    public static void onClientTick(MinecraftClient client) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (!config.enableMovementVariation || client.player == null) {
            return;
        }

 
        if (movementCooldown > 0) {
            movementCooldown--;
            return;
        }

 
        long currentTime = System.currentTimeMillis();
        int baseInterval = getMovementInterval(config.movementIntensity);

        if (currentTime - lastMovement > baseInterval) {
            performMovementVariation(client, config);
        }
    }

    private static int getMovementInterval(int intensity) {
        switch (intensity) {
            case 1: return 45000; 
            case 2: return 30000; 
            case 3: return 20000; 
            default: return 30000;
        }
    }

    public static void performMovementVariation(MinecraftClient client, AutoBowConfig config) {
        if (client.player == null || isPerformingMovement) return;

        isPerformingMovement = true;

        try {
            switch (movementPattern % 6) {
                case 0: 
                    performRotationVariation(client, config);
                    break;
                case 1: 
                    performPitchVariation(client, config);
                    break;
                case 2: 
                    performShootingPause(client, config);
                    break;
                case 3: 
                    performInventorySimulation(client, config);
                    break;
                case 4: 
                    performHotbarVariation(client, config);
                    break;
                case 5: 
                    performCombinationMovement(client, config);
                    break;
            }

            movementPattern++;
            lastMovement = System.currentTimeMillis();

            if (config.enableDebugMode) {
                System.out.println("[Movement Variation] Performed pattern " + (movementPattern - 1) % 6 +
                        " with intensity " + config.movementIntensity);
            }

        } catch (Exception e) {
            if (config.enableDebugMode) {
                System.out.println("[Movement Variation] Error: " + e.getMessage());
            }
        } finally {
            isPerformingMovement = false;
        }
    }

    private static void performRotationVariation(MinecraftClient client, AutoBowConfig config) {
        float yawVariation = getVariationAmount(config.movementIntensity, 15.0f);
        client.player.setYaw(client.player.getYaw() + yawVariation);

 
        movementCooldown = 20; 
    }

    private static void performPitchVariation(MinecraftClient client, AutoBowConfig config) {
        float pitchVariation = getVariationAmount(config.movementIntensity, 10.0f);
        float newPitch = Math.max(-90, Math.min(90, client.player.getPitch() + pitchVariation));
        client.player.setPitch(newPitch);

        movementCooldown = 15; 
    }

    private static void performShootingPause(MinecraftClient client, AutoBowConfig config) {
 
        AutoBowHandler.pauseForBreak();

 
        int pauseDuration = getPauseDuration(config.movementIntensity);

 
        new Thread(() -> {
            try {
                Thread.sleep(pauseDuration);
                AutoBowHandler.resumeFromBreak();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        movementCooldown = pauseDuration / 50; 
    }

    private static void performInventorySimulation(MinecraftClient client, AutoBowConfig config) {
 
        if (client.currentScreen == null) {
 
            movementCooldown = 10; 
        }
    }

    private static void performHotbarVariation(MinecraftClient client, AutoBowConfig config) {
 
        int currentSlot = client.player.getInventory().selectedSlot;
        int newSlot = (currentSlot + 1) % 9;

        client.player.getInventory().selectedSlot = newSlot;

 
        new Thread(() -> {
            try {
                Thread.sleep(100 + (config.movementIntensity * 50));
                if (client.player != null) {
                    client.player.getInventory().selectedSlot = currentSlot;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        movementCooldown = 30; 
    }

    private static void performCombinationMovement(MinecraftClient client, AutoBowConfig config) {
 
        float yawVariation = getVariationAmount(config.movementIntensity, 8.0f);
        float pitchVariation = getVariationAmount(config.movementIntensity, 5.0f);

        client.player.setYaw(client.player.getYaw() + yawVariation);
        float newPitch = Math.max(-90, Math.min(90, client.player.getPitch() + pitchVariation));
        client.player.setPitch(newPitch);

        movementCooldown = 25; 
    }

    private static float getVariationAmount(int intensity, float baseAmount) {
        float multiplier = intensity * 0.5f; 
        float variation = (float) (Math.random() * baseAmount * multiplier * 2 - baseAmount * multiplier);
        return variation;
    }

    private static int getPauseDuration(int intensity) {
        switch (intensity) {
            case 1: return 500; 
            case 2: return 750; 
            case 3: return 1000; 
            default: return 750;
        }
    }

    public static boolean isPerformingMovement() {
        return isPerformingMovement;
    }

    public static void resetMovementPattern() {
        movementPattern = 0;
        lastMovement = 0;
        isPerformingMovement = false;
        movementCooldown = 0;
    }

    public static String getMovementStatus() {
        if (isPerformingMovement) {
            return "Active";
        } else if (movementCooldown > 0) {
            return "Cooldown (" + (movementCooldown / 20.0f) + "s)";
        } else {
            return "Ready";
        }
    }
}
