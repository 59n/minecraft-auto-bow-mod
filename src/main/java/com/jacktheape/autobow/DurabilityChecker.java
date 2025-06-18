package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class DurabilityChecker {

    public static void checkCurrentBowDurability() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return;
        }

        ItemStack heldItem = client.player.getMainHandStack();

        if (!(heldItem.getItem() instanceof BowItem)) {
            client.player.sendMessage(
                    Text.literal("§e[Auto Bow] No bow in hand!"),
                    false
            );
            return;
        }

        if (!heldItem.isDamageable()) {
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Bow has unlimited durability!"),
                    false
            );
            return;
        }

        int maxDurability = heldItem.getMaxDamage();
        int currentDamage = heldItem.getDamage();
        int remainingDurability = maxDurability - currentDamage;
        double durabilityPercentage = ((double) remainingDurability / maxDurability) * 100;

        String colorCode = getDurabilityColorCode(durabilityPercentage);

        client.player.sendMessage(
                Text.literal(String.format(
                        "%s[Auto Bow] Bow Durability: %d/%d (%.1f%%)",
                        colorCode, remainingDurability, maxDurability, durabilityPercentage
                )),
                false
        );

 
        System.out.printf("[Auto Bow] Durability Check - %d/%d (%.1f%%)%n",
                remainingDurability, maxDurability, durabilityPercentage);
    }

    private static String getDurabilityColorCode(double percentage) {
        if (percentage > 50) {
            return "§a"; 
        } else if (percentage > 25) {
            return "§e"; 
        } else if (percentage > 10) {
            return "§6"; 
        } else {
            return "§c"; 
        }
    }
}
