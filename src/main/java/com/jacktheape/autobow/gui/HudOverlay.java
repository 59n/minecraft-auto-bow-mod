package com.jacktheape.autobow.gui;

import com.jacktheape.autobow.AutoBowConfig;
import com.jacktheape.autobow.AutoBowHandler;
import com.jacktheape.autobow.AmmoManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class HudOverlay {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void register() {
        // Updated for 1.21 - Use the correct callback signature
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(false);
            renderHud(context, tickDelta);
        });
    }

    private static void renderHud(DrawContext context, float tickDelta) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        // Only render if HUD is enabled in config
        if (!config.showHudOverlay) {
            return;
        }

        // Only render if mod is enabled
        if (!AutoBowHandler.isEnabled() || client.player == null) {
            return;
        }

        // Check both main hand and offhand for bows
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

        // Only render if holding a bow in either hand
        if (activeBowStack == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Position HUD in top-right corner
        int hudX = screenWidth - 160;
        int hudY = 10;

        // Background panel with rounded corners effect
        context.fill(hudX - 5, hudY - 5, hudX + 150, hudY + 95, 0x80000000);
        context.drawBorder(hudX - 5, hudY - 5, 155, 100, 0xFF555555);

        // Title with color coding
        context.drawText(client.textRenderer, Text.literal("§6Auto Bow Status"), hudX, hudY, 0xFFFFFF, true);
        hudY += 12;

        // Status indicator with hand information
        String status = AutoBowHandler.isEnabled() ? "§aENABLED" : "§cDISABLED";
        String handType = foundBowInOffhand ? " (Offhand)" : " (Main Hand)";
        if (AutoBowHandler.isEnabled()) {
            status += handType;
        }
        context.drawText(client.textRenderer, Text.literal("Status: " + status), hudX, hudY, 0xFFFFFF, true);
        hudY += 10;

        // Next shot timer with progress indication
        int nextShotTime = AutoBowHandler.getNextShotCountdown();
        if (nextShotTime > 0) {
            String timerText = String.format("Cooldown: §e%.1fs", nextShotTime / 20.0f);
            context.drawText(client.textRenderer, Text.literal(timerText), hudX, hudY, 0xFFFFFF, true);

            // Cooldown progress bar
            renderProgressBar(context, hudX, hudY + 10, 100, 4, nextShotTime, config.maxCooldownTime, 0xFFFF6600);
            hudY += 18;
        } else if (AutoBowHandler.isDrawing()) {
            int drawProgress = AutoBowHandler.getDrawProgress();
            String drawText = String.format("Drawing: §e%.1fs", drawProgress / 20.0f);
            context.drawText(client.textRenderer, Text.literal(drawText), hudX, hudY, 0xFFFFFF, true);

            // Draw progress bar
            renderProgressBar(context, hudX, hudY + 10, 100, 4, drawProgress, config.maxDrawTime, 0xFF00FF00);
            hudY += 18;
        } else {
            context.drawText(client.textRenderer, Text.literal("§aReady to shoot"), hudX, hudY, 0xFFFFFF, true);
            hudY += 10;
        }

        // Durability bar with enhanced visuals
        renderDurabilityBar(context, activeBowStack, hudX, hudY);
        hudY += 15;

        // Ammunition count with infinity detection
        renderAmmoStatus(context, activeBowStack, hudX, hudY);
        hudY += 10;

        // Current settings preview
        context.drawText(client.textRenderer, Text.literal("§7Timing: " +
                        String.format("%.1f-%.1fs", config.minDrawTime/20.0f, config.maxDrawTime/20.0f)),
                hudX, hudY, 0xFFFFFF, true);
        hudY += 10;

        // Advanced randomization status (if enabled and in debug mode)
        if (config.useAdvancedRandomization && config.enableDebugMode) {
            context.drawText(client.textRenderer, Text.literal("§7Advanced Timing"),
                    hudX, hudY, 0xFFFFFF, true);
        }
    }

    private static void renderProgressBar(DrawContext context, int x, int y, int width, int height,
                                          int current, int max, int color) {
        // Background
        context.fill(x, y, x + width, y + height, 0xFF333333);

        // Progress fill
        if (max > 0) {
            int progressWidth = (int) ((float) current / max * (width - 2));
            context.fill(x + 1, y + 1, x + 1 + progressWidth, y + height - 1, color);
        }

        // Border
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

        // Durability bar background
        context.fill(x, y, x + 100, y + 8, 0xFF333333);

        // Durability bar fill with color coding
        int barWidth = (int) (98 * durabilityPercent);
        int barColor;
        if (durabilityPercent > 0.5f) {
            barColor = 0xFF00FF00; // Green
        } else if (durabilityPercent > 0.25f) {
            barColor = 0xFFFFFF00; // Yellow
        } else if (durabilityPercent > 0.1f) {
            barColor = 0xFFFF6600; // Orange
        } else {
            barColor = 0xFFFF0000; // Red
        }

        context.fill(x + 1, y + 1, x + 1 + barWidth, y + 7, barColor);

        // Durability border
        context.drawBorder(x, y, 100, 8, 0xFF666666);

        // Durability text
        String durabilityText = String.format("%d/%d", remainingDurability, maxDurability);
        context.drawText(client.textRenderer,
                Text.literal(durabilityText), x + 105, y, 0xFFFFFF, true);
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
