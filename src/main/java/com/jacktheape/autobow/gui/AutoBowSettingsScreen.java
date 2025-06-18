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
    private final String[] tabNames = {"Timing", "Movement", "Simple Mode", "Efficiency", "Learning", "HUD"};


    private EnhancedSlider minDrawTimeSlider;
    private EnhancedSlider maxDrawTimeSlider;
    private EnhancedSlider minCooldownSlider;
    private EnhancedSlider maxCooldownSlider;
    private EnhancedSlider durabilityThresholdSlider;
    private EnhancedSlider xpThresholdSlider;


    private CyclingButtonWidget<Integer> xpCheckIntervalButton;
    private CyclingButtonWidget<Integer> simpleShootDurationButton;
    private CyclingButtonWidget<Integer> simpleBreakDurationButton;
    private CyclingButtonWidget<Integer> efficiencyBreakDurationButton;
    private CyclingButtonWidget<Integer> maxEfficiencySessionsButton;
    private CyclingButtonWidget<String> movementIntensityButton;
    private CyclingButtonWidget<String> hudPositionButton;
    private CyclingButtonWidget<Integer> hudScaleButton;

    public AutoBowSettingsScreen(Screen parent) {
        super(Text.literal("Auto Bow Settings"));
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


        this.addDrawableChild(new TextWidget(centerX - 75, 10, 150, 20,
                Text.literal("§6§lAuto Bow Settings"), this.textRenderer));


        String modeText = "Current Mode: " + config.operatingMode;
        this.addDrawableChild(new TextWidget(centerX - 60, 25, 120, 15,
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
            case 5: renderHudTab(leftX, rightX, startY, spacing, contentWidth); break;
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

        currentY += spacing;


        efficiencyBreakDurationButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Break: " + value + " min"))
                .values(3, 5, 8, 10, 12, 15, 20)
                .initially(config.efficiencyBreakDuration)
                .build(leftX, currentY, 200, 20, Text.literal("Break Duration"));
        this.addDrawableChild(efficiencyBreakDurationButton);


        maxEfficiencySessionsButton = CyclingButtonWidget.builder((Integer value) ->
                        Text.literal("Max/Day: " + value))
                .values(3, 4, 5, 6, 8, 10, 12, 15, 20)
                .initially(config.maxDailyEfficiencySessions)
                .build(rightX, currentY, 140, 20, Text.literal("Max Sessions"));
        this.addDrawableChild(maxEfficiencySessionsButton);

        currentY += spacing + 10;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 40,
                Text.literal("§7Monitors McMMO XP rates and takes breaks when efficiency drops below threshold."),
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
                            System.out.println("[Settings] Movement toggled to: " + config.enableMovementVariation);
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

    private void renderTimingTab(int leftX, int rightX, int startY, int spacing, int contentWidth) {
        int currentY = startY;


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
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


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
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


        this.addDrawableChild(new TextWidget(leftX, currentY, contentWidth, 20,
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

    private String ticksToSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0);
    }

    private String getMovementIntensityText() {
        switch (config.movementIntensity) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            default: return "Low";
        }
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
                config.showBossbarDebugInfo = defaultConfig.showBossbarDebugInfo;
                config.xpReductionThreshold = defaultConfig.xpReductionThreshold;
                config.efficiencyBreakDuration = defaultConfig.efficiencyBreakDuration;
                config.maxDailyEfficiencySessions = defaultConfig.maxDailyEfficiencySessions;
                break;
            case 4:
                config.enableLearningMode = defaultConfig.enableLearningMode;
                config.enableServerAdaptation = defaultConfig.enableServerAdaptation;
                config.showAdaptationMessages = defaultConfig.showAdaptationMessages;
                break;
            case 5:
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
                "Tab: " + tabNames[currentTab] + " • Mode: " + config.operatingMode,
                this.width / 2, this.height - 15, 0x808080);
    }

    private void saveAndClose() {
        try {

            if (xpCheckIntervalButton != null) config.xpCheckInterval = xpCheckIntervalButton.getValue();
            if (simpleShootDurationButton != null) config.simpleShootDuration = simpleShootDurationButton.getValue();
            if (simpleBreakDurationButton != null) config.simpleBreakDuration = simpleBreakDurationButton.getValue();
            if (efficiencyBreakDurationButton != null) config.efficiencyBreakDuration = efficiencyBreakDurationButton.getValue();
            if (maxEfficiencySessionsButton != null) config.maxDailyEfficiencySessions = maxEfficiencySessionsButton.getValue();
            if (hudPositionButton != null) config.hudPosition = hudPositionButton.getValue();
            if (hudScaleButton != null) config.hudScale = hudScaleButton.getValue();


            if (movementIntensityButton != null) {
                String intensityValue = movementIntensityButton.getValue();
                switch (intensityValue) {
                    case "Low": config.movementIntensity = 1; break;
                    case "Medium": config.movementIntensity = 2; break;
                    case "High": config.movementIntensity = 3; break;
                    default: config.movementIntensity = 1; break;
                }
                System.out.println("[Settings] Movement intensity set to: " + config.movementIntensity);
            }

            System.out.println("[Settings] Final save state - Movement enabled: " + config.enableMovementVariation +
                    ", Intensity: " + config.movementIntensity + ", Mode: " + config.operatingMode);

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
