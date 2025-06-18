package com.jacktheape.autobow.gui;

import com.jacktheape.autobow.AutoBowConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class AutoBowSettingsScreen extends Screen {
    private final Screen parent;
    private final AutoBowConfig config;

    // Custom slider implementations to avoid setValue() access issues
    private CustomSlider minDrawTimeSlider;
    private CustomSlider maxDrawTimeSlider;
    private CustomSlider minCooldownSlider;
    private CustomSlider maxCooldownSlider;
    private CustomSlider durabilityThresholdSlider;

    // Toggle buttons
    private ButtonWidget durabilityProtectionButton;
    private ButtonWidget statusMessagesButton;
    private ButtonWidget debugModeButton;

    public AutoBowSettingsScreen(Screen parent) {
        super(Text.literal("Auto Bow Settings"));
        this.parent = parent;
        this.config = AutoBowConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;
        int currentY = startY;

        // Title
        this.addDrawableChild(new TextWidget(centerX - 60, 20, 120, 20,
                Text.literal("Auto Bow Settings"), this.textRenderer));

        // Draw Time Settings
        this.addDrawableChild(new TextWidget(centerX - 100, currentY, 200, 20,
                Text.literal("Draw Time Range (seconds)"), this.textRenderer));
        currentY += 20;

        // Min Draw Time Slider
        minDrawTimeSlider = new CustomSlider(centerX - 100, currentY, 90, 20,
                Text.literal("Min: " + ticksToSeconds(config.minDrawTime)),
                (config.minDrawTime - 5) / 95.0) {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 95) + 5;
                config.minDrawTime = ticks;
                this.setMessage(Text.literal("Min: " + ticksToSeconds(ticks)));

                // Ensure max is always >= min
                if (config.maxDrawTime < config.minDrawTime) {
                    config.maxDrawTime = config.minDrawTime;
                    maxDrawTimeSlider.updateValue((config.maxDrawTime - 5) / 95.0);
                }
            }
        };
        this.addDrawableChild(minDrawTimeSlider);

        // Max Draw Time Slider
        maxDrawTimeSlider = new CustomSlider(centerX + 10, currentY, 90, 20,
                Text.literal("Max: " + ticksToSeconds(config.maxDrawTime)),
                (config.maxDrawTime - 5) / 95.0) {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 95) + 5;
                config.maxDrawTime = Math.max(ticks, config.minDrawTime);
                this.setMessage(Text.literal("Max: " + ticksToSeconds(config.maxDrawTime)));
            }
        };
        this.addDrawableChild(maxDrawTimeSlider);

        currentY += spacing + 10;

        // Cooldown Settings
        this.addDrawableChild(new TextWidget(centerX - 100, currentY, 200, 20,
                Text.literal("Cooldown Range (seconds)"), this.textRenderer));
        currentY += 20;

        // Min Cooldown Slider
        minCooldownSlider = new CustomSlider(centerX - 100, currentY, 90, 20,
                Text.literal("Min: " + ticksToSeconds(config.minCooldownTime)),
                config.minCooldownTime / 100.0) {
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

        // Max Cooldown Slider
        maxCooldownSlider = new CustomSlider(centerX + 10, currentY, 90, 20,
                Text.literal("Max: " + ticksToSeconds(config.maxCooldownTime)),
                config.maxCooldownTime / 100.0) {
            @Override
            protected void updateMessage() {
                int ticks = (int)(this.value * 100);
                config.maxCooldownTime = Math.max(ticks, config.minCooldownTime);
                this.setMessage(Text.literal("Max: " + ticksToSeconds(config.maxCooldownTime)));
            }
        };
        this.addDrawableChild(maxCooldownSlider);

        currentY += spacing + 10;

        // Durability Threshold
        this.addDrawableChild(new TextWidget(centerX - 100, currentY, 200, 20,
                Text.literal("Durability Protection"), this.textRenderer));
        currentY += 20;

        durabilityThresholdSlider = new CustomSlider(centerX - 100, currentY, 200, 20,
                Text.literal("Stop at " + config.durabilityThreshold + " durability"),
                (config.durabilityThreshold - 1) / 99.0) {
            @Override
            protected void updateMessage() {
                int threshold = (int)(this.value * 99) + 1;
                config.durabilityThreshold = threshold;
                this.setMessage(Text.literal("Stop at " + threshold + " durability"));
            }
        };
        this.addDrawableChild(durabilityThresholdSlider);

        currentY += spacing + 10;

        // Toggle Buttons
        durabilityProtectionButton = ButtonWidget.builder(
                        Text.literal("Durability Protection: " + (config.enableDurabilityProtection ? "ON" : "OFF")),
                        button -> {
                            config.enableDurabilityProtection = !config.enableDurabilityProtection;
                            button.setMessage(Text.literal("Durability Protection: " + (config.enableDurabilityProtection ? "ON" : "OFF")));
                        })
                .dimensions(centerX - 100, currentY, 200, 20)
                .build();
        this.addDrawableChild(durabilityProtectionButton);

        currentY += spacing;

        statusMessagesButton = ButtonWidget.builder(
                        Text.literal("Status Messages: " + (config.showStatusMessages ? "ON" : "OFF")),
                        button -> {
                            config.showStatusMessages = !config.showStatusMessages;
                            button.setMessage(Text.literal("Status Messages: " + (config.showStatusMessages ? "ON" : "OFF")));
                        })
                .dimensions(centerX - 100, currentY, 200, 20)
                .build();
        this.addDrawableChild(statusMessagesButton);

        currentY += spacing;

        debugModeButton = ButtonWidget.builder(
                        Text.literal("Debug Mode: " + (config.enableDebugMode ? "ON" : "OFF")),
                        button -> {
                            config.enableDebugMode = !config.enableDebugMode;
                            button.setMessage(Text.literal("Debug Mode: " + (config.enableDebugMode ? "ON" : "OFF")));
                        })
                .dimensions(centerX - 100, currentY, 200, 20)
                .build();
        this.addDrawableChild(debugModeButton);

        currentY += spacing + 20;

        // Action Buttons
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Defaults"), button -> {
            resetToDefaults();
        }).dimensions(centerX - 100, currentY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), button -> {
            saveAndClose();
        }).dimensions(centerX + 5, currentY, 95, 20).build());

        currentY += spacing;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(centerX - 50, currentY, 100, 20).build());
    }

    private String ticksToSeconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0);
    }

    private void resetToDefaults() {
        AutoBowConfig defaultConfig = new AutoBowConfig();
        config.minDrawTime = defaultConfig.minDrawTime;
        config.maxDrawTime = defaultConfig.maxDrawTime;
        config.minCooldownTime = defaultConfig.minCooldownTime;
        config.maxCooldownTime = defaultConfig.maxCooldownTime;
        config.durabilityThreshold = defaultConfig.durabilityThreshold;
        config.enableDurabilityProtection = defaultConfig.enableDurabilityProtection;
        config.showStatusMessages = defaultConfig.showStatusMessages;
        config.enableDebugMode = defaultConfig.enableDebugMode;

        // Refresh the screen
        this.client.setScreen(new AutoBowSettingsScreen(parent));
    }

    private void saveAndClose() {
        config.validateAndFix();
        config.saveConfig();
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fixed: renderBackground now requires mouseX, mouseY, and delta parameters for 1.21
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw additional help text
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Adjust settings to customize your auto bow behavior",
                this.width / 2, this.height - 30, 0x808080);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // Custom Slider class to work around setValue() access issues
    private abstract class CustomSlider extends SliderWidget {
        public CustomSlider(int x, int y, int width, int height, Text text, double value) {
            super(x, y, width, height, text, value);
        }

        // Public method to update value without using private setValue()
        public void updateValue(double newValue) {
            this.value = Math.max(0.0, Math.min(1.0, newValue));
            this.updateMessage();
        }

        @Override
        protected void applyValue() {
            this.updateMessage();
        }
    }
}
