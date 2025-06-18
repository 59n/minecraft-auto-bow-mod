package com.jacktheape.autobow;

import com.jacktheape.autobow.gui.AutoBowSettingsScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoBowKeybinds {
    public static KeyBinding toggleAutoBow;
    public static KeyBinding forceEnable;
    public static KeyBinding checkDurability;
    public static KeyBinding openSettings;

    public static void registerKeybinds() {
        toggleAutoBow = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autobow.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.autobow"
        ));

        forceEnable = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autobow.force_enable",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.autobow"
        ));

        checkDurability = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autobow.check_durability",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.autobow"
        ));

        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autobow.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.autobow"
        ));
    }

    public static void handleKeyPress() {
        if (toggleAutoBow.wasPressed()) {
            AutoBowHandler.toggleAutoBow();
        }

        if (forceEnable.wasPressed()) {
            AutoBowHandler.forceEnable();
        }

        if (checkDurability.wasPressed()) {
            DurabilityChecker.checkCurrentBowDurability();
        }

        if (openSettings.wasPressed()) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new AutoBowSettingsScreen(client.currentScreen));
        }
    }
}
