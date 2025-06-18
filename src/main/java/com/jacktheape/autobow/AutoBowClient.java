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
        HudOverlay.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AutoBowHandler.onClientTick(client);
            ModeBasedSessionManager.onClientTick(client);
            ServerProfileManager.onClientTick(client);
            BossbarXpMonitor.onClientTick(client);
            AutoBowKeybinds.handleKeyPress();


            AutoBowConfig config = AutoBowConfig.getInstance();
            if (config.enableMovementVariation) {
                MovementVariationManager.onClientTick(client);
            }
        });

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableBossbarXpMonitoring) {
            System.out.println("[Auto Bow Client] Auto-starting bossbar monitor");
            BossbarXpMonitor.startMonitoring();
        }

        System.out.println("[Auto Bow Client] Initialized with mode: " + config.operatingMode);
    }
}
