package com.jacktheape.autobow;

import java.util.Random;

public class AdvancedRandomizer {
    private static final Random random = new Random();
    private static long lastShotTime = 0;
    private static int consecutiveFastShots = 0;
    private static int consecutiveSlowShots = 0;
    private static double currentBias = 0.5; // 0.0 = always min, 1.0 = always max

    // Gaussian distribution parameters
    private static final double GAUSSIAN_MEAN = 0.5;
    private static final double GAUSSIAN_STDDEV = 0.15;

    public static int getRandomizedDrawTime() {
        AutoBowConfig config = AutoBowConfig.getInstance();

        // Use different randomization strategies based on configuration
        double normalizedValue = generateNormalizedValue();

        // Apply micro-variations (±2 ticks)
        double microVariation = (random.nextGaussian() * 0.1) * 2;
        normalizedValue = Math.max(0.0, Math.min(1.0, normalizedValue + microVariation));

        // Convert to actual tick range
        int range = config.maxDrawTime - config.minDrawTime;
        int drawTime = config.minDrawTime + (int)(normalizedValue * range);

        // Apply pattern breaking logic
        drawTime = applyPatternBreaking(drawTime, config);

        if (config.enableDebugMode) {
            System.out.println("[Advanced Randomizer] Draw time: " + drawTime +
                    " (normalized: " + String.format("%.3f", normalizedValue) + ")");
        }

        return drawTime;
    }

    public static int getRandomizedCooldownTime() {
        AutoBowConfig config = AutoBowConfig.getInstance();

        // Use complementary randomization for cooldown
        double normalizedValue = generateNormalizedValue();

        // Inverse correlation with draw time for more natural feel
        normalizedValue = 1.0 - normalizedValue;

        // Apply micro-variations
        double microVariation = (random.nextGaussian() * 0.08) * 2;
        normalizedValue = Math.max(0.0, Math.min(1.0, normalizedValue + microVariation));

        int range = config.maxCooldownTime - config.minCooldownTime;
        int cooldownTime = config.minCooldownTime + (int)(normalizedValue * range);

        return cooldownTime;
    }

    private static double generateNormalizedValue() {
        // Use Gaussian distribution for more natural randomness
        double gaussianValue;
        do {
            gaussianValue = random.nextGaussian() * GAUSSIAN_STDDEV + GAUSSIAN_MEAN;
        } while (gaussianValue < 0.0 || gaussianValue > 1.0);

        // Apply bias drift for long-term pattern variation
        updateBias();

        // Blend Gaussian with bias
        double blendFactor = 0.7; // 70% Gaussian, 30% bias
        return (gaussianValue * blendFactor) + (currentBias * (1.0 - blendFactor));
    }

    private static void updateBias() {
        // Slowly drift the bias to create long-term pattern changes
        double driftAmount = (random.nextGaussian() * 0.02);
        currentBias += driftAmount;

        // Keep bias within reasonable bounds
        currentBias = Math.max(0.1, Math.min(0.9, currentBias));

        // Occasionally reset bias for unpredictability
        if (random.nextDouble() < 0.001) { // 0.1% chance per call
            currentBias = 0.3 + (random.nextDouble() * 0.4); // Reset to 0.3-0.7 range
        }
    }

    private static int applyPatternBreaking(int baseTime, AutoBowConfig config) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastShot = currentTime - lastShotTime;

        // Track consecutive fast/slow shots
        if (baseTime <= (config.minDrawTime + config.maxDrawTime) / 2) {
            consecutiveFastShots++;
            consecutiveSlowShots = 0;
        } else {
            consecutiveSlowShots++;
            consecutiveFastShots = 0;
        }

        // Break patterns after too many consecutive similar timings
        if (consecutiveFastShots >= 3) {
            baseTime = config.maxDrawTime - random.nextInt(5); // Force a slow shot
            consecutiveFastShots = 0;
        } else if (consecutiveSlowShots >= 3) {
            baseTime = config.minDrawTime + random.nextInt(5); // Force a fast shot
            consecutiveSlowShots = 0;
        }

        // Occasional random spikes for unpredictability
        if (random.nextDouble() < 0.05) { // 5% chance
            int spike = random.nextBoolean() ?
                    random.nextInt(8) - 4 : // Small spike ±4 ticks
                    random.nextInt(16) - 8; // Large spike ±8 ticks
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

    // Get human-readable randomization info for debugging
    public static String getRandomizationInfo() {
        return String.format("Bias: %.3f, Fast: %d, Slow: %d",
                currentBias, consecutiveFastShots, consecutiveSlowShots);
    }
}
