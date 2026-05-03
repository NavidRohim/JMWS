package me.brynview.navidrohim.jmws.client.utils;

import com.mojang.authlib.GameProfile;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Random player functions that play sounds or sends action bar alerts.
 */
public class PlayerUtils {
    private static final List<String> sentWarningsInServer = new ArrayList<>();

    /**
     * Send an alert to the user.
     * @param text What text to send the user. Make it an i18n key if possible.
     * @param overlayText If to overlay text on to the action bar, if `false` it will be put in the user's chat.
     * @param ignoreConfig If to ignore the users, set config value. If they have alerts turned off but ignoreConfig is true, the alert will be sent regardless.
     * @param messageType What colour the message will be. Named with importance instead of colour.
     */
    public static void sendUserAlert(Component text, boolean overlayText, boolean ignoreConfig, MessageType messageType) {

        // Check if player allows alerts, check if player exists, and make sure it has not been sent before if one-time message
        if (!sentWarningsInServer.contains(text.getString()) && (JMWSClientCommon.config.showAlerts.get() || ignoreConfig) && JMWSCommon.minecraftClientInstance.player != null)
        {
            String finalText = text.getString();

            // Check if user allows coloured text, add colour tag if so
            if (JMWSClientCommon.config.colouredText.get()) {
                finalText = messageType.toString() + text.getString();
            }

            if (JMWSClientCommon.currentNotificationScreen != null)
            {
                JMWSClientCommon.currentNotificationScreen.displayAlert(Component.literal(finalText));
            }
            else if (overlayText) {
                JMWSCommon.minecraftClientInstance.gui.setOverlayMessage(Component.literal(finalText), false); // Action bar
            } else {
                JMWSCommon.minecraftClientInstance.gui.getChat().addClientSystemMessage(Component.literal(finalText)); // Chat
            }

            // Add alert to cache if it's one-time
            if (messageType.oneTimeOnly)
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

        if (JMWSClientCommon.config.playEffects.get() && JMWSCommon.minecraftClientInstance.player != null) {
            JMWSCommon.minecraftClientInstance.player.playSound(sound, 0.09f, 1f); // Lower volume so it becomes background noise
        }
    }

    public static Optional<GameProfile> getUserFromUUID(UUID user)
    {
        ClientPacketListener clientPacketListener = Objects.requireNonNull(JMWSCommon.minecraftClientInstance.getConnection());
        @Nullable PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(user);

        if (playerInfo != null)
        {
            return Optional.of(playerInfo.getProfile());
        }
        return Optional.empty();
    }

    public static String getUsernameFromUUID(UUID user)
    {
        Optional<GameProfile> profile = getUserFromUUID(user);
        return profile.isPresent() ? profile.get().name() : CommonUtils.unknownUser;
    }

    public static String getUsernameFromUUIDForShare(UUID user)
    {
        Optional<GameProfile> profile = getUserFromUUID(user);
        return profile.isPresent() ? profile.get().name() : "S";
    }

    public static @NotNull UUID ourUUID()
    {
        LocalPlayer playerUUID = Objects.requireNonNull(JMWSCommon.minecraftClientInstance.player, "Player is null! This should never happen and was likely because this method was called too early.");
        return playerUUID.getUUID();
    }

    public static @Nullable PlayerInfo getPlayerInfoFromUUID(UUID player)
    {
        try {
            return Objects.requireNonNull(JMWSCommon.minecraftClientInstance.getConnection()).getPlayerInfo(player);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static @NotNull PlayerInfo getOurPlayerInfo()
    {
        @Nullable PlayerInfo ourPlayerInfo = getPlayerInfoFromUUID(ourUUID());
        if (ourPlayerInfo != null)
        {
            return ourPlayerInfo;
        }
        throw new RuntimeException("Our player info is null! This should never happen. Was this called at the wrong time?");
    }
}
