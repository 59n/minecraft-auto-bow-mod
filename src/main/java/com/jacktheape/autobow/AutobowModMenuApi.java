package com.jacktheape.autobow;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import com.jacktheape.autobow.gui.AutoBowSettingsScreen;

public class AutobowModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AutoBowSettingsScreen(parent);
    }
}
