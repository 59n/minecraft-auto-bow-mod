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
    private final String[] tabNames = {"Timing", "Movement", "Simple Mode", "Efficiency", "Learning", "Messages", "Advanced", "HUD"};


    private EnhancedSlider minDrawTimeSlider;
    private EnhancedSlider maxDrawTimeSlider;
    private EnhancedSlider minCooldownSlider;
    private EnhancedSlider maxCooldownSlider;
    private EnhancedSlider durabilityThresholdSlider;
    private EnhancedSlider xpThresholdSlider;
    private EnhancedSlider farmingSessionDurationSlider;
    private EnhancedSlider breakDurationSlider;


    private CyclingButtonWidget<Integer> xpCheckIntervalButton;
    private CyclingButtonWidget<Integer> simpleShootDurationButton;
    private CyclingButtonWidget<Integer> simpleBreakDurationButton;
    private CyclingButtonWidget<Integer> efficiencyBreakDurationButton;
    private CyclingButtonWidget<Integer> maxEfficiencySessionsButton;
    private CyclingButtonWidget<Integer> maxDailyFarmingSessionsButton;
    private CyclingButtonWidget<Integer> maxSessionDurationButton;
    private CyclingButtonWidget<String> movementIntensityButton;
    private CyclingButtonWidget<String> hudPositionButton;
    private CyclingButtonWidget<Integer> hudScaleButton;

    public AutoBowSettingsScreen(Screen parent) {
        super(Text.literal("Auto Bow Settings - Complete Control"));
        this.parent = parent;
        this.config = AutoBowConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int contentWidth = Math.min(700, this.width - 40);
        int leftX = centerX - contentWidth / 2;
        int rightX = leftX + contentWidth / 2 + 10;
        int startY = 80;
        int spacing = 25;

        this.clearChildren();


        this.addDrawableChild(new TextWidget(centerX - 100, 10, 200, 20,
                Text.literal("§6§lAuto Bow Settings - Complete Control"), this.textRenderer));


        String modeText = "Mode: " + config.operatingMode + " | Chat Messages: " +
                (config.showStatusMessages ? "§aON" : "§cOFF");
        this.addDrawableChild(new TextWidget(centerX - 120, 25, 240, 15,
                Text.literal(modeText), this.textRenderer));


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
            case 1: renderMovementTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 2: renderSimpleModeTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 3: renderEfficiencyTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 4: renderLearningTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 5: renderMessagesTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 6: renderAdvancedTab(leftX, rightX, startY, spacing, contentWidth); break;
            case 7: renderHudTab(leftX, rightX, startY, spacing, contentWidth); break;
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


    private void renderMessagesTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Chat Message Control"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("All Status Messages: " + (config.showStatusMessages ? "§aON" : "§cOFF")),
                        button -> {
                            config.showStatusMessages = !config.showStatusMessages;
                            button.setMessage(Text.literal("All Status Messages: " + (config.showStatusMessages ? "§aON" : "§cOFF")));
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

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Daily Limit Messages: " + (config.showDailyLimitMessages ? "§aON" : "§cOFF")),
                        button -> {
                            config.showDailyLimitMessages = !config.showDailyLimitMessages;
                            button.setMessage(Text.literal("Daily Limit Messages: " + (config.showDailyLimitMessages ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Adaptation Messages: " + (config.showAdaptationMessages ? "§aON" : "§cOFF")),
                        button -> {
                            config.showAdaptationMessages = !config.showAdaptationMessages;
                            button.setMessage(Text.literal("Adaptation Messages: " + (config.showAdaptationMessages ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Bossbar Debug: " + (config.showBossbarDebugInfo ? "§aON" : "§cOFF")),
                        button -> {
                            config.showBossbarDebugInfo = !config.showBossbarDebugInfo;
                            button.setMessage(Text.literal("Bossbar Debug: " + (config.showBossbarDebugInfo ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Quick Presets"), this.textRenderer));
        currentY += 25;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Silent Mode (No Chat)"),
                        button -> {
                            config.showStatusMessages = false;
                            config.showSessionNotifications = false;
                            config.showDailyLimitMessages = false;
                            config.showAdaptationMessages = false;
                            config.showBossbarDebugInfo = false;
                            this.init();
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Verbose Mode (All Messages)"),
                        button -> {
                            config.showStatusMessages = true;
                            config.showSessionNotifications = true;
                            config.showDailyLimitMessages = true;
                            config.showAdaptationMessages = true;
                            config.showBossbarDebugInfo = false;
                            this.init();
                        })
                .dimensions(rightX, currentY, 140, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 60,
                Text.literal("§7Control all chat message types. Use Silent Mode to eliminate " +
                        "all chat spam while keeping HUD functionality. Verbose Mode enables " +
                        "all notifications for detailed feedback."),
                this.textRenderer));
    }
    private void renderMovementTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Movement Variation Settings"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Movement: " + (config.enableMovementVariation ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableMovementVariation = !config.enableMovementVariation;
                            button.setMessage(Text.literal("Movement: " + (config.enableMovementVariation ? "§aON" : "§cOFF")));
                            config.saveConfig();
                            config.enableServerAdaptation = false;
                            System.out.println("[Settings] Movement locked to: " + config.enableMovementVariation);
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        movementIntensityButton = CyclingButtonWidget.builder((String value) ->
                        Text.literal("Intensity: " + value))
                .values("Low", "Medium", "High")
                .initially(getMovementIntensityText())
                .build(leftX, currentY, contentWidth, 20, Text.literal("Movement Intensity"));
        this.addDrawableChild(movementIntensityButton);

        currentY += spacing + 10;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")),
                        button -> {
                            config.useAdvancedRandomization = !config.useAdvancedRandomization;
                            button.setMessage(Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 60,
                Text.literal("§7Movement adds random mouse movements to avoid detection. Turn OFF for pure shooting."),
                this.textRenderer));
    }
    private void renderSimpleModeTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Simple Timed Mode"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Simple Mode: " + (config.operatingMode.equals("SIMPLE") ? "§aACTIVE" : "§cINACTIVE")),
                        button -> {
                            config.operatingMode = config.operatingMode.equals("SIMPLE") ? "EFFICIENCY" : "SIMPLE";
                            button.setMessage(Text.literal("Simple Mode: " + (config.operatingMode.equals("SIMPLE") ? "§aACTIVE" : "§cINACTIVE")));
                            this.init();
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        simpleShootDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Shoot: " + value + " min"))
                .values(5, 10, 15, 20, 25, 30, 45, 60, 90, 120)
                .initially(config.simpleShootDuration)
                .build(leftX, currentY, 200, 20, Text.literal("Shoot Duration"));
        this.addDrawableChild(simpleShootDurationButton);

        simpleBreakDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Break: " + value + " min"))
                .values(1, 2, 3, 5, 8, 10, 15, 20, 30)
                .initially(config.simpleBreakDuration)
                .build(rightX, currentY, 140, 20, Text.literal("Break Duration"));
        this.addDrawableChild(simpleBreakDurationButton);

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Ignore Daily Limits: " + (config.simpleIgnoreDailyLimits ? "§aYES" : "§cNO")),
                        button -> {
                            config.simpleIgnoreDailyLimits = !config.simpleIgnoreDailyLimits;
                            button.setMessage(Text.literal("Ignore Daily Limits: " + (config.simpleIgnoreDailyLimits ? "§aYES" : "§cNO")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 40,
                Text.literal("§7Shoots for X minutes, then breaks for Y minutes. No XP monitoring or daily limits."),
                this.textRenderer));
    }
    private void renderEfficiencyTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Efficiency-Based Mode"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Efficiency Mode: " + (config.operatingMode.equals("EFFICIENCY") ? "§aACTIVE" : "§cINACTIVE")),
                        button -> {
                            config.operatingMode = config.operatingMode.equals("EFFICIENCY") ? "SIMPLE" : "EFFICIENCY";
                            button.setMessage(Text.literal("Efficiency Mode: " + (config.operatingMode.equals("EFFICIENCY") ? "§aACTIVE" : "§cINACTIVE")));
                            this.init();
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Bossbar XP Monitor: " + (config.enableBossbarXpMonitoring ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableBossbarXpMonitoring = !config.enableBossbarXpMonitoring;
                            button.setMessage(Text.literal("Bossbar XP Monitor: " + (config.enableBossbarXpMonitoring ? "§aON" : "§cOFF")));
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
                        Text.literal("XP Check: " + value + "ms"))
                .values(500, 1000, 1500, 2000, 3000, 5000)
                .initially((int)config.xpCheckInterval)
                .build(leftX, currentY, 200, 20, Text.literal("XP Check Interval"));
        this.addDrawableChild(xpCheckIntervalButton);

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

        currentY += spacing;


        efficiencyBreakDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Break: " + value + " min"))
                .values(3, 5, 8, 10, 12, 15, 20)
                .initially(config.efficiencyBreakDuration)
                .build(leftX, currentY, 200, 20, Text.literal("Break Duration"));
        this.addDrawableChild(efficiencyBreakDurationButton);


        maxEfficiencySessionsButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Max/Day: " + value))
                .values(3, 4, 5, 6, 8, 10, 12, 15, 20, 999)
                .initially(config.maxDailyEfficiencySessions)
                .build(rightX, currentY, 140, 20, Text.literal("Max Sessions"));
        this.addDrawableChild(maxEfficiencySessionsButton);

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Daily Limits: " + (config.enableDailySessionLimits ? "§aENABLED" : "§cDISABLED")),
                        button -> {
                            config.enableDailySessionLimits = !config.enableDailySessionLimits;
                            button.setMessage(Text.literal("Daily Limits: " + (config.enableDailySessionLimits ? "§aENABLED" : "§cDISABLED")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 40,
                Text.literal("§7Monitors McMMO XP rates and takes breaks when efficiency drops. Daily limits are optional."),
                this.textRenderer));
    }
    private void renderLearningTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Learning & Adaptation Mode"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Learning Mode: " + (config.operatingMode.equals("LEARNING") ? "§aACTIVE" : "§cINACTIVE")),
                        button -> {
                            config.operatingMode = config.operatingMode.equals("LEARNING") ? "SIMPLE" : "LEARNING";
                            button.setMessage(Text.literal("Learning Mode: " + (config.operatingMode.equals("LEARNING") ? "§aACTIVE" : "§cINACTIVE")));
                            this.init();
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Server Adaptation: " + (config.enableServerAdaptation ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableServerAdaptation = !config.enableServerAdaptation;
                            button.setMessage(Text.literal("Server Adaptation: " + (config.enableServerAdaptation ? "§aON" : "§cOFF")));

                            if (config.enableServerAdaptation && client.player != null) {
                                client.player.sendMessage(
                                        Text.literal("§e[Auto Bow] WARNING: Server adaptation may override your movement settings!"),
                                        false
                                );
                            }
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

        currentY += spacing;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Adaptation Messages: " + (config.showAdaptationMessages ? "§aON" : "§cOFF")),
                        button -> {
                            config.showAdaptationMessages = !config.showAdaptationMessages;
                            button.setMessage(Text.literal("Adaptation Messages: " + (config.showAdaptationMessages ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 60,
                Text.literal("§7Combines efficiency monitoring with server-specific adaptation and learning."),
                this.textRenderer));
    }
    private void renderHudTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
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


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
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
    private void resetCurrentTab() {
        AutoBowConfig defaultConfig = AutoBowConfig.createDefault();

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
                config.enableMovementVariation = defaultConfig.enableMovementVariation;
                config.movementIntensity = defaultConfig.movementIntensity;
                config.useAdvancedRandomization = defaultConfig.useAdvancedRandomization;
                break;
            case 2:
                config.simpleShootDuration = defaultConfig.simpleShootDuration;
                config.simpleBreakDuration = defaultConfig.simpleBreakDuration;
                config.simpleIgnoreDailyLimits = defaultConfig.simpleIgnoreDailyLimits;
                break;
            case 3:
                config.enableBossbarXpMonitoring = defaultConfig.enableBossbarXpMonitoring;
                config.xpCheckInterval = defaultConfig.xpCheckInterval;
                config.xpReductionThreshold = defaultConfig.xpReductionThreshold;
                config.efficiencyBreakDuration = defaultConfig.efficiencyBreakDuration;
                config.maxDailyEfficiencySessions = defaultConfig.maxDailyEfficiencySessions;
                config.enableDailySessionLimits = defaultConfig.enableDailySessionLimits;
                break;
            case 4:
                config.enableServerAdaptation = defaultConfig.enableServerAdaptation;
                config.showAdaptationMessages = defaultConfig.showAdaptationMessages;
                break;
            case 5:
                config.showStatusMessages = defaultConfig.showStatusMessages;
                config.showSessionNotifications = defaultConfig.showSessionNotifications;
                config.showDailyLimitMessages = defaultConfig.showDailyLimitMessages;
                config.showAdaptationMessages = defaultConfig.showAdaptationMessages;
                config.showBossbarDebugInfo = defaultConfig.showBossbarDebugInfo;
                break;
            case 6:
                config.enableSessionManagement = defaultConfig.enableSessionManagement;
                config.useEfficiencyBasedSessions = defaultConfig.useEfficiencyBasedSessions;
                config.farmingSessionDuration = defaultConfig.farmingSessionDuration;
                config.breakDuration = defaultConfig.breakDuration;
                config.maxDailyFarmingSessions = defaultConfig.maxDailyFarmingSessions;
                config.maxSessionDuration = defaultConfig.maxSessionDuration;
                config.useAdvancedRandomization = defaultConfig.useAdvancedRandomization;
                config.enableDebugMode = defaultConfig.enableDebugMode;
                break;
            case 7:
                config.showHudOverlay = defaultConfig.showHudOverlay;
                config.hudPosition = defaultConfig.hudPosition;
                config.hudScale = defaultConfig.hudScale;
                config.showXpRate = defaultConfig.showXpRate;
                config.showEfficiency = defaultConfig.showEfficiency;
                config.showSessionInfo = defaultConfig.showSessionInfo;
                config.showDurability = defaultConfig.showDurability;
                break;
        }

        this.init();
    }
    private String getMovementIntensityText() {
        switch (config.movementIntensity) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "Low";
        }
    }


    private void renderAdvancedTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Advanced Configuration"), this.textRenderer));
        currentY += 25;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§eSession Management System"), this.textRenderer));
        currentY += 20;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Legacy Session Management: " + (config.enableSessionManagement ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableSessionManagement = !config.enableSessionManagement;
                            button.setMessage(Text.literal("Legacy Session Management: " + (config.enableSessionManagement ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Efficiency-Based Sessions: " + (config.useEfficiencyBasedSessions ? "§aON" : "§cOFF")),
                        button -> {
                            config.useEfficiencyBasedSessions = !config.useEfficiencyBasedSessions;
                            button.setMessage(Text.literal("Efficiency-Based Sessions: " + (config.useEfficiencyBasedSessions ? "§aON" : "§cOFF")));
                        })
                .dimensions(leftX, currentY, contentWidth, 20)
                .build());

        currentY += spacing;


        farmingSessionDurationSlider = new EnhancedSlider(leftX, currentY, 200, 18,
                Text.literal("Legacy Session: " + config.farmingSessionDuration + " min"),
                (config.farmingSessionDuration - 1) / 59.0, "minutes") {
            @Override
            protected void updateMessage() {
                int duration = (int)(this.value * 59) + 1;
                config.farmingSessionDuration = duration;
                this.setMessage(Text.literal("Legacy Session: " + duration + " min"));
            }
        };
        this.addDrawableChild(farmingSessionDurationSlider);

        breakDurationSlider = new EnhancedSlider(rightX, currentY, 140, 18,
                Text.literal("Legacy Break: " + config.breakDuration + " min"),
                (config.breakDuration - 1) / 29.0, "minutes") {
            @Override
            protected void updateMessage() {
                int duration = (int)(this.value * 29) + 1;
                config.breakDuration = duration;
                this.setMessage(Text.literal("Legacy Break: " + duration + " min"));
            }
        };
        this.addDrawableChild(breakDurationSlider);

        currentY += spacing + 10;


        maxDailyFarmingSessionsButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Legacy Max/Day: " + value))
                .values(1, 3, 5, 8, 10, 15, 20, 999)
                .initially(config.maxDailyFarmingSessions)
                .build(leftX, currentY, 200, 20, Text.literal("Legacy Daily Limit"));
        this.addDrawableChild(maxDailyFarmingSessionsButton);

        maxSessionDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Max Duration: " + value + " min"))
                .values(10, 15, 20, 30, 45, 60, 90, 120)
                .initially(config.maxSessionDuration)
                .build(rightX, currentY, 140, 20, Text.literal(""));
        this.addDrawableChild(maxSessionDurationButton);

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§eRandomization & Detection"), this.textRenderer));
        currentY += 20;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")),
                        button -> {
                            config.useAdvancedRandomization = !config.useAdvancedRandomization;
                            button.setMessage(Text.literal("Advanced Randomization: " + (config.useAdvancedRandomization ? "§aON" : "§cOFF")));
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


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§eSession Tracking Status"), this.textRenderer));
        currentY += 20;

        String sessionStatus = "Sessions Today: " + config.sessionsCompletedToday +
                " | Total Time: " + formatTime(config.totalFarmingTimeToday);
        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 15,
                Text.literal("§7" + sessionStatus), this.textRenderer));
        currentY += 15;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Reset Session Data"),
                        button -> {
                            config.sessionsCompletedToday = 0;
                            config.totalFarmingTimeToday = 0;
                            config.lastDayReset = System.currentTimeMillis();
                            config.saveConfig();
                            this.init();
                        })
                .dimensions(leftX, currentY, 200, 20)
                .build());
    }


    private void renderTimingTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;

        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
                Text.literal("§6Timing Configuration"), this.textRenderer));
        currentY += 20;


        minDrawTimeSlider = new EnhancedSlider(leftX, currentY, 140, 18,
                Text.literal("Min Draw: " + ticksToSeconds(config.minDrawTime)),
                (config.minDrawTime - 1) / 99.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 99) + 1;
                config.minDrawTime = ticks;
                this.setMessage(Text.literal("Min Draw: " + ticksToSeconds(ticks)));

                if (config.maxDrawTime < config.minDrawTime) {
                    config.maxDrawTime = config.minDrawTime;
                    maxDrawTimeSlider.updateValue((config.maxDrawTime - 1) / 99.0);
                }
            }
        };
        this.addDrawableChild(minDrawTimeSlider);

        maxDrawTimeSlider = new EnhancedSlider(rightX, currentY, 140, 18,
                Text.literal("Max Draw: " + ticksToSeconds(config.maxDrawTime)),
                (config.maxDrawTime - 1) / 99.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 99) + 1;
                config.maxDrawTime = Math.max(ticks, config.minDrawTime);
                this.setMessage(Text.literal("Max Draw: " + ticksToSeconds(config.maxDrawTime)));
            }
        };
        this.addDrawableChild(maxDrawTimeSlider);

        currentY += spacing;


        minCooldownSlider = new EnhancedSlider(leftX, currentY, 140, 18,
                Text.literal("Min Cooldown: " + ticksToSeconds(config.minCooldownTime)),
                config.minCooldownTime / 100.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 100);
                config.minCooldownTime = ticks;
                this.setMessage(Text.literal("Min Cooldown: " + ticksToSeconds(ticks)));

                if (config.maxCooldownTime < config.minCooldownTime) {
                    config.maxCooldownTime = config.minCooldownTime;
                    maxCooldownSlider.updateValue(config.maxCooldownTime / 100.0);
                }
            }
        };
        this.addDrawableChild(minCooldownSlider);

        maxCooldownSlider = new EnhancedSlider(rightX, currentY, 140, 18,
                Text.literal("Max Cooldown: " + ticksToSeconds(config.maxCooldownTime)),
                config.maxCooldownTime / 100.0, "seconds") {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 100);
                config.maxCooldownTime = Math.max(ticks, config.minCooldownTime);
                this.setMessage(Text.literal("Max Cooldown: " + ticksToSeconds(config.maxCooldownTime)));
            }
        };
        this.addDrawableChild(maxCooldownSlider);

        currentY += spacing + 10;


        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Durability Protection: " + (config.enableDurabilityProtection ? "§aON" : "§cOFF")),
                        button -> {
                            config.enableDurabilityProtection = !config.enableDurabilityProtection;
                            button.setMessage(Text.literal("Durability Protection: " + (config.enableDurabilityProtection ? "§aON" : "§cOFF")));
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

        currentY += spacing + 10;


        String timingInfo = String.format("Current: %.1f-%.1fs draw, %.1f-%.1fs cooldown",
                config.minDrawTime/20.0f, config.maxDrawTime/20.0f,
                config.minCooldownTime/20.0f, config.maxCooldownTime/20.0f);
        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 15,
                Text.literal("§7" + timingInfo), this.textRenderer));
    }




    private String ticksToSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0);
    }

    private String formatTime(long milliseconds) {
        long minutes = milliseconds / 60000;
        if (minutes > 60) {
            return String.format("%.1fh", minutes / 60.0);
        }
        return minutes + "m";
    }

    private void saveAndClose() {
        try {

            if (xpCheckIntervalButton != null) config.xpCheckInterval = xpCheckIntervalButton.getValue();
            if (simpleShootDurationButton != null) config.simpleShootDuration = simpleShootDurationButton.getValue();
            if (simpleBreakDurationButton != null) config.simpleBreakDuration = simpleBreakDurationButton.getValue();
            if (efficiencyBreakDurationButton != null) config.efficiencyBreakDuration = efficiencyBreakDurationButton.getValue();
            if (maxEfficiencySessionsButton != null) config.maxDailyEfficiencySessions = maxEfficiencySessionsButton.getValue();
            if (maxDailyFarmingSessionsButton != null) config.maxDailyFarmingSessions = maxDailyFarmingSessionsButton.getValue();
            if (maxSessionDurationButton != null) config.maxSessionDuration = maxSessionDurationButton.getValue();
            if (hudPositionButton != null) config.hudPosition = hudPositionButton.getValue();
            if (hudScaleButton != null) config.hudScale = hudScaleButton.getValue();


            if (movementIntensityButton != null && config.enableMovementVariation) {
                String intensityValue = movementIntensityButton.getValue();
                switch (intensityValue) {
                    case "Low": config.movementIntensity = 1; break;
                    case "Medium": config.movementIntensity = 2; break;
                    case "High": config.movementIntensity = 3; break;
                    default: config.movementIntensity = 1; break;
                }
            }

            System.out.println("[Settings] Saving complete configuration - " +
                    "Messages: " + config.showStatusMessages +
                    ", Mode: " + config.operatingMode +
                    ", Movement: " + config.enableMovementVariation);

            config.saveConfig();
            this.client.setScreen(parent);
        } catch (Exception e) {
            System.err.println("[Settings Error] Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
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
