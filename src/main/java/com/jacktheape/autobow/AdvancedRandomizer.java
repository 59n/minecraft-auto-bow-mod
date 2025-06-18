package com.jacktheape.autobow;

import java.util.Random;

public class AdvancedRandomizer {
    private static final Random random = new Random();
    private static long lastShotTime = 0;
    private static int consecutiveFastShots = 0;
    private static int consecutiveSlowShots = 0;
    private static double currentBias = 0.5; 

 
    private static final double GAUSSIAN_MEAN = 0.5;
    private static final double GAUSSIAN_STDDEV = 0.15;

    public static int getRandomizedDrawTime() {
        AutoBowConfig config = AutoBowConfig.getInstance();

 
        double normalizedValue = generateNormalizedValue();

 
        double microVariation = (random.nextGaussian() * 0.1) * 2;
        normalizedValue = Math.max(0.0, Math.min(1.0, normalizedValue + microVariation));

 
        int range = config.maxDrawTime - config.minDrawTime;
        int drawTime = config.minDrawTime + (int)(normalizedValue * range);

 
        drawTime = applyPatternBreaking(drawTime, config);

        if (config.enableDebugMode) {
            System.out.println("[Advanced Randomizer] Draw time: " + drawTime +
                    " (normalized: " + String.format("%.3f", normalizedValue) + ")");
        }

        return drawTime;
    }

    public static int getRandomizedCooldownTime() {
        AutoBowConfig config = AutoBowConfig.getInstance();

 
        double normalizedValue = generateNormalizedValue();

 
        normalizedValue = 1.0 - normalizedValue;

 
        double microVariation = (random.nextGaussian() * 0.08) * 2;
        normalizedValue = Math.max(0.0, Math.min(1.0, normalizedValue + microVariation));

        int range = config.maxCooldownTime - config.minCooldownTime;
        int cooldownTime = config.minCooldownTime + (int)(normalizedValue * range);

        return cooldownTime;
    }

    private static double generateNormalizedValue() {
 
        double gaussianValue;
        do {
            gaussianValue = random.nextGaussian() * GAUSSIAN_STDDEV + GAUSSIAN_MEAN;
        } while (gaussianValue < 0.0 || gaussianValue > 1.0);

 
        updateBias();

 
        double blendFactor = 0.7; 
        return (gaussianValue * blendFactor) + (currentBias * (1.0 - blendFactor));
    }

    private static void updateBias() {
 
        double driftAmount = (random.nextGaussian() * 0.02);
        currentBias += driftAmount;

 
        currentBias = Math.max(0.1, Math.min(0.9, currentBias));

 
        if (random.nextDouble() < 0.001) { 
            currentBias = 0.3 + (random.nextDouble() * 0.4); 
        }
    }

    private static int applyPatternBreaking(int baseTime, AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastShot = currentTime - lastShotTime;

 
        if (baseTime <= (config.minDrawTime + config.maxDrawTime) / 2) {
            consecutiveFastShots++;
            consecutiveSlowShots = 0;
        } else {
            consecutiveSlowShots++;
            consecutiveFastShots = 0;
        }

 
        if (consecutiveFastShots >= 3) {
            baseTime = config.maxDrawTime - random.nextInt(5); 
            consecutiveFastShots = 0;
        } else if (consecutiveSlowShots >= 3) {
            baseTime = config.minDrawTime + random.nextInt(5); 
            consecutiveSlowShots = 0;
        }

 
        if (random.nextDouble() < 0.05) { 
            int spike = random.nextBoolean() ?
                    random.nextInt(8) - 4 : 
                    random.nextInt(16) - 8; 
            baseTime = Math.max(config.minDrawTime,
                    Math.min(config.maxDrawTime, baseTime + spike));
        }

        lastShotTime = currentTime;
        return baseTime;
    }

    public static void resetPatternTracking() {
        consecutiveFastShots = 0;
        consecutiveSlowShots = 0;
        currentBias = 0.5;
        lastShotTime = 0;
    }

 
    public static String getRandomizationInfo() {
        return String.format("Bias: %.3f, Fast: %d, Slow: %d",
                currentBias, consecutiveFastShots, consecutiveSlowShots);
    }
}
