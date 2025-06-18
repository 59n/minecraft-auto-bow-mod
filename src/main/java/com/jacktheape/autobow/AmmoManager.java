package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;

public class AmmoManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static int lastArrowCount = -1;
    private static boolean hasWarnedLowAmmo = false;
    private static boolean hasWarnedNoAmmo = false;

    public static void checkAmmunition() {
        if (client.player == null) return;

        // Skip ammunition checks if player has infinite arrows
        if (hasInfiniteArrows()) {
            resetWarnings();
            return;
        }

        int currentArrowCount = getArrowCount();
        AutoBowConfig config = AutoBowConfig.getInstance();

        // Check for ammunition depletion (only for non-infinite scenarios)
        if (currentArrowCount == 0 && !hasWarnedNoAmmo) {
            if (config.showStatusMessages) {
                client.player.sendMessage(
                        Text.literal("§c[Auto Bow] No arrows remaining! Auto bow disabled."),
                        false
                );
            }
            AutoBowHandler.disableDueToNoAmmo();
            hasWarnedNoAmmo = true;
            hasWarnedLowAmmo = false;
        }
        // Check for low ammunition warning
        else if (currentArrowCount <= 16 && currentArrowCount > 0 && !hasWarnedLowAmmo) {
            if (config.showStatusMessages) {
                client.player.sendMessage(
                        Text.literal("§e[Auto Bow] Low on arrows! " + currentArrowCount + " remaining."),
                        false
                );
            }
            hasWarnedLowAmmo = true;
        }
        // Reset warnings when ammo is restocked
        else if (currentArrowCount > 16) {
            hasWarnedLowAmmo = false;
            hasWarnedNoAmmo = false;
        }

        // Log arrow count changes in debug mode
        if (config.enableDebugMode && currentArrowCount != lastArrowCount) {
            System.out.println("[Auto Bow Debug] Arrow count changed: " +
                    lastArrowCount + " -> " + currentArrowCount);
        }

        lastArrowCount = currentArrowCount;
    }

    public static int getArrowCount() {
        if (client.player == null) return 0;

        int totalArrows = 0;

        // Check main inventory
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ARROW) {
                totalArrows += stack.getCount();
            }
        }

        return totalArrows;
    }

    public static boolean hasArrows() {
        return hasInfiniteArrows() || getArrowCount() > 0;
    }

    public static boolean hasInfiniteArrows() {
        if (client.player == null) return false;

        // Check if player is in creative mode
        if (isInCreativeMode()) {
            return true;
        }

        // Check if currently held bow has infinity enchantment
        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem.getItem() instanceof net.minecraft.item.BowItem) {
            return hasInfinityEnchantment(heldItem);
        }

        return false;
    }

    public static boolean isInCreativeMode() {
        if (client.player == null) return false;

        // Check player's creative mode abilities
        return client.player.getAbilities().creativeMode;
    }

    public static boolean hasInfinityEnchantment(ItemStack bowStack) {
        if (bowStack.isEmpty()) return false;

        try {
            // Updated for 1.21 - Use the new enchantment API
            var enchantments = bowStack.getEnchantments();
            var registryManager = client.world.getRegistryManager();
            var enchantmentRegistry = registryManager.get(RegistryKeys.ENCHANTMENT);
            var infinityEntry = enchantmentRegistry.entryOf(Enchantments.INFINITY);
            return enchantments.getLevel(infinityEntry) > 0;
        } catch (Exception e) {
            // Fallback for any API issues
            return false;
        }
    }

    public static String getAmmoStatus() {
        if (isInCreativeMode()) {
            return "§aCreative Mode";
        }

        if (client.player != null) {
            ItemStack heldItem = client.player.getMainHandStack();
            if (hasInfinityEnchantment(heldItem)) {
                return "§aInfinity";
            }
        }

        int count = getArrowCount();
        if (count == 0) {
            return "§cEmpty";
        } else if (count <= 16) {
            return "§eLow (" + count + ")";
        } else {
            return "§a" + count;
        }
    }

    public static void resetWarnings() {
        hasWarnedLowAmmo = false;
        hasWarnedNoAmmo = false;
    }
}
