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

 
        if (hasInfiniteArrows()) {
            resetWarnings();
            return;
        }

        int currentArrowCount = getArrowCount();
        AutoBowConfig config = AutoBowConfig.getInstance();

 
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
 
        else if (currentArrowCount <= 16 && currentArrowCount > 0 && !hasWarnedLowAmmo) {
            if (config.showStatusMessages) {
                client.player.sendMessage(
                        Text.literal("§e[Auto Bow] Low on arrows! " + currentArrowCount + " remaining."),
                        false
                );
            }
            hasWarnedLowAmmo = true;
        }
 
        else if (currentArrowCount > 16) {
            hasWarnedLowAmmo = false;
            hasWarnedNoAmmo = false;
        }

 
        if (config.enableDebugMode && currentArrowCount != lastArrowCount) {
            System.out.println("[Auto Bow Debug] Arrow count changed: " +
                    lastArrowCount + " -> " + currentArrowCount);
        }

        lastArrowCount = currentArrowCount;
    }

    public static int getArrowCount() {
        if (client.player == null) return 0;

        int totalArrows = 0;

 
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

 
        if (isInCreativeMode()) {
            return true;
        }

 
        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem.getItem() instanceof net.minecraft.item.BowItem) {
            return hasInfinityEnchantment(heldItem);
        }

        return false;
    }

    public static boolean isInCreativeMode() {
        if (client.player == null) return false;

 
        return client.player.getAbilities().creativeMode;
    }

    public static boolean hasInfinityEnchantment(ItemStack bowStack) {
        if (bowStack.isEmpty()) return false;

        try {
 
            var enchantments = bowStack.getEnchantments();
            var registryManager = client.world.getRegistryManager();
            var enchantmentRegistry = registryManager.get(RegistryKeys.ENCHANTMENT);
            var infinityEntry = enchantmentRegistry.entryOf(Enchantments.INFINITY);
            return enchantments.getLevel(infinityEntry) > 0;
        } catch (Exception e) {
 
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
