package com.jacktheape.autobow;

import com.jacktheape.autobow.gui.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class AutoBowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoBowKeybinds.registerKeybinds();
        HudOverlay.register(); // Register the HUD overlay

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AutoBowHandler.onClientTick(client);
            AutoBowKeybinds.handleKeyPress();
        });
    }
}
