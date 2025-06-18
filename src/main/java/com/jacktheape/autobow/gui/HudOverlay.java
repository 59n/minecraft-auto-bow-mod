package com.jacktheape.autobow.gui;

import com.jacktheape.autobow.AutoBowConfig;
import com.jacktheape.autobow.AutoBowHandler;
import com.jacktheape.autobow.AmmoManager;
import com.jacktheape.autobow.ModeBasedSessionManager;
import com.jacktheape.autobow.ServerProfileManager;
import com.jacktheape.autobow.BossbarXpMonitor;
import com.jacktheape.autobow.MovementVariationManager;
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


        String titleText = "§6Auto Bow - " + config.operatingMode + " Mode";
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


        if (config.showSessionInfo) {
            String modeText = "§7Mode: §e" + config.operatingMode;
            context.drawText(client.textRenderer, Text.literal(modeText), hudX, hudY, 0xFFFFFF, true);
            hudY += 10;

            if (ModeBasedSessionManager.isInSession()) {
                long sessionDuration = ModeBasedSessionManager.getSessionTimeElapsed();
                int minutesElapsed = (int) (sessionDuration / 60000);
                int secondsElapsed = (int) ((sessionDuration % 60000) / 1000);

                String sessionText = "";
                switch (config.operatingMode) {
                    case "SIMPLE":
                        sessionText = "§aShoot: " + minutesElapsed + "m " + secondsElapsed + "s";


                        long remainingShootTime = (config.simpleShootDuration * 60 * 1000) - sessionDuration;
                        if (remainingShootTime > 0) {
                            int remainingMinutes = (int) (remainingShootTime / 60000);
                            int remainingSeconds = (int) ((remainingShootTime % 60000) / 1000);
                            context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                            hudY += 10;
                            sessionText = "§7Remaining: " + remainingMinutes + "m " + remainingSeconds + "s";
                        }
                        break;

                    case "EFFICIENCY":
                        double currentEfficiency = ModeBasedSessionManager.getCurrentSessionEfficiency();
                        String efficiencyColor = currentEfficiency > 80 ? "§a" : currentEfficiency > 60 ? "§e" : "§c";
                        sessionText = "§aSession: " + minutesElapsed + "m " + secondsElapsed + "s";
                        context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                        hudY += 10;

                        sessionText = "Efficiency: " + efficiencyColor + String.format("%.1f%%", currentEfficiency);
                        context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                        hudY += 10;


                        double threshold = config.xpReductionThreshold * 100;
                        String thresholdText = currentEfficiency < threshold ? "§c⚠ Below Threshold" : "§a✓ Above Threshold";
                        sessionText = "§7Threshold: " + (int)threshold + "% " + thresholdText;
                        break;

                    case "LEARNING":
                        sessionText = "§aLearning: " + minutesElapsed + "m " + secondsElapsed + "s";
                        context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                        hudY += 10;


                        sessionText = "§7Adapting to server patterns...";
                        break;
                }

                context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                hudY += 10;

            } else if (ModeBasedSessionManager.isInBreak()) {
                long timeRemaining = ModeBasedSessionManager.getBreakTimeRemaining();
                int minutesRemaining = (int) (timeRemaining / 60000);
                int secondsRemaining = (int) ((timeRemaining % 60000) / 1000);

                context.drawText(client.textRenderer,
                        Text.literal("§6Break: " + minutesRemaining + "m " + secondsRemaining + "s"),
                        hudX, hudY, 0xFFFFFF, true);
                hudY += 10;


                long totalBreakTime = 0;
                switch (config.operatingMode) {
                    case "SIMPLE":
                        totalBreakTime = config.simpleBreakDuration * 60 * 1000;
                        break;
                    case "EFFICIENCY":
                    case "LEARNING":
                        totalBreakTime = config.efficiencyBreakDuration * 60 * 1000;
                        break;
                }

                if (totalBreakTime > 0) {
                    long elapsed = totalBreakTime - timeRemaining;
                    renderProgressBar(context, hudX, hudY, hudWidth, 4, (int)elapsed, (int)totalBreakTime, 0xFFFF6600);
                    hudY += 8;
                }


                String breakReason = "";
                switch (config.operatingMode) {
                    case "SIMPLE":
                        breakReason = "§7Reason: Timed break";
                        break;
                    case "EFFICIENCY":
                    case "LEARNING":
                        breakReason = "§7Reason: Efficiency dropped";
                        break;
                }
                context.drawText(client.textRenderer, Text.literal(breakReason), hudX, hudY, 0xFFFFFF, true);
                hudY += 10;
            }


            if (!config.operatingMode.equals("SIMPLE")) {
                String sessionText = "§7Sessions: " + config.sessionsCompletedToday + "/" +
                        (config.operatingMode.equals("EFFICIENCY") ? config.maxDailyEfficiencySessions : config.maxDailyFarmingSessions);

                if (config.enableBossbarXpMonitoring && BossbarXpMonitor.isMonitoring()) {
                    long totalXpToday = BossbarXpMonitor.getTotalXpToday();
                    if (totalXpToday > 0) {
                        sessionText += " (§e" + formatNumber(totalXpToday) + " XP§7)";
                    }
                }
                context.drawText(client.textRenderer, Text.literal(sessionText), hudX, hudY, 0xFFFFFF, true);
                hudY += 10;
            } else if (config.operatingMode.equals("SIMPLE") && config.enableBossbarXpMonitoring && BossbarXpMonitor.isMonitoring()) {

                long totalXpToday = BossbarXpMonitor.getTotalXpToday();
                if (totalXpToday > 0) {
                    context.drawText(client.textRenderer,
                            Text.literal("§7Today: §e" + formatNumber(totalXpToday) + " XP"),
                            hudX, hudY, 0xFFFFFF, true);
                    hudY += 10;
                }
            }
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
            String movementState = "";


            try {
                movementState = MovementVariationManager.getMovementStatus();
                context.drawText(client.textRenderer,
                        Text.literal("§7Movement: " + movementStatus + " (" + movementState + ")"),
                        hudX, hudY, 0xFFFFFF, true);
            } catch (Exception e) {
                context.drawText(client.textRenderer,
                        Text.literal("§7Movement: " + movementStatus),
                        hudX, hudY, 0xFFFFFF, true);
            }
            hudY += 10;
        } else {
            context.drawText(client.textRenderer,
                    Text.literal("§7Movement: §cOFF"),
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
                return new HudPosition(screenWidth - hudWidth - margin, screenHeight - 250, hudWidth);
            case "Bottom Left":
                return new HudPosition(margin, screenHeight - 250, hudWidth);
            default:
                return new HudPosition(screenWidth - hudWidth - margin, margin, hudWidth);
        }
    }

    private static int calculateHudHeight(AutoBowConfig config) {
        int baseHeight = 80;

        if (config.showXpRate && config.enableBossbarXpMonitoring) {
            baseHeight += 10;
        }

        if (config.showEfficiency && config.enableBossbarXpMonitoring) {
            baseHeight += 20;
        }

        if (config.enableServerAdaptation) {
            baseHeight += 20;
        }

        if (config.showSessionInfo) {
            switch (config.operatingMode) {
                case "SIMPLE":
                    baseHeight += 40;
                    break;
                case "EFFICIENCY":
                    baseHeight += 60;
                    break;
                case "LEARNING":
                    baseHeight += 50;
                    break;
            }
        }

        if (config.showDurability) {
            baseHeight += 25;
        }

        baseHeight += 50;

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
