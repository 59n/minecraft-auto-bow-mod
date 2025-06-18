package com.jacktheape.autobow.gui;

import com.jacktheape.autobow.AutoBowConfig;
import com.jacktheape.autobow.AutoBowHandler;
import com.jacktheape.autobow.AmmoManager;
import com.jacktheape.autobow.SessionManager;
import com.jacktheape.autobow.ServerProfileManager;
import com.jacktheape.autobow.BossbarXpMonitor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class HudOverlay {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(false);
            renderHud(context, tickDelta);
        });
    }

    private static void renderHud(DrawContext context, float tickDelta) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (!config.showHudOverlay) {
            return;
        }

        if (!AutoBowHandler.isEnabled() || client.player == null) {
            return;
        }

 
        ItemStack mainHandItem = client.player.getMainHandStack();
        ItemStack offhandItem = client.player.getOffHandStack();

        ItemStack activeBowStack = null;
        boolean foundBowInMainHand = mainHandItem.getItem() instanceof BowItem;
        boolean foundBowInOffhand = offhandItem.getItem() instanceof BowItem;

        if (foundBowInMainHand) {
            activeBowStack = mainHandItem;
        } else if (foundBowInOffhand) {
            activeBowStack = offhandItem;
        }

        if (activeBowStack == null) {
            return;
        }

 
        HudPosition position = calculateHudPosition(config);
        int hudX = position.x;
        int hudY = position.y;
        int hudWidth = position.width;
        int hudHeight = calculateHudHeight(config);

 
        float scale = config.hudScale / 100.0f;
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);

 
        hudX = (int) (hudX / scale);
        hudY = (int) (hudY / scale);
        hudWidth = (int) (hudWidth / scale);

 
        context.fill(hudX - 5, hudY - 5, hudX + hudWidth + 5, hudY + hudHeight + 5, 0x90000000);
        context.drawBorder(hudX - 5, hudY - 5, hudWidth + 10, hudHeight + 10, 0xFF555555);

 
        String titleText = config.useEfficiencyBasedSessions ? "§6Auto Bow - Efficiency Mode" : "§6Auto Bow Status";
        context.drawText(client.textRenderer, Text.literal(titleText), hudX, hudY, 0xFFFFFF, true);
        hudY += 12;

 
        String status = AutoBowHandler.isEnabled() ? "§aENABLED" : "§cDISABLED";
        String handType = foundBowInOffhand ? " (Offhand)" : " (Main Hand)";
        if (AutoBowHandler.isEnabled()) {
            status += handType;
        }

        if (AutoBowHandler.isPausedForBreak()) {
            status = "§6PAUSED (Break)";
        }

        context.drawText(client.textRenderer, Text.literal("Status: " + status), hudX, hudY, 0xFFFFFF, true);
        hudY += 10;

 
        if (config.showXpRate && config.enableBossbarXpMonitoring && BossbarXpMonitor.isMonitoring()) {
            double currentXpRate = BossbarXpMonitor.getCurrentXpRate();
            String xpRateColor = BossbarXpMonitor.isDiminishingReturnsActive() ? "§c" : "§a";
            context.drawText(client.textRenderer,
                    Text.literal("McMMO XP: " + xpRateColor + String.format("%.0f/min", currentXpRate)),
                    hudX, hudY, 0xFFFFFF, true);
            hudY += 10;
        }

 
        if (config.showEfficiency && config.enableBossbarXpMonitoring && BossbarXpMonitor.isMonitoring()) {
            double efficiency = BossbarXpMonitor.getEfficiencyPercentage();
            String efficiencyColor = efficiency > 80 ? "§a" : efficiency > 60 ? "§e" : "§c";
            context.drawText(client.textRenderer,
                    Text.literal("Efficiency: " + efficiencyColor + String.format("%.1f%%", efficiency)),
                    hudX, hudY, 0xFFFFFF, true);
            hudY += 10;

 
            int efficiencyWidth = (int) (hudWidth * Math.min(efficiency / 100.0, 1.0));
            int barColor = efficiency > 80 ? 0xFF00AA00 : efficiency > 60 ? 0xFFFFAA00 : 0xFFAA0000;
            renderProgressBar(context, hudX, hudY, hudWidth, 4, efficiencyWidth, hudWidth, barColor);
            hudY += 8;
        }

 
        if (config.enableServerAdaptation) {
            ServerProfileManager.ServerProfile profile = ServerProfileManager.getCurrentProfile();
            if (profile != null) {
                String serverName = profile.serverName.length() > 20 ?
                        profile.serverName.substring(0, 17) + "..." : profile.serverName;
                context.drawText(client.textRenderer,
                        Text.literal("§7Server: " + serverName),
                        hudX, hudY, 0xFFFFFF, true);
                hudY += 10;

                if (ServerProfileManager.isInLearningMode()) {
                    context.drawText(client.textRenderer,
                            Text.literal("§6Learning patterns..."),
                            hudX, hudY, 0xFFFFFF, true);
                    hudY += 10;
                } else {
                    int adaptationConfidence = profile.getAdaptationConfidence();
                    String confidenceColor = adaptationConfidence > 80 ? "§a" : adaptationConfidence > 50 ? "§e" : "§c";
                    context.drawText(client.textRenderer,
                            Text.literal("§7Confidence: " + confidenceColor + adaptationConfidence + "%"),
                            hudX, hudY, 0xFFFFFF, true);
                    hudY += 10;
                }
            }
        }

 
        if (config.showSessionInfo && config.enableSessionManagement) {
            if (SessionManager.isInFarmingSession()) {
                long sessionDuration = SessionManager.getSessionTimeRemaining(config);
                int minutesElapsed = (int) (sessionDuration / 60000);
                int secondsElapsed = (int) ((sessionDuration % 60000) / 1000);

                if (config.useEfficiencyBasedSessions) {
                    context.drawText(client.textRenderer,
                            Text.literal("§aSession: " + minutesElapsed + "m " + secondsElapsed + "s"),
                            hudX, hudY, 0xFFFFFF, true);
                    hudY += 10;

                    double currentEfficiency = SessionManager.getCurrentSessionEfficiency();
                    double threshold = config.xpReductionThreshold * 100;
                    String thresholdText = currentEfficiency < threshold ? "§c⚠ Below" : "§a✓ Above";
                    context.drawText(client.textRenderer,
                            Text.literal("§7Threshold: " + (int)threshold + "% " + thresholdText),
                            hudX, hudY, 0xFFFFFF, true);
                    hudY += 10;
                }

            } else if (SessionManager.isInBreakPeriod()) {
                long timeRemaining = SessionManager.getBreakTimeRemaining(config);
                int minutesRemaining = (int) (timeRemaining / 60000);
                int secondsRemaining = (int) ((timeRemaining % 60000) / 1000);
                context.drawText(client.textRenderer,
                        Text.literal("§6Break: " + minutesRemaining + "m " + secondsRemaining + "s"),
                        hudX, hudY, 0xFFFFFF, true);
                hudY += 10;

 
                long breakDuration = config.breakDuration * 60 * 1000;
                long elapsed = breakDuration - timeRemaining;
                renderProgressBar(context, hudX, hudY, hudWidth, 4, (int)elapsed, (int)breakDuration, 0xFFFF6600);
                hudY += 8;
            }

 
            String sessionText = "§7Sessions: " + config.sessionsCompletedToday + "/" + config.maxDailyFarmingSessions;
            if (config.enableBossbarXpMonitoring && BossbarXpMonitor.isMonitoring()) {
                long totalXpToday = BossbarXpMonitor.getTotalXpToday();
                if (totalXpToday > 0) {
                    sessionText += " (§e" + formatNumber(totalXpToday) + " XP§7)";
                }
            }
            context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
            hudY += 10;
        }

 
        int nextShotTime = AutoBowHandler.getNextShotCountdown();
        if (nextShotTime > 0) {
            String timerText = String.format("Cooldown: §e%.1fs", nextShotTime / 20.0f);
            context.drawText(client.textRenderer, Text.literal(timerText), hudX, hudY, 0xFFFFFF, true);

            renderProgressBar(context, hudX, hudY + 10, hudWidth, 4, nextShotTime, config.maxCooldownTime, 0xFFFF6600);
            hudY += 18;
        } else if (AutoBowHandler.isDrawing()) {
            int drawProgress = AutoBowHandler.getDrawProgress();
            String drawText = String.format("Drawing: §e%.1fs", drawProgress / 20.0f);
            context.drawText(client.textRenderer, Text.literal(drawText), hudX, hudY, 0xFFFFFF, true);

            renderProgressBar(context, hudX, hudY + 10, hudWidth, 4, drawProgress, config.maxDrawTime, 0xFF00FF00);
            hudY += 18;
        } else if (!AutoBowHandler.isPausedForBreak()) {
            context.drawText(client.textRenderer, Text.literal("§aReady to shoot"), hudX, hudY, 0xFFFFFF, true);
            hudY += 10;
        }

 
        if (config.showDurability) {
            hudY += 5; 
            renderDurabilityBar(context, activeBowStack, hudX, hudY);
            hudY += 20; 
        }

 
        renderAmmoStatus(context, activeBowStack, hudX, hudY);
        hudY += 10;

 
        context.drawText(client.textRenderer, Text.literal("§7Timing: " +
                        String.format("%.1f-%.1fs", config.minDrawTime/20.0f, config.maxDrawTime/20.0f)),
                hudX, hudY, 0xFFFFFF, true);
        hudY += 10;

 
        if (config.enableMovementVariation) {
            String movementStatus = getMovementIntensityText(config.movementIntensity);
            context.drawText(client.textRenderer, Text.literal("§7Movement: " + movementStatus),
                    hudX, hudY, 0xFFFFFF, true);
            hudY += 10;
        }

 
        if (config.enableBossbarXpMonitoring) {
            String monitorStatus = BossbarXpMonitor.isMonitoring() ? "§aActive" : "§cInactive";
            context.drawText(client.textRenderer, Text.literal("§7Bossbar: " + monitorStatus),
                    hudX, hudY, 0xFFFFFF, true);
        }

        context.getMatrices().pop();
    }

    private static class HudPosition {
        int x, y, width;

        HudPosition(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }

    private static HudPosition calculateHudPosition(AutoBowConfig config) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int hudWidth = 220;
        int margin = 10;

        switch (config.hudPosition) {
            case "Top Right":
                return new HudPosition(screenWidth - hudWidth - margin, margin, hudWidth);
            case "Top Left":
                return new HudPosition(margin, margin, hudWidth);
            case "Bottom Right":
                return new HudPosition(screenWidth - hudWidth - margin, screenHeight - 200, hudWidth);
            case "Bottom Left":
                return new HudPosition(margin, screenHeight - 200, hudWidth);
            default:
                return new HudPosition(screenWidth - hudWidth - margin, margin, hudWidth);
        }
    }

    private static int calculateHudHeight(AutoBowConfig config) {
        int baseHeight = 60;

        if (config.showXpRate && config.enableBossbarXpMonitoring) {
            baseHeight += 10;
        }

        if (config.showEfficiency && config.enableBossbarXpMonitoring) {
            baseHeight += 20;
        }

        if (config.enableServerAdaptation) {
            baseHeight += 20;
        }

        if (config.showSessionInfo && config.enableSessionManagement) {
            baseHeight += config.useEfficiencyBasedSessions ? 40 : 30;
        }

        if (config.showDurability) {
            baseHeight += 25; 
        }

        baseHeight += 40; 

        if (config.enableMovementVariation) {
            baseHeight += 10;
        }

        return baseHeight;
    }

    private static String getMovementIntensityText(int intensity) {
        switch (intensity) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "Medium";
        }
    }

    private static String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }

    private static void renderProgressBar(DrawContext context, int x, int y, int width, int height,
                                          int current, int max, int color) {
        context.fill(x, y, x + width, y + height, 0xFF333333);

        if (max > 0) {
            int progressWidth = (int) ((float) current / max * (width - 2));
            context.fill(x + 1, y + 1, x + 1 + progressWidth, y + height - 1, color);
        }

        context.drawBorder(x, y, width, height, 0xFF666666);
    }

    private static void renderDurabilityBar(DrawContext context, ItemStack bowStack, int x, int y) {
        if (!bowStack.isDamageable()) {
            context.drawText(client.textRenderer,
                    Text.literal("Durability: §aUnlimited"), x, y, 0xFFFFFF, true);
            return;
        }

        int maxDurability = bowStack.getMaxDamage();
        int currentDamage = bowStack.getDamage();
        int remainingDurability = maxDurability - currentDamage;
        float durabilityPercent = (float) remainingDurability / maxDurability;

 
        int barWidth = 180;
        context.fill(x, y, x + barWidth, y + 6, 0xFF333333);

        int progressWidth = (int) (178 * durabilityPercent);
        int barColor;
        if (durabilityPercent > 0.5f) {
            barColor = 0xFF00FF00;
        } else if (durabilityPercent > 0.25f) {
            barColor = 0xFFFFFF00;
        } else if (durabilityPercent > 0.1f) {
            barColor = 0xFFFF6600;
        } else {
            barColor = 0xFFFF0000;
        }

        context.fill(x + 1, y + 1, x + 1 + progressWidth, y + 5, barColor);
        context.drawBorder(x, y, barWidth, 6, 0xFF666666);

 
        String durabilityText = String.format("%d/%d (%.0f%%)", remainingDurability, maxDurability, durabilityPercent * 100);
        context.drawText(client.textRenderer,
                Text.literal(durabilityText), x, y + 8, 0xFFFFFF, true);
    }

    private static void renderAmmoStatus(DrawContext context, ItemStack heldItem, int x, int y) {
        if (AmmoManager.isInCreativeMode()) {
            context.drawText(client.textRenderer, Text.literal("Arrows: §aCreative Mode"), x, y, 0xFFFFFF, true);
        } else if (AmmoManager.hasInfinityEnchantment(heldItem)) {
            context.drawText(client.textRenderer, Text.literal("Arrows: §aInfinity ∞"), x, y, 0xFFFFFF, true);
        } else {
            int arrowCount = AmmoManager.getArrowCount();
            String ammoColor;
            String ammoIcon;

            if (arrowCount > 64) {
                ammoColor = "§a";
                ammoIcon = "●●●";
            } else if (arrowCount > 16) {
                ammoColor = "§e";
                ammoIcon = "●●○";
            } else if (arrowCount > 0) {
                ammoColor = "§c";
                ammoIcon = "●○○";
            } else {
                ammoColor = "§4";
                ammoIcon = "○○○";
            }

            context.drawText(client.textRenderer,
                    Text.literal("Arrows: " + ammoColor + arrowCount + " " + ammoIcon),
                    x, y, 0xFFFFFF, true);
        }
    }
}
