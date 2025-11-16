package me.brynview.navidrohim.jmws.client.helper;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Random player functions that play sounds or sends action bar alerts.
 */
public class PlayerHelper {
    private static final List<String> sentWarningsInServer = new ArrayList<>();

    /**
     * Send an alert to the user.
     * @param text What text to send the user. Make it an i18n key if possible.
     * @param overlayText If to overlay text on to the action bar, if `false` it will be put in the users chat.
     * @param ignoreConfig If to ignore the users set config value. If they have alerts turned off but ignoreConfig is true, the alert will be sent regardless.
     * @param messageType What colour the message will be. Named with importance instead of colour.
     */
    public static void sendUserAlert(Component text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {

        // Check if player allows alerts, check if player exists, and make sure it has not been sent before if one-time message
        if (!sentWarningsInServer.contains(text.getString()) && (CommonClass.config.showAlerts.get() || ignoreConfig) && CommonClass.minecraftClientInstance.player != null)
        {
            String finalText = text.getString();

            // Check if user allows coloured text, add colour tag if so
            if (CommonClass.config.colouredText.get()) {
                finalText = messageType.toString() + text.getString();
            }

            if (overlayText) {
                CommonClass.minecraftClientInstance.gui.setOverlayMessage(Component.literal(finalText), false); // Action bar
            } else {
                CommonClass.minecraftClientInstance.gui.getChat().addMessage(Component.literal(finalText)); // Chat
            }

            // Add alert to cache if it's one-time
            if (messageType.equals(JMWSMessageType.ONE_TIME_WARNING))
            {
                sentWarningsInServer.add(text.getString());
            }

        }
    }

    /**
     * Clears the warning cache. Warnings being alerts with JMWSMessageType.ONE_TIME_WARNING as the messageType.
     * This is usually called every time the player leaves the server.
     */
    public static void clearWarningAlertCache()
    {
        sentWarningsInServer.clear();
    }

    /**
     * Play a sound effect to the user.
     * @param sound SoundEvent -- Find a list of mod-defined sound events in JMWSSounds or SoundEvents class for vanilla sounds.
     */
    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects.get() && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f); // Lower volume so it becomes background noise
        }
    }

    @Nullable
    public static Player getUserFromUUID(UUID user)
    {
        return CommonClass.minecraftClientInstance.player.getCommandSenderWorld().getPlayerByUUID(user);
    }

    public static UUID ourUUID()
    {
        return CommonClass.minecraftClientInstance.player.getUUID();
    }

}
