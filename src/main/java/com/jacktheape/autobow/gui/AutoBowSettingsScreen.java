package com.jacktheape.autobow.gui;

import com.jacktheape.autobow.AutoBowConfig;
import com.jacktheape.autobow.SessionManager;
import com.jacktheape.autobow.ServerProfileManager;
import com.jacktheape.autobow.BossbarXpMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class AutoBowSettingsScreen extends Screen {
    private final Screen parent;
    private final AutoBowConfig config;
    private int currentTab = 0;
    private final String[] tabNames = {"Timing", "XP Monitor", "Sessions", "HUD", "Advanced"};

 
    private EnhancedSlider minDrawTimeSlider;
    private EnhancedSlider maxDrawTimeSlider;
    private EnhancedSlider minCooldownSlider;
    private EnhancedSlider maxCooldownSlider;
    private EnhancedSlider durabilityThresholdSlider;
    private EnhancedSlider xpThresholdSlider;

 
    private CyclingButtonWidget<Integer> xpCheckIntervalButton;
    private CyclingButtonWidget<Integer> breakDurationButton;
    private CyclingButtonWidget<Integer> maxSessionsButton;
    private CyclingButtonWidget<String> movementIntensityButton;
    private CyclingButtonWidget<Integer> maxSessionDurationButton;
    private CyclingButtonWidget<String> hudPositionButton;
    private CyclingButtonWidget<Integer> hudScaleButton;

    public AutoBowSettingsScreen(Screen parent) {
        super(Text.literal("Auto Bow Settings - Tabbed Interface"));
        this.parent = parent;
        this.config = AutoBowConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int contentWidth = Math.min(600, this.width - 40);
        int leftX = centerX - contentWidth / 2;
        int rightX = leftX + contentWidth / 2 + 10;
        int startY = 80;
        int spacing = 25;

        this.clearChildren();

 
        this.addDrawableChild(new TextWidget(centerX - 150, 10, 300, 20,
                Text.literal("§6§lAuto Bow Settings"), this.textRenderer));

 
        this.addDrawableChild(new TextWidget(centerX - 180, 25, 360, 15,
                Text.literal("§7Tabbed interface for organized configuration"), this.textRenderer));

 
        int tabWidth = contentWidth / tabNames.length;
        for (int i = 0; i < tabNames.length; i++) {
            final int tabIndex = i;
            boolean isActive = i == currentTab;

            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(isActive ? "§e" + tabNames[i] : "§7" + tabNames[i]),
                            button -> {
                                currentTab = tabIndex;
                                this.init(); 
                            })
                    .dimensions(leftX + (i * tabWidth), 45, tabWidth - 2, 20)
                    .build());
        }

 
        switch (currentTab) {
            case 0: renderTimingTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 1: renderXpMonitorTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 2: renderSessionsTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 3: renderHudTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 4: renderAdvancedTab(leftX, rightX, startY, spacing, contentWidth); break;
        }

 
        int buttonY = this.height - 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Tab"), button -> {
            resetCurrentTab();
        }).dimensions(leftX, buttonY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), button -> {
            saveAndClose();
        }).dimensions(leftX + 110, buttonY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(leftX + 220, buttonY, 100, 20).build());
    }

    private void renderTimingTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Draw Time Range"), this.textRenderer));
        currentY += 20;

        minDrawTimeSlider = new EnhancedSlider(leftX, currentY, 140, 18,
                Text.literal("Min: " + ticksToSeconds(config.minDrawTime)),
                (config.minDrawTime - 5) / 95.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 95) + 5;
                config.minDrawTime = ticks;
                this.setMessage(Text.literal("Min: " + ticksToSeconds(ticks)));

                if (config.maxDrawTime < config.minDrawTime) {
                    config.maxDrawTime = config.minDrawTime;
                    maxDrawTimeSlider.updateValue((config.maxDrawTime - 5) / 95.0);
                }
            }
        };
        this.addDrawableChild(minDrawTimeSlider);

        maxDrawTimeSlider = new EnhancedSlider(rightX, currentY, 140, 18,
                Text.literal("Max: " + ticksToSeconds(config.maxDrawTime)),
                (config.maxDrawTime - 5) / 95.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 95) + 5;
                config.maxDrawTime = Math.max(ticks, config.minDrawTime);
                this.setMessage(Text.literal("Max: " + ticksToSeconds(config.maxDrawTime)));
            }
        };
        this.addDrawableChild(maxDrawTimeSlider);

        currentY += spacing + 10;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Cooldown Range"), this.textRenderer));
        currentY += 20;

        minCooldownSlider = new EnhancedSlider(leftX, currentY, 140, 18,
                Text.literal("Min: " + ticksToSeconds(config.minCooldownTime)),
                config.minCooldownTime / 100.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 100);
                config.minCooldownTime = ticks;
                this.setMessage(Text.literal("Min: " + ticksToSeconds(ticks)));

                if (config.maxCooldownTime < config.minCooldownTime) {
                    config.maxCooldownTime = config.minCooldownTime;
                    maxCooldownSlider.updateValue(config.maxCooldownTime / 100.0);
                }
            }
        };
        this.addDrawableChild(minCooldownSlider);

        maxCooldownSlider = new EnhancedSlider(rightX, currentY, 140, 18,
                Text.literal("Max: " + ticksToSeconds(config.maxCooldownTime)),
                config.maxCooldownTime / 100.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 100);
                config.maxCooldownTime = Math.max(ticks, config.minCooldownTime);
                this.setMessage(Text.literal("Max: " + ticksToSeconds(config.maxCooldownTime)));
            }
        };
        this.addDrawableChild(maxCooldownSlider);

        currentY += spacing + 10;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Durability Protection"), this.textRenderer));
        currentY += 20;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Protection: " + (config.enableDurabilityProtection ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableDurabilityProtection = !config.enableDurabilityProtection;
                            button.setMessage(Text.literal("Protection: " + (config.enableDurabilityProtection ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

        durabilityThresholdSlider = new EnhancedSlider(leftX, currentY, contentWidth, 18,
                Text.literal("Stop at " + config.durabilityThreshold + " durability"),
                (config.durabilityThreshold - 1) / 99.0, "durability") {
            @Override
            protected void updateMessage() {
                int threshold = (int)(this.value * 99) + 1;
                config.durabilityThreshold = threshold;
                this.setMessage(Text.literal("Stop at " + threshold + " durability"));
            }
        };
        this.addDrawableChild(durabilityThresholdSlider);
    }

    private void renderXpMonitorTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6McMMO XP Monitoring"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Bossbar Monitor: " + (config.enableBossbarXpMonitoring ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableBossbarXpMonitoring = !config.enableBossbarXpMonitoring;
                            button.setMessage(Text.literal("Bossbar Monitor: " + (config.enableBossbarXpMonitoring ? "§aON" : "§cOFF")));
                            if (config.enableBossbarXpMonitoring) {
                                BossbarXpMonitor.startMonitoring();
                            } else {
                                BossbarXpMonitor.stopMonitoring();
                            }
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

 
        xpCheckIntervalButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("XP Check Interval: " + value + "ms"))
                .values(500, 1000, 1500, 2000, 3000, 5000)
                .initially((int)config.xpCheckInterval)
                .build(leftX, currentY, 200, 20, Text.literal("XP Check Interval"));
        this.addDrawableChild(xpCheckIntervalButton);

 
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Debug: " + (config.showBossbarDebugInfo ? "§aON" : "§cOFF")),
                        button -> {
                            config.showBossbarDebugInfo = !config.showBossbarDebugInfo;
                            button.setMessage(Text.literal("Debug: " + (config.showBossbarDebugInfo ? "§aON" : "§cOFF")));
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());

        currentY += spacing;

 
        xpThresholdSlider = new EnhancedSlider(leftX, currentY, contentWidth, 18,
                Text.literal("Efficiency Threshold: " + (int)(config.xpReductionThreshold * 100) + "%"),
                (config.xpReductionThreshold - 0.3) / 0.6, "percentage") {
            @Override
            protected void updateMessage() {
                double threshold = 0.3 + (this.value * 0.6);
                config.xpReductionThreshold = threshold;
                this.setMessage(Text.literal("Efficiency Threshold: " + (int)(threshold * 100) + "%"));
            }
        };
        this.addDrawableChild(xpThresholdSlider);

        currentY += spacing + 10;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Server Adaptation"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Auto-Adapt: " + (config.enableServerAdaptation ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableServerAdaptation = !config.enableServerAdaptation;
                            button.setMessage(Text.literal("Auto-Adapt: " + (config.enableServerAdaptation ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Re-Learn Server"),
                        button -> {
                            ServerProfileManager.forceOptimizeCurrentServer();
                            if (client.player != null) {
                                client.player.sendMessage(Text.literal("§a[Auto Bow] Started re-learning server"), false);
                            }
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());
    }

    private void renderSessionsTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Efficiency-Based Sessions"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Efficiency Mode: " + (config.useEfficiencyBasedSessions ? "§aON" : "§cOFF")),
                        button -> {
                            config.useEfficiencyBasedSessions = !config.useEfficiencyBasedSessions;
                            button.setMessage(Text.literal("Efficiency Mode: " + (config.useEfficiencyBasedSessions ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Session Management: " + (config.enableSessionManagement ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableSessionManagement = !config.enableSessionManagement;
                            button.setMessage(Text.literal("Session Management: " + (config.enableSessionManagement ? "§aON" : "§cOFF")));
                            if (!config.enableSessionManagement) {
                                SessionManager.forceEndSession();
                            }
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

 
        breakDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Break Duration: " + value + " min"))
                .values(3, 5, 8, 10, 12, 15, 20)
                .initially(config.breakDuration)
                .build(leftX, currentY, 200, 20, Text.literal("Break Duration"));
        this.addDrawableChild(breakDurationButton);

        maxSessionDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Max Session: " + value + " min"))
                .values(15, 20, 25, 30, 45, 60)
                .initially(config.maxSessionDuration)
                .build(rightX, currentY, 140, 20, Text.literal("Max Session"));
        this.addDrawableChild(maxSessionDurationButton);

        currentY += spacing;

        maxSessionsButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Max Sessions/Day: " + value))
                .values(3, 4, 5, 6, 8, 10, 12, 15, 20)
                .initially(config.maxDailyFarmingSessions)
                .build(leftX, currentY, 200, 20, Text.literal("Max Sessions"));
        this.addDrawableChild(maxSessionsButton);

        movementIntensityButton = CyclingButtonWidget.builder((String value) ->
                        Text.literal("Movement: " + value))
                .values("Off", "Low", "Medium", "High")
                .initially(getMovementIntensityText())
                .build(rightX, currentY, 140, 20, Text.literal("Movement"));
        this.addDrawableChild(movementIntensityButton);
    }

    private void renderHudTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6HUD Display Options"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Show HUD: " + (config.showHudOverlay ? "§aON" : "§cOFF")),
                        button -> {
                            config.showHudOverlay = !config.showHudOverlay;
                            button.setMessage(Text.literal("Show HUD: " + (config.showHudOverlay ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

 
        hudPositionButton = CyclingButtonWidget.builder((String value) ->
                        Text.literal("Position: " + value))
                .values("Top Right", "Top Left", "Bottom Right", "Bottom Left")
                .initially(config.hudPosition)
                .build(leftX, currentY, 200, 20, Text.literal("HUD Position"));
        this.addDrawableChild(hudPositionButton);

 
        hudScaleButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Scale: " + value + "%"))
                .values(50, 75, 100, 125, 150)
                .initially(config.hudScale)
                .build(rightX, currentY, 140, 20, Text.literal("HUD Scale"));
        this.addDrawableChild(hudScaleButton);

        currentY += spacing;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6HUD Elements"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Show XP Rate: " + (config.showXpRate ? "§aON" : "§cOFF")),
                        button -> {
                            config.showXpRate = !config.showXpRate;
                            button.setMessage(Text.literal("Show XP Rate: " + (config.showXpRate ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Show Efficiency: " + (config.showEfficiency ? "§aON" : "§cOFF")),
                        button -> {
                            config.showEfficiency = !config.showEfficiency;
                            button.setMessage(Text.literal("Show Efficiency: " + (config.showEfficiency ? "§aON" : "§cOFF")));
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());

        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Show Session Info: " + (config.showSessionInfo ? "§aON" : "§cOFF")),
                        button -> {
                            config.showSessionInfo = !config.showSessionInfo;
                            button.setMessage(Text.literal("Show Session Info: " + (config.showSessionInfo ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Show Durability: " + (config.showDurability ? "§aON" : "§cOFF")),
                        button -> {
                            config.showDurability = !config.showDurability;
                            button.setMessage(Text.literal("Show Durability: " + (config.showDurability ? "§aON" : "§cOFF")));
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());
    }

    private void renderAdvancedTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6General Options"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Status Messages: " + (config.showStatusMessages ? "§aON" : "§cOFF")),
                        button -> {
                            config.showStatusMessages = !config.showStatusMessages;
                            button.setMessage(Text.literal("Status Messages: " + (config.showStatusMessages ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Debug Mode: " + (config.enableDebugMode ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableDebugMode = !config.enableDebugMode;
                            button.setMessage(Text.literal("Debug Mode: " + (config.enableDebugMode ? "§aON" : "§cOFF")));
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());

        currentY += spacing + 10;

 
        this.addDrawableChild(new TextWidget(leftX, currentY, 200, 20,
                Text.literal("§6Advanced Features"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")),
                        button -> {
                            config.useAdvancedRandomization = !config.useAdvancedRandomization;
                            button.setMessage(Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Session Notifications: " + (config.showSessionNotifications ? "§aON" : "§cOFF")),
                        button -> {
                            config.showSessionNotifications = !config.showSessionNotifications;
                            button.setMessage(Text.literal("Session Notifications: " + (config.showSessionNotifications ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());
    }

    private void resetCurrentTab() {
        AutoBowConfig defaultConfig = new AutoBowConfig();

        switch (currentTab) {
            case 0: 
                config.minDrawTime = defaultConfig.minDrawTime;
                config.maxDrawTime = defaultConfig.maxDrawTime;
                config.minCooldownTime = defaultConfig.minCooldownTime;
                config.maxCooldownTime = defaultConfig.maxCooldownTime;
                config.durabilityThreshold = defaultConfig.durabilityThreshold;
                config.enableDurabilityProtection = defaultConfig.enableDurabilityProtection;
                break;
            case 1: 
                config.enableBossbarXpMonitoring = defaultConfig.enableBossbarXpMonitoring;
                config.xpCheckInterval = defaultConfig.xpCheckInterval;
                config.showBossbarDebugInfo = defaultConfig.showBossbarDebugInfo;
                config.xpReductionThreshold = defaultConfig.xpReductionThreshold;
                config.enableServerAdaptation = defaultConfig.enableServerAdaptation;
                break;
            case 2: 
                config.useEfficiencyBasedSessions = defaultConfig.useEfficiencyBasedSessions;
                config.enableSessionManagement = defaultConfig.enableSessionManagement;
                config.breakDuration = defaultConfig.breakDuration;
                config.maxSessionDuration = defaultConfig.maxSessionDuration;
                config.maxDailyFarmingSessions = defaultConfig.maxDailyFarmingSessions;
                config.enableMovementVariation = defaultConfig.enableMovementVariation;
                config.movementIntensity = defaultConfig.movementIntensity;
                break;
            case 3: 
                config.showHudOverlay = defaultConfig.showHudOverlay;
                config.hudPosition = defaultConfig.hudPosition;
                config.hudScale = defaultConfig.hudScale;
                config.showXpRate = defaultConfig.showXpRate;
                config.showEfficiency = defaultConfig.showEfficiency;
                config.showSessionInfo = defaultConfig.showSessionInfo;
                config.showDurability = defaultConfig.showDurability;
                break;
            case 4: 
                config.showStatusMessages = defaultConfig.showStatusMessages;
                config.enableDebugMode = defaultConfig.enableDebugMode;
                config.useAdvancedRandomization = defaultConfig.useAdvancedRandomization;
                config.showSessionNotifications = defaultConfig.showSessionNotifications;
                break;
        }

        this.init(); 
    }

    private String ticksToSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0);
    }

    private String getMovementIntensityText() {
        if (!config.enableMovementVariation) {
            return "Off";
        }
        switch (config.movementIntensity) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "Medium";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

 
        int centerX = this.width / 2;
        int contentWidth = Math.min(600, this.width - 40);
        int leftX = centerX - contentWidth / 2;

        context.fill(leftX - 5, 65, leftX + contentWidth + 5, this.height - 50, 0x40000000);
        context.drawBorder(leftX - 5, 65, contentWidth + 10, this.height - 115, 0xFF444444);

        super.render(context, mouseX, mouseY, delta);

 
        context.drawCenteredTextWithShadow(this.textRenderer,
                "§7Tab: " + tabNames[currentTab] + " • Use tabs to organize settings",
                this.width / 2, this.height - 15, 0x808080);
    }

    private void saveAndClose() {
        try {

            if (xpCheckIntervalButton != null) {
                config.xpCheckInterval = xpCheckIntervalButton.getValue();
                System.out.println("[Settings Debug] Saving XP interval: " + config.xpCheckInterval);
            }

            if (breakDurationButton != null) {
                config.breakDuration = breakDurationButton.getValue();
                System.out.println("[Settings Debug] Saving break duration: " + config.breakDuration);
            }

            if (maxSessionsButton != null) {
                config.maxDailyFarmingSessions = maxSessionsButton.getValue();
            }

            if (maxSessionDurationButton != null) {
                config.maxSessionDuration = maxSessionDurationButton.getValue();
            }

            if (hudPositionButton != null) {
                config.hudPosition = hudPositionButton.getValue();
            }

            if (hudScaleButton != null) {
                config.hudScale = hudScaleButton.getValue();
            }


            if (movementIntensityButton != null) {
                String movementValue = movementIntensityButton.getValue();
                System.out.println("[Settings Debug] Movement button value: " + movementValue);

                if (movementValue.equals("Off")) {
                    config.enableMovementVariation = false;
                    config.movementIntensity = 1;
                    System.out.println("[Settings Debug] Movement disabled");
                } else {
                    config.enableMovementVariation = true;
                    switch (movementValue) {
                        case "Low":
                            config.movementIntensity = 1;
                            break;
                        case "Medium":
                            config.movementIntensity = 2;
                            break;
                        case "High":
                            config.movementIntensity = 3;
                            break;
                        default:
                            config.movementIntensity = 2;
                            break;
                    }
                    System.out.println("[Settings Debug] Movement enabled with intensity: " + config.movementIntensity);
                }
            }

            System.out.println("[Settings Debug] Final movement state - Enabled: " + config.enableMovementVariation +
                    ", Intensity: " + config.movementIntensity);


            config.validateAndFix();
            config.saveConfig();

            this.client.setScreen(parent);
        } catch (Exception e) {
            System.err.println("[Settings Error] Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

 
    private abstract class EnhancedSlider extends SliderWidget {
        private final String unit;

        public EnhancedSlider(int x, int y, int width, int height, Text text, double value, String unit) {
            super(x, y, width, height, text, value);
            this.unit = unit;
        }

        public void updateValue(double newValue) {
            this.value = Math.max(0.0, Math.min(1.0, newValue));
            this.updateMessage();
        }

        @Override
        protected void applyValue() {
            this.updateMessage();
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            if (this.isHovered()) {
                context.drawBorder(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2, 0xFF00AA00);
            }
        }
    }
}
