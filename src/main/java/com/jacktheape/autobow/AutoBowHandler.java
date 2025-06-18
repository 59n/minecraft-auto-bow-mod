package com.jacktheape.autobow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBowHandler {
    private static boolean autoBowEnabled = true;
    private static int drawTime = 0;
    private static int targetDrawTime = 20;
    private static int cooldownTime = 0;
    private static int targetCooldownTime = 10;
    private static boolean isDrawing = false;
    private static boolean usingOffhand = false;
    private static boolean packetSent = false;

    public static void onClientTick(MinecraftClient client) {
        if (!autoBowEnabled || client.player == null || client.world == null) {
            return;
        }

        // Check both main hand and offhand for bows
        ItemStack mainHandItem = client.player.getMainHandStack();
        ItemStack offhandItem = client.player.getOffHandStack();

        ItemStack bowStack = null;
        Hand activeHand = null;

        if (mainHandItem.getItem() instanceof BowItem) {
            bowStack = mainHandItem;
            activeHand = Hand.MAIN_HAND;
            usingOffhand = false;
        } else if (offhandItem.getItem() instanceof BowItem) {
            bowStack = offhandItem;
            activeHand = Hand.OFF_HAND;
            usingOffhand = true;
        }

        if (bowStack != null && activeHand != null) {
            AutoBowConfig config = AutoBowConfig.getInstance();

            // Check ammunition before proceeding
            AmmoManager.checkAmmunition();
            if (!AmmoManager.hasArrows()) {
                return;
            }

            // Check durability if protection is enabled
            if (config.enableDurabilityProtection && isDurabilityTooLow(bowStack)) {
                disableModDueToDurability(client);
                return;
            }

            handleAutoBowWithPackets(client, activeHand);
        } else {
            resetBowState(client);
        }
    }

    private static void handleAutoBowWithPackets(MinecraftClient client, Hand hand) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        // Handle cooldown period between shots
        if (cooldownTime > 0) {
            cooldownTime--;
            if (config.enableDebugMode) {
                String handType = usingOffhand ? "offhand" : "main hand";
                System.out.println("[Auto Bow Debug] Cooldown: " + cooldownTime + " (" + handType + ")");
            }
            return;
        }

        // Start drawing if not already drawing
        if (!isDrawing) {
            startDrawingWithPackets(client, hand);
        }

        // Continue drawing
        if (isDrawing) {
            drawTime++;

            if (config.enableDebugMode) {
                String handType = usingOffhand ? "offhand" : "main hand";
                System.out.println("[Auto Bow Debug] Drawing with packets: " + drawTime + "/" + targetDrawTime + " (" + handType + ")");
            }

            // Release when target draw time is reached
            if (drawTime >= targetDrawTime) {
                releaseBowWithPackets(client, hand);
            }
        }
    }

    private static void startDrawingWithPackets(MinecraftClient client, Hand hand) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        isDrawing = true;
        drawTime = 0;
        packetSent = false;
        targetDrawTime = AdvancedRandomizer.getRandomizedDrawTime();

        try {
            if (client.getNetworkHandler() != null) {
                // Set the use key pressed BEFORE sending packet
                client.options.useKey.setPressed(true);

                // Small delay to ensure key state is registered
                Thread.sleep(5);

                // Get player's current rotation for the packet
                float yaw = client.player.getYaw();
                float pitch = client.player.getPitch();
                int sequence = 0;

                PlayerInteractItemC2SPacket startPacket = new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch);
                client.getNetworkHandler().sendPacket(startPacket);
                packetSent = true;

                // Ensure player starts using the item locally
                if (!client.player.isUsingItem()) {
                    client.player.setCurrentHand(hand);
                }

                if (config.enableDebugMode) {
                    String handType = usingOffhand ? "offhand" : "main hand";
                    System.out.println("[Auto Bow Debug] Enhanced START packet sent for " + handType +
                            ", target time: " + targetDrawTime + ", key pressed: " + client.options.useKey.isPressed());
                }
            }
        } catch (Exception e) {
            if (config.enableDebugMode) {
                System.out.println("[Auto Bow Debug] Error sending enhanced start packet: " + e.getMessage());
            }
        }
    }

    private static void releaseBowWithPackets(MinecraftClient client, Hand hand) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        try {
            if (client.getNetworkHandler() != null && packetSent) {
                // Release the use key FIRST
                client.options.useKey.setPressed(false);

                // Small delay to ensure state change is processed
                Thread.sleep(5);

                // Send the release packet
                PlayerActionC2SPacket releasePacket = new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        client.player.getBlockPos(),
                        Direction.DOWN
                );
                client.getNetworkHandler().sendPacket(releasePacket);

                // Ensure player stops using item locally
                if (client.player.isUsingItem()) {
                    client.player.stopUsingItem();
                }

                if (config.enableDebugMode) {
                    String handType = usingOffhand ? "offhand" : "main hand";
                    System.out.println("[Auto Bow Debug] Enhanced RELEASE packet sent for " + handType +
                            ", was using item: " + client.player.isUsingItem());
                }
            }
        } catch (Exception e) {
            if (config.enableDebugMode) {
                System.out.println("[Auto Bow Debug] Error sending enhanced release packet: " + e.getMessage());
            }
        }

        // Reset drawing state
        isDrawing = false;
        packetSent = false;
        drawTime = 0;
        targetCooldownTime = AdvancedRandomizer.getRandomizedCooldownTime();
        cooldownTime = targetCooldownTime;
    }





    private static void resetBowState(MinecraftClient client) {
        if (isDrawing && packetSent) {
            // Send release packet if we were in the middle of drawing
            try {
                if (client.getNetworkHandler() != null) {
                    PlayerActionC2SPacket releasePacket = new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN,
                            Direction.DOWN
                    );
                    client.getNetworkHandler().sendPacket(releasePacket);
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }

        isDrawing = false;
        packetSent = false;
        drawTime = 0;
        cooldownTime = 0;
        usingOffhand = false;
    }

    private static boolean isDurabilityTooLow(ItemStack bowStack) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        if (!bowStack.isDamageable()) {
            return false;
        }

        int maxDurability = bowStack.getMaxDamage();
        int currentDamage = bowStack.getDamage();
        int remainingDurability = maxDurability - currentDamage;

        return remainingDurability <= config.durabilityThreshold;
    }

    private static void disableModDueToDurability(MinecraftClient client) {
        AutoBowConfig config = AutoBowConfig.getInstance();

        autoBowEnabled = false;
        resetBowState(client);

        if (config.showStatusMessages && client.player != null) {
            String handType = usingOffhand ? "offhand" : "main hand";
            client.player.sendMessage(
                    Text.literal("§c[Auto Bow] Disabled - Bow durability too low in " + handType + "!"),
                    false
            );
        }
    }

    public static void disableDueToNoAmmo() {
        autoBowEnabled = false;

        MinecraftClient client = MinecraftClient.getInstance();
        resetBowState(client);

        AutoBowConfig config = AutoBowConfig.getInstance();
        if (config.enableDebugMode) {
            System.out.println("[Auto Bow] Mod disabled due to no ammunition");
        }
    }

    public static void toggleAutoBow() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        autoBowEnabled = !autoBowEnabled;
        String status = autoBowEnabled ? "enabled" : "disabled";

        MinecraftClient client = MinecraftClient.getInstance();

        if (!autoBowEnabled) {
            resetBowState(client);
        } else {
            AmmoManager.resetWarnings();
            AdvancedRandomizer.resetPatternTracking();
        }

        if (config.showStatusMessages && client.player != null) {
            String message = "§a[Auto Bow] " + status;
            if (autoBowEnabled) {
                message += " | Network Packet Mode";
            }
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    public static boolean isEnabled() {
        return autoBowEnabled;
    }

    public static boolean isDrawing() {
        return isDrawing;
    }

    public static int getNextShotCountdown() {
        return cooldownTime;
    }

    public static int getDrawProgress() {
        return drawTime;
    }

    public static boolean isUsingOffhand() {
        return usingOffhand;
    }

    public static void forceEnable() {
        AutoBowConfig config = AutoBowConfig.getInstance();
        autoBowEnabled = true;
        AmmoManager.resetWarnings();
        AdvancedRandomizer.resetPatternTracking();

        MinecraftClient client = MinecraftClient.getInstance();
        if (config.showStatusMessages && client.player != null) {
            client.player.sendMessage(
                    Text.literal("§a[Auto Bow] Force enabled | Network Packet Mode"),
                    false
            );
        }
    }
}
